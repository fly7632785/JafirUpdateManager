package com.jafir.updatemanager;

/**
 * Created by jafir on 15/6/27.
 */
public class UpdateInfo {

    /**
     * 版本号
     */
    private String version;
    /**
     * apk下载地址
     */
    private String url;
    /**
     * 升级描述信息
     */
    private String description;
    /**
     * 版本名称
     */
    private String versionName;

    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
