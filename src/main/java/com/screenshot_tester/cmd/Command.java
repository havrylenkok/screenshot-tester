package com.screenshot_tester.cmd;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.screenshot_tester.configuration.RulesDriver;
import com.screenshot_tester.crawler.Crawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.yandex.qatools.ashot.comparison.ImageDiff;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.open;
import static com.screenshot_tester.configuration.RulesDriver.aShot;

public class Command extends RulesDriver {

    public String compileImagePath(File screenshotDirectory, String url) {
        return Paths.get(screenshotDirectory.getAbsolutePath(), url.replaceAll("\\W", "") + ".png").toString();
    }

    protected void setUpSelenium() {
        ChromeDriverManager.getInstance().version(DRIVER_VERSION).setup();
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver chromeDriver = new ChromeDriver(capabilities);
        WebDriverRunner.setWebDriver(chromeDriver);
        chromeDriver.manage().window().setSize(new Dimension(windowW, windowH));
        Configuration.timeout = SELENIUM_LOCATION_TIMEOUT;
    }

    protected void tearDownSelenium() {
        if (!System.getProperty("os.name").startsWith("Mac OS X")) {
            WebDriverRunner.getWebDriver().quit();
            clearTempFs();
        }
    }

    public void takeScreenshots(String urlsFilePath, String outputDirPath) throws IOException {

        setUpSelenium();

        File screenshotDirectory = new File(outputDirPath);
        assert screenshotDirectory.exists() || screenshotDirectory.mkdir(); // create dir if not exists
        File urls = new File(urlsFilePath);
        Stream<String> stream = Files.lines(urls.toPath());
        stream.forEach(url -> {
            open(url);
            try {
                BufferedImage screenshot = aShot.takeScreenshot(WebDriverRunner.getWebDriver()).getImage();
                File output = new File(compileImagePath(screenshotDirectory, url));
                output.createNewFile();
                ImageIO.write(screenshot, "png", output);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed saving file for url: " + url);
            }
        });

        tearDownSelenium();
    }

    public void compareScreenshots(String urlsFilePath, String inputDirPath) throws IOException {
        setUpSelenium();

        File originalScreenshots = new File(inputDirPath);
        if (!originalScreenshots.exists()) {
            System.out.println("Folder with original screenshots is not specified. Exiting.");
            throw new NoSuchFileException(originalScreenshots.getAbsolutePath());
        }
        File diffScreenshots = new File(originalScreenshots.getAbsolutePath().concat("-diff"));
        assert diffScreenshots.exists() || diffScreenshots.mkdir(); // create dir if not exists
        File urls = new File(urlsFilePath);
        Stream<String> stream = Files.lines(urls.toPath());
        stream.forEach(url -> {
            open(url);
            try {
                File expectedFile = new File(compileImagePath(originalScreenshots, url));
                BufferedImage expected = ImageIO.read(expectedFile);
                BufferedImage actual = aShot.takeScreenshot(WebDriverRunner.getWebDriver()).getImage();

                ImageDiff diff = imgDiffer.makeDiff(expected, actual);

                if(diff.hasDiff()) {
                    BufferedImage diffImg = diff.getMarkedImage();
                    File output = new File(compileImagePath(diffScreenshots, url));
                    output.createNewFile();
                    ImageIO.write(diffImg, "png", output);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed comparing file for url: " + url);
            }
        });

        tearDownSelenium();
    }

    public void crawl(String urlsFilePath, String seedUrlsFilePath) throws Exception {
        String urlsFilename = urlsFilePath;
        File urls = new File(urlsFilename);
        urls.createNewFile();
        urls.delete();
        urls.createNewFile();


        String crawlStorageFolder = "data/crawl/root";
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setIncludeHttpsPages(true);
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        File seedUrls = new File(seedUrlsFilePath);
        seedUrls.createNewFile();
        Stream<String> stream = Files.lines(seedUrls.toPath());
        stream.forEach(controller::addSeed);

        controller.start(Crawler.class, 10);
    }
}
