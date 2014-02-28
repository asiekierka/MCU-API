package org.mcupdater;

import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

public abstract class MCUApp {
	
	public Logger baseLogger;

	public abstract void setStatus(String string);
	//public abstract void setProgressBar(int i);
	public abstract void log(String msg);
	public abstract Profile requestLogin(String username);
	public abstract void addServer(ServerList entry);
	public abstract DownloadQueue submitNewQueue(String queueName, String parent, Collection<Downloadable> files, File basePath, File cachePath);
	public abstract DownloadQueue submitAssetsQueue(String queueName, String parent, MinecraftVersion version);
}