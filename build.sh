cd androidapp
ant release || (echo 'Build failed.' && exit)
cd -

adb shell exit 0 || (echo 'No connected device. Exiting.' && exit)

adb uninstall com.wininup.voiladlna
adb install androidapp/bin/VoilaDLNA-release-unsigned.apk

adb logcat -c
adb logcat | grep 'Web Console'
