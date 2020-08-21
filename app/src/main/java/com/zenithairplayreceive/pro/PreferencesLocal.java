//@formatter:off
package com.zenithairplayreceive.pro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;


public class PreferencesLocal {

	public static boolean CommitDevName(Context a_context, String a_sNameDev) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(a_context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Const.PREF_DEVICE_NAME, a_sNameDev);
		editor.commit();
		return true;
	}

	public static String GetDevName(Context a_context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(a_context);
		String sNameDev = sharedPreferences.getString(Const.PREF_DEVICE_NAME, Build.MODEL);
		return sNameDev;
	}
}
