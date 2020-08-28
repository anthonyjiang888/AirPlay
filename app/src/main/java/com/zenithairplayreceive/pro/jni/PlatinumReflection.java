//@formatter:off
package com.zenithairplayreceive.pro.jni;

public class PlatinumReflection {

	public static interface ActionReflectionListener {
		public void OnActionInvoke(int cmd, String value, String data, String title);
		public void OnActionInvoke(int cmd, String value, byte data[], String title);
		public void OnAudioInit(int bits, int channels, int samplerate, int isaudio);
		public void OnAudioProcess(byte data[], double timestamp, int seqnum);
		public void OnAudioDestroy();
	}

	//----------------------------------------------------------------
	private static final int MEDIA_RENDER_CTL_MSG_BASE			= 0x100;
	public static final int MEDIA_RENDER_CTL_MSG_SET_AV_URL		= (MEDIA_RENDER_CTL_MSG_BASE + 0);
	public static final int MEDIA_RENDER_CTL_MSG_STOP			= (MEDIA_RENDER_CTL_MSG_BASE + 1);
	public static final int MEDIA_RENDER_CTL_MSG_PLAY			= (MEDIA_RENDER_CTL_MSG_BASE + 2);
	public static final int MEDIA_RENDER_CTL_MSG_PAUSE			= (MEDIA_RENDER_CTL_MSG_BASE + 3);
	public static final int MEDIA_RENDER_CTL_MSG_SEEK			= (MEDIA_RENDER_CTL_MSG_BASE + 4);
	public static final int MEDIA_RENDER_CTL_MSG_SETVOLUME		= (MEDIA_RENDER_CTL_MSG_BASE + 5);
	public static final int MEDIA_RENDER_CTL_MSG_SETMUTE		= (MEDIA_RENDER_CTL_MSG_BASE + 6);
	public static final int MEDIA_RENDER_CTL_MSG_SETPLAYMODE	= (MEDIA_RENDER_CTL_MSG_BASE + 7);
	public static final int MEDIA_RENDER_CTL_MSG_PRE			= (MEDIA_RENDER_CTL_MSG_BASE + 8);
	public static final int MEDIA_RENDER_CTL_MSG_NEXT			= (MEDIA_RENDER_CTL_MSG_BASE + 9);
	public static final int MEDIA_RENDER_CTL_MSG_SETMETADATA	= (MEDIA_RENDER_CTL_MSG_BASE + 10);
	public static final int MEDIA_RENDER_CTL_MSG_SETIPADDR		= (MEDIA_RENDER_CTL_MSG_BASE + 11);

	//----------------------------------------------------------------
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION		= (MEDIA_RENDER_CTL_MSG_BASE + 0);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION		= (MEDIA_RENDER_CTL_MSG_BASE + 1);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE	= (MEDIA_RENDER_CTL_MSG_BASE + 2);

	//----------------------------------------------------------------
	public static final String RENDERER_TO_CONTROL_POINTER_INTENT	= "com.zenithairplayreceive.pro.platinum.tocontrolpointer.cmd.intent";
	public static final String GET_RENDERER_TO_CONTROL_POINTER		= "get_dlna_renderer_tocontrolpointer.cmd";
	public static final String GET_PARAM_MEDIA_DURATION				= "get_param_media_duration";
	public static final String GET_PARAM_MEDIA_POSITION				= "get_param_media_position";
	public static final String GET_PARAM_MEDIA_PLAY_STATE			= "get_param_media_playingstate";

	//----------------------------------------------------------------
	public static final String MEDIA_PLAYINGSTATE_STOP		= "STOPPED";
	public static final String MEDIA_PLAYINGSTATE_PAUSE		= "PAUSED_PLAYBACK";
	public static final String MEDIA_PLAYINGSTATE_PLAYING	= "PLAYING";
	public static final String MEDIA_PLAYINGSTATE_TRANSTION	= "TRANSITIONING";
	public static final String MEDIA_PLAYINGSTATE_NOMEDIA	= "NO_MEDIA_PRESENT";

	//----------------------------------------------------------------
	public static final String MEDIA_SEEK_TIME_TYPE_REL_TIME	= "REL_TIME";
	public static final String MEDIA_SEEK_TIME_TYPE_TRACK_NR	= "TRACK_NR";


	private static ActionReflectionListener m_listenerReflection;

	// Below functions are called from CPP
	public static void ActionReflection(int cmd, String value, String data, String title) {
		if (m_listenerReflection != null) {
			m_listenerReflection.OnActionInvoke(cmd, value, data, title);
		}
	}

	public static void ActionReflection(int cmd, String value, byte data[], String title) {
		if (m_listenerReflection != null) {
			m_listenerReflection.OnActionInvoke(cmd, value, data, title);
		}
	}

	public static void AudioInit(int bits, int channels, int samplerate, int isaudio) {
		if (m_listenerReflection != null) {
			m_listenerReflection.OnAudioInit(bits, channels, samplerate, isaudio);
		}
	}

	public static void AudioProcess(byte data[], double timestamp, int seqnum) {
		if (m_listenerReflection != null) {
			m_listenerReflection.OnAudioProcess(data, timestamp, seqnum);
		}
	}

	public static void AudioDestroy() {
		if (m_listenerReflection != null) {
			m_listenerReflection.OnAudioDestroy();
		}
	}

	public static void SetActionInvokeListener(ActionReflectionListener listener) {
		m_listenerReflection = listener;
	}
}
