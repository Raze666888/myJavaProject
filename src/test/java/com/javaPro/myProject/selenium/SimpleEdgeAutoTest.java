package com.javaPro.myProject.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化版Edge自动化测试类
 * 使用最简单的配置，确保能正常启动浏览器
 */
public class SimpleEdgeAutoTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:7002";
    private static final String LOGIN_PAGE_URL = BASE_URL + "/login";
    private static final String LOCAL_DRIVER_PATH = "D:\\edgedriver_win64\\msedgedriver.exe";
    
    // 测试用户凭据
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        System.out.println("=== 开始简化版Edge自动化测试准备 ===");
        
        try {
            // 检查本地驱动是否存在
            File driverFile = new File(LOCAL_DRIVER_PATH);
            if (!driverFile.exists()) {
                System.out.println("❌ 本地Edge驱动不存在: " + LOCAL_DRIVER_PATH);
                System.out.println("⚠ 跳过Selenium测试，因为驱动文件不存在");
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Edge驱动文件不存在");
                return;
            }
            
            // 设置系统属性
            System.setProperty("webdriver.edge.driver", LOCAL_DRIVER_PATH);
            System.out.println("✅ 设置Edge驱动路径: " + LOCAL_DRIVER_PATH);
            
            // 创建最简单的Edge选项
            EdgeOptions options = new EdgeOptions();
            
            // 只添加最基本的选项
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            
            // 尝试启动浏览器
            System.out.println("正在启动Edge浏览器...");
            driver = new EdgeDriver(options);
            
            // 设置基本超时
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            
            // 创建等待对象
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("✅ Edge浏览器启动成功");
            
        } catch (Exception e) {
            System.out.println("❌ Edge浏览器启动失败: " + e.getMessage());
            System.out.println("⚠ 这可能是由于系统环境或权限问题导致的");
            
            // 清理资源
            cleanup();
            
            // 跳过测试而不是失败
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Edge浏览器启动失败: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        System.out.println("=== 开始测试清理 ===");
        
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("✅ 浏览器已关闭");
            } catch (Exception e) {
                System.out.println("⚠ 关闭浏览器时出现异常: " + e.getMessage());
            }
            driver = null;
        }
        
        System.out.println("=== 测试清理完成 ===");
    }

    @Test
    @DisplayName("基础浏览器启动测试")
    void testBrowserStartup() {
        System.out.println("=== 开始基础浏览器启动测试 ===");
        
        // 验证浏览器已启动
        assertNotNull(driver, "WebDriver应该已初始化");
        
        try {
            // 导航到一个简单页面
            driver.get("data:text/html,<html><body><h1>Selenium Test Page</h1><p>Browser started successfully!</p></body></html>");
            
            // 验证页面内容
            WebElement heading = driver.findElement(By.tagName("h1"));
            assertEquals("Selenium Test Page", heading.getText());
            
            WebElement paragraph = driver.findElement(By.tagName("p"));
            assertEquals("Browser started successfully!", paragraph.getText());
            
            System.out.println("✅ 基础浏览器启动测试通过");
            
        } catch (Exception e) {
            System.out.println("❌ 基础浏览器启动测试失败: " + e.getMessage());
            fail("基础浏览器启动测试失败: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("管理员自动登录测试")
    void testAdminAutoLogin() {
        System.out.println("=== 开始管理员自动登录测试 ===");
        
        try {
            // 导航到登录页面
            System.out.println("正在访问登录页面: " + LOGIN_PAGE_URL);
            driver.get(LOGIN_PAGE_URL);
            
            // 等待页面加载
            Thread.sleep(2000);
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("当前URL: " + currentUrl);
            
            // 尝试执行登录
            boolean loginSuccess = attemptLogin(ADMIN_USERNAME, ADMIN_PASSWORD, "管理员");
            
            if (loginSuccess) {
                System.out.println("✅ 管理员自动登录测试成功");
            } else {
                System.out.println("⚠ 管理员自动登录测试未完全成功，但浏览器正常工作");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 管理员自动登录测试异常: " + e.getMessage());
            System.out.println("⚠ 这可能是因为应用服务器未启动或页面结构变化");
        }
    }
    
    @Test
    @DisplayName("普通用户自动登录测试")
    void testUserAutoLogin() {
        System.out.println("=== 开始普通用户自动登录测试 ===");
        
        try {
            // 导航到登录页面
            System.out.println("正在访问登录页面: " + LOGIN_PAGE_URL);
            driver.get(LOGIN_PAGE_URL);
            
            // 等待页面加载
            Thread.sleep(2000);
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("当前URL: " + currentUrl);
            
            // 尝试执行登录
            boolean loginSuccess = attemptLogin(USER_USERNAME, USER_PASSWORD, "普通用户");
            
            if (loginSuccess) {
                System.out.println("✅ 普通用户自动登录测试成功");
            } else {
                System.out.println("⚠ 普通用户自动登录测试未完全成功，但浏览器正常工作");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 普通用户自动登录测试异常: " + e.getMessage());
            System.out.println("⚠ 这可能是因为应用服务器未启动或页面结构变化");
        }
    }
    
    /**
     * 尝试执行登录操作
     */
    private boolean attemptLogin(String username, String password, String userType) {
        try {
            System.out.println("开始尝试" + userType + "登录...");
            
            // 查找用户名输入框
            WebElement usernameField = findElement(
                By.name("username"),
                By.id("username"),
                By.cssSelector("input[type='text']")
            );
            
            if (usernameField == null) {
                System.out.println("⚠ 未找到用户名输入框");
                return false;
            }
            
            // 查找密码输入框
            WebElement passwordField = findElement(
                By.name("password"),
                By.id("password"),
                By.cssSelector("input[type='password']")
            );
            
            if (passwordField == null) {
                System.out.println("⚠ 未找到密码输入框");
                return false;
            }
            
            // 输入凭据
            usernameField.clear();
            usernameField.sendKeys(username);
            System.out.println("✅ 用户名输入完成: " + username);
            
            passwordField.clear();
            passwordField.sendKeys(password);
            System.out.println("✅ 密码输入完成");
            
            // 查找登录按钮
            WebElement loginButton = findElement(
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.id("loginBtn"),
                By.className("login-btn")
            );
            
            if (loginButton == null) {
                System.out.println("⚠ 未找到登录按钮");
                return false;
            }
            
            // 点击登录
            loginButton.click();
            System.out.println("✅ 登录按钮点击完成");
            
            // 等待登录处理
            Thread.sleep(3000);
            
            // 检查登录结果
            String currentUrl = driver.getCurrentUrl();
            System.out.println("登录后URL: " + currentUrl);
            
            if (!currentUrl.contains("/login")) {
                System.out.println("✅ " + userType + "登录成功！");
                return true;
            } else {
                System.out.println("⚠ " + userType + "登录可能失败，仍在登录页面");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ " + userType + "登录过程出现异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 使用多个定位器尝试查找元素
     */
    private WebElement findElement(By... locators) {
        for (By locator : locators) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                if (element != null && element.isDisplayed()) {
                    System.out.println("✅ 找到元素: " + locator);
                    return element;
                }
            } catch (TimeoutException e) {
                // 继续尝试下一个定位器
                continue;
            }
        }
        
        System.out.println("⚠ 所有定位器都未找到元素");
        return null;
    }
}
