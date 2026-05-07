package com.javaPro.myProject.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.junit.jupiter.api.Assumptions;

import java.io.File;

/**
 * Selenium WebDriver 配置工具类
 * 统一管理Edge驱动配置和浏览器选项
 */
public class WebDriverConfig {
    
    // Edge驱动路径
    private static final String LOCAL_EDGE_DRIVER_PATH = "drivers/msedgedriver";
    
    /**
     * 配置Edge驱动（优先使用本地驱动）
     */
    public static void setupEdgeDriver() {
        try {
            if (new File(LOCAL_EDGE_DRIVER_PATH).exists()) {
                System.setProperty("webdriver.edge.driver", LOCAL_EDGE_DRIVER_PATH);
                System.out.println("✅ 使用本地Edge驱动: " + LOCAL_EDGE_DRIVER_PATH);
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
    }
    
    /**
     * 创建标准Edge选项（可见模式）
     */
    public static EdgeOptions createStandardEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        
        // 基础优化选项
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
        // 性能优化
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-sync");
        
        return options;
    }
    
    /**
     * 创建无头模式Edge选项（最快速度）
     */
    public static EdgeOptions createHeadlessEdgeOptions() {
        EdgeOptions options = createStandardEdgeOptions();
        
        // 启用无头模式
        options.addArguments("--headless");
        
        // 额外的性能优化
        options.addArguments("--disable-images");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-client-side-phishing-detection");
        options.addArguments("--disable-hang-monitor");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-prompt-on-repost");
        
        // 设置小窗口尺寸
        options.addArguments("--window-size=800,600");
        
        return options;
    }
    
    /**
     * 创建Edge WebDriver实例
     */
    public static WebDriver createEdgeDriver(boolean headless) {
        setupEdgeDriver();
        
        EdgeOptions options = headless ? createHeadlessEdgeOptions() : createStandardEdgeOptions();
        
        try {
            return new EdgeDriver(options);
        } catch (Exception e) {
            System.out.println("❌ 无法启动Edge浏览器: " + e.getMessage());
            Assumptions.assumeTrue(false, "Edge浏览器启动失败，跳过测试");
            return null; // 这行不会执行，但编译器需要
        }
    }
    
    /**
     * 检查本地驱动是否存在
     */
    public static boolean isLocalDriverAvailable() {
        return new File(LOCAL_EDGE_DRIVER_PATH).exists();
    }
    
    /**
     * 获取本地驱动路径
     */
    public static String getLocalDriverPath() {
        return LOCAL_EDGE_DRIVER_PATH;
    }
}
