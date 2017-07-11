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
