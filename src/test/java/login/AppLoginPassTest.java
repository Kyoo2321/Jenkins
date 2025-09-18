package login;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(AppLoginPassTest.ScreenshotWatcher.class)
public class AppLoginPassTest {

    private static ExtentReports extent;
    private WebDriver driver;
    ExtentTest test;

    @BeforeAll
    void setupReport() {
    ExtentSparkReporter spark = new ExtentSparkReporter("target/ValidatePromartLoginPass.html");

    // 🔹 Forzar modo offline incrustando CSS/JS
    spark.config().setOfflineMode(true);

    extent = new ExtentReports();
    extent.attachReporter(spark);
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        test = extent.createTest(testInfo.getDisplayName());
        WebDriverManager.chromedriver().setup();

        // ChromeOptions headless + perfil temporal
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        String tempProfile = System.getProperty("user.dir") + "/target/chrome-profile-" + System.currentTimeMillis();
        options.addArguments("--user-data-dir=" + tempProfile);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // usar solo Explicit Wait
        test.info("ChromeDriver initialized in headless mode with unique profile");
    }

    @Test
    @DisplayName("ValidatePromartLoginPass")
    void testGoogleTitle() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://www.promart.pe/");
        test.info("Promart login page opened");

        // Click en "Mi cuenta"
        WebElement miCuentaBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.js-user.vdk")));
        miCuentaBtn.click();
        test.info("Click en Mi cuenta");

        // Click en "Inicio sesión"
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.info-icon.logged")));
        loginBtn.click();
        test.info("Click en inicio sesión");

        // Insert username
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputEmail")));
        emailInput.sendKeys("kevinosco0@gmail.com");
        test.info("insert username");

        // Insert password
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputPassword")));
        passwordInput.sendKeys("Polosco123@");
        test.info("insert password");

        // Click en "Ingresar"
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("classicLoginBtn")));
        submitBtn.click();
        test.info("Click button Ingresar");

        // Validar login
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("body.home.home-redisign.not-express.logged")));
        test.info("Login page loaded");

        String title = driver.getTitle();
        test.info("Title obtained: " + title);

        String welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.txt-user"))).getText();
        test.info("User obtained: " + welcome);

        assertTrue(welcome.contains("kevinosco0"));
        test.pass("Login was successful");
    }

    @AfterAll
    void flushReport() {
        extent.flush();
    }

    static class ScreenshotWatcher implements TestWatcher {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof AppLoginPassTest) {
                AppLoginPassTest myTest = (AppLoginPassTest) testInstance;
                try {
                    String screenshotPath = myTest.takeScreenshot(context.getDisplayName());
                    myTest.test.fail("Test failed: " + cause.getMessage())
                            .addScreenCaptureFromPath(screenshotPath);
                } catch (Exception e) {
                    myTest.test.warning("Could not take screenshot: " + e.getMessage());
                } finally {
                    if (myTest.driver != null) {
                        myTest.driver.quit();
                        myTest.test.info("Browser closed (after failure)");
                    }
                }
            }
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof AppLoginPassTest) {
                AppLoginPassTest myTest = (AppLoginPassTest) testInstance;
                if (myTest.driver != null) {
                    myTest.driver.quit();
                    myTest.test.info("Browser closed (test successful)");
                }
            }
        }
    }

    private String takeScreenshot(String testName) throws IOException {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);
        String destDir = System.getProperty("user.dir") + "/target/screenshots/";
        String destPath = destDir + testName + ".png";
        Files.createDirectories(Paths.get(destDir));
        Files.copy(src.toPath(), Paths.get(destPath));
        return destPath.replace("\\", "/");
    }
}
