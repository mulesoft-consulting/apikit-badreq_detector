#!/bin/bash

set -e

STUDIO_DIR='/Applications/AnypointStudio_64.app'
PLUGIN_DIR="$STUDIO_DIR/Contents/Eclipse/dropins/org.mule.tooling.ui.contribution.apikit-badrequest-extractor.3.8.3"

echo "Removing existing plugin..."
rm -rf $PLUGIN_DIR
mvn clean install -DskipTests
echo "Build complete, unzipping into Studio dropins directory..."
unzip target/update-site/plugins/org.mule.tooling.ui.contribution.apikit-badrequest-extractor.*.jar -d $PLUGIN_DIR
echo 'Launching Studio in console...'
$STUDIO_DIR/Contents/MacOS/AnypointStudio --args -consoleLog
