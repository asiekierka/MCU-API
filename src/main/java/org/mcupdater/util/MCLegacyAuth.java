package org.mcupdater.util;

import org.mcupdater.Version;
import org.mcupdater.model.LoginData;
import org.mcupdater.util.MCLoginException.ResponseType;

import java.util.HashMap;
import java.util.logging.Level;

public class MCLegacyAuth {
	public static LoginData login(String username, String password) throws Exception {
		try {
			HashMap<String, Object> localHashMap = new HashMap<>();
			localHashMap.put("user", username);
			localHashMap.put("password", password);
			localHashMap.put("version", 13);
			String str = HTTPSUtils.executePost("https://login.minecraft.net/", localHashMap);
			if (str == null) {
				//showError("Can't connect to minecraft.net");
				throw new MCLoginException(ResponseType.NOCONNECTION);
			}
			if (!str.contains(":")) {
				if (str.trim().equals("Bad login")) {
					throw new MCLoginException(ResponseType.BADLOGIN);
				} else if (str.trim().equals("Old version")) {
					throw new MCLoginException(ResponseType.OLDVERSION);
				} else if (str.trim().equals("User not premium")) {
					throw new MCLoginException(ResponseType.OLDLAUNCHER);
				} else {
					throw new MCLoginException(str);
				}
			}
			if (!Version.isMasterBranch()) {
				MCUpdater.apiLogger.info("Login response string: " + str);
			}
			String[] arrayOfString = str.split(":");

			LoginData login = new LoginData();
			login.setUserName(arrayOfString[2].trim());
			login.setLatestVersion(arrayOfString[0].trim());
			login.setSessionId(arrayOfString[3].trim());
			login.setUUID(arrayOfString[4].trim());
			return login;

		} catch (MCLoginException mcle) {
			throw mcle;
		} catch (Exception localException) {
			MCUpdater.apiLogger.log(Level.SEVERE, "General error", localException);
			throw localException;
		}
	}
}