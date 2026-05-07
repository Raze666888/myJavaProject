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

import java.time.Duration;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 极速启动Edge浏览器自动化登录测试
 * 专注于最快的启动速度和测试执行
 */
public class TurboEdgeLoginTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:7002";
    
    // 极速配置
    private static final int TURBO_TIMEOUT = 2;     // 极短超时
    private static final int POLL_INTERVAL = 50;    // 高频轮询

    @BeforeEach
    void turboSetUp() {
        long startTime = System.currentTimeMillis();
        System.out.println("🚀 TURBO模式启动");
        


        // 2. 配置Edge驱动
        System.setProperty("webdriver.edge.driver", "D:\\edgedriver_win64\\msedgedriver.exe");

        // 3. 极简Edge配置
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless");                    // 无头模式
        options.addArguments("--no-sandbox");                  
        options.addArguments("--disable-dev-shm-usage");       
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-images");              // 禁用图片
        // 注释掉JS禁用，因为应用需要JavaScript
        // options.addArguments("--disable-javascript");          // 禁用JS
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-extensions");          
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-features=TranslateUI,VizDisplayCompositor");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-sync");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-hang-monitor");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-prompt-on-repost");
        options.addArguments("--disable-web-security");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=800,600");         // 小窗口
        
        // 4. 极速启动
        driver = new EdgeDriver(options);
        
        // 5. 优化超时配置
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));  // 增加页面加载超时
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(3));
        
        // 6. 高频等待
        wait = new WebDriverWait(driver, Duration.ofSeconds(TURBO_TIMEOUT), Duration.ofMillis(POLL_INTERVAL));
        
        long endTime = System.currentTimeMillis();
        System.out.println("⚡ TURBO启动完成: " + (endTime - startTime) + "ms");
    }

    @Test
    @DisplayName("TURBO管理员登录")
    void turboAdminLogin() {
        turboLogin("admin", "123");
    }

    @Test
    @DisplayName("TURBO用户登录")
    void turboUserLogin() {
        turboLogin("moka", "123");
    }

    @Test
    @DisplayName("TURBO错误密码")
    void turboWrongPassword() {
        driver.get(BASE_URL + "/login");
        
        WebElement user = wait.until(ExpectedConditions.elementToBeClickable(By.id("account")));
        WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        
        user.sendKeys("admin");
        pass.sendKeys("wrong");
        btn.click();
        
        // 极速验证
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertTrue(driver.getCurrentUrl().contains("/login"));
        System.out.println("⚡ 错误密码测试完成");
    }

    /**
     * TURBO登录方法
     */
    private void turboLogin(String username, String password) {
        driver.get(BASE_URL + "/login");
        
        WebElement user = wait.until(ExpectedConditions.elementToBeClickable(By.id("account")));
        WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        
        user.sendKeys(username);
        pass.sendKeys(password);
        btn.click();
        
        wait.until(ExpectedConditions.urlContains("/web/"));
        assertTrue(driver.getCurrentUrl().contains("/web/"));
        System.out.println("⚡ " + username + " TURBO登录完成");
    }

    /**
     * 极速应用检查（Socket方式，最快）
     */
    private boolean isTurboAppRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", 7002), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @AfterEach
    void turboTearDown() {
        if (driver != null) {
            driver.quit(); // 立即关闭
        }
        System.out.println("⚡ TURBO测试完成\n");
    }
}
