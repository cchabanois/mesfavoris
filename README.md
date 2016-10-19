[![Build Status](https://travis-ci.org/cchabanois/mesfavoris.svg?branch=master)](https://travis-ci.org/cchabanois/mesfavoris)
[![Gitter](https://badges.gitter.im/cchabanois/mesfavoris.svg)](https://gitter.im/cchabanois/mesfavoris?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

# mesfavoris
Mesfavoris is an eclipse plugin that allows you to bookmark your files and share them with your team using GDrive.

## Features
- bookmark files and urls
- save your bookmarks on GDrive so that you can use them from your desktop and laptop computer.
- share some of your bookmarks
- bookmarks can resist changes in the files thanks to Bitap algorithm 
- numbered bookmarks

## Screenshot
![Alt text](/docs/screenshot.png?raw=true "Screenshot")


## Using Mesfavoris

### Installation
Currently, only a development version is available. The update site is updated for each successful build.

The update site url is : https://dl.bintray.com/cchabanois/mesfavoris/updates

![Alt text](/docs/install.png?raw=true "Install Mesfavoris")

Currently, 4 features are available :
- mesfavoris : you need to install at least this feature
- mesfavoris for Java : better support for java files
- mesfavoris for Git
- mesfavoris for Perforce

### Shortcuts
All commands start with M1+B . M1 is the COMMAND key on MacOS X and the CTRL key on most other platforms.
You can display all shortcuts by first pressing M1+B :

![Alt text](/docs/shortcuts.png?raw=true "Shortcuts")

### Connecting to GDrive
Click on the GDrive icon in the "Mes favoris" view.

![Alt text](/docs/connectToGdriveIcon.png?raw=true "GDrive Icon")

The first time your click on it, this will open your browser and start the OAuth flow.

> Note : currently, you cannot connect to multiple accounts. However, you can share bookmarks between accounts. You can also delete your current credentials if you selected the wrong account during authentication (Preferences/Mes Favoris/GDrive : delete credentials).

## License
Licensed under the [EPL License](http://www.eclipse.org/legal/epl-v10.html).
