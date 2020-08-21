//@formatter:off
package com.zenithairplayreceive.pro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xindawn.airgl.NativeVideo;
import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.center.DlnaMediaModelFactory;
import com.zenithairplayreceive.pro.center.MediaControlBroadcastFactory;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;
import com.zenithairplayreceive.pro.view.ViewGl;

public class ActivityAirGl extends Activity implements MediaControlBroadcastFactory.ListenerMediaControl {

	private static final LogCommon log = LogFactory.CreateLog();

	private static final int REFRESH_SPEED = 0x0001;
	private static final int EXIT_ACTIVITY = 0x0002;
	private static final int EXIT_DELAY_TIME = 1000;

	static NativeVideo m_nativeVideo = new NativeVideo();

	private ViewGroup	m_viewContent;
	private ViewGl	m_viewGl;
	private String	m_sFilePath;

	private DlnaMediaModel m_modelDlnaMedia = new DlnaMediaModel();
	private MediaControlBroadcastFactory m_factoryMediaControlBorcast;
	public static Handler m_handler;

	private int m_iIsOpened = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		GetFilePath();

		m_factoryMediaControlBorcast = new MediaControlBroadcastFactory(this);
		m_factoryMediaControlBorcast.Register(this);

		m_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case EXIT_ACTIVITY:
						log.d("EXIT_ACTIVITY");
						finish();
						break;
				}
			}

		};

		try {
			InitViews();
		}
		catch (NullPointerException e) {
			finish();
		}

		//m_sFilePath = "http://192.168.1.106:55556/airmirror-760ba1810b647cb0.flv";

		if ((m_iIsOpened = m_nativeVideo.Open(m_sFilePath)) != 0) {
			log.d("excute OnCommandStop" + "[" + m_iIsOpened + "]");
			finish();
		}

		log.d("m_nativeVideo.Open" + "[" + m_iIsOpened + "]");

		m_nativeVideo.Start();

		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		window.setAttributes(params);

		setContentView(R.layout.activity_airgl);
		m_viewContent = (RelativeLayout)findViewById(R.id.ID_VIEW_CONTENT);
		m_viewContent.addView(m_viewGl);
//		setContentView(m_viewGl);
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_viewGl.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_viewGl.onResume();
	}

	private void RemoveExitMessage() {
		m_handler.removeMessages(EXIT_ACTIVITY);
	}

	private void DelayToExit() {
		RemoveExitMessage();
		//m_handler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
		m_handler.sendEmptyMessage(EXIT_ACTIVITY);
	}


	private void GetFilePath() {
		Intent intent = getIntent();
		m_modelDlnaMedia = DlnaMediaModelFactory.CreateFromIntent(intent);
		m_sFilePath = m_modelDlnaMedia.GetUrl();
		if (m_sFilePath == null) {
			finish();
		}
	}

	private void InitViews() throws NullPointerException {
		m_viewGl = new ViewGl(this);
		if (m_viewGl == null)
			throw new NullPointerException();
	}

//	@Override
//	protected void onResume() {
//		super.onResume();
//		pManager = ((PowerManager) getSystemService(POWER_SERVICE));
//		mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
//			| PowerManager.ON_AFTER_RELEASE, TAG);
//		mWakeLock.acquire();
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		if(null != mWakeLock) {
//			mWakeLock.release();
//		}
//	}

	@Override
	protected void onDestroy() {
		log.d("onDestroy 1");
//		m_viewGl.destroyDrawingCache();
//		m_viewGl = null;

		m_factoryMediaControlBorcast.Unregister();
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int i1, KeyEvent keyevent) {
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

		log.d("finish 1");
		super.finish();
		log.d("finish 2");

		m_nativeVideo.Close();
		log.d("finish 3");
	}

	@Override
	public void OnCommandPlay() {
	}

	@Override
	public void OnCommandPause() {
	}

	@Override
	public void OnCommandStop(int type) {
		if (1 == type) {
			log.d("excute OnCommandStop" + "[" + type + "]");
			DelayToExit();
		}
		else {
			log.d("ignore OnCommandStop" + "[" + type + "]");
		}
	}

	@Override
	public void OnCommandSeek(int time) {
	}

	@Override
	public void OnCommandCover(byte data[]) {
	}

	@Override
	public void OnCommandMetaData(String data) {
	}

	@Override
	public void OnCommandIpAddr(String data) {
	}
}
