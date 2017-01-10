third party dependencies repository creation :
cd ~/git/mesfavoris/releng/dependencies
java -jar "/Applications/Eclipse 2.app/Contents/Eclipse/plugins/org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar" -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher -metadataRepository file:/Users/cchabanois/git/mesfavoris/releng/dependencies/repository -artifactRepository file:/Users/cchabanois/git/mesfavoris/releng/dependencies/repository -source source -configs gtk.linux.x86 -compress -publishArtifacts
