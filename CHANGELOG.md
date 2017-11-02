# Change Log

## [release-1.8.0](https://github.com/cchabanois/mesfavoris/tree/release-1.8.0) (2017-10-30)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.7.0...release-1.8.0)

**Implemented enhancements:**

- Add a popup menu to import team project for concerned bookmarks  [\#49](https://github.com/cchabanois/mesfavoris/issues/49)
- Add a bookmark properties provider for github urls [\#48](https://github.com/cchabanois/mesfavoris/issues/48)
- Allow pasting after a given bookmark, not only on folders [\#43](https://github.com/cchabanois/mesfavoris/issues/43)

**Closed issues:**

- Make sure we respect guidelines for use of Google trademarks everywhere [\#51](https://github.com/cchabanois/mesfavoris/issues/51)
- Use a different handler to delete bookmarks and remove shared bookmark folder [\#50](https://github.com/cchabanois/mesfavoris/issues/50)
- Some favicons icons cannot be loaded [\#47](https://github.com/cchabanois/mesfavoris/issues/47)
- Icon for url bookmarks should be stored as 16x16 icon [\#45](https://github.com/cchabanois/mesfavoris/issues/45)
- A bookmark is not created when pasting an url from chrome [\#44](https://github.com/cchabanois/mesfavoris/issues/44)
- Marker not correctly updated when file associated to bookmark changes [\#42](https://github.com/cchabanois/mesfavoris/issues/42)
- ClassNotFoundException due to Automated Error Reporting not configured correctly [\#41](https://github.com/cchabanois/mesfavoris/issues/41)

## [release-1.7.0](https://github.com/cchabanois/mesfavoris/tree/release-1.7.0) (2017-08-28)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.6.0...release-1.7.0)

**Implemented enhancements:**

- Add new bookmark just after the selected one [\#36](https://github.com/cchabanois/mesfavoris/issues/36)

**Fixed bugs:**

- NPE when double-clicking on a bookmark \(sometimes\) [\#40](https://github.com/cchabanois/mesfavoris/issues/40)
- log : Bookmark property 'lineNumber' registered several times with different definitions [\#39](https://github.com/cchabanois/mesfavoris/issues/39)
- Favicon not downloaded when on the root directory and link cannot be retrieved from the html document [\#38](https://github.com/cchabanois/mesfavoris/issues/38)
- Bookmark properties not displayed anymore if properties view is closed then re-opened [\#37](https://github.com/cchabanois/mesfavoris/issues/37)

## [release-1.6.0](https://github.com/cchabanois/mesfavoris/tree/release-1.6.0) (2017-08-24)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.5.0...release-1.6.0)

**Implemented enhancements:**

- Add shortcuts : bookmark to bookmark or bookmark folder [\#35](https://github.com/cchabanois/mesfavoris/issues/35)
- For url bookmarks, add command  to copy the url to clipboard [\#34](https://github.com/cchabanois/mesfavoris/issues/34)
- Cannot bookmark perforce changelist from the history view [\#33](https://github.com/cchabanois/mesfavoris/issues/33)
- Use exponential backoff for gDrive [\#32](https://github.com/cchabanois/mesfavoris/issues/32)

## [release-1.5.0](https://github.com/cchabanois/mesfavoris/tree/release-1.5.0) (2017-06-25)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.4.0...release-1.5.0)

**Implemented enhancements:**

- Update gdrive api sdk [\#31](https://github.com/cchabanois/mesfavoris/issues/31)
- When adding a bookmark to a gdrive document, use gdrive API to get icon and doc title  [\#28](https://github.com/cchabanois/mesfavoris/issues/28)
- Add an entry to contextual menu to delete bookmark problems for selected bookmarks [\#25](https://github.com/cchabanois/mesfavoris/issues/25)
- Add changelog for the project [\#24](https://github.com/cchabanois/mesfavoris/issues/24)

**Fixed bugs:**

- Bookmark label providers disposed at the wrong time [\#29](https://github.com/cchabanois/mesfavoris/issues/29)
- Tooltip is "Cannot goto bookmark : null" for "cannot goto bookmark" problem [\#27](https://github.com/cchabanois/mesfavoris/issues/27)

**Closed issues:**

- Build failing after update of Travis Ubuntu Trusty 14.04  [\#30](https://github.com/cchabanois/mesfavoris/issues/30)
- Fix deprecated bookmark property value from the property view [\#26](https://github.com/cchabanois/mesfavoris/issues/26)

## [release-1.4.0](https://github.com/cchabanois/mesfavoris/tree/release-1.4.0) (2017-04-22)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.3.0...release-1.4.0)

**Implemented enhancements:**

- Add category for properties in property view [\#8](https://github.com/cchabanois/mesfavoris/issues/8)
- Add Show In/Properties to the contextual menu [\#7](https://github.com/cchabanois/mesfavoris/issues/7)
- Make delay for polling GDrive changes configurable [\#6](https://github.com/cchabanois/mesfavoris/issues/6)
- Add an entry in the contextual menu to delete bookmark markers. [\#5](https://github.com/cchabanois/mesfavoris/issues/5)
- Add a bookmark to a git commit from the git history view [\#3](https://github.com/cchabanois/mesfavoris/issues/3)

**Fixed bugs:**

- Bookmark name should not contain the line number [\#4](https://github.com/cchabanois/mesfavoris/issues/4)
-  Shortcuts in the comment viewer \(to copy/paste\) are not working  properly [\#2](https://github.com/cchabanois/mesfavoris/issues/2)

## [release-1.3.0](https://github.com/cchabanois/mesfavoris/tree/release-1.3.0) (2017-03-15)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.2.0...release-1.3.0)

**Implemented enhancements:**

- Store problems for bookmarks having an issue and add an overlay icon in the view [\#17](https://github.com/cchabanois/mesfavoris/issues/17)
- Add bookmark problem when bookmark properties need to be updated [\#16](https://github.com/cchabanois/mesfavoris/issues/16)
- Add support for placeholder undefined bookmark problem [\#15](https://github.com/cchabanois/mesfavoris/issues/15)
- Do not create a new revision for each change on a shared bookmark folder. [\#14](https://github.com/cchabanois/mesfavoris/issues/14)
- Auto-update bookmark problems when a bookmark is added [\#12](https://github.com/cchabanois/mesfavoris/issues/12)
- Add bookmark problem when a bookmark with a local path is shared [\#10](https://github.com/cchabanois/mesfavoris/issues/10)

**Fixed bugs:**

- On linux, it is difficult to click on a bookmark problem tooltip link [\#13](https://github.com/cchabanois/mesfavoris/issues/13)
- Recent bookmarks virtual folder slow down the plugin [\#11](https://github.com/cchabanois/mesfavoris/issues/11)
- when adding bookmark on the line of a method signature, the bookmark is not a method bookmark [\#9](https://github.com/cchabanois/mesfavoris/issues/9)

## [release-1.2.0](https://github.com/cchabanois/mesfavoris/tree/release-1.2.0) (2017-01-26)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.1.0...release-1.2.0)

**Implemented enhancements:**

- Add code coverage [\#19](https://github.com/cchabanois/mesfavoris/issues/19)
- Add a confirmation dialog when deleting bookmarks [\#18](https://github.com/cchabanois/mesfavoris/issues/18)

## [release-1.1.0](https://github.com/cchabanois/mesfavoris/tree/release-1.1.0) (2016-12-18)
[Full Changelog](https://github.com/cchabanois/mesfavoris/compare/release-1.0.0...release-1.1.0)

**Implemented enhancements:**

- Virtual folder for recent bookmarks [\#23](https://github.com/cchabanois/mesfavoris/issues/23)
- When adding a bookmark, if active part is bookmarks part, consider previous active part instead. [\#22](https://github.com/cchabanois/mesfavoris/issues/22)
- Add support for bookmark to a perforce submitted changelist [\#21](https://github.com/cchabanois/mesfavoris/issues/21)
- Add support for bookmark to a git repository commit [\#20](https://github.com/cchabanois/mesfavoris/issues/20)

## [release-1.0.0](https://github.com/cchabanois/mesfavoris/tree/release-1.0.0) (2016-11-16)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*