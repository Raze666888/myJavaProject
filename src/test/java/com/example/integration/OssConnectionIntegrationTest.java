package com.example.integration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.BucketInfo;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.javaPro.myProject.config.OssConfig;
import com.javaPro.myProject.service.FileUploadService;
import com.javaPro.myProject.service.OssDirectUploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OSS连接集成测试
 * 用于验证AccessKey更新后OSS功能是否正常
 *
 * 运行前请确保：
 * 1. application.yml中的OSS配置已正确设置
 * 2. oss.enabled=true
 * 3. AccessKey和Secret已更新为最新值
 *
 * 注：CI环境中禁用此测试，需要真实的OSS连接
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OSS连接集成测试")
@Disabled("需要真实的OSS连接，在CI环境中禁用")
public class OssConnectionIntegrationTest {

    @Autowired(required = false)
    private OSS ossClient;

    @Autowired(required = false)
    private OssDirectUploadService ossDirectUploadService;

    @Autowired(required = false)
    private FileUploadService fileUploadService;

    @Value("${oss.bucketName:test-bucket}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @BeforeEach
    void setUp() {
        // 检查基础配置
        assertNotNull(ossClient, "OSS客户端未正确配置");
        assertNotNull(ossDirectUploadService, "OSS直传服务未正确配置");
        assertFalse(accessKeyId.isEmpty(), "AccessKeyId未配置");
        assertFalse(endpoint.isEmpty(), "Endpoint未配置");
    }

    @Test
    @DisplayName("测试OSS基础连接")
    void testBasicOssConnection() {
        // 测试是否能正常获取存储桶信息
        try {
            BucketInfo bucketInfo = ossClient.getBucketInfo(bucketName);
            assertNotNull(bucketInfo, "无法获取存储桶信息");
            assertNotNull(bucketInfo.getBucket(), "存储桶对象为空");
            assertEquals(bucketName, bucketInfo.getBucket().getName(), "存储桶名称不匹配");

            System.out.println("✓ OSS基础连接测试通过");
            System.out.println("  - 存储桶名称: " + bucketInfo.getBucket().getName());
            System.out.println("  - 所在地域: " + bucketInfo.getBucket().getLocation());
            System.out.println("  - 创建时间: " + bucketInfo.getBucket().getCreationDate());

        } catch (Exception e) {
            fail("OSS基础连接测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试OSS直传签名生成")
    void testOssSignatureGeneration() {
        try {
            Map<String, Object> signature = ossDirectUploadService.getOssSignature();

            assertNotNull(signature, "签名为空");
            assertTrue(signature.containsKey("accessid"), "签名缺少accessid");
            assertTrue(signature.containsKey("policy"), "签名缺少policy");
            assertTrue(signature.containsKey("signature"), "签名缺少signature");
            assertTrue(signature.containsKey("expire"), "签名缺少expire");
            assertTrue(signature.containsKey("host"), "签名缺少host");
            assertTrue(signature.containsKey("dir"), "签名缺少dir");

            assertEquals(accessKeyId, signature.get("accessid"), "AccessKeyId不匹配");

            // 检查过期时间是否合理（应该在当前时间之后的30分钟内）
            long expire = (Long) signature.get("expire");
            long currentTime = System.currentTimeMillis() / 1000;
            assertTrue(expire > currentTime, "签名已过期");
            assertTrue(expire <= currentTime + 1800, "签名过期时间过长");

            System.out.println("✓ OSS直传签名生成测试通过");
            System.out.println("  - AccessId: " + signature.get("accessid"));
            System.out.println("  - 过期时间: " + expire);

        } catch (Exception e) {
            fail("OSS直传签名生成测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试文件名生成")
    void testFileNameGeneration() {
        String originalName = "测试文件.jpg";
        String generatedName = ossDirectUploadService.generateFileName(originalName);

        assertNotNull(generatedName, "生成的文件名为空");
        assertNotEquals(originalName, generatedName, "生成的文件名与原文件名相同");
        assertTrue(generatedName.endsWith(".jpg"), "生成的文件名扩展名不正确");
        assertTrue(generatedName.length() > originalName.length(), "生成的文件名长度不合理");

        System.out.println("✓ 文件名生成测试通过");
        System.out.println("  - 原文件名: " + originalName);
        System.out.println("  - 新文件名: " + generatedName);
    }

    @Test
    @DisplayName("测试简单文件上传")
    void testSimpleFileUpload() throws Exception {
        if (fileUploadService == null) {
            System.out.println("⚠ 文件上传服务未配置，跳过上传测试");
            return;
        }

        // 创建测试文件
        String testContent = "这是一个测试文件内容 - " + System.currentTimeMillis();
        MockMultipartFile testFile = new MockMultipartFile(
            "file",
            "test-upload.txt",
            "text/plain",
            testContent.getBytes(StandardCharsets.UTF_8)
        );

        try {
            String uploadedUrl = fileUploadService.uploadFile(testFile);

            assertNotNull(uploadedUrl, "上传结果URL为空");
            assertTrue(uploadedUrl.startsWith("http"), "上传URL格式不正确");
            assertTrue(uploadedUrl.contains("test-upload"), "上传URL不包含文件名");

            System.out.println("✓ 文件上传测试通过");
            System.out.println("  - 文件大小: " + testFile.getSize() + " bytes");
            System.out.println("  - 上传URL: " + uploadedUrl);

        } catch (Exception e) {
            fail("文件上传测试失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试OSS对象操作")
    void testOssObjectOperations() {
        try {
            // 测试文件名
            String testKey = "test/connection-test-" + System.currentTimeMillis() + ".txt";
            String testContent = "OSS连接测试内容 - " + System.currentTimeMillis();

            // 1. 上传测试文件
            PutObjectRequest putRequest = new PutObjectRequest(
                bucketName,
                testKey,
                new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8))
            );

            PutObjectResult putResult = ossClient.putObject(putRequest);
            assertNotNull(putResult, "上传结果为空");
            assertNotNull(putResult.getETag(), "ETag为空");

            // 2. 检查文件是否存在
            boolean doesExist = ossClient.doesObjectExist(bucketName, testKey);
            assertTrue(doesExist, "上传的文件不存在");

            // 3. 获取文件信息
            ObjectMetadata objectMetadata = ossClient.getObjectMetadata(bucketName, testKey);
            assertNotNull(objectMetadata, "文件元数据为空");
            assertEquals(testContent.length(), objectMetadata.getContentLength(), "文件大小不匹配");

            // 4. 清理测试文件
            ossClient.deleteObject(bucketName, testKey);
            assertFalse(ossClient.doesObjectExist(bucketName, testKey), "测试文件清理失败");

            System.out.println("✓ OSS对象操作测试通过");
            System.out.println("  - 测试文件: " + testKey);
            System.out.println("  - 文件大小: " + testContent.length() + " bytes");
            System.out.println("  - ETag: " + putResult.getETag());

        } catch (Exception e) {
            fail("OSS对象操作测试失败: " + e.getMessage());
        }
    }
}