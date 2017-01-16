package org.mcupdater.model;

import org.mcupdater.util.MCUpdater;

import java.util.Locale;

public class ConfigFile implements IPackElement {
	private String url;
	private String path;
	private String md5;
	private boolean noOverwrite;
	
	public ConfigFile(String url, String path, boolean noOverwrite, String md5)
	{
		setUrl(url);
		setPath(path);
		setNoOverwrite(noOverwrite);
		setMD5(md5);
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public boolean isNoOverwrite() {
		return noOverwrite;
	}

	public void setNoOverwrite(boolean noOverwrite) {
		this.noOverwrite = noOverwrite;
	}

	public String getMD5()
	{
		if (md5 == null) {
			MCUpdater.apiLogger.warning("No MD5 for ConfigFile: " + path);
			return "";
		}
		return md5;
	}
	
	public void setMD5(String md5)
	{
		if( md5 != null )
			this.md5 = md5.toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getFriendlyName() {
		return "ConfigFile: " + getPath();
	}

	@Override
	public String toString() {
		return getFriendlyName();
	}

}
