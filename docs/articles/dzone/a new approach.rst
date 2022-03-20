*This article was posted on dzone in 2017 but removed in 2021 "Due to old content clean up"*

########################################
Bookmark Your Code: a New Approach
########################################
This Eclipse IDE plugin will help you solve the usual annoyances with the bookmark system by allowing you to label and organize your bookmarks.

======================
The Existing Solutions
======================

You can already add bookmarks in Eclipse; each bookmark can have a description and there is a corresponding marker added in the file. That way, you can navigate to resources that you use frequently.

However, this support for bookmarks is quite minimal: no shorcuts by default, no folders to organize the bookmarks, no numbered bookmarks... Built-in eclipse bookmarks

There are a few plugins in the marketplace trying to solve some of the shortcomings, like `quickbookmark <http://marian.schedenig.name/projects/quickbookmarks/>`_ quickbookmark.

Support is better in Intellij IDEA where you have shortcuts and bookmarks with mnemonics but still no way to organize bookmarks in folders.

In all IDEs, bookmarks are thought of as a temporary thing. The developer is supposed to have only a few bookmarks so that he can quickly go back to a few places in the projects he is currently working on.

**The concept of bookmarks can be pushed further in IDEs.**

======================
What Bookmarks Are For
======================

Bookmarks are used to speed up code navigation by jumping to code easily. But this encompasses several use cases:

- Quickly jump to code you are working on. Generally, these bookmarks are short-lived and you will delete them quickly.
- Track important places in the code. Depending on the code base, there can be many.
- Understand a large code base. Generally, you use a debugger to debug your application step by step and add bookmarks where needed, and the order for these bookmarks is important.
- Explain an existing codebase by sharing the important places with comments.

So what is missing in Eclipse and most other IDEs?

1. Folders so you can organize your bookmarks.
2. Comments about a bookmark. Often you can only set a label.
3. Long-term persistence. Eclipse bookmarks are persisted with the workspace somewhere. But they are deleted when the corresponding project is deleted.

==========
MesFavoris
==========

MesFavoris is an Eclipse plugin that was created to solve these shortcomings and test new ideas.

MesFavoris screenshot

-------------------------------------
Bookmark What You Need as a Developer
-------------------------------------

You can bookmark files (in the workspace or not) but that's not all :

- Bookmark URLs. The idea is not to replace your browser but it can be useful to have bookmarks to the documentation for example.
- Bookmark folders.
- Bookmark git commit.

---------------
Rich Bookmark
---------------

Bookmark in MesFavoris is not just a file path, a lineNumber, and a comment:

Bookmarks properties

This means there are generally multiple ways to goto to the bookmarked element. MesFavoris will use the best one.

-----------------------------------------------
Use Bookmark Folders to Organize Your Bookmarks
-----------------------------------------------

This is one of the features that makes MesFavoris different. Instead of having a few bookmarks (say 20-30), you can have hundreds of bookmarks because you can organize them (I have more than one thousand bookmarks at work).

-----------------------------
Save Your Bookmarks on GDrive
-----------------------------

This is what I was referring to when I wrote "long-term persistence:" you can use your bookmarks on all your workspaces, both on your desktop or on your laptop.

Why GDrive and not Github, for example? Because of the GDrive sharing model. You often don't want to share your bookmarks and when you do, you often want to share only with your team or only with some people.

----------------------------
Share Some of Your Bookmarks
----------------------------

I think bookmarks are often personal. They reflect the way you are understanding the code, what is important to you in the code. That said, sharing some bookmarks can be useful :

- For onboarding new team members on a project.
- Faster than copy/pasting class name/line numbers on your favorite instant messaging application.

-----------------
Resist to Changes
-----------------

Code always changes. This means that bookmarks are generally only valid for a relatively short time and only on a given branch. MesFavoris bookmarks resist changing thanks to an approximate string matching algorithm.
And you can update a bookmark if it becomes out of date:
Image title 	Image title

------------------
Numbered bookmarks
------------------

Allows to set and recall bookmarks by number. This is a more common useful feature but still useful.

====================
Additional resources
====================

- MesFavoris project page
- Using IntelliJ Bookmarks
- Mes Favoris is covered by the Eclipse Public License 1.0

