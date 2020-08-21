//@formatter:off
package com.zenithairplayreceive.pro;

import android.app.Application;

import com.zenithairplayreceive.pro.device.DeviceInfo;
import com.zenithairplayreceive.pro.device.DeviceUpdateBroadcastFactory;
import com.zenithairplayreceive.pro.util.LogCommon;
import com.zenithairplayreceive.pro.util.LogFactory;


public class ApplicationAirPlay extends Application {

	private static final LogCommon log = LogFactory.CreateLog();

	private static ApplicationAirPlay m_instance;

	private DeviceInfo m_infoDevice;

	public synchronized static ApplicationAirPlay Instance() {
		return m_instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		log.e("ApplicationAirPlay onCreate");

		m_instance = this;
		m_infoDevice = new DeviceInfo();
	}

	public void UpdateDevInfo(String a_sName, String a_sUuid) {
		m_infoDevice.sDevName = a_sName;
		m_infoDevice.sUuid = a_sUuid;
	}

	public DeviceInfo GetDevInfo() {
		return m_infoDevice;
	}

	public void SetDevStatus(boolean a_bFlag) {
		m_infoDevice.bStatus = a_bFlag;
		DeviceUpdateBroadcastFactory.SendDevUpdateBroadcast(this);
	}

}
