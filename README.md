# screenshot-tester
Automate testing of your web app with absolutely no code

# Project status

Base functionality is working but crawler mode is undpredictable. Still in development.

# Build

```
1. https://github.com/robben1234/screenshot-tester.git && cd screenshot-tester
2. mvn package
```

# Usage 

```
java screenshot_tester options...
 -a VAL    : auth via Selenium before taking screenshots (screen,crawl modes). It waits 10 seconds after clicking login button so your
             loaders should be finished by the time it go to the next page.
             Format: -a "login_page_url username_xpath username_value password_xpath password_value login_button_xpath"
 -i FILE   : input directory (default: screenshots)
 -m VAL    : modes of app: 
             	 - !!!LAB!!! crawl. Example:  -m crawl -u urls.txt -s seed_urls.txt
             	 - screen. Example: -m screen -o screenshots_dir -u urls.txt
             	 - diff (results in dir {-i val}-diff) Example: -m diff -i screenshots_dir -u urls.txt
 -o OUTPUT : output directory (default: screenshots)
 -s FILE   : text file of seed urls for crawler (default: seedUrls.txt)
 -u FILE   : text file of urls (default: urls.txt)
 ```

# How to use
1. Use crawler mode `java screenshot_tester -m crawl` / put urls you want to test in a txt file
2. Run screenshot mode `java screenshot_tester -m screen` -> see results in screenshots dir
3. Deploy features / do development or other stuff that will influence your web app
4. Run diff mode `java screenshot_tester -m diff` -> see everything that is different from previous state of app in screenshots-diff dir

# Todo
- Auth method so user can specify where should Selenium log in before running screenshotter
- Crawler improvements (Auth, etc...)
