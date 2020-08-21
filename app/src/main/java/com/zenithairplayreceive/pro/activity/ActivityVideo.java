//@formatter:off
package com.zenithairplayreceive.pro.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.center.DlnaEventBroadcastFactory;
import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.center.DlnaMediaModelFactory;
import com.zenithairplayreceive.pro.center.MediaControlBroadcastFactory;
import com.zenithairplayreceive.pro.player.EnginePlayVideo;
import com.zenithairplayreceive.pro.player.ListenerEnginePlay;
import com.zenithairplayreceive.pro.player.TimerAbstract;
import com.zenithairplayreceive.pro.player.TimerCheckDelay;
import com.zenithairplayreceive.pro.player.TimerSingleSecond;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;
import com.zenithairplayreceive.pro.util.UtilCommon;
import com.zenithairplayreceive.pro.util.UtilDlna;

public class ActivityVideo extends ActivityBase implements
		MediaControlBroadcastFactory.ListenerMediaControl,
		OnBufferingUpdateListener,
		OnSeekCompleteListener,
		OnErrorListener
	{

	private class ListenerEnginePlayVideo implements ListenerEnginePlay {
		@Override
		public void OnTrackPlay(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StartTimer();
			DlnaEventBroadcastFactory.SendEventStatePlay(m_context);
			m_managerUi.ShowPlay(false);
			m_managerUi.ShowViewControl(true);
		}

		@Override
		public void OnTrackStop(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventStateStop(m_context);
			m_managerUi.ShowPlay(true);
			m_managerUi.UpdateMediaInfoView(m_modelMedia);
			m_managerUi.ShowViewControl(true);
			m_managerUi.ShowViewLoad(false);
			DelayToExit();
		}

		@Override
		public void OnTrackPause(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventStatePause(m_context);
			m_managerUi.ShowPlay(true);
			m_managerUi.ShowViewControl();
		}

		@Override
		public void OnTrackPrepareSync(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			DlnaEventBroadcastFactory.SendEventTranstion(m_context);
		}

		@Override
		public void OnTrackPrepareComplete(DlnaMediaModel itemInfo) {
			m_timerPlayPos.StopTimer();
			int duration = m_enginePlayVideo.GetDuration();
			DlnaEventBroadcastFactory.SendEventDuration(m_context, duration);
			m_managerUi.SetSeekBarMax(duration);
			m_managerUi.SetTimeTotal(duration);
		}

		@Override
		public void OnTrackStreamError(DlnaMediaModel itemInfo) {
			log.e("OnTrackStreamError");
			m_timerPlayPos.StopTimer();
			m_enginePlayVideo.Stop();
			m_managerUi.ShowPlayErrorTip();
		}

		@Override
		public void OnTrackPlayComplete(DlnaMediaModel itemInfo) {
			log.e("OnTrackPlayComplete");
			m_enginePlayVideo.Stop();
		}
	}

	private class ManagerUi implements OnClickListener, SurfaceHolder.Callback, OnSeekBarChangeListener {

		public View			m_viewPrepare;
		public View			m_viewLoad;

		public TextView		m_txtSpeedPrepare;
		public TextView		m_txtSpeedLoad;

		public View		m_viewControl;
		public View		m_viewToolUp;
		public View		m_viewToolDown;

		public ImageButton		m_btnPlay;
		public ImageButton		m_btnPause;
		public SeekBar		m_seekbar;
		public TextView		m_txtTimeCurrnet;
		public TextView		m_txtTimeTotal;
		public TextView		m_txtTitle;

		private SurfaceView		m_viewSurface;
		private SurfaceHolder	m_holderSurface = null;

		private TranslateAnimation	m_animHideDown;
		private TranslateAnimation	m_animHideUp;
		private AlphaAnimation		m_animHideAlpha;

		private boolean m_bTouchSeekBar = false;

		public ManagerUi() {
			InitView();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			m_bIsSurfaceCreate = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			m_bIsSurfaceCreate = false;
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
			m_bTouchSeekBar = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_bTouchSeekBar = false;
			Seek(seekBar.getProgress());
			m_managerUi.ShowViewControl(true);
		}

		public void InitView() {
			m_viewPrepare = findViewById(R.id.prepare_panel);
			m_txtSpeedPrepare = (TextView)findViewById(R.id.tv_prepare_speed);

			m_viewLoad = findViewById(R.id.loading_panel);
			m_txtSpeedLoad = (TextView)findViewById(R.id.tv_speed);

			m_viewControl = findViewById(R.id.control_panel);
			m_viewToolUp = findViewById(R.id.up_toolview);
			m_viewToolDown = findViewById(R.id.down_toolview);

			m_txtTitle = (TextView)findViewById(R.id.tv_title);

			m_btnPlay = (ImageButton)findViewById(R.id.btn_play);
			m_btnPause = (ImageButton)findViewById(R.id.btn_pause);
			m_btnPlay.setOnClickListener(this);
			m_btnPause.setOnClickListener(this);
			m_seekbar = (SeekBar)findViewById(R.id.playback_seeker);
			m_txtTimeCurrnet = (TextView)findViewById(R.id.tv_curTime);
			m_txtTimeTotal = (TextView)findViewById(R.id.tv_totalTime);

			SetListenerSeekBar(this);

			m_viewSurface = (SurfaceView)findViewById(R.id.surfaceView);
			m_holderSurface = m_viewSurface.getHolder();
			m_holderSurface.addCallback(this);
			m_holderSurface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			m_animHideDown = new TranslateAnimation(0.0f, 0.0f, 0.0f, 200.0f);
			m_animHideDown.setDuration(1000);

			m_animHideAlpha = new AlphaAnimation(1, 0);
			m_animHideAlpha.setDuration(1000);

			m_animHideUp = new TranslateAnimation(0.0f, 0.0f, 0.0f, -124.0f);
			m_animHideUp.setDuration(1000);
		}

		public void ShowViewPrepareLoad(boolean isShow) {
			if (isShow) {
				m_viewPrepare.setVisibility(View.VISIBLE);
			}
			else {
				m_viewPrepare.setVisibility(View.GONE);
			}
		}

		public void ShowViewControl(boolean isShow) {
			if (isShow) {
				m_viewToolUp.setVisibility(View.VISIBLE);
				m_viewToolDown.setVisibility(View.VISIBLE);
				m_viewPrepare.setVisibility(View.GONE);
				DelayToHideControlPanel();
			}
			else {
				if (m_viewToolDown.isShown()) {
					m_viewToolDown.startAnimation(m_animHideDown);
					m_viewToolUp.startAnimation(m_animHideUp);

					m_viewToolUp.setVisibility(View.GONE);
					m_viewToolDown.setVisibility(View.GONE);
				}
			}
		}

		public void ShowViewControl() {
			RemoveHideMessage();
			m_viewToolUp.setVisibility(View.VISIBLE);
			m_viewToolDown.setVisibility(View.VISIBLE);
		}

		public void ShowViewLoad(boolean isShow) {
			if (isShow) {
				m_viewLoad.setVisibility(View.VISIBLE);
			}
			else {
				if (m_viewLoad.isShown()) {
					m_viewLoad.startAnimation(m_animHideAlpha);
					m_viewLoad.setVisibility(View.GONE);
				}
			}
		}

		public void ShowPlay(boolean bShow) {
			if (bShow) {
				m_btnPlay.setVisibility(View.VISIBLE);
				m_btnPause.setVisibility(View.INVISIBLE);
			}
			else {
				m_btnPlay.setVisibility(View.INVISIBLE);
				m_btnPause.setVisibility(View.VISIBLE);
			}
		}

		public void TogglePlayPause() {
			if (m_btnPlay.isShown()) {
				Play();
			}
			else {
				Pause();
			}
		}

		public void SetSeekBarProgress(int time) {
			if (!m_bTouchSeekBar) {
				m_seekbar.setProgress(time);
			}
		}

		public void SetSeekBarSecondProgress(int time) {
			m_seekbar.setSecondaryProgress(time);
		}

		public void SetSeekBarMax(int max) {
			m_seekbar.setMax(max);
		}

		public void SetTimeCurrent(int curTime) {
			String timeString = UtilDlna.FormateTime(curTime);
			m_txtTimeCurrnet.setText(timeString);
		}

		public void SetTimeTotal(int totalTime) {
			String timeString = UtilDlna.FormateTime(totalTime);
			m_txtTimeTotal.setText(timeString);
		}

		public void UpdateMediaInfoView(DlnaMediaModel modelMedia) {
			SetTimeCurrent(0);
			SetTimeTotal(0);
			SetSeekBarMax(100);
			SetSeekBarProgress(0);
			m_txtTitle.setText(modelMedia.GetTitle());
		}

		public void SetSpeed(float speed) {
			String showString = (int)speed + "KB/" + getResources().getString(R.string.second);
			m_txtSpeedPrepare.setText(showString);
			m_txtSpeedLoad.setText(showString);
		}

		public void SetListenerSeekBar(OnSeekBarChangeListener listener) {
			m_seekbar.setOnSeekBarChangeListener(listener);
		}

		public boolean IsShowingViewControl() {
			return m_viewToolDown.getVisibility() == View.VISIBLE ? true : false;
		}

		public boolean IsShowingViewLoad() {
			if (m_viewLoad.getVisibility() == View.VISIBLE
			||	m_viewPrepare.getVisibility() == View.VISIBLE
				) {
				return true;
			}

			return false;
		}

		public void ShowPlayErrorTip() {
			Toast.makeText(ActivityVideo.this, R.string.toast_videoplay_fail, Toast.LENGTH_SHORT).show();
		}
	}


	private static final LogCommon log = LogFactory.CreateLog();

	private final static int REFRESH_CURPOS = 0x0001;
	private final static int HIDE_TOOL = 0x0002;
	private final static int EXIT_ACTIVITY = 0x0003;
	private final static int REFRESH_SPEED = 0x0004;
	private final static int CHECK_DELAY = 0x0005;

	private final static int EXIT_DELAY_TIME = 1000;
	private final static int HIDE_DELAY_TIME = 1000;

	private ManagerUi			m_managerUi;
	private EnginePlayVideo		m_enginePlayVideo;
	private ListenerEnginePlayVideo			m_listenerEnginePlay;
	private MediaControlBroadcastFactory	m_factoryMediaControl;

	private Context m_context;
	private DlnaMediaModel m_modelMedia = new DlnaMediaModel();
	private Handler m_handler;

	private TimerAbstract		m_timerPlayPos;
	private TimerAbstract		m_timerNetwork;
	private TimerCheckDelay		m_timerCheckDelay;

	private boolean m_bIsSurfaceCreate = false;
	private boolean m_bIsDestroy = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.e("onCreate");
		setContentView(R.layout.video_player_layout);
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
		onDestroy();
	}

	@Override
	protected void onDestroy() {
		log.e("onDestroy");
		m_bIsDestroy = true;
		m_timerCheckDelay.StopTimer();
		m_timerNetwork.StopTimer();
		m_factoryMediaControl.Unregister();
		m_timerPlayPos.StopTimer();
		m_enginePlayVideo.Exit();
		super.onDestroy();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int actionIdx = event.getActionIndex();

		if (actionIdx == 0 && action == MotionEvent.ACTION_UP) {
			if (!m_managerUi.IsShowingViewControl()) {
				m_managerUi.ShowViewControl(true);
				return true;
			}
			else {
				DelayToHideControlPanel();
			}
		}

		return super.dispatchTouchEvent(event);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		//	log.e("onBufferingUpdate --> percen = " + percent + ", curPos = " + mp.getCurrentPosition());
		int duration = m_enginePlayVideo.GetDuration();
		int time = duration * percent / 100;
		m_managerUi.SetSeekBarSecondProgress(time);
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
		m_managerUi.ShowViewControl(true);
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
						break;
					case HIDE_TOOL:
						if (!m_enginePlayVideo.IsPause()) {
							m_managerUi.ShowViewControl(false);
						}
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
				}
			}

		};

		m_timerPlayPos.SetHandler(m_handler, REFRESH_CURPOS);

		m_timerNetwork = new TimerSingleSecond(this);
		m_timerNetwork.SetHandler(m_handler, REFRESH_SPEED);
		m_timerCheckDelay = new TimerCheckDelay(this);
		m_timerCheckDelay.SetHandler(m_handler, CHECK_DELAY);

		m_enginePlayVideo = new EnginePlayVideo(this, m_managerUi.m_holderSurface);
		m_enginePlayVideo.SetListenerBufferingUpdate(this);
		m_enginePlayVideo.SetListenerSeekComplete(this);
		m_listenerEnginePlay = new ListenerEnginePlayVideo();
		m_enginePlayVideo.SetListenerEnginePlay(m_listenerEnginePlay);

		m_factoryMediaControl = new MediaControlBroadcastFactory(m_context);
		m_factoryMediaControl.Register(this);

		m_timerNetwork.StartTimer();
		m_timerCheckDelay.StartTimer();
	}

	private void RefreshIntent(Intent intent) {
		RemoveExitMessage();
		if (intent != null) {
			m_modelMedia = DlnaMediaModelFactory.CreateFromIntent(intent);
		}

		m_managerUi.UpdateMediaInfoView(m_modelMedia);
		if (m_bIsSurfaceCreate) {
			m_enginePlayVideo.PlayMedia(m_modelMedia);
		}
		else {
			DelayToPlayMedia(m_modelMedia);
		}

		m_managerUi.ShowViewPrepareLoad(true);
		m_managerUi.ShowViewLoad(false);
		m_managerUi.ShowViewControl(false);
	}

	private void DelayToPlayMedia(final DlnaMediaModel mMediaInfo) {
		m_handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!m_bIsDestroy) {
					m_enginePlayVideo.PlayMedia(mMediaInfo);
				}
				else {
					log.e("activity destroy...so don't PlayMedia...");
				}
			}
		}, 1000);
	}

	private void RemoveHideMessage() {
		m_handler.removeMessages(HIDE_TOOL);
	}

	private void DelayToHideControlPanel() {
		RemoveHideMessage();
		m_handler.sendEmptyMessageDelayed(HIDE_TOOL, HIDE_DELAY_TIME);
	}

	private void RemoveExitMessage() {
		m_handler.removeMessages(EXIT_ACTIVITY);
	}

	private void DelayToExit() {
		RemoveExitMessage();
		m_handler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
	}

	public void Play() {
		m_enginePlayVideo.Play();
	}

	public void Pause() {
		m_enginePlayVideo.Pause();
	}

	public void Stop() {
		m_enginePlayVideo.Stop();
	}

	public void RefreshCurrentPos() {
		int pos = m_enginePlayVideo.GetCurrentPos();

		m_managerUi.SetSeekBarProgress(pos);
		DlnaEventBroadcastFactory.SendEventSeek(m_context, pos);
	}

	public void RefreshSpeed() {
		if (m_managerUi.IsShowingViewLoad()) {
			float speed = UtilCommon.GetSysNetworkDownloadSpeed();
			m_managerUi.SetSpeed(speed);
		}
	}

	public void CheckDelay() {
		int pos = m_enginePlayVideo.GetCurrentPos();

		boolean ret = m_timerCheckDelay.IsDelay(pos);
		if (ret) {
			m_managerUi.ShowViewLoad(true);
		}
		else {
			m_managerUi.ShowViewLoad(false);
		}

		m_timerCheckDelay.SetPos(pos);

	}

	public void Seek(int pos) {
		m_enginePlayVideo.SkipTo(pos);
		m_managerUi.SetSeekBarProgress(pos);
	}

}
