//@formatter:off
package com.zenithairplayreceive.pro.util;

import android.content.Context;

import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.PreferencesLocal;
import com.zenithairplayreceive.pro.jni.PlatinumReflection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UtilDlna {

	private static final LogCommon log = LogFactory.CreateLog();

	public final static String DLNA_OBJECTCLASS_MUSIC	= "object.item.audioItem";
	public final static String DLNA_OBJECTCLASS_VIDEO	= "object.item.videoItem";
	public final static String DLNA_OBJECTCLASS_PHOTO	= "object.item.imageItem";
	public final static String DLNA_OBJECTCLASS_SCREEN	= "object.item.screenItem";


	public static boolean SetDevName(Context context, String friendName) {
		return PreferencesLocal.CommitDevName(context, friendName);
	}

	public static String GetDevName(Context context) {
		return PreferencesLocal.GetDevName(context);
	}

	public static String Creat12BitUuid(Context context) {
		String sUuidDefault = "123456789abc";

		String sMac = UtilCommon.GetLocalMacAddress(context);

		sMac = sMac.replace(":", "");
		sMac = sMac.replace(".", "");

		if (sMac.length() != 12) {
			sMac = sUuidDefault;
		}

		sMac += "-dmr";
		return sMac;
	}

	public static int ParseSeekTime(String a_sData) throws Exception {
		int iPosSeek = 0;

		String[] vsTimeSeek = a_sData.split("=");
		if (2 != vsTimeSeek.length) {
			return iPosSeek;
		}
		String sTypeTime = vsTimeSeek[0];
		String sPosition = vsTimeSeek[1];
		if (PlatinumReflection.MEDIA_SEEK_TIME_TYPE_REL_TIME.equals(sTypeTime)) {
			iPosSeek = ConvertSeekTimeToMilli(sPosition);
		}
		else {
			log.e("timetype = " + sTypeTime + ", position = " + sPosition);
		}

		return iPosSeek;
	}

	public static int ConvertSeekTimeToMilli(String a_sTime) {
		int iHour	= 0;
		int iMin	= 0;
		int iSec	= 0;
		int iMilli	= 0;
		String[] vsTime = a_sTime.split(":");

		if (3 != vsTime.length)
			return 0;
		if (!IsNumeric(vsTime[0]))
			return 0;

		iHour = Integer.parseInt(vsTime[0]);
		if (!IsNumeric(vsTime[1]))
			return 0;

		iMin = Integer.parseInt(vsTime[1]);
		String[] vsTime2 = vsTime[2].split("\\.");
		if (2 == vsTime2.length) {
			//00:00:00.000
			if (!IsNumeric(vsTime2[0]))
				return 0;
			iSec = Integer.parseInt(vsTime2[0]);
			if (!IsNumeric(vsTime2[1]))
				return 0;
			iMilli = Integer.parseInt(vsTime2[1]);
		}
		else if (1 == vsTime2.length) {
			//00:00:00
			if (!IsNumeric(vsTime2[0]))
				return 0;
			iSec = Integer.parseInt(vsTime2[0]);
		}
		return (iHour * 3600000 + iMin * 60000 + iSec * 1000 + iMilli);
	}

	public static boolean IsNumeric(String a_sValue) {
		if ("".equals(a_sValue))
			return false;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher matcher = pattern.matcher(a_sValue);
		if (!matcher.matches()) {
			return false;
		}
		return true;
	}

	public static String FormatTimeFromMilli(int a_iTime) {
		String sHour	= "00";
		String sMin		= "00";
		String sSec		= "00";
		String sSplit	= ":";

		int iTimeTemp = a_iTime;
		int iTemp = 0;

		if (iTimeTemp >= 3600000) {
			iTemp = iTimeTemp / 3600000;
			sHour = FormatHunToStr(iTemp);
			iTimeTemp -= iTemp * 3600000;
		}
		if (iTimeTemp >= 60000) {
			iTemp = iTimeTemp / 60000;
			sMin = FormatHunToStr(iTemp);
			iTimeTemp -= iTemp * 60000;
		}
		if (iTimeTemp >= 1000) {
			iTemp = iTimeTemp / 1000;
			sSec = FormatHunToStr(iTemp);
			iTimeTemp -= iTemp * 1000;
		}

		String sResult = sHour + sSplit + sMin + sSplit + sSec;
		return sResult;
	}

	private static String FormatHunToStr(int a_iHun) {
		a_iHun = a_iHun % 100;
		if (a_iHun > 9)
			return ("" + a_iHun);
		else
			return ("0" + a_iHun);
	}

	public static String FormateTime(long a_lMilli) {
		String sValue = "";
		int iHour = 0;
		int iTime = (int)(a_lMilli / 1000);
		int iSec = iTime % 60;
		int iMin = iTime / 60;

		if (iMin >= 60) {
			iHour = iMin / 60;
			iMin %= 60;
			sValue = String.format("%02d:%02d:%02d", iHour, iMin, iSec);
		}
		else {
			sValue = String.format("%02d:%02d", iMin, iSec);
		}

		return sValue;
	}

	public static boolean IsAudioItem(DlnaMediaModel a_modelMedia) {
		String sObjectClass = a_modelMedia.GetObjectClass();
		if (sObjectClass.contains(DLNA_OBJECTCLASS_MUSIC))
			return true;
		return false;
	}

	public static boolean isVideoItem(DlnaMediaModel a_modelMedia) {
		String sObjectClass = a_modelMedia.GetObjectClass();
		if (sObjectClass.contains(DLNA_OBJECTCLASS_VIDEO))
			return true;
		return false;
	}

	public static boolean isImageItem(DlnaMediaModel a_modelMedia) {
		String sObjectClass = a_modelMedia.GetObjectClass();
		if (sObjectClass.contains(DLNA_OBJECTCLASS_PHOTO))
			return true;
		return false;
	}

	public static boolean isScreenItem(DlnaMediaModel a_modelMedia) {
		String sObjectClass = a_modelMedia.GetObjectClass();
		if (sObjectClass.contains(DLNA_OBJECTCLASS_SCREEN))
			return true;
		return false;
	}

}
