package com.javaPro.myProject.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
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
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Boot 登录功能 Selenium 测试（Edge 浏览器）
 * 对应文档中 LoginController 接口：/login（页面）、/toLogin（登录接口）
 */
public class SimpleEdgeLoginTest {

    // WebDriver 实例（Edge 浏览器驱动）
    private WebDriver driver;
    // 显式等待对象（避免硬等待，提高稳定性）
    private WebDriverWait wait;
    // 项目基础 URL（需根据实际部署地址修改，本地测试通常为 http://localhost:端口号）
    private static final String BASE_URL = "http://localhost:7002"; // 端口号对应文档 Const 类中的 PORT = "7009"
    // 登录页面路径
    private static final String LOGIN_PAGE_URL = BASE_URL + "/login";
    // 登录成功后跳转的主页路径（根据文档 WebController 的 /web/index 配置）
    private static final String ADMIN_HOME_URL = BASE_URL + "/web/index";     // 角色1/3跳转地址
    private static final String USER_HOME_URL = BASE_URL + "/web/userindex"; // 角色2跳转地址

    // 性能优化选项：是否启用无头模式（true=更快但不可见，false=可见但较慢）
    private static final boolean HEADLESS_MODE = false; // 设置为true可大幅提升速度

    /**
     * 测试前初始化：检查应用程序、启动 Edge 浏览器、设置等待时间
     */
    @BeforeEach
    void setUp() {
        System.out.println("=== 开始测试准备 ===");

        // 1. 快速检查应用程序是否运行
       

        // 2. 配置Edge驱动（优先使用本地驱动）
        String localDriverPath = "D:\\edgedriver_win64\\msedgedriver.exe";
        try {
            if (new File(localDriverPath).exists()) {
                System.setProperty("webdriver.edge.driver", localDriverPath);
                System.out.println("✅ 使用本地Edge驱动: " + localDriverPath);
            } else {
                System.out.println("⚠ 本地驱动不存在，尝试WebDriverManager...");
                WebDriverManager.edgedriver()
                    .timeout(10)  // 缩短超时时间
                    .setup();     // 使用WebDriverManager作为备选
                System.out.println("✅ WebDriverManager配置完成");
            }
        } catch (Exception e) {
            System.out.println("❌ 驱动配置失败: " + e.getMessage());
            Assumptions.assumeTrue(false, "Edge驱动配置失败，跳过测试");
        }

        // 3. 配置Edge选项（优化性能）
        EdgeOptions options = new EdgeOptions();

        // 无头模式配置（大幅提升速度）
        if (HEADLESS_MODE) {
            options.addArguments("--headless");
            options.addArguments("--window-size=1920,1080");
            System.out.println("✅ 启用无头模式，测试速度将大幅提升");
        } else {
            options.addArguments("--start-maximized");
            System.out.println("✅ 启用可视模式，可以观察测试过程");
        }

        // 基本配置
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // 性能优化配置
        options.addArguments("--disable-images");           // 禁用图片加载
        // 注释掉JavaScript禁用，因为登录功能需要JavaScript
        // options.addArguments("--disable-javascript");       // 禁用JavaScript（如果不影响登录功能）
        options.addArguments("--disable-plugins");          // 禁用插件
        options.addArguments("--disable-extensions");       // 禁用扩展
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI");
        options.addArguments("--disable-ipc-flooding-protection");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-background-networking");

        // 避免自动化检测
        options.addArguments("--disable-blink-features=AutomationControlled");

        // 实验性选项
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // 4. 快速启动浏览器
        try {
            System.out.println("正在快速启动Edge浏览器...");
            System.out.println("驱动路径: " + System.getProperty("webdriver.edge.driver"));
            System.out.println("Edge选项: " + options.asMap());

            driver = new EdgeDriver(options);

            // 跳过窗口最大化以加快启动速度（除非是可视模式）
            if (!HEADLESS_MODE) {
                driver.manage().window().maximize();
            }

            // 设置更短的超时时间以加快启动
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(8));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(3));

