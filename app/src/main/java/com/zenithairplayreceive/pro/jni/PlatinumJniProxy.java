//@formatter:off
package com.zenithairplayreceive.pro.jni;

import android.content.Context;

import com.zenithairplayreceive.pro.ApplicationAirPlay;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlatinumJniProxy {

	static {
		System.loadLibrary("jniinterface");
		System.loadLibrary("mediaserver");
	}

	public static native int startMediaRender(String friendname, String libpath, String activecode, int width, int height, int airtunes_port, int airplay_port, int rcv_size, Context obj);
	public static native int stopMediaRender();
	public static native boolean responseGenaEvent(int cmd, byte[] value, byte[] data);
	public static native boolean enableLogPrint(boolean flag);

	public static String FindLibrary(Context context, String sLibName) {
		String sResult = null;
		ClassLoader classLoader = (context.getClassLoader());
		if (classLoader != null) {
			try {
				Method methodFindLib = classLoader.getClass().getMethod("findLibrary", new Class<?>[] {String.class});
				if (methodFindLib != null) {
					Object objPath = methodFindLib.invoke(classLoader, new Object[] {sLibName});
					if (objPath != null && objPath instanceof String) {
						sResult = (String)objPath;
					}
				}
			}
			catch (NoSuchMethodException e) {
			}
			catch (IllegalAccessException e) {
			}
			catch (IllegalArgumentException e) {
			}
			catch (InvocationTargetException e) {
			}
			catch (Exception e) {
			}
		}

		return sResult;
	}


	public static int StartRender(String sFriendName) {
		if (sFriendName == null)
			sFriendName = "";

		int iRet = -1;

		String sObjPath = FindLibrary(ApplicationAirPlay.Instance(), "mediaserver");
//		iRet = startMediaRender(sFriendName, sObjPath, "000000000", 1280, 720, 47000, 7000, 128 * 1024, ApplicationAirPlay.Instance());
		iRet = startMediaRender(sFriendName, sObjPath, "000000000", 1440, 810, 47000, 7000, 1440 * 810, ApplicationAirPlay.Instance());
		return iRet;
	}

	public static boolean ResponseEvent(int iCmd, String sValue, String sData) {
		if (sValue == null)	sValue = "";
		if (sData == null)	sData = "";
		boolean bRet = false;
		try {
			bRet = responseGenaEvent(iCmd, sValue.getBytes("utf-8"), sData.getBytes("utf-8"));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bRet;
	}
}
