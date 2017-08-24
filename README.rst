
########################################
mesfavoris : a bookmarks eclipse plugin
########################################



.. image:: /docs/mesfavoris-300x356.png?raw=true
    :alt: Logo
Mesfavoris is an **eclipse plugin** that allows you to **bookmark** your files and share them with your team using GDrive.

|build| |codecov| |gitter| |stars| |license| |version|

.. contents::

========
Features
========
- bookmark files and urls
- use bookmark folders to organize your bookmarks
- save your bookmarks on GDrive so that you can use them from your desktop and laptop computer.
- share some of your bookmarks
- bookmarks can resist changes in the files thanks to Bitap algorithm 
- numbered bookmarks

.. image:: /docs/screenshot.png?raw=true
    :align: center
    :alt: Screenshot

============
Presentation
============

.. image:: /docs/youTubePresentation.png?raw=true
    :align: center
    :alt: YouTube presentation
    :target: https://youtu.be/sbpUu-ABFKc

============
Installation
============

.. image:: https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png
   :target: http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=3176231
   :alt: Drag to your running Eclipse workspace to install mesfavoris
Drag this image to your running Eclipse workspace to install mesfavoris

You can also add the release update site url to your eclipse installation : https://dl.bintray.com/cchabanois/mesfavoris/updates

There is also a development update site that is updated for each successful build.

The development update site url is : https://dl.bintray.com/cchabanois/mesfavoris-development/updates

.. image:: /docs/install.png?raw=true
    :alt: Install Mesfavoris

Currently, 4 features are available :

- mesfavoris : you need to install at least this feature
- mesfavoris for Java : better support for java files
- mesfavoris for Git
- mesfavoris for Perforce

=====
Usage
=====

---------
Shortcuts
---------
All commands start with M1+B . M1 is the COMMAND key on MacOS X and the CTRL key on most other platforms.
You can display all shortcuts by first pressing M1+B :

.. image:: /docs/shortcuts.png?raw=true
    :alt: Shortcuts

----------------
Adding bookmarks
----------------

Folders
~~~~~~~
Use bookmark folders to organize your bookmarks. In the view named "Mes Favoris", select an existing folder, right click and select "New/New Folder". To create a top level folder, do not select an existing folder or bookmark.

Bookmarks
~~~~~~~~~
A bookmark is just a set of properties that are used to locate the resource pointed by the bookmark. You can view the properties associated with a bookmark in the Properties view :

.. image:: /docs/bookmarkProperties.png?raw=true
    :alt: Bookmark Properties

For bookmarks in text files, bookmark contains a path to the file and a line number. However this line number is only used as a hint. A bookmark can be shared, used for different branches and new text can be added before. This means that the line number can quickly become irrelevant. Instead, we mainly use the lineContent property instead. The content of the bookmarked line can change, of course, but less often and generally not completly (or anyway the bookmark becomes irrelevant).
We use the  `Bitap algorithm <https://en.wikipedia.org/wiki/Bitap_algorithm>`_ for fuzzy string search.


When adding a new bookmark on a text file, be careful to add this bookmark on a line :

