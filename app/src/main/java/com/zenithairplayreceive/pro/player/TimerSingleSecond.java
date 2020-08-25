//@formatter:off
package com.zenithairplayreceive.pro.player;

import android.content.Context;

public class TimerSingleSecond extends TimerAbstract {

	public TimerSingleSecond(Context context) {
		super(context);
		SetInterval(1000);
	}
}
