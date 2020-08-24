//@formatter:off
package com.zenithairplayreceive.pro.util;


public class LogFactory {
	private static final String TAG = "MediaRender";
	private static LogCommon log = null;

	public static LogCommon CreateLog() {
		if (log == null) {
			log = new LogCommon();
		}

		log.SetTag(TAG);
		return log;
	}

	public static LogCommon CreateLog(String tag) {
		if (log == null) {
			log = new LogCommon();
		}

		if (tag == null || tag.length() < 1) {
			log.SetTag(TAG);
		}
		else {
			log.SetTag(tag);
		}
		return log;
	}
}