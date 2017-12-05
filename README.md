# screenshot-tester
Automate testing of your web app with absolutely no code

# Project status

Base functionality is working but acess to features is not implemented (currently all code is being run by @Test annotations of jUnit). Still in early development.

# How to use
1. Use crawler mode / put urls you want to test in a txt file
2. Run screenshot mode -> see results in screenshots dir
3. Deploy features / do development or other stuff that will influence your web app
4. Run diff mode -> see everything that is different from previous state of app in screenshots-diff dir

# Todo
- Parser class so the tool could be ran via console
- Auth method so user can specify where should Selenium log in before running screenshotter
- Crawler improvements (Auth, etc...)
