//@formatter:off
package com.zenithairplayreceive.pro.util;

import com.zenithairplayreceive.pro.helper.HelperDownload;
import com.zenithairplayreceive.pro.helper.HelperFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RunFileDownload implements Runnable {

	private static final LogCommon log = LogFactory.CreateLog();

	private final static int TIME_OUT_CONNECT = 5000;

	public String	m_sRequesetMethod = "GET";
	public String	m_sRequestUrl;
	public String	m_sUriSave;
	public int		m_iResponseCode = 0;
	public boolean	m_bIsDownloadSuccess = false;
	public HelperDownload.ListenerDownLoad m_listenerDownload;

	public RunFileDownload(String a_sRequestUrl, String a_sUriSave, HelperDownload.ListenerDownLoad a_sListenerDownload) {
		m_sRequestUrl = a_sRequestUrl;
		m_sUriSave = a_sUriSave;
		m_listenerDownload = a_sListenerDownload;
	}

	@Override
	public void run() {
		boolean isParamValid = IsParamValid();
		if (isParamValid) {
			boolean ret = false;
			int count = 0;
			while (true) {
				ret = Request();
				if (ret || count > 2) {
					break;
				}
				count++;
				log.e("Request fail,cur count = " + count);
			}
		}
		else {
			log.e("IsParamValid = false!!!");
		}

		if (m_listenerDownload != null) {
			m_listenerDownload.OnDownloadResult(m_bIsDownloadSuccess, m_sUriSave);
		}
	}

	private boolean Request() {

		InputStream inputStream = null;
		try {
			URL url = new URL(m_sRequestUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod(m_sRequesetMethod);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setConnectTimeout(TIME_OUT_CONNECT);
			m_iResponseCode = connection.getResponseCode();
			if (m_iResponseCode != 200) {
				log.e("m_iResponseCode = " + m_iResponseCode + ", so Fail!!!");
				return false;
			}

			inputStream = connection.getInputStream();
			m_bIsDownloadSuccess = HelperFile.WriteFile(m_sUriSave, inputStream);

			inputStream.close();
			return m_bIsDownloadSuccess;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			log.e("catch MalformedURLException e = " + e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
			log.e("catch IOException e = " + e.getMessage() + ", inputStream = " + inputStream);
		}

		return false;
	}

	public boolean IsParamValid() {
		if (m_sRequestUrl == null || m_sUriSave == null) {
			return false;
		}

		return true;
	}
}
