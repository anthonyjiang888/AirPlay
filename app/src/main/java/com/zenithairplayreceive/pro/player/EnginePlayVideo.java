//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.UtilCommon;
import com.zenithairplayreceive.pro.util.LogFactory;

import java.util.HashMap;
import java.util.Map;


public class EnginePlayVideo extends EnginePlayAbstract {

	private final LogCommon log = LogFactory.CreateLog();

	private SurfaceHolder				m_holder = null;
	private OnBufferingUpdateListener	m_listenerBufferingUpdate;
	private OnSeekCompleteListener		m_listenerSeekComplete;
	private OnErrorListener				m_listenerError;

	public EnginePlayVideo(Context context, SurfaceHolder holder) {
		super(context);
		SetHolder(holder);
	}

	public void SetHolder(SurfaceHolder holder) {
		m_holder = holder;
	}

	public void SetListenerBufferingUpdate(OnBufferingUpdateListener listener) {
		m_listenerBufferingUpdate = listener;
	}

	public void SetListenerSeekComplete(OnSeekCompleteListener listener) {
		m_listenerSeekComplete = listener;
	}

	public void SetListenerError(OnErrorListener listener) {
		m_listenerError = listener;
	}

	@Override
	protected boolean PrepareSelf() {

		m_playerMedia.reset();
		try {
			Uri uri = Uri.parse(m_modelDlnaMedia.GetUrl());
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Range", "bytes=0-");
			m_playerMedia.setDataSource(m_context, uri, headers);
			m_playerMedia.setAudioStreamType(AudioManager.STREAM_MUSIC);
			if (m_holder != null) {
				m_playerMedia.setDisplay(m_holder);
			}
			if (m_listenerBufferingUpdate != null) {
				m_playerMedia.setOnBufferingUpdateListener(m_listenerBufferingUpdate);
			}
			if (m_listenerSeekComplete != null) {
				m_playerMedia.setOnSeekCompleteListener(m_listenerSeekComplete);
			}
			if (m_listenerError != null) {
				m_playerMedia.setOnErrorListener(m_listenerError);
			}
			m_playerMedia.prepareAsync();
			log.e("m_playerMedia.prepareAsync path = " + m_modelDlnaMedia.GetUrl());
			m_iStatePlay = StatePlay.MPS_PARESYNC;
			PerformListenerPlay(m_iStatePlay);
		}
		catch (Exception e) {
			e.printStackTrace();
			m_iStatePlay = StatePlay.MPS_INVALID;
			PerformListenerPlay(m_iStatePlay);
			return false;
		}

		return true;
	}

	@Override
	protected boolean PrepareComplete(MediaPlayer mp) {
		m_iStatePlay = StatePlay.MPS_PARECOMPLETE;
		if (m_listenerEnginePlay != null) {
			m_listenerEnginePlay.OnTrackPrepareComplete(m_modelDlnaMedia);
		}

		if (m_holder != null) {
			UtilCommon.ViewSize viewSize = UtilCommon.GetFitSize(m_context, mp);
			m_holder.setFixedSize(viewSize.width, viewSize.height);
		}

		m_playerMedia.start();

		m_iStatePlay = StatePlay.MPS_PLAYING;
		PerformListenerPlay(m_iStatePlay);

		return true;
	}

}