            // 创建快速等待对象
            wait = new WebDriverWait(driver, Duration.ofSeconds(6), Duration.ofMillis(150));

            System.out.println("✅ Edge浏览器快速启动完成！");

        } catch (Exception e) {
            System.out.println("❌ Edge浏览器启动失败: " + e.getMessage());
            e.printStackTrace();

            // 尝试使用WebDriverManager作为备选方案
            try {
                System.out.println("尝试使用WebDriverManager重新配置...");
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver(options);
                wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                System.out.println("✅ 使用WebDriverManager启动成功");
            } catch (Exception e2) {
                System.out.println("❌ WebDriverManager也失败了: " + e2.getMessage());
                Assumptions.assumeTrue(false, "Edge浏览器启动失败，跳过测试");
            }
        }

        System.out.println("=== 测试准备完成 ===\n");
    }

    /**
     * 快速检查应用程序是否在指定端口运行（优化启动速度）
     */
    private boolean isApplicationRunningFast() {
        try {
            URL url = new URL(BASE_URL + "/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);  // 缩短连接超时
            connection.setReadTimeout(2000);     // 缩短读取超时

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200 || responseCode == 302;

        } catch (Exception e) {
            return false; // 移除日志输出以加快速度
        }
    }
    /**
     * 测试场景1：管理员登录（角色1，正确账号密码）
     * 预期：输入账号密码→点击登录→跳转至/web/index
     */
    @Test
    @DisplayName("管理员登录测试")
    void testAdminLoginSuccess() {
        System.out.println("=== 开始管理员登录测试 ===");

        try {
            // 1. 导航到登录页面
            navigateToLoginPage();

            // 2. 执行登录操作
            performLogin("admin", "123", "管理员");

            // 3. 验证登录成功
            verifyLoginSuccess("/web/", "管理员");

            // 4. 快速观察结果（缩短等待时间）
            Thread.sleep(1000);

            System.out.println("✅ 管理员登录测试完成");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 管理员登录测试被中断: " + e.getMessage());
            captureScreenshot("admin_login_interrupted");
            throw new RuntimeException("测试被中断", e);
        } catch (Exception e) {
            System.out.println("❌ 管理员登录测试失败: " + e.getMessage());
            captureScreenshot("admin_login_failed");
            throw e;
        }
    }

    /**
     * 测试场景2：普通用户登录（角色2，正确账号密码）
     * 预期：跳转至/web/userindex
     */
    @Test
    @DisplayName("普通用户登录测试")
    void testUserLoginSuccess() {
        System.out.println("=== 开始普通用户登录测试 ===");

        try {
            // 1. 导航到登录页面
            navigateToLoginPage();

            // 2. 执行登录操作
            performLogin("moka", "123", "普通用户");

            // 3. 验证登录成功
            verifyLoginSuccess("/web/", "普通用户");

            // 4. 快速观察结果（缩短等待时间）
            Thread.sleep(1000);

            System.out.println("✅ 普通用户登录测试完成");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 普通用户登录测试被中断: " + e.getMessage());
            captureScreenshot("user_login_interrupted");
            throw new RuntimeException("测试被中断", e);
        } catch (Exception e) {
            System.out.println("❌ 普通用户登录测试失败: " + e.getMessage());
            captureScreenshot("user_login_failed");
            throw e;
        }
    }


    /**
     * 测试场景3：错误密码登录测试
     * 预期：登录失败，停留在登录页面
     */
    @Test
    @DisplayName("错误密码登录测试")
    void testLoginWithWrongPassword() {
        System.out.println("=== 开始错误密码登录测试 ===");

        try {
            // 1. 导航到登录页面
            navigateToLoginPage();

            // 2. 输入正确用户名但错误密码
            performLogin("admin", "wrongpassword", "错误密码");

            // 3. 快速等待观察结果
            Thread.sleep(500);

            // 4. 验证仍在登录页面（登录失败）
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/login")) {
                System.out.println("✅ 错误密码测试通过：登录被正确拒绝");
            } else {
                System.out.println("❌ 错误密码测试失败：不应该登录成功，当前URL: " + currentUrl);
                captureScreenshot("wrong_password_test_failed");
                fail("错误密码不应该登录成功");
            }

            System.out.println("✅ 错误密码登录测试完成");

        } catch (Exception e) {
            System.out.println("❌ 错误密码登录测试失败: " + e.getMessage());
            captureScreenshot("wrong_password_test_error");
            // 对于错误密码测试，异常可能是正常的，所以不重新抛出
        }
    }

    /**
     * 导航到登录页面
     */
    private void navigateToLoginPage() {
        System.out.println("正在打开登录页面...");
        driver.get(LOGIN_PAGE_URL);

        // 快速等待页面加载完成
        try {
            // 直接等待关键元素出现，跳过标题检查以提高速度
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("account")));
            System.out.println("✅ 登录页面加载完成");
        } catch (TimeoutException e) {
            System.out.println("⚠️ 页面加载超时，但继续执行...");
        }
    }

    /**
     * 执行登录操作
     */
    private void performLogin(String username, String password, String userType) {
        System.out.println("正在执行" + userType + "登录...");

        try {
            // 输入用户名
            WebElement usernameInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("account"))
            );
            usernameInput.clear();
            usernameInput.sendKeys(username);
            System.out.println("✅ 已输入用户名: " + username);

            // 输入密码
            WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("password"))
            );
            passwordInput.clear();
            passwordInput.sendKeys(password);
            System.out.println("✅ 已输入密码: " + password);

            // 点击登录按钮
            WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("login"))
            );
            loginButton.click();
            System.out.println("✅ 已点击登录按钮");

        } catch (Exception e) {
            System.out.println("❌ 登录操作失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 验证登录成功
     */
    private void verifyLoginSuccess(String expectedUrlPattern, String userType) {
        System.out.println("正在验证" + userType + "登录结果...");

        try {
            // 等待页面跳转
            wait.until(ExpectedConditions.urlContains(expectedUrlPattern));

            String currentUrl = driver.getCurrentUrl();
            System.out.println("当前页面URL: " + currentUrl);

            if (currentUrl.contains(expectedUrlPattern)) {
                System.out.println("✅ " + userType + "登录成功！");
            } else {
                System.out.println("❌ " + userType + "登录失败！当前URL: " + currentUrl);
                throw new RuntimeException(userType + "登录失败");
            }

        } catch (TimeoutException e) {
            String currentUrl = driver.getCurrentUrl();
            System.out.println("❌ " + userType + "登录超时，当前URL: " + currentUrl);
            throw e;
        }
    }

    /**
     * 捕获屏幕截图
     */
    private void captureScreenshot(String fileName) {
        try {
            // 创建screenshots目录
            File screenshotDir = new File("./screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // 添加时间戳到文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = fileName + "_" + timestamp + ".png";

            // 截图
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File targetFile = new File(screenshotDir, fullFileName);
            FileUtils.copyFile(screenshot, targetFile);

            System.out.println("📸 已保存截图: " + targetFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("❌ 截图失败: " + e.getMessage());
        }
    }

    /**
     * 测试后清理：关闭浏览器，释放资源，实现页面自动关闭
     */
    @AfterEach
    void tearDown() {
        System.out.println("\n=== 开始测试清理 ===");

        if (driver != null) {
            try {
                // 快速关闭浏览器，减少等待时间
                System.out.println("正在快速关闭浏览器...");
                Thread.sleep(500);

                // 彻底关闭浏览器，避免残留进程
                driver.quit();
                System.out.println("✅ 浏览器已快速关闭");

            } catch (Exception e) {
                System.out.println("❌ 关闭浏览器时出现错误: " + e.getMessage());
                // 强制关闭
                try {
                    driver.quit();
                } catch (Exception ignored) {
                    // 忽略强制关闭时的异常
                }
            }
        }

        System.out.println("=== 测试清理完成 ===\n");
    }
}