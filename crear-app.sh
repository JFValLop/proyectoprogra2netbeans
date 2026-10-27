#!/bin/bash
cd "$(dirname "$0")"
mvn -q clean package || exit 1
rm -rf target/app target/solo-jar
mkdir -p target/solo-jar
cp target/Billets.jar target/solo-jar/
jpackage --type app-image --name Billets --input target/solo-jar --main-jar Billets.jar --main-class com.umg.api.Launcher --icon Billets.icns --dest target/app --java-options "--enable-native-access=ALL-UNNAMED" || exit 1
/usr/libexec/PlistBuddy -c "Add :NSCameraUsageDescription string Billets usa la cámara para leer el QR de los boletos" target/app/Billets.app/Contents/Info.plist
codesign --force --deep -s - target/app/Billets.app
echo "Listo: target/app/Billets.app"
