########################################
mesfavoris : a bookmarks eclipse plugin
########################################
Mesfavoris is an **eclipse plugin** that allows you to **bookmark** your files and share them with your team using GDrive.

|build| |gitter|

.. contents::

========
Features
========
- bookmark files and urls
- save your bookmarks on GDrive so that you can use them from your desktop and laptop computer.
- share some of your bookmarks
- bookmarks can resist changes in the files thanks to Bitap algorithm 
- numbered bookmarks

.. image:: /docs/screenshot.png?raw=true
    :width: 100%
    :align: center
    :alt: Screenshot


============
Installation
============
Currently, only a development version is available. The update site is updated for each successful build.

The update site url is : https://dl.bintray.com/cchabanois/mesfavoris/updates

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

bookmarks
~~~~~~~~~
properties
Bitap
bookmark types
how it works

placeholders
~~~~~~~~~~~~

numbered bookmarks
~~~~~~~~~~~~~~~~~~

---------------
Share bookmarks
---------------
Use cases : laptop/desktop computer , share with team

Connecting to GDrive
~~~~~~~~~~~~~~~~~~~~
Click on the GDrive icon in the "Mes favoris" view.

.. image:: /docs/connectToGdriveIcon.png?raw=true
    :alt: Connect to Gdrive

The first time your click on it, this will open your browser and start the OAuth flow.

.. note::  Currently, you cannot connect to multiple accounts. However, you can share bookmarks between accounts. You can also delete your current credentials if you selected the wrong account during authentication (Preferences/Mes Favoris/GDrive : delete credentials).

Add bookmark folder to GDrive
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Share with others
~~~~~~~~~~~~~~~~~

=======
License
=======
Licensed under the `EPL License <http://www.eclipse.org/legal/epl-v10.html>`_


.. |build| image:: https://travis-ci.org/cchabanois/mesfavoris.svg?branch=master
    :target: https://travis-ci.org/cchabanois/mesfavoris
    :alt: Build status of the master branch
 
.. |gitter| image:: https://badges.gitter.im/cchabanois/mesfavoris.svg
    :target: https://gitter.im/cchabanois/mesfavoris?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge
    :alt: Chat on Gitter
