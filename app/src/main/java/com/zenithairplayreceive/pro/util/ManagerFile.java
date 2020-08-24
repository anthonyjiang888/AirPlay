//@formatter:off
package com.zenithairplayreceive.pro.util;

public class ManagerFile {

	public static String GetRootDirToSave() {
		if (UtilCommon.HasSdCard()) {
			return UtilCommon.GetRootFilePath() + "icons/";
		}
		else {
			return UtilCommon.GetRootFilePath() + "com.zenithairplayreceive.pro/icons/";
		}
	}

	public static String GetFullPathToSave(String uri) {
		return GetRootDirToSave() + GetUriFormatted(uri);
	}

	public static String GetUriFormatted(String uri) {
		uri = uri.replace("/", "_");
		uri = uri.replace(":", "");
		uri = uri.replace("?", "_");
		uri = uri.replace("%", "_");

		int length = uri.length();
		if (length > 150) {
			uri = uri.substring(length - 150);
		}

		return uri;
	}

}
