//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.zenithairplayreceive.pro.center.DlnaMediaModel;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;


public abstract class EnginePlayAbstract implements
		IEnginePlayBase,
		OnCompletionListener,
		OnPreparedListener,
		OnErrorListener
	{

	private static final LogCommon log = LogFactory.CreateLog();

	protected Context			m_context;
	protected MediaPlayer		m_playerMedia;
	protected DlnaMediaModel	m_modelDlnaMedia;
	protected int				m_iStatePlay;

	protected ListenerEnginePlay m_listenerEnginePlay;

	protected abstract boolean PrepareSelf();
	protected abstract boolean PrepareComplete(MediaPlayer mp);

	public EnginePlayAbstract(Context context) {
		m_context = context;
		InitDefault();
	}

	protected void InitDefault() {
		m_playerMedia = new MediaPlayer();
		m_playerMedia.setOnCompletionListener(this);
		m_playerMedia.setOnPreparedListener(this);
		m_modelDlnaMedia = null;
		m_iStatePlay = StatePlay.MPS_NOFILE;
	}

	public void SetListenerEnginePlay(ListenerEnginePlay listener) {
		m_listenerEnginePlay = listener;
	}

	@Override
	public void Play() {
		switch (m_iStatePlay) {
			case StatePlay.MPS_PAUSE:
				m_playerMedia.start();
				m_iStatePlay = StatePlay.MPS_PLAYING;
				PerformListenerPlay(m_iStatePlay);
				break;
			case StatePlay.MPS_STOP:
				PrepareSelf();
				break;
			default:
				break;
		}
	}

	@Override
	public void Pause() {
		switch (m_iStatePlay) {
			case StatePlay.MPS_PLAYING:
				m_playerMedia.pause();
				m_iStatePlay = StatePlay.MPS_PAUSE;
				PerformListenerPlay(m_iStatePlay);
				break;
			default:
				break;
		}
	}

	@Override
	public void Stop() {
		if (m_iStatePlay != StatePlay.MPS_NOFILE) {
			m_playerMedia.reset();
			m_iStatePlay = StatePlay.MPS_STOP;
			PerformListenerPlay(m_iStatePlay);
		}
	}

	@Override
	public void SkipTo(int time) {
		switch (m_iStatePlay) {
			case StatePlay.MPS_PLAYING:
			case StatePlay.MPS_PAUSE:
				int time2 = ReviseSeekValue(time);
				m_playerMedia.seekTo(time2);
				break;
			default:
				break;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		PrepareComplete(mp);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		log.e("onCompletion...");
		if (m_listenerEnginePlay != null) {
			m_listenerEnginePlay.OnTrackPlayComplete(m_modelDlnaMedia);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		log.e("onError --> what = " + what);
		return false;
	}

	public void Exit() {
		Stop();
		m_playerMedia.release();
		m_modelDlnaMedia = null;
		m_iStatePlay = StatePlay.MPS_NOFILE;
	}

	public boolean IsPlaying() {
		return m_iStatePlay == StatePlay.MPS_PLAYING;
	}

	public boolean IsPause() {
		return m_iStatePlay == StatePlay.MPS_PAUSE;
	}

	public void PlayMedia(DlnaMediaModel mediaInfo) {
		if (mediaInfo != null) {
			m_modelDlnaMedia = mediaInfo;
			PrepareSelf();
		}
	}

	public int GetCurrentPos() {
		if (m_iStatePlay == StatePlay.MPS_PLAYING || m_iStatePlay == StatePlay.MPS_PAUSE) {
			return m_playerMedia.getCurrentPosition();
		}

		return 0;
	}

	public int GetDuration() {
		switch (m_iStatePlay) {
			case StatePlay.MPS_PLAYING:
			case StatePlay.MPS_PAUSE:
			case StatePlay.MPS_PARECOMPLETE:
				return m_playerMedia.getDuration();
		}

		return 0;
	}

	public int GetStatePlay() {
		return m_iStatePlay;
	}

	protected void PerformListenerPlay(int a_iStatePlay) {
		if (m_listenerEnginePlay != null) {
			switch (a_iStatePlay) {
				case StatePlay.MPS_INVALID:
					m_listenerEnginePlay.OnTrackStreamError(m_modelDlnaMedia);
					break;
				case StatePlay.MPS_STOP:
					m_listenerEnginePlay.OnTrackStop(m_modelDlnaMedia);
					break;
				case StatePlay.MPS_PLAYING:
					m_listenerEnginePlay.OnTrackPlay(m_modelDlnaMedia);
					break;
				case StatePlay.MPS_PAUSE:
					m_listenerEnginePlay.OnTrackPause(m_modelDlnaMedia);
					break;
				case StatePlay.MPS_PARESYNC:
					m_listenerEnginePlay.OnTrackPrepareSync(m_modelDlnaMedia);
					break;
			}
		}
	}

	private int ReviseSeekValue(int a_iValue) {
		if (a_iValue < 0) {
			a_iValue = 0;
		}

		if (a_iValue > m_playerMedia.getDuration()) {
			a_iValue = m_playerMedia.getDuration();
		}

		return a_iValue;
	}

}
