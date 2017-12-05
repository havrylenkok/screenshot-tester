package com.screenshot_tester;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.screenshot_tester.configuration.BaseChromeDriver;
import com.screenshot_tester.crawler.Crawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.junit.Test;
import ru.yandex.qatools.ashot.comparison.ImageDiff;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.url;

public class Commands extends BaseChromeDriver {


    public String compileImagePath(File screenshotDirectory, String url) {
        return Paths.get(screenshotDirectory.getAbsolutePath(), url.replaceAll("\\W", "") + ".png").toString();
    }

    @Test
    public void takeScreenshots() throws IOException {
        File screenshotDirectory = new File("screenshots");
        assert screenshotDirectory.exists() || screenshotDirectory.mkdir(); // create dir if not exists
        File urls = new File("url.txt");
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
    }

    @Test
    public void compareScreenshots() throws IOException {
        File originalScreenshots = new File("screenshots");
        if (!originalScreenshots.exists()) {
            System.out.println("Folder with original screenshots is not specified. Exiting.");
            throw new NoSuchFileException(originalScreenshots.getAbsolutePath());
        }
        File diffScreenshots = new File(originalScreenshots.getAbsolutePath().concat("-diff"));
        assert diffScreenshots.exists() || diffScreenshots.mkdir(); // create dir if not exists
        File urls = new File("url.txt");
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

    }

    @Test
    public void crawlWebsite() throws Exception {
        String urlsFilename = "urls.txt";
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

        File seedUrls = new File("seedUrls.txt");
        seedUrls.createNewFile();
        Stream<String> stream = Files.lines(seedUrls.toPath());
        stream.forEach(controller::addSeed);

        controller.start(Crawler.class, 10);


    }
}
