# Manifest needs for the aircraft detail (AR) feature

`AndroidManifest.xml` was not edited by the details worker. For `ModelUi.Ar` (ARCore + SceneView) add:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.ar" android:required="false" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
<!-- inside <application> -->
<meta-data android:name="com.google.ar.core" android:value="optional" />
```

`required=false` keeps the app installable on devices without ARCore; the AR screen then shows "AR is not supported on this device."
Also add to the Gradle dependencies if missing: nothing (the `arsceneview` artifact already brings `com.google.ar:core`).
`INTERNET` is already declared. The video, PDF and model downloads use https from the API origin (see `network_security_config`).
