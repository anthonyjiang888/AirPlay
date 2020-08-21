//@formatter:off
package com.zenithairplayreceive.pro.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.center.DlnaEventBroadcastFactory;
import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.center.DlnaMediaModelFactory;
import com.zenithairplayreceive.pro.center.MediaControlBroadcastFactory;
import com.zenithairplayreceive.pro.helper.HelperDownloadLyric;
import com.zenithairplayreceive.pro.helper.HelperLoader;
import com.zenithairplayreceive.pro.player.EnginePlayMusic;
import com.zenithairplayreceive.pro.player.ListenerEnginePlay;
import com.zenithairplayreceive.pro.player.TimerAbstract;
import com.zenithairplayreceive.pro.player.TimerCheckDelay;
import com.zenithairplayreceive.pro.player.TimerSingleSecond;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;
import com.zenithairplayreceive.pro.util.UtilCommon;
import com.zenithairplayreceive.pro.util.UtilDlna;
import com.zenithairplayreceive.pro.util.UtilImage;
import com.zenithairplayreceive.pro.util.UtilMusic;
import com.zenithairplayreceive.pro.view.ViewLyric;
import com.zenithairplayreceive.pro.view.ViewVisualizer;

import java.io.File;

public class ActivityMusic extends ActivityBase implements
		MediaControlBroadcastFactory.ListenerMediaControl,
		OnBufferingUpdateListener,
		OnSeekCompleteListener,
		OnErrorListener,
		HelperDownloadLyric.ListenerDownloadLyric
	{

	private class ListenerEnginePlayMusic implements ListenerEnginePlay {
		@Override
		public void OnTrackPlay(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StartTimer();
			DlnaEventBroadcastFactory.SendEventStatePlay(m_context);
			m_managerUi.ShowPlay(false);
			m_managerUi.ShowViewPrepareLoad(false);
			m_managerUi.ShowViewControl(true);
		}

		@Override
		public void OnTrackStop(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventStateStop(m_context);
			m_managerUi.ShowPlay(true);
			m_managerUi.UpdateViewMediaInfo(m_modelMedia);
			m_managerUi.ShowViewLoad(false);
			DelayToExit();
		}

		@Override
		public void OnTrackPause(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventStatePause(m_context);
			m_managerUi.ShowPlay(true);
		}

		@Override
		public void OnTrackPrepareSync(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventTranstion(m_context);
		}

		@Override
		public void OnTrackPrepareComplete(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			int duration = m_enginePlayMusic.GetDuration();
			DlnaEventBroadcastFactory.SendEventDuration(m_context, duration);
			m_managerUi.SetSeekbarMax(duration);
			m_managerUi.SetTimeTotal(duration);
		}

		@Override
		public void OnTrackStreamError(DlnaMediaModel itemInfo) {
			log.e("OnTrackStreamError");
			m_timerPlayPos.StopTimer();
			m_enginePlayMusic.Stop();
			m_managerUi.ShowPlayErrorTip();
		}

		@Override
		public void OnTrackPlayComplete(DlnaMediaModel itemInfo) {
			log.e("OnTrackPlayComplete");
			m_enginePlayMusic.Stop();
		}
	}


	private class ManagerUi implements OnClickListener, OnSeekBarChangeListener, OnDataCaptureListener {

		private final int DRAW_OFFSET_Y = 200;

		public View		m_viewPrepare;
		public View		m_viewLoad;
		public TextView m_txtSpeedPrepare;
		public TextView m_txtSpeedLoad;

		public View			m_viewControl;
		public TextView		m_txtSong;
		public TextView		m_txtArtist;
		public TextView		m_txtAlbum;

		public ImageButton	m_btnPlay;
		public ImageButton	m_btnPause;
		public SeekBar		m_seekbar;
		public TextView		m_txtTimeCurrent;
		public TextView		m_txtTimeTotal;
		public ViewVisualizer	m_viewVisualizer;
		public ImageView		m_imgAlbum;

		public TranslateAnimation	m_animHideDown;
		public AlphaAnimation		m_animHideAlpha;

		public View			m_viewSongInfo;
		public ViewLyric	m_viewLyric;

		private boolean	m_bShowLyric = false;
		private boolean	m_bTouchSeekbar = false;



		public ManagerUi() {
			InitView();
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_play:
					Play();
					break;
				case R.id.btn_pause:
					Pause();
					break;
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			m_managerUi.SetTimeCurrent(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_bTouchSeekbar = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_bTouchSeekbar = false;
			Seek(seekBar.getProgress());
		}

		@Override
		public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
			m_viewVisualizer.UpdateVisualizer(fft);
		}

		@Override
		public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
			m_viewVisualizer.UpdateVisualizer(waveform);
		}

		public void InitView() {
			m_viewPrepare = findViewById(R.id.prepare_panel);
			m_txtSpeedPrepare = (TextView)findViewById(R.id.tv_prepare_speed);

			m_viewLoad = findViewById(R.id.loading_panel);
			m_txtSpeedLoad = (TextView)findViewById(R.id.tv_speed);

			m_viewControl = findViewById(R.id.control_panel);
			m_txtSong = (TextView)findViewById(R.id.tv_title);
			m_txtArtist = (TextView)findViewById(R.id.tv_artist);
			m_txtAlbum = (TextView)findViewById(R.id.tv_album);

			m_btnPlay = (ImageButton)findViewById(R.id.btn_play);
			m_btnPause = (ImageButton)findViewById(R.id.btn_pause);
			m_btnPlay.setOnClickListener(this);
			m_btnPause.setOnClickListener(this);
			m_seekbar = (SeekBar)findViewById(R.id.playback_seeker);
			m_txtTimeCurrent = (TextView)findViewById(R.id.tv_curTime);
			m_txtTimeTotal = (TextView)findViewById(R.id.tv_totalTime);
			m_viewVisualizer = (ViewVisualizer)findViewById(R.id.mp_freq_view);
			m_imgAlbum = (ImageView)findViewById(R.id.iv_album);
			SetListenerSeekbar(this);

			m_viewSongInfo = findViewById(R.id.song_info_view);
			m_viewLyric = (ViewLyric)findViewById(R.id.lrc_view);

			m_animHideDown = new TranslateAnimation(0.0f, 0.0f, 0.0f, 200.0f);
			m_animHideDown.setDuration(1000);

			m_animHideAlpha = new AlphaAnimation(1, 0);
			m_animHideAlpha.setDuration(1000);

			UpdateAlbumPicture(getResources().getDrawable(R.drawable.mp_music_default));
		}

		public void RefreshLyric(int a_iPos) {
			if (a_iPos > 0) {
				m_viewLyric.SetOffsetY(
					DRAW_OFFSET_Y - m_viewLyric.SelectIndex(a_iPos) * (
						m_viewLyric.GetSizeWord() + ViewLyric.INTERVAL - 1
					)
				);
			}
			else {
				m_viewLyric.SetOffsetY(DRAW_OFFSET_Y);
			}
			m_viewLyric.invalidate();
		}

		public void UpdateAlbumPicture(Drawable drawable) {
			Bitmap bitmap = UtilImage.CreateBitmapRotateReflect(drawable);
			if (bitmap != null) {
				m_imgAlbum.setImageBitmap(bitmap);
			}
		}

		public void UpdateViewMediaInfo(DlnaMediaModel a_modelMedia) {
			SetTimeCurrent(0);
			SetTimeTotal(0);
			SetSeekbarMax(100);
			SetSeekbarProgress(0);

			m_txtSong.setText(a_modelMedia.GetTitle());
			m_txtArtist.setText(a_modelMedia.GetArtist());
			m_txtAlbum.setText(a_modelMedia.GetAlbum());
		}

		public void UpdateViewLyric(DlnaMediaModel a_modelMedia) {
			log.e("UpdateViewLyric song:" + a_modelMedia.GetTitle() + ", artist:" + a_modelMedia.GetArtist());

			m_viewLyric.Read(a_modelMedia.GetTitle(), a_modelMedia.GetArtist());
			int pos = 0;
			pos = m_enginePlayMusic.GetCurrentPos();
			RefreshLyric(pos);
		}

		public void ShowViewLyric(boolean a_bShow) {
			m_bShowLyric = a_bShow;
			if (a_bShow) {
				m_viewLyric.setVisibility(View.VISIBLE);
				m_viewSongInfo.setVisibility(View.GONE);
			}
			else {
				m_viewLyric.setVisibility(View.GONE);
				m_viewSongInfo.setVisibility(View.VISIBLE);
			}
		}

		public void ShowViewPrepareLoad(boolean a_bShow) {
			if (a_bShow) {
				m_viewPrepare.setVisibility(View.VISIBLE);
			}
			else {
				m_viewPrepare.setVisibility(View.GONE);
			}
		}

		public void ShowViewControl(boolean a_bShow) {
			if (a_bShow) {
				m_viewControl.setVisibility(View.VISIBLE);
			}
			else {
				m_viewControl.setVisibility(View.GONE);
			}

		}

		public void ShowViewLoad(boolean a_bShow) {
			if (a_bShow) {
				m_viewLoad.setVisibility(View.VISIBLE);
			}
			else {
				if (m_viewLoad.isShown()) {
					m_viewLoad.startAnimation(m_animHideAlpha);
					m_viewLoad.setVisibility(View.GONE);
				}
			}
		}

		public void ShowPlay(boolean a_bShow) {
			if (a_bShow) {
				m_btnPlay.setVisibility(View.VISIBLE);
				m_btnPause.setVisibility(View.INVISIBLE);
			}
			else {
				m_btnPlay.setVisibility(View.INVISIBLE);
				m_btnPause.setVisibility(View.VISIBLE);
			}
		}

		public void ShowPlayErrorTip() {
			Toast.makeText(ActivityMusic.this, R.string.toast_musicplay_fail, Toast.LENGTH_SHORT).show();
		}

		public boolean IsShowingViewLyric() {
			return m_bShowLyric;
		}

		public boolean IsShowingViewControl() {
			return m_viewControl.getVisibility() == View.VISIBLE ? true : false;
		}

		public boolean IsShowingViewLoad() {
			if (m_viewLoad.getVisibility() == View.VISIBLE ||
				m_viewPrepare.getVisibility() == View.VISIBLE) {
				return true;
			}

			return false;
		}

		public void TogglePlayPause() {
			if (m_btnPlay.isShown()) {
				Play();
			}
			else {
				Pause();
			}
		}

		public void SetSeekbarProgress(int a_iTime) {
			if (!m_bTouchSeekbar) {
				m_seekbar.setProgress(a_iTime);
			}
		}

		public void SetSeekbarSecondProgress(int a_iTime) {
			m_seekbar.setSecondaryProgress(a_iTime);
		}

		public void SetSeekbarMax(int a_iMax) {
			m_seekbar.setMax(a_iMax);
		}

		public void SetTimeCurrent(int a_iTimeCurrent) {
			String timeString = UtilDlna.FormateTime(a_iTimeCurrent);
			m_txtTimeCurrent.setText(timeString);
		}

		public void SetTimeTotal(int a_iTimeTotal) {
			String timeString = UtilDlna.FormateTime(a_iTimeTotal);
			m_txtTimeTotal.setText(timeString);
		}

		public void SetSpeed(float a_fSpeed) {
			String showString = (int)a_fSpeed + "KB/" + getResources().getString(R.string.second);
			m_txtSpeedPrepare.setText(showString);
			m_txtSpeedLoad.setText(showString);
		}

		public void SetListenerSeekbar(OnSeekBarChangeListener a_listenerSeekbar) {
			m_seekbar.setOnSeekBarChangeListener(a_listenerSeekbar);
		}

	}


	private static final LogCommon log = LogFactory.CreateLog();

	private final static int REFRESH_CURPOS				= 0x0001;
	private final static int EXIT_ACTIVITY				= 0x0003;
	private final static int REFRESH_SPEED				= 0x0004;
	private final static int CHECK_DELAY				= 0x0005;
	private final static int LOAD_DRAWABLE_COMPLETE		= 0x0006;
	private final static int UPDATE_LRC_VIEW			= 0x0007;

	private final static int EXIT_DELAY_TIME	= 1000;

	private Context		m_context;

	private ManagerUi			m_managerUi;
	private EnginePlayMusic		m_enginePlayMusic;
	private ListenerEnginePlayMusic		m_listenerEnginePlayMusic;
	private MediaControlBroadcastFactory	m_factoryMediaControlBroadcast;
	private HelperDownloadLyric				m_helperDownloadLyric;

	private DlnaMediaModel m_modelMedia = new DlnaMediaModel();
	private Handler		m_handler;

	private TimerAbstract		m_timerPlayPos;
	private TimerAbstract		m_timerNetwork;
	private TimerCheckDelay		m_timerCheckDelay;

	private boolean m_bIsDestroy = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.e("onCreate");
		setContentView(R.layout.music_player_layout);
		InitView();
		InitData();

		RefreshIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		log.e("onNewIntent");
		RefreshIntent(intent);

		super.onNewIntent(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();

		m_enginePlayMusic.Exit();
		m_helperDownloadLyric.Deinit();
		m_timerCheckDelay.StopTimer();
		m_timerNetwork.StopTimer();
		m_factoryMediaControlBroadcast.Unregister();
		m_timerPlayPos.StopTimer();

		finish();
	}

	@Override
	protected void onDestroy() {
		log.e("onDestroy");
		m_bIsDestroy = true;

		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();

		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
				if (keyAction == KeyEvent.ACTION_UP) {
					if (m_managerUi.IsShowingViewLyric()) {
						m_managerUi.ShowViewLyric(false);
					}
					else {
						m_managerUi.ShowViewLyric(true);
					}
					return true;
				}
				break;
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//	log.e("onBufferingUpdate --> percen = " + percent + ", curPos = " + mp.getCurrentPosition());

		int duration = m_enginePlayMusic.GetDuration();
		int time = duration * percent / 100;
		m_managerUi.SetSeekbarSecondProgress(time);
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		log.e("onSeekComplete ...");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		m_managerUi.ShowPlayErrorTip();
		log.e("onError what = " + what + ", extra = " + extra);
		return false;
	}

	@Override
	public void OnCommandPlay() {
		Play();
	}

	@Override
	public void OnCommandPause() {
		Pause();
	}

	@Override
	public void OnCommandStop(int type) {
		Stop();
	}

	@Override
	public void OnCommandSeek(int time) {
		log.e("onSeekCmd time = " + time);
		Seek(time);
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

	@Override
	public void OnCompleteDownloadLyric(boolean isSuccess, String song, String artist) {
		if (isSuccess && song.equals(m_modelMedia.GetTitle()) && artist.equals(m_modelMedia.GetArtist())) {
			Message msg = m_handler.obtainMessage(UPDATE_LRC_VIEW);
			msg.sendToTarget();
		}
	}

	public void InitView() {
		m_context = this;
		m_managerUi = new ManagerUi();
	}

	public void InitData() {
		m_timerPlayPos = new TimerSingleSecond(this);
		m_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case REFRESH_CURPOS:
						RefreshCurrentPos();
						m_managerUi.RefreshLyric(m_enginePlayMusic.GetCurrentPos());
						break;
					case EXIT_ACTIVITY:
						finish();
						break;
					case REFRESH_SPEED:
						RefreshSpeed();
						break;
					case CHECK_DELAY:
						CheckDelay();
						break;
					case LOAD_DRAWABLE_COMPLETE:
						Object object = msg.obj;
						Drawable drawable = null;
						if (object != null) {
							drawable = (Drawable)object;
						}
						WhenLoadDrawableComplete(drawable);
						break;
					case UPDATE_LRC_VIEW:
						m_managerUi.UpdateViewLyric(m_modelMedia);
						break;
				}
			}

		};

		m_timerPlayPos.SetHandler(m_handler, REFRESH_CURPOS);

		m_timerNetwork = new TimerSingleSecond(this);
		m_timerNetwork.SetHandler(m_handler, REFRESH_SPEED);
		m_timerCheckDelay = new TimerCheckDelay(this);
		m_timerCheckDelay.SetHandler(m_handler, CHECK_DELAY);

		m_enginePlayMusic = new EnginePlayMusic(this);
		m_enginePlayMusic.SetListenerBufferingUpdate(this);
		m_enginePlayMusic.SetListenerSeekComplete(this);
		m_enginePlayMusic.SetListenerDataCapture(m_managerUi);
		m_listenerEnginePlayMusic = new ListenerEnginePlayMusic();
		m_enginePlayMusic.SetListenerEnginePlay(m_listenerEnginePlayMusic);

		m_factoryMediaControlBroadcast = new MediaControlBroadcastFactory(m_context);
		m_factoryMediaControlBroadcast.Register(this);

		m_helperDownloadLyric = new HelperDownloadLyric();
		m_helperDownloadLyric.Init();

		m_timerNetwork.StartTimer();
		m_timerCheckDelay.StartTimer();

		m_managerUi.ShowViewLyric(false);
	}

	private void RefreshIntent(Intent intent) {
		log.e("RefreshIntent");
		RemoveExitMsg();
		if (intent != null) {
			m_modelMedia = DlnaMediaModelFactory.CreateFromIntent(intent);
		}

		m_managerUi.UpdateViewMediaInfo(m_modelMedia);
		m_enginePlayMusic.PlayMedia(m_modelMedia);
		HelperLoader.SyncDownloadDrawable(m_modelMedia.GetUriAlbumIcon(), m_handler, LOAD_DRAWABLE_COMPLETE);

		m_managerUi.ShowViewPrepareLoad(true);
		m_managerUi.ShowViewLoad(false);
		m_managerUi.ShowViewControl(false);

		boolean need = CheckNeedDownLyric(m_modelMedia);
		log.e("CheckNeedDownLyric need = " + need);
		if (need) {
			m_helperDownloadLyric.SyncDownloadLyric(m_modelMedia.GetTitle(), m_modelMedia.GetArtist(), this);
		}
		m_managerUi.UpdateViewLyric(m_modelMedia);
	}

	private void RemoveExitMsg() {
		m_handler.removeMessages(EXIT_ACTIVITY);
	}

	private void DelayToExit() {
		log.e("DelayToExit");
		RemoveExitMsg();
		m_handler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
	}

	public void Play() {
		m_enginePlayMusic.Play();
	}

	public void Pause() {
		m_enginePlayMusic.Pause();
	}

	public void Stop() {
		m_enginePlayMusic.Stop();
	}

	public void Seek(int pos) {
		m_enginePlayMusic.SkipTo(pos);
		m_managerUi.SetSeekbarProgress(pos);
	}

	public void RefreshCurrentPos() {
		int pos = m_enginePlayMusic.GetCurrentPos();

		m_managerUi.SetSeekbarProgress(pos);
		DlnaEventBroadcastFactory.SendEventSeek(m_context, pos);
	}

	public void RefreshSpeed() {
		if (m_managerUi.IsShowingViewLoad()) {
			float speed = UtilCommon.GetSysNetworkDownloadSpeed();
			m_managerUi.SetSpeed(speed);
		}
	}

	public void CheckDelay() {
		int pos = m_enginePlayMusic.GetCurrentPos();

		boolean ret = m_timerCheckDelay.IsDelay(pos);
		if (ret) {
			m_managerUi.ShowViewLoad(true);
		}
		else {
			m_managerUi.ShowViewLoad(false);
		}

		m_timerCheckDelay.SetPos(pos);
	}

	public void WhenLoadDrawableComplete(Drawable drawable) {
		if (m_bIsDestroy || drawable == null) {
			return;
		}

		m_managerUi.UpdateAlbumPicture(drawable);
	}

	private boolean CheckNeedDownLyric(DlnaMediaModel mediaInfo) {
		String lyricPath = UtilMusic.GetLyricFile(mediaInfo.GetTitle(), mediaInfo.GetArtist());
		if (lyricPath != null) {
			File f = new File(lyricPath);
			if (f.exists()) {
				return false;
			}
		}

		return true;
	}

}
