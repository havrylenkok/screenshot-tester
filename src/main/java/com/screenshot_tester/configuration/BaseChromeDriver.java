package com.screenshot_tester.configuration;

import static org.junit.Assert.assertEquals;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import java.net.MalformedURLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.yandex.qatools.allure.annotations.Attachment;

public class BaseChromeDriver extends RulesDriver {

  @BeforeClass
  public static void setUp() throws MalformedURLException {
    ChromeDriverManager.getInstance().version(DRIVER_VERSION).setup();
    DesiredCapabilities capabilities = DesiredCapabilities.chrome();
    ChromeOptions options = new ChromeOptions();
    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
    WebDriver chromeDriver = new ChromeDriver(capabilities);
    WebDriverRunner.setWebDriver(chromeDriver);
    chromeDriver.manage().window().setSize(new Dimension(windowW, windowH));
    Configuration.timeout = SELENIUM_LOCATION_TIMEOUT;
  }

  @AfterClass
  public static void tearDown() {
    //check if tests runs locally
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      WebDriverRunner.getWebDriver().quit();
      clearTempFs();
    }
  }

  @Attachment(value = "Page screenshot", type = "image/png")
  protected byte[] saveAllureScreenshot() {
    return ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
  }
}