- that does not contain a password or any other confidential information
- that contains text that is relevant and does not appear elsewhere around the line (do not put a bookmark on an empty line for example


You can add a bookmark from the edit menu or using the shortcut : ``M1+B B``. If you are in a text editor, a marker will be added :

.. image:: /docs/bookmarkMarker.png?raw=true
    :alt: Bookmark Marker

The bookmark will be added in the bookmark folder that is selected in the *Mes Favoris* view.

Placeholders
~~~~~~~~~~~~
As you can see on the bookmark properties screenshot, bookmarks often have a *filePath* property with the absolute path to the file.
This can be a problem if you want to share your bookmarks with your team or between your desktop computer and laptop computer.

You can define placeholders in eclipse Preferences :

.. image:: /docs/placeholdersPreferencePage.png?raw=true
    :alt: Placeholders preference page

And apply a placeholder to existing bookmarks : (replace absolute path with ${PLACEHOLDER_NAME}) :

.. image:: /docs/placeholdersApply.png?raw=true
    :alt: Placeholders preference page


Bookmark types
~~~~~~~~~~~~~~
A bookmark can be of several types. For exemple, it can contain properties from a file bookmark, java bookmark and git bookmark. 
The available bookmarks types are :

- file/folder bookmark : bookmark to a resource inside the eclipse workspace
- external file/folder bookmark : bookmark to a resource outside the eclipse workspace. You can drap & drop file or folder to the bookmarks view
- text editor bookmark : bookmark to a specific line in a text file (file can be outside eclipse workspace)
- java bookmark : either to a type member or to a specific line in a java file
- url bookmark : bookmark to an url. You can copy an url from your browser and paste it into the *Mes Favoris* view

.. image:: /docs/urlBookmarks.png?raw=true
    :alt: Url bookmarks in the *Mes Favoris* view

- url bookmark to gdrive file : Copy the url and paste it to the *Mes Favoris* view. The file title and icon will be used for the bookmark.
- git bookmark : will add information from the git project to the bookmark
- git commit bookmark : bookmark to a git commit. To add a bookmark to a commit, open it in the commit viewer and add your favori as usual

.. image:: /docs/gitCommitBookmarks.png?raw=true
    :alt: Git commit bookmarks in the *Mes Favoris* view

- perforce bookmark : will add information from the perforce project to the bookmark
- perforce changelist bookmark

.. image:: /docs/perforceChangeListBookmarks.png?raw=true
    :alt: Perforce changelist bookmarks in the *Mes Favoris* view

Numbered bookmarks
~~~~~~~~~~~~~~~~~~
Numbered bookmarks allows to set and recall bookmarks by number. Up to 10 bookmarks, from 0 to 9 are supported.
You can set a number when you create a bookmark (``M1+B F1`` - ``M1+B F10``) or using the popup menu on a the bookmark in the view :

.. image:: /docs/setNumberForBookmarkMenu.png?raw=true
    :alt: Set Number Shortcut
    
A number can also be set on a bookmark folder so that you can quickly goto it.

---------------
Share bookmarks
---------------
You can save some of your bookmarks to gDrive. It can be useful

- when you work on the same projects on both your laptop computer and desktop computer
- if you want to share some bookmarks with your team

Connecting to GDrive
~~~~~~~~~~~~~~~~~~~~
Click on the GDrive icon in the *Mes favoris* view.

.. image:: /docs/connectToGdriveIcon.png?raw=true
    :alt: Connect to Gdrive

The first time your click on it, this will open your browser and start the OAuth flow : 

.. image:: /docs/gdriveOAuth.png?raw=true
    :alt: Gdrive oauth flow


.. note::  Currently, you cannot connect to multiple accounts. However, you can share bookmarks between accounts. You can also delete your current credentials if you selected the wrong account during authentication (Preferences/Mes Favoris/GDrive : delete credentials).

Add bookmark folder to GDrive
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Select the bookmark folder you want to add to gDrive and click on *Add to GDrive* :

.. image:: /docs/gdriveMenu.png?raw=true
    :alt: Gdrive Menu

.. note::  The bookmarks you added to gDrive are still available when you are not connected but are read-only.

Bookmark files are available in the *eclipse-bookmarks* folder in your google drive :

.. image:: /docs/eclipse-bookmarks-gdrive.png?raw=true
    :alt: eclipse-bookmarks folder in your google drive

Share with others
~~~~~~~~~~~~~~~~~
You can share your bookmark files from the google drive web app or directly from eclipse (*Gdrive/Share bookmarks file*)

Import bookmarks file
~~~~~~~~~~~~~~~~~~~~~
You can import bookmarks files that are already on your gDrive or that are shared with you. It is also possible to add a link when a bookmarks file is shared by link.

.. image:: /docs/importBookmarksFile.png?raw=true
    :alt: import bookmarks file

---------------------
Fix bookmark problems
---------------------

The number of bookmark problems are displayed on the *Mes Favoris* bar in the view. A tooltip will display additional information and possibly a link to fix the problem.

.. image:: /docs/bookmarkProblemsBar.png?raw=true
    :alt: bookmark problems
    
.. image:: /docs/bookmarkProblemsTooltip.png?raw=true
    :alt: bookmark problems tooltip
    

Bookmark properties out of date
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Bookmark properties can become out of date. MesFavoris is designed so that it often does not prevent the bookmark to work as expected but it's better to update bookmark properties so that they are still accurate.

When you click on a bookmark, new properties are retrieved. If they are different from existing ones, a warning icon is added to the bookmark and you can update the properties using :

- click 'Use new properties' on the tooltip of the *Mes Favoris* bar
- click the 'Use new properties' icon on the toolbar
- if the bookmark has changed too much and does not direct you to the expected place, go to the wanted place and update it (``M1+B U``)

Other bookmark problems
~~~~~~~~~~~~~~~~~~~~~~~

- "Cannot goto bookmark" : you need to update the bookmark using ``M1+B U``
- "Some properties are using local paths" : shared bookmarks should use placeholders.
- "Placeholders undefined" : bookmark is using a placeholder that is undefined


=======
License
=======
Licensed under the `EPL License <http://www.eclipse.org/legal/epl-v10.html>`_

Some icons by Yusuke Kamiyamane. Licensed under a Creative Commons Attribution 3.0 License.

Logo is adapted from https://commons.wikimedia.org/wiki/File:Spin_(Bookmark)1.jpg by sirooziya [GFDL (http://www.gnu.org/copyleft/fdl.html) or CC BY-SA 3.0 (http://creativecommons.org/licenses/by-sa/3.0)

.. |build| image:: https://travis-ci.org/cchabanois/mesfavoris.svg?branch=master
    :target: https://travis-ci.org/cchabanois/mesfavoris
    :alt: Build status of the master branch
 
.. |gitter| image:: https://badges.gitter.im/cchabanois/mesfavoris.svg
    :target: https://gitter.im/cchabanois/mesfavoris?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge
    :alt: Chat on Gitter
.. |codecov| image:: https://codecov.io/gh/cchabanois/mesfavoris/branch/master/graph/badge.svg
    :target: https://codecov.io/gh/cchabanois/mesfavoris

.. |stars| image:: https://img.shields.io/github/stars/cchabanois/mesfavoris.svg
    :target: https://github.com/cchabanois/mesfavoris/stargazers
    
.. |license| image:: https://img.shields.io/badge/license-Eclipse-blue.svg
    :target: https://github.com/cchabanois/mesfavoris/blob/master/LICENSE

.. |version| image:: https://img.shields.io/bintray/v/cchabanois/mesfavoris/releases.svg
    :target: https://marketplace.eclipse.org/marketplace-client-intro?mpc_install=3176231
