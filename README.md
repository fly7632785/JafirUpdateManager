# JafirUpdateManager
a manager to update with progress dialog

### screenshot
![cool style](https://github.com/fly7632785/JafirUpdateManager/raw/master/screenshots/screenshot1.gif)
![native style](https://github.com/fly7632785/JafirUpdateManager/raw/master/screenshots/screenshot2.gif)


### Simple Use
you should have a Xml setting file of AppVersion, like this
```
<?xml version="1.0" encoding="utf-8"?>
<update>
    <version>1</version>
    <versionName>version 1.0.0</versionName>
    <url>http://jafir-my-love.oss-cn-shanghai.aliyuncs.com/test.apk</url>
    <description>
      some description...
    </description>
</update> 
```
```
  UpdateManager manager = new UpdateManager(MainActivity.this);
                //set xml URL on the server
                manager.setURL("http://jafir-my-love.oss-cn-shanghai.aliyuncs.com/test_update_info.xml");
                manager.start();
```
### android 7.0
in order to use and install apk in android 7.0, you should add 'fileProvider' in your AndroidManifest.xml
```
  <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.file.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
  </provider>
```
and create file_paths.xml in res/xml/
 ```
 <?xml version="1.0" encoding="utf-8"?>
 <paths >
     <external-path path="." name="external_storage_root" />
     <files-path path="." name="dir_root" />
 </paths>
```


### Gradle
```
repositories {
			...
			maven { url 'https://jitpack.io' }
		}
```
```
compile 'com.github.fly7632785:JafirUpdateManager:1.0.0'
```
