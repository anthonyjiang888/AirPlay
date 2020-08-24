//@formatter:off
package com.zenithairplayreceive.pro.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceUpdateBroadcastReceiver extends BroadcastReceiver {

	private DeviceUpdateBroadcastFactory.ListenerDeviceUpdate m_listener;

	public void SetListener(DeviceUpdateBroadcastFactory.ListenerDeviceUpdate listener) {
		m_listener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null) {
			return;
		}

		if (DeviceUpdateBroadcastFactory.PARAM_DEV_UPDATE.equalsIgnoreCase(action)) {
			if (m_listener != null) {
				m_listener.OnUpdateDevice();
			}
		}
	}
}
