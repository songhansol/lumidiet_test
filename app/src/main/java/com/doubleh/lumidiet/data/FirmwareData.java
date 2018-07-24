package com.doubleh.lumidiet.data;

/**
 * Created by user-pc on 2016-10-12.
 */

public class FirmwareData {
    String version, url, filePath, realVersion = "", codeName;

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setRealVersion(String realVersion) {
        this.realVersion = realVersion;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getRealVersion() {
        return realVersion;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

	public String getCodeName() {
		return codeName;
	}
}
