package com.javaPro.myProject.modules.template;

import com.javaPro.myProject.config.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

/**
 * 单元测试模板
 * 
 * 说明：
 * 1. 将 "template" 替换为实际的模块名称
 * 2. 将 "TemplateService" 替换为实际的Service类名
 * 3. 将 "TemplateEntity" 替换为实际的Entity类名
 * 4. 按照示例方法的格式编写测试方法
 * 
 * @author Test Team
 * @date 2026-05-09
 */
@DisplayName("模板服务单元测试")
public class TemplateServiceTest extends BaseUnitTest {
    
    @Autowired
    private TemplateService templateService;
    
    private TemplateEntity testEntity;
    
    @BeforeEach
    public void setUp() {
        // 初始化测试数据
        testEntity = new TemplateEntity();
        testEntity.setName("测试实体");
        testEntity.setDescription("这是一个测试实体");
        testEntity.setStatus("1");
    }
    
    @Test
    @DisplayName("测试创建实体")
    public void testCreateEntity() {
        // When: 执行操作
        TemplateEntity result = templateService.insert(testEntity);
        
        // Then: 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("测试实体");
    }
    
    @Test
    @DisplayName("测试根据ID查询实体")
    public void testQueryEntityById() {
        // Given: 准备测试数据
        TemplateEntity inserted = templateService.insert(testEntity);
        Integer entityId = inserted.getId();
        
        // When: 执行操作
        TemplateEntity found = templateService.queryById(entityId);
        
        // Then: 验证结果
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(entityId);
        assertThat(found.getName()).isEqualTo("测试实体");
    }
    
    @Test
    @DisplayName("测试查询不存在的实体")
    public void testQueryNonExistentEntity() {
        // When: 查询不存在的ID
        TemplateEntity found = templateService.queryById(9999);
        
        // Then: 应该返回null
        assertThat(found).isNull();
    }
    
    @Test
    @DisplayName("测试更新实体")
    public void testUpdateEntity() {
        // Given: 创建并修改实体
        TemplateEntity inserted = templateService.insert(testEntity);
        inserted.setName("更新后的名称");
        
        // When: 执行更新
        TemplateEntity updated = templateService.update(inserted);
        
        // Then: 验证更新结果
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("更新后的名称");
        
        // 验证数据库中的数据已更新
        TemplateEntity found = templateService.queryById(inserted.getId());
        assertThat(found.getName()).isEqualTo("更新后的名称");
    }
    
    @Test
    @DisplayName("测试删除实体")
    public void testDeleteEntity() {
        // Given: 创建实体
        TemplateEntity inserted = templateService.insert(testEntity);
        Integer entityId = inserted.getId();
        
        // When: 执行删除
        boolean deleted = templateService.deleteById(entityId);
        
        // Then: 验证删除结果
        assertThat(deleted).isTrue();
        assertThat(templateService.queryById(entityId)).isNull();
    }
    
    @Test
    @DisplayName("测试实体字段验证")
    public void testEntityFieldValidation() {
        // When: 设置字段值
        testEntity.setName("有效的名称");
        testEntity.setStatus("1");
        TemplateEntity result = templateService.insert(testEntity);
        
        // Then: 验证字段值
        assertThat(result.getName()).isNotBlank();
        assertThat(result.getStatus()).isIn("0", "1");
    }
}
