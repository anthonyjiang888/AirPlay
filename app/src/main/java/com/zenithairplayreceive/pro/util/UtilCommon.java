//@formatter:off
package com.zenithairplayreceive.pro.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.InputStream;

public class UtilCommon {

	public static class ViewSize {
		public int width = 0;
		public int height = 0;
	}


	private static long		m_lSysNetworkSpeedLastTs	= 0;
	private static long		m_lSystNetworkLastBytes		= 0;
	private static float	m_fSysNetowrkLastSpeed		= 0.0f;


	public static boolean HasSdCard() {
		String sStatus = Environment.getExternalStorageState();
		if (!sStatus.equals(Environment.MEDIA_MOUNTED))
			return false;
		return true;
	}

	public static String GetRootFilePath() {
		if (HasSdCard()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
		}
		else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath: /data/data/
		}
	}

	public static boolean CheckStateNetwork(Context context) {
		boolean bNetState = false;
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager != null) {
			NetworkInfo[] vInfo = manager.getAllNetworkInfo();
			if (vInfo != null) {
				for (int i = 0; i < vInfo.length; i++) {
					if (vInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						bNetState = true;
						break;
					}
				}
			}
		}
		return bNetState;
	}

	public static String GetLocalMacAddress(Context context) {
		String sMacDefault = "00:00:00:00:00:00";
		String sMacWifi = GetWifiMacAddress(context);
		InputStream input = null;

		if (null != sMacWifi) {
			if (!sMacWifi.equals(sMacDefault))
				return sMacWifi;
		}
		try {
			ProcessBuilder builder = new ProcessBuilder("busybox", "ifconfig");
			Process process = builder.start();
			input = process.getInputStream();

			byte[] b = new byte[1024];
			StringBuffer buffer = new StringBuffer();
			while (input.read(b) > 0) {
				buffer.append(new String(b));
			}
			String sSubValue = buffer.substring(0);
			String sFlagSys = "HWaddr ";
			int index = sSubValue.indexOf(sFlagSys);
			//List <String> address   = new ArrayList <String> ();
			if (0 < index) {
				sSubValue = buffer.substring(index + sFlagSys.length());
				//address.add(value.substring(0,18));
				sMacDefault = sSubValue.substring(0, 17);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return sMacDefault;
	}

	public static String GetWifiMacAddress(Context mc) {
		WifiManager manager = (WifiManager)mc.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}

	public static MulticastLock OpenWifiBroadcast(Context context) {
		WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		MulticastLock lockMulticast = manager.createMulticastLock("MediaRender");
		if (lockMulticast != null) {
			lockMulticast.acquire();
		}
		return lockMulticast;
	}

	public static void SetVolumeCurrent(int iPercent, Context context) {
		AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		int iVolumeMax = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		manager.setStreamVolume(
			AudioManager.STREAM_MUSIC,
			(iVolumeMax * iPercent) / 100,
			AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI
		);
		manager.setMode(AudioManager.MODE_INVALID);
	}

	public static void SetVolumeMute(Context context) {
		AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamMute(AudioManager.STREAM_MUSIC, true);
	}

	public static void SetVolumeUnmute(Context context) {
		AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamMute(AudioManager.STREAM_MUSIC, false);
	}

	public static void ShowToast(Context context, String sTip) {
		Toast.makeText(context, sTip, Toast.LENGTH_SHORT).show();
	}

	public static int GetScreenWidth(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}

	public static int GetScreenHeight(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}

	public static ViewSize GetFitSize(Context context, MediaPlayer mediaPlayer) {
		int iVideoWidth		= mediaPlayer.getVideoWidth();
		int iVideoHeight	= mediaPlayer.getVideoHeight();
		double dFit1 = iVideoWidth * 1.0 / iVideoHeight;

		int iWidth2		= GetScreenWidth(context);
		int iHeight2	= GetScreenHeight(context);
		double dFit2 = iWidth2 * 1.0 / iHeight2;

		double dFit = 1;
		if (dFit1 > dFit2) {
			dFit = iWidth2 * 1.0 / iVideoWidth;
		}
		else {
			dFit = iHeight2 * 1.0 / iVideoHeight;
		}

		ViewSize viewSize = new ViewSize();
		viewSize.width = (int)(dFit * iVideoWidth);
		viewSize.height = (int)(dFit * iVideoHeight);

		return viewSize;
	}

	public static boolean GetStateWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (wifistate != State.CONNECTED) {
			return false;
		}

		State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		boolean ret = State.CONNECTED != mobileState;
		return ret;
	}

	public static boolean GetStateMobile(Context context) {
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State stateWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (stateWifi != State.CONNECTED) {
			return false;
		}

		State stateMobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		boolean bRet = State.CONNECTED == stateMobile;
		return bRet;
	}

	public static float GetSysNetworkDownloadSpeed() {
		long lMilliNow = System.currentTimeMillis();
		long lBytesNow = TrafficStats.getTotalRxBytes();

		long lTimeInterval = lMilliNow - m_lSysNetworkSpeedLastTs;
		long lBytes = lBytesNow - m_lSystNetworkLastBytes;

		if (lTimeInterval > 0) m_fSysNetowrkLastSpeed = (float)lBytes * 1.0f / (float)lTimeInterval;

		m_lSysNetworkSpeedLastTs = lMilliNow;
		m_lSystNetworkLastBytes = lBytesNow;

		return m_fSysNetowrkLastSpeed;
	}
}


