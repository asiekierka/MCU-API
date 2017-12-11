package org.mcupdater.util;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mcupdater.api.Version;
import org.mcupdater.model.CurseProject;

public enum CurseModCache {
	INSTANCE;
	
	private static final String DOWNLOAD = "/download";
	private static final String BASE_URL = "https://minecraft.curseforge.com/projects/";
	
	private CurseModCache() {
		// TODO: handle a serialized data cache location (possibly lean on DownloadCache here?)
		/**
		 * This is primarily a performance concern, as re-scraping Curse with EVERY SINGLE LOAD
		 * can get very slow very quickly. We don't want that, and we don't want Curse outages
		 * to prevent people from loading the game. So caching results is important.
		 */
	}
	
	private static String baseURL(CurseProject curse) {
		return BASE_URL+curse.getProject();
	}
	
	public static String fetchURL(CurseProject curse) {
		if( curse.getFile() > 0 ) {
			// if we have a file, just go there
			final String url = baseURL(curse)+"/files/"+curse.getFile()+DOWNLOAD;
			curse.setURL(url);
			return curse.getURL();
		} else {
			// autodiscovery scraping time
			MCUpdater.apiLogger.log(Level.INFO, "Performing URL autodiscovery for "+curse);
			final String filesURL = baseURL(curse)+"/files";
			Document filesDoc;
			try {
				filesDoc = Jsoup.connect(filesURL).validateTLSCertificates(false).get();
			} catch (IOException e) {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read project data for "+curse, e);
				return null;
			}
			
			// TODO: re-request this, filtering by MC version on their end before proceeding
			
			// identify and set file, then re-invoke fetchURL
			Elements fileList = filesDoc.getElementsByClass("project-file-list-item");
			ProjectFile file = new ProjectFile();
			for( Element el : fileList) {
				final Element elFname = el.getElementsByClass("project-file-name-container").first().children().first();
				//MCUpdater.apiLogger.log(Level.FINEST, elFname.toString());
				final String href = elFname.attr("href");
				final String fileNum = href.substring(href.lastIndexOf('/')+1);
				final int id = Integer.parseInt(fileNum);

				final Element elRelease = el.getElementsByClass("project-file-release-type").first();
				final String release_type = elRelease.children().first().attr("title");
				CurseProject.ReleaseType type = CurseProject.ReleaseType.parse(release_type);
				if( type.worseThan(curse.getReleaseType()) || type.worseThan(file.release_type) ) {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", release type mismatch, "+type);
					continue;
				}

				// filter for MC version
				final String mc_version = el.getElementsByClass("version-label").first().text();
				if( !Version.fuzzyMatch(mc_version, curse.getMCVersion()) ) {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", mc version mismatch, "+mc_version);
					continue;
				}
				
				final int upload_date = Integer.parseInt(el.getElementsByClass("standard-date").first().attr("data-epoch"));
				if( upload_date > file.upload_date ) {
					// take the newer build
					MCUpdater.apiLogger.log(Level.FINE, "Selecting "+curse+":"+id);
					file.release_type = type;
					file.upload_date = upload_date;
					file.id = id;
					file.mc_version = mc_version;
				} else {
					MCUpdater.apiLogger.log(Level.FINE, "Skipping "+curse+":"+id+", older upload date");
				}
			}
			
			// did we find a url?
			if( file.id > 0 ) {
				curse.setFile(file.id);
				return fetchURL(curse);
			} else {
				MCUpdater.apiLogger.log(Level.SEVERE, "Unable to find candidate for "+curse+" after checking "+fileList.size()+" files");
			}
		}
		
		return null;
	}
	
	public static String fetchMD5(CurseProject curse) {
		// must have a URL before we can look for an MD5 for it
		if( curse.getURL().isEmpty() ) {
			fetchURL(curse);
		}
		
		final String downloadURL = curse.getURL();
		if( downloadURL.isEmpty() ) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to fetch MD5 for "+curse+" with no URL");
			return null;
		}
		
		final String fileURL;
		if( downloadURL.endsWith(DOWNLOAD) )
			fileURL = downloadURL.substring(0, downloadURL.length() - DOWNLOAD.length());
		else {
			MCUpdater.apiLogger.log(Level.SEVERE, "Download URL for "+curse+" did not end with "+DOWNLOAD+", refusing to look for MD5");
			return null;
		}
		
		Document fileDoc;
		try {
			fileDoc = Jsoup.connect(fileURL).validateTLSCertificates(false).get();
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.SEVERE, "Unable to read file data for "+curse, e);
			return null;
		}
		Element elMD5 = fileDoc.getElementsByClass("md5").first();
		curse.setMD5(elMD5.text());
		return curse.getMD5();
	}
	
	public static String getTextID(long modID) {
		final String origURL = BASE_URL+modID;
		try {
			final Response res = Jsoup.connect(origURL).followRedirects(true).execute();
			final String newURL = res.url().getPath();
			final String textID = newURL.substring(newURL.lastIndexOf('/')+1);
			MCUpdater.apiLogger.log(Level.FINE, "Found text ID '"+textID+"' for curse:"+modID);
			return textID;
		} catch (IOException e) {
			MCUpdater.apiLogger.log(Level.WARNING, "Unable to find text ID for curse:"+modID, e);
			return Long.toString(modID);
		}
	}
	public static String getTextID(CurseProject curse) {
		final Long modID = Long.parseLong(curse.getProject());
		final String textID = getTextID(modID);
		curse.setProject(textID);
		return textID;
	}
	
	public static class ProjectFile {
		public CurseProject.ReleaseType release_type = CurseProject.ReleaseType.ALPHA;
		public int upload_date = 0;
		public String mc_version = null;
		public int id = 0;
		// TODO: add support for file size because why not
		
		public ProjectFile() {}
	}
}