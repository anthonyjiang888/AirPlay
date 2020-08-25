//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;

import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;


public class EnginePlayMusic extends EnginePlayAbstract {

	private final LogCommon log = LogFactory.CreateLog();

	private OnBufferingUpdateListener	m_listenerBufferingUpdate;
	private OnSeekCompleteListener		m_listenerSeekComplete;
	private OnDataCaptureListener		m_listenerDataCapture;

	private Visualizer m_visualizer;

	public EnginePlayMusic(Context context) {
		super(context);
	}

	public void SetListenerBufferingUpdate(OnBufferingUpdateListener listener) {
		m_listenerBufferingUpdate = listener;
	}

	public void SetListenerSeekComplete(OnSeekCompleteListener listener) {
		m_listenerSeekComplete = listener;
	}

	public void SetListenerDataCapture(OnDataCaptureListener listener) {
		m_listenerDataCapture = listener;
	}

	public boolean VisualizerReinit(int iSessionId) {
		VisualizerRelease();

		final int iMaxRate = Visualizer.getMaxCaptureRate();
		m_visualizer = new Visualizer(iSessionId);
		m_visualizer.setCaptureSize(256);
		if (m_listenerDataCapture != null) {
			m_visualizer.setDataCaptureListener(m_listenerDataCapture, iMaxRate / 2, false, true);
		}

		return true;
	}

	public void VisualizerRelease() {
		if (m_visualizer != null) {
			m_visualizer.setEnabled(false);
			m_visualizer.release();
			m_visualizer = null;
		}
	}

	public void VisualizerEnable(boolean flag) {
		if (m_visualizer != null) {
			m_visualizer.setEnabled(flag);
		}
	}

	@Override
	public void Play() {
		super.Play();
		VisualizerEnable(true);
	}

	@Override
	public void Pause() {
		super.Pause();
		VisualizerEnable(false);
	}

	@Override
	public void Stop() {
		super.Stop();
		VisualizerEnable(false);
	}

	@Override
	public void Exit() {
		super.Exit();
		VisualizerRelease();
	}

	@Override
	protected boolean PrepareSelf() {

		m_playerMedia.reset();
		try {
			m_playerMedia.setDataSource(m_modelDlnaMedia.GetUrl());
			m_playerMedia.setAudioStreamType(AudioManager.STREAM_MUSIC);

			if (m_listenerBufferingUpdate != null) {
				m_playerMedia.setOnBufferingUpdateListener(m_listenerBufferingUpdate);
			}
			m_playerMedia.prepareAsync();
			log.e("m_playerMedia.prepareAsync path = " + m_modelDlnaMedia.GetUrl());

			m_iStatePlay = StatePlay.MPS_PARESYNC;
			PerformListenerPlay(m_iStatePlay);
			VisualizerEnable(true);
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
		log.e("PrepareComplete");
		m_iStatePlay = StatePlay.MPS_PARECOMPLETE;
		if (m_listenerEnginePlay != null) {
			m_listenerEnginePlay.OnTrackPrepareComplete(m_modelDlnaMedia);
		}

		m_playerMedia.start();

		m_iStatePlay = StatePlay.MPS_PLAYING;
		PerformListenerPlay(m_iStatePlay);
		VisualizerReinit(m_playerMedia.getAudioSessionId());
		VisualizerEnable(true);
		return true;
	}

}
