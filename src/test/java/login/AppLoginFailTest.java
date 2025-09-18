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
@ExtendWith(AppLoginFailTest.ScreenshotWatcher.class)
public class AppLoginFailTest {

    private static ExtentReports extent;
    private WebDriver driver;
    ExtentTest test;

    @BeforeAll
    void setupReport() {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/ValidatePromartLoginFail.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        test = extent.createTest(testInfo.getDisplayName());
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        test.info("ChromeDriver initialized");
    }

    @Test //
    @DisplayName("ValidatePromartLoginFail")
    void testGoogleTitle() {
        driver.get("https://www.promart.pe/");
        test.info("Promart login page opened");

        driver.manage().window().maximize();
        test.info("Page maximized");

        driver.findElement(By.cssSelector("a[class='js-user vdk']")).click();
        test.info("Click en Mi cuenta");
        driver.findElement(By.cssSelector("a[class='info-icon logged']")).click();
        test.info("Click en inicio sesion");
        driver.findElement(By.id("inputEmail")).sendKeys("kevinosco0@gmail.com");
        test.info("insert username");
        driver.findElement(By.id("inputPassword")).sendKeys("Polosco123");
        test.info("insert password");
        driver.findElement(By.cssSelector("button[id='classicLoginBtn']")).click();
        test.info("Click button Ingresar");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-i18n='vtexid.invalidAuth']")));

        // Validacion de contenido
        String alert = driver.findElement(By.cssSelector("span[data-i18n='vtexid.invalidAuth']")).getText();
        test.info("Alert obtained: " + alert);

        assertTrue(alert.contains("incorrecta"));
        test.pass("LoginFail was successful");
    }

    @AfterAll
    void flushReport() {
        extent.flush();
    }

    // 🔹 Vigilante para gestionar fallos y cierres del navegador
    static class ScreenshotWatcher implements TestWatcher {

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof AppLoginFailTest) {
                AppLoginFailTest myTest = (AppLoginFailTest) testInstance;
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
            if (testInstance instanceof AppLoginFailTest) {
                AppLoginFailTest myTest = (AppLoginFailTest) testInstance;
                if (myTest.driver != null) {
                    myTest.driver.quit();
                    myTest.test.info("Browser closed (test successful)");
                }
            }
        }
    }

    // 🔹 Metodo para tomar screenshots
    private String takeScreenshot(String testName) throws IOException {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);

        String destDir = "C:/Users/fiore/Desktop/Kevin/QA/Proyectos/prueba/target/screenshots/";
        String destPath = destDir + testName + ".png";

        // Crear carpeta si no existe
        Files.createDirectories(Paths.get(destDir));

        // Copiar archivo de captura de pantalla
        Files.copy(src.toPath(), Paths.get(destPath));

        // 🔹 ExtentReports necesita una ruta con "/"
        return destPath.replace("\\", "/");
    }

}
