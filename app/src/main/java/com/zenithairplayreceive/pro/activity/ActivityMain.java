//@formatter:off
package com.zenithairplayreceive.pro.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zenithairplayreceive.pro.ApplicationAirPlay;
import com.zenithairplayreceive.pro.PreferencesLocal;
import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.center.MediaRenderProxy;
import com.zenithairplayreceive.pro.device.DeviceInfo;
import com.zenithairplayreceive.pro.device.DeviceUpdateBroadcastFactory;
import com.zenithairplayreceive.pro.util.UtilDlna;
import com.zenithairplayreceive.pro.view.DialogProgress;

public class ActivityMain extends Activity implements
		View.OnClickListener,
		DeviceUpdateBroadcastFactory.ListenerDeviceUpdate
	{
	private DialogProgress		m_dlgProgress;

	private Button		m_btnStart;
	private Button		m_btnRestart;
	private Button		m_btnStop;
	private Button		m_btnHelp;
	private Button		m_btnContact;
	private TextView	m_txtStatus;
	private TextView	m_txtDevName;

	private ApplicationAirPlay	m_application;
	private MediaRenderProxy	m_renderProxy;
	private DeviceUpdateBroadcastFactory m_factoryBroadcast;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		// View
		m_dlgProgress	= new DialogProgress(this);
		m_dlgProgress.setCanceledOnTouchOutside(false);
		m_dlgProgress.setCancelable(false);

		m_btnStart		= findViewById(R.id.startServiceBtn);
		m_btnRestart	= findViewById(R.id.restartServiceBtn);
		m_btnStop		= findViewById(R.id.stopServiceBtn);
		m_btnHelp		= findViewById(R.id.helpBtn);
		m_btnContact	= findViewById(R.id.contactBtn);
		m_txtStatus		= findViewById(R.id.serviceStatusView);
		m_txtDevName	= findViewById(R.id.devNameView);

		m_btnStart.setEnabled(true);
		m_btnStart.setFocusable(true);
		m_btnRestart.setEnabled(false);
		m_btnRestart.setFocusable(false);
		m_btnStop.setEnabled(false);
		m_btnStop.setFocusable(false);

		m_btnStart.setOnClickListener(this);
		m_btnRestart.setOnClickListener(this);
		m_btnStop.setOnClickListener(this);
		m_btnHelp.setOnClickListener(this);
		m_txtDevName.setOnClickListener(this);
		m_btnContact.setOnClickListener(this);

		String sDevName = null;
		try {
			sDevName = UtilDlna.GetDevName(this);
			if (sDevName == null || sDevName.isEmpty())
				sDevName = Build.MODEL;
		}
		catch (Exception e) {
			sDevName = Build.MODEL;
		}
		m_txtDevName.setText(sDevName);

		// Init
		m_application = ApplicationAirPlay.Instance();
		m_renderProxy = new MediaRenderProxy(getApplicationContext());
		m_factoryBroadcast = new DeviceUpdateBroadcastFactory(this);
		m_factoryBroadcast.Register(this);
	}

	@Override
	protected void onDestroy() {
		m_factoryBroadcast.Unregister();
		m_dlgProgress.dismiss();
		m_dlgProgress = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.startServiceBtn:
				if (m_dlgProgress != null)
					m_dlgProgress.show();
				m_btnStart.setEnabled(false);
				m_btnStart.setFocusable(false);
				m_btnRestart.setEnabled(false);
				m_btnRestart.setFocusable(false);
				m_renderProxy.EngineStart();
				break;
			case R.id.restartServiceBtn:
				if (m_dlgProgress != null)
					m_dlgProgress.show();
				m_btnStart.setEnabled(false);
				m_btnStart.setFocusable(false);
				m_btnRestart.setEnabled(false);
				m_btnRestart.setFocusable(false);
				m_renderProxy.EngineRestart();
				break;
			case R.id.stopServiceBtn:
				if (m_dlgProgress != null)
					m_dlgProgress.show();
				m_btnStop.setEnabled(false);
				m_btnStop.setFocusable(false);
				m_renderProxy.EngineStop();
				break;
			case R.id.helpBtn:
				Intent intent = new Intent(ActivityMain.this, ActivityHelp.class);
				startActivity(intent);
				break;
			case R.id.contactBtn:
				ShowDialogContact();
				break;
			case R.id.devNameView:
				ShowDialogDevName();
				break;
		}
	}

	@Override
	public void OnUpdateDevice() {
		DeviceInfo	devInfo = m_application.GetDevInfo();
		m_txtStatus.setText(devInfo.bStatus ? "Running" : "Off");
		m_txtDevName.setText(devInfo.sDevName);

		if (devInfo.bStatus) {
			m_btnStart.setEnabled(false);
			m_btnStart.setFocusable(false);
			m_btnRestart.setEnabled(true);
			m_btnRestart.setFocusable(true);
			m_btnStop.setEnabled(true);
			m_btnStop.setFocusable(true);
		}
		else {
			m_btnStart.setEnabled(true);
			m_btnStart.setFocusable(true);
			m_btnRestart.setEnabled(false);
			m_btnRestart.setFocusable(false);
			m_btnStop.setEnabled(false);
			m_btnStop.setFocusable(false);
		}
		if (m_dlgProgress != null)
			m_dlgProgress.hide();
	}

	public void ShowDialogContact() {
		Dialog dialog = new Dialog(ActivityMain.this, R.style.URLDialog);
		dialog.setCancelable(true);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.setContentView(R.layout.dialog_info);
		dialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		dialog.getWindow().setAttributes(lp);
	}

	public void ShowDialogDevName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Input Device Name");

		View view = LayoutInflater.from(ActivityMain.this).inflate(R.layout.dialog_device_name, null);
		final EditText edtName = view.findViewById(R.id.ID_EDT_DEVICE_NAME);
		edtName.setText(m_txtDevName.getText());
		edtName.selectAll();
		builder.setView(view);
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String sDevName = edtName.getText().toString();
				if (sDevName == null || sDevName.isEmpty())
					return;
				PreferencesLocal.CommitDevName(getApplicationContext(), sDevName);
				m_txtDevName.setText(sDevName);
			}
		});
		builder.setNegativeButton("CANCEL", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
