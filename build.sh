cd androidapp
ant debug
cd -
adb uninstall com.wininup.voiladlna
adb install androidapp/bin/VoilaDLNA-debug-unaligned.apk
adb logcat -c
adb logcat | grep 'Web Console'

