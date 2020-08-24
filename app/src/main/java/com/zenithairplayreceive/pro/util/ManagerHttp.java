//@formatter:off
package com.zenithairplayreceive.pro.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ManagerHttp {
	private static final DefaultHttpClient m_client;

	static {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(					params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(			params, "UTF-8");
		HttpConnectionParams.setStaleCheckingEnabled(	params, false);
		HttpConnectionParams.setConnectionTimeout(		params, 15 * 1000);
		HttpConnectionParams.setSoTimeout(				params, 15 * 1000);
		HttpConnectionParams.setSocketBufferSize(		params, 50 * 1024);
		HttpClientParams.setRedirecting(				params, false);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
			new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)
		);
		schemeRegistry.register(
			new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
		);
		ClientConnectionManager manager = new ThreadSafeClientConnManager(
			params, schemeRegistry
		);
		m_client = new DefaultHttpClient(manager, params);
	}

	public static HttpResponse Execute(HttpHead head) throws IOException {
		return m_client.execute(head);
	}

	public static HttpResponse Execute(HttpHost host, HttpGet get)
		throws IOException {
		return m_client.execute(host, get);
	}

	public static HttpResponse Execute(HttpGet get) throws IOException {
		return m_client.execute(get);
	}

	public static InputStream ExecuteGet(String url, Long begin, StringBuffer fileSize) throws Exception {
		HttpUriRequest request = null;
		request = new HttpGet(url);
		request.setHeader("X-Target-Encoding", "UTF-8");
		if (begin != null) {
			request.setHeader("Range", "bytes=" + begin.intValue() + "-");
		}
		HttpClient client = new DefaultHttpClient();
		try {
			HttpParams hcp = client.getParams();
			if (null != hcp) {
				final int TIMEOUT_MS = 10 * 1000;
				ConnManagerParams.setTimeout(hcp, TIMEOUT_MS);
				HttpConnectionParams.setSoTimeout(hcp, TIMEOUT_MS);
				HttpConnectionParams.setConnectionTimeout(hcp, TIMEOUT_MS);
				ConnRouteParams.setDefaultProxy(hcp, null);
			}
			HttpEntity resEntity = null;
			InputStream is = null;
			for (int i = 0; i < 3; i++) {
				try {
					HttpResponse rsp = client.execute(request);
					resEntity = rsp.getEntity();

					is = resEntity.getContent();
					if (fileSize != null) {
						fileSize = fileSize.append(resEntity.getContentLength());
					}
					if (is != null) {
						return is;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					if (i + 1 == 3) {
						return null;
					}
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private static String ConvertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String sLine = null;
		try {
			while ((sLine = reader.readLine()) != null) {
				builder.append(sLine + "\n");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return builder.toString();
	}
}
