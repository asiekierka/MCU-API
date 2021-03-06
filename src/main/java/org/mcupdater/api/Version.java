package org.mcupdater.api;

import org.mcupdater.MCUApp;

import java.io.IOException;
import java.util.Properties;

public class Version {
	public static final int MAJOR_VERSION;
	public static final int MINOR_VERSION;
	public static final int BUILD_VERSION;
	public static final String BUILD_BRANCH;
	public static final String BUILD_LABEL;
	static {
		Properties prop = new Properties();
		try {
			prop.load(Version.class.getResourceAsStream("/org/mcupdater/api/version.properties"));
		} catch (IOException e) {
		}
		int major;
		int minor;
		int build;
		String branch;
		try {
			major = Integer.valueOf(prop.getProperty("major", "0"));
			minor = Integer.valueOf(prop.getProperty("minor", "0"));
			build = Integer.valueOf(prop.getProperty("build_version", "0"));
			branch = prop.getProperty("git_branch", "unknown");
		} catch (Exception e) {
			major = 3;
			minor = 4;
			build = 999;
			branch = "develop";
		}
		MAJOR_VERSION = major;
		MINOR_VERSION = minor;
		BUILD_VERSION = build;
		BUILD_BRANCH = branch;
		if( BUILD_BRANCH.equals("unknown") || BUILD_BRANCH.equals("master") ) {
			BUILD_LABEL = "";
		} else {
			BUILD_LABEL = " ("+BUILD_BRANCH+")";
		}
	}
	
	public static final String API_VERSION = MAJOR_VERSION + "." + MINOR_VERSION;
	public static final String VERSION = "v"+MAJOR_VERSION+"."+MINOR_VERSION+"."+BUILD_VERSION;
	
	public static boolean isVersionOld(String packVersion) {
		if( packVersion == null ) return false;	// can't check anything if they don't tell us
		String parts[] = packVersion.split("\\.");
		try {
			int mcuParts[] = { MAJOR_VERSION, MINOR_VERSION, BUILD_VERSION };
			for( int q = 0; q < mcuParts.length && q < parts.length; ++q ) {
				int packPart = Integer.valueOf(parts[q]);
				if( packPart > mcuParts[q] ) return true;
				if( packPart < mcuParts[q] ) return false; // Since we check major, then minor, then build, if the required value < current value, we can stop checking.
			}
			return false;
		} catch( NumberFormatException e ) {
			log("Got non-numerical pack format version '"+packVersion+"'");
		} catch( ArrayIndexOutOfBoundsException e ) {
			log("Got malformed pack format version '"+packVersion+"'");
		}
		return false;
	}

	public static boolean fuzzyMatch(String version1, String version2) {
		return version1.equals(version2) || version1.startsWith(version2) || version2.startsWith(version1);
	}

	public static boolean requestedFeatureLevel(String packVersion, String featureLevelVersion) {
		String packParts[] = packVersion.split("\\.");
		String featureParts[] = featureLevelVersion.split("\\.");
		try {
			for (int q = 0; q < featureParts.length; ++q ) {
				if (Integer.valueOf(packParts[q]) > Integer.valueOf(featureParts[q])) return true;
				if (Integer.valueOf(packParts[q]) < Integer.valueOf(featureParts[q])) return false;
			}
			return true;
		} catch( NumberFormatException e ) {
			log("Got non-numerical pack format version '"+packVersion+"'");
		} catch( ArrayIndexOutOfBoundsException e ) {
			log("Got malformed pack format version '"+packVersion+"'");
		}
		return false;
	}
	
	public static boolean isMasterBranch() {
		return BUILD_BRANCH.equals("master");
	}
	public static boolean isDevBranch() {
		return BUILD_BRANCH.equals("develop");
	}
	
	// for error logging support
	public static void setApp( MCUApp app ) {
		_app = app;
	}
	private static MCUApp _app;
	private static void log(String msg) {
		if( _app != null ) {
			_app.log(msg);
		} else {
			System.out.println(msg);
		}
	}
}
