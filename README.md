To configure, you must include your correct GCM Sender ID and android icons in your config.xml:

```
<preference name="GcmSenderId" value="123456789"/>
<resource-file src="res/android/drawable/notification.png" target="res/drawable/notification.png"/>
<resource-file src="res/android/drawable-hdpi/notification.png" target="res/drawable-hdpi/notification.png"/>
<resource-file src="res/android/drawable-ldpi/notification.png" target="res/drawable-ldpi/notification.png"/>
<resource-file src="res/android/drawable-mdpi/notification.png" target="res/drawable-mdpi/notification.png"/>
<resource-file src="res/android/drawable-xhdpi/notification.png" target="res/drawable-xhdpi/notification.png"/>
<resource-file src="res/android/drawable-xxhdpi/notification.png" target="res/drawable-xxhdpi/notification.png"/>
<resource-file src="res/android/drawable-xxxhdpi/notification.png" target="res/drawable-xxxhdpi/notification.png"/>
```
