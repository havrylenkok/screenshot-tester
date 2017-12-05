package com.screenshot_tester.configuration;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.io.TemporaryFilesystem;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

/**
 * <what class do>
 *
 * @author Kyrylo Havrylenko
 * @see
 */
public class RulesDriver {

  public final Gson gson = new Gson();
  public static final double DELTA = 0.0001;
  protected static final ObjectMapper map;
  // use local the newest driver and a stable on server
  protected static final String DRIVER_VERSION = System.getProperty("os.name").startsWith("Mac OS X") ? "2.33" : "2.32";
  // don't wait too long on items when running tests local. Long timeout only for cases when server is lagging
  protected static final int SELENIUM_LOCATION_TIMEOUT = System.getProperty("os.name").startsWith("Mac OS X") ? 30000 : 250000;
  static {
    map = new ObjectMapper();
    map.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  public static AShot aShot = new AShot().shootingStrategy(ShootingStrategies.simple());
  public static ImageDiffer imgDiffer = new ImageDiffer();

  protected static int windowW = 1280;
  protected static int windowH = 860;

  @Rule
  public TestWatcher screenshotOnFailure = new TestWatcher() {
    @Override
    protected void failed(Throwable e, Description description) {
      super.failed(e, description);
      System.out.println(
          "Failed test: " + description.getClassName() + "::" + description.getMethodName());
      makeScreenshotOnFailure();

    }

    @Attachment("Screenshot on failure")
    public byte[] makeScreenshotOnFailure() {
      return ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
    }
  };

  @Rule
  public TestRule logStarting = new TestWatcher() {
    protected void starting(Description desc) {
      System.out.println("Starting test: " + desc.getClassName() + "::" + desc.getMethodName());
    }
  };

  @Rule
  public TestRule logSuccess = new TestWatcher() {
    @Override
    protected void succeeded(Description description) {
      super.succeeded(description);
      System.out.println(
          "Succeeded test: " + description.getClassName() + "::" + description.getMethodName());
    }
  };

  protected static void clearTempFs() {
    TemporaryFilesystem tempFS = TemporaryFilesystem.getDefaultTmpFS();
    tempFS.deleteTemporaryFiles();
  }
}
