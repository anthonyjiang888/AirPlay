//@formatter:off
package com.zenithairplayreceive.pro.util;

import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UtilNet {

	private static final LogCommon log = LogFactory.CreateLog();

	public static Drawable RequestDrawableByUri(String a_sUri) {
		if (a_sUri == null || a_sUri.length() == 0) {
			return null;
		}

		Drawable drawable = null;
		int index = 0;
		while (true) {
			if (index >= 3) {
				break;
			}
			drawable = GetDrawableFromUri(a_sUri);
			if (drawable != null) {
				break;
			}
			index++;
		}

		return drawable;
	}

	public static Drawable GetDrawableFromUri(String a_sUri) {
		if (a_sUri == null || a_sUri.length() < 1) {
			return null;
		}
		Drawable drawable = null;
		try {
			URL url = new URL(a_sUri);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			InputStream input = connection.getInputStream();
			if (connection.getResponseCode() != 200) {
				log.e("GetDrawableFromUri.getResponseCode() = " + connection.getResponseCode() + "\n"
					+ "uri :" + a_sUri + "is invalid!!!"
				);
				input.close();
				return null;
			}
			drawable = Drawable.createFromStream(input, "src");
			input.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			//log.e("GetDrawableFromUri catch exception!!!e = " + e.getMessage());
		}

		return drawable;
	}
}
