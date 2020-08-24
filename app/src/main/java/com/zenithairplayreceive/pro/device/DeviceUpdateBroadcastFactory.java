//@formatter:off
package com.zenithairplayreceive.pro.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class DeviceUpdateBroadcastFactory {

	public static interface ListenerDeviceUpdate {
		public void OnUpdateDevice();
	}


	public static final String PARAM_DEV_UPDATE = "com.zenithairplayreceive.pro.PARAM_DEV_UPDATE";

	private Context m_context;
	private DeviceUpdateBroadcastReceiver m_receiver;

	public DeviceUpdateBroadcastFactory(Context context) {
		m_context = context;
	}

	public void Register(ListenerDeviceUpdate listener) {
		if (m_receiver == null) {
			m_receiver = new DeviceUpdateBroadcastReceiver();
			m_receiver.SetListener(listener);
			m_context.registerReceiver(m_receiver, new IntentFilter(PARAM_DEV_UPDATE));
		}
	}

	public void Unregister() {
		if (m_receiver != null) {
			m_context.unregisterReceiver(m_receiver);
			m_receiver = null;
		}
	}

	public static void SendDevUpdateBroadcast(Context context) {
		Intent intent = new Intent();
		intent.setAction(PARAM_DEV_UPDATE);
		context.sendBroadcast(intent);
	}
}
