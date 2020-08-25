//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public abstract class TimerAbstract {

	private class MyTimeTask extends TimerTask {
		@Override
		public void run() {
			if (m_handler != null) {
				Message msg = m_handler.obtainMessage(m_iIdMst);
				msg.sendToTarget();
			}
		}
	}


	private final static int TIMER_INTERVAL = 1000;

	protected Context		m_context;
	protected Timer			m_timer;
	protected MyTimeTask	m_task;
	protected Handler		m_handler;
	protected int m_iInterval = TIMER_INTERVAL;
	protected int m_iIdMst;

	public TimerAbstract(Context context) {
		m_context = context;
		m_timer = new Timer();
	}

	public void SetHandler(Handler a_handler, int a_iIdMsg) {
		m_handler = a_handler;
		m_iIdMst = a_iIdMsg;
	}

	public void SetInterval(int a_iInterval) {
		m_iInterval = a_iInterval;
	}

	public void StartTimer() {
		if (m_task == null) {
			m_task = new MyTimeTask();
			m_timer.schedule(m_task, 0, m_iInterval);
		}
	}

	public void StopTimer() {
		if (m_task != null) {
			m_task.cancel();
			m_task = null;
		}
	}
}
