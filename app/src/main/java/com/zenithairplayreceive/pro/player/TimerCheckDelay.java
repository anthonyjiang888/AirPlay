//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;

public class TimerCheckDelay extends TimerAbstract {

	private int m_iPosLast = 0;

	public TimerCheckDelay(Context context) {
		super(context);
	}

	public void SetPos(int pos) {
		m_iPosLast = pos;
	}

	public boolean IsDelay(int pos) {
		if (pos == 0 || pos != m_iPosLast) {
			return false;
		}

		return true;
	}

}
