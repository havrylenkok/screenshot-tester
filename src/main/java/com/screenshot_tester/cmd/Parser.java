package com.screenshot_tester.cmd;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static final String SCREEN_MODE = "screen";
    private static final String DIFF_MODE = "diff";
    private static final String CRAWL_MODE = "crawl";

    @Option(name="-m", required = true, usage=
            "modes of app: " +
                    "\n\t - !!!LAB!!! "+CRAWL_MODE+". Example:  -m "+CRAWL_MODE+" -u urls.txt -s seed_urls.txt" +
                    "\n\t - "+SCREEN_MODE+". Example: -m "+SCREEN_MODE+" -o screenshots_dir -u urls.txt" +
                    "\n\t - "+DIFF_MODE+" (results in dir {-i val}-diff) Example: -m "+DIFF_MODE+" -i screenshots_dir -u urls.txt")
    private String mode;

    @Option(name="-o", usage="output directory",metaVar="OUTPUT")
    private File out = new File("screenshots");

    @Option(name="-i", usage = "input directory")
    private File input = new File("screenshots");

    @Option(name = "-u", usage = "text file of urls")
    private File urls = new File("urls.txt");

    @Option(name = "-s", usage = "text file of seed urls for crawler")
    private File seedUrls = new File("seedUrls.txt");

    @Argument
    private List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        new Parser().doMain(args);
    }

    public void doMain(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);

        // console row length
        parser.setUsageWidth(140);

        try {
            parser.parseArgument(args);

        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java screenshot_tester options...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        Command commands = new Command();
        switch (mode) {
            case SCREEN_MODE:
                commands.takeScreenshots(urls.getAbsolutePath(), out.getAbsolutePath());
                break;
            case DIFF_MODE:
                commands.compareScreenshots(urls.getAbsolutePath(), input.getAbsolutePath());
                break;
            case CRAWL_MODE:
                commands.crawl(urls.getAbsolutePath(), seedUrls.getAbsolutePath());
                break;
            default:
                System.err.println("Wrong mode: " + mode);
                parser.printUsage(System.err);
                throw new Exception(mode);
        }
    }
}