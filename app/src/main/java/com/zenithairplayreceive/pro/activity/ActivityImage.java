//@formatter:off
package com.zenithairplayreceive.pro.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.center.DlnaMediaModelFactory;
import com.zenithairplayreceive.pro.center.MediaControlBroadcastFactory;
import com.zenithairplayreceive.pro.helper.HelperDownload;
import com.zenithairplayreceive.pro.helper.HelperFile;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;
import com.zenithairplayreceive.pro.util.ManagerFile;
import com.zenithairplayreceive.pro.util.UtilCommon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ActivityImage extends ActivityBase implements
		MediaControlBroadcastFactory.ListenerMediaControl,
		HelperDownload.ListenerDownLoad
	{

	private class ManagerUi {
		public ImageView	m_imgView;
		public View			m_viewLoad;

		public Bitmap m_bitmapRecycle;
		public boolean m_bIsScaleBitmap = false;

		public ManagerUi() {
			InitView();
		}

		private void InitView() {
			m_imgView = (ImageView)findViewById(R.id.imageview);
			m_viewLoad = findViewById(R.id.show_load_progress);
		}

		public void SetBitmap(Bitmap bitmap) {
			if (m_bitmapRecycle != null && !m_bitmapRecycle.isRecycled()) {
				m_imgView.setImageBitmap(null);
				m_bitmapRecycle.recycle();
				m_bitmapRecycle = null;
			}

			if (m_bIsScaleBitmap) {
				m_imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
			else {
				m_imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			}

			m_bitmapRecycle = bitmap;
			m_imgView.setImageBitmap(m_bitmapRecycle);
		}

		public boolean IsShowingLoadView() {
			if (m_viewLoad.getVisibility() == View.VISIBLE) {
				return true;
			}

			return false;
		}

		public void ShowProgress(boolean a_bShow) {
			if (a_bShow) {
				m_viewLoad.setVisibility(View.VISIBLE);
			}
			else {
				m_viewLoad.setVisibility(View.GONE);
			}
		}

		public void ShowLoadFailTip() {
			ShowToask(R.string.load_image_fail);
		}

		public void ShowParseFailTip() {
			ShowToask(R.string.parse_image_fail);
		}

		private void ShowToask(int tip) {
			Toast.makeText(ActivityImage.this, tip, Toast.LENGTH_SHORT).show();
		}
	}


	private class RunDelCacheFile implements Runnable {
		private Thread m_thread;
		private String m_sFilePath;

		public RunDelCacheFile() {
		}

		public boolean Start(String directory) {
			if (m_thread != null) {
				if (m_thread.isAlive()) {
					return false;
				}
			}
			m_sFilePath = directory;
			m_thread = new Thread(this);
			m_thread.start();

			return true;
		}

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			log.e("RunDelCacheFile run...");
			try {
				HelperFile.DeleteDirectory(m_sFilePath);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			long interval = System.currentTimeMillis() - time;
			log.e("RunDelCacheFile del over, cost time = " + interval);
		}

	}


	private static final LogCommon log = LogFactory.CreateLog();

	private static final int REFRESH_SPEED = 0x0001;
	private static final int EXIT_ACTIVITY = 0x0002;
	private static final int EXIT_DELAY_TIME = 1000;


	private int m_iScreenWidth = 0;
	private int m_iScreenHeight = 0;

	private Handler			m_handler;
	private ManagerUi		m_managerUi;
	private HelperDownload	m_helperDownload;
	private RunDelCacheFile	m_runDelCacheFile;

	private DlnaMediaModel m_modelMedia = new DlnaMediaModel();
	private MediaControlBroadcastFactory m_factoryMediaControlBroadcast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.e("onCreate");

		setContentView(R.layout.image_player_layout);

		InitView();
		InitData();
		RefreshIntent(getIntent());
	}

	@Override
	protected void onDestroy() {
		log.e("onDestroy");

		m_factoryMediaControlBroadcast.Unregister();
		m_helperDownload.Deinit();
		m_runDelCacheFile.Start(ManagerFile.GetRootDirToSave());
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		RefreshIntent(intent);
	}

	@Override
	public void OnDownloadResult(boolean isSuccess, String savePath) {
		OnTransDelLoadResult(isSuccess, savePath);
	}

	@Override
	public void OnCommandPlay() {
	}

	@Override
	public void OnCommandPause() {
	}

	@Override
	public void OnCommandStop(int type) {
		log.e("OnCommandStop");
		DelayToExit();
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

	private void InitView() {
		m_managerUi = new ManagerUi();
	}

	private void InitData() {
		m_iScreenWidth = UtilCommon.GetScreenWidth(this);
		m_iScreenHeight = UtilCommon.GetScreenHeight(this);


		m_factoryMediaControlBroadcast = new MediaControlBroadcastFactory(this);
		m_factoryMediaControlBroadcast.Register(this);

		m_helperDownload = new HelperDownload();
		m_helperDownload.Init();

		m_runDelCacheFile = new RunDelCacheFile();

		m_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case EXIT_ACTIVITY:
						finish();
						break;
				}
			}
		};
	}

	private void RefreshIntent(Intent intent) {
		RemoveExitMessage();
		if (intent != null) {
			m_modelMedia = DlnaMediaModelFactory.CreateFromIntent(intent);
		}

		String requesUrl = m_modelMedia.GetUrl();
		if (requesUrl.contains("/mnt/sdcard/Android/data/")) {
			//airplay pushed image is in Local
			m_managerUi.ShowProgress(false);
			OnDownloadResult(true, requesUrl);
		}
		else {
			//dlna pushed image must be downloaded
			String saveUri = ManagerFile.GetFullPathToSave(requesUrl);
			if (null == saveUri || saveUri.length() < 1) {
				return;
			}
			m_managerUi.ShowProgress(true);
			m_helperDownload.SyncDownLoadFile(m_modelMedia.GetUrl(), ManagerFile.GetFullPathToSave(requesUrl), this);
		}
	}

	private void RemoveExitMessage() {
		m_handler.removeMessages(EXIT_ACTIVITY);
	}

	private void DelayToExit() {
		RemoveExitMessage();
		m_handler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
	}

	private void OnTransDelLoadResult(final boolean a_bIsSuccess, final String a_sSavePath) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m_managerUi.ShowProgress(false);

				if (!a_bIsSuccess) {
					m_managerUi.ShowLoadFailTip();
					return;
				}

				Bitmap bitmap = DecodeOptionsFile(a_sSavePath);
				if (bitmap == null) {
					m_managerUi.ShowParseFailTip();
					return;
				}

				m_managerUi.SetBitmap(bitmap);
			}
		});
	}

	public Bitmap DecodeOptionsFile(String a_sFilePath) {
		try {
			File file = new File(a_sFilePath);
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(file), null, opt);
			int iTempWidth	= opt.outWidth;
			int iTempHeight	= opt.outHeight;
			int iScale = 1;
			if (iTempWidth <= m_iScreenWidth && iTempHeight <= m_iScreenHeight) {
				iScale = 1;
				m_managerUi.m_bIsScaleBitmap = false;
			}
			else {
				double dFitWidth	= iTempWidth * 1.0 / m_iScreenWidth;
				double dFitHeight	= iTempHeight * 1.0 / m_iScreenHeight;
				double dFit = dFitWidth > dFitHeight ? dFitWidth : dFitHeight;
				iScale = (int)(dFit + 0.5);
				m_managerUi.m_bIsScaleBitmap = true;
			}
			Bitmap bitmap = null;
			if (iScale == 1) {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
				if (bitmap != null) {
					log.e("scale = 1 bitmap.size = " + bitmap.getRowBytes() * bitmap.getHeight());
				}
			}
			else {
				BitmapFactory.Options opt2 = new BitmapFactory.Options();
				opt2.inSampleSize = iScale;
				bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opt2);
				if (bitmap != null) {
					log.e("scale = " + opt2.inSampleSize + " bitmap.size = " + bitmap.getRowBytes() * bitmap.getHeight());
				}
			}

			return bitmap;
		}
		catch (FileNotFoundException e) {
			log.e("fileNotFoundException, e: " + e.toString());
		}
		return null;
	}

}
