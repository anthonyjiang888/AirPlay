//@formatter:off
package com.zenithairplayreceive.pro.util;

import android.util.Log;

public class LogCommon {
	private String tag = "LogCommon";
	public static int logLevel = Log.VERBOSE;
	public static boolean isDebug = true;

	public LogCommon() {
	}

	public LogCommon(String tag) {
		this.tag = tag;
	}

	public void SetTag(String tag) {
		this.tag = tag;
	}

	private String GetFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}

			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}

			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}

			return "[" + Thread.currentThread().getId() + ": " + st.getFileName() + ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	public void Info(Object str) {
		if (logLevel <= Log.INFO) {
			String name = GetFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.i(tag, ls);
		}
	}

	public void i(Object str) {
		if (isDebug) {
			Info(str);
		}
	}

	public void Verbose(Object str) {
		if (logLevel <= Log.VERBOSE) {
			String name = GetFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.v(tag, ls);
		}
	}

	public void v(Object str) {
		if (isDebug) {
			Verbose(str);
		}
	}

	public void Warn(Object str) {
		if (logLevel <= Log.WARN) {
			String name = GetFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.w(tag, ls);
		}
	}

	public void w(Object str) {
		if (isDebug) {
			Warn(str);
		}
	}

	public void Error(Object str) {
		if (logLevel <= Log.ERROR) {
			String name = GetFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.e(tag, ls);
		}
	}

	public void Error(Exception ex) {
		if (logLevel <= Log.ERROR) {
			StringBuffer sb = new StringBuffer();
			String name = GetFunctionName();
			StackTraceElement[] sts = ex.getStackTrace();

			if (name != null) {
				sb.append(name + " - " + ex + "\r\n");
			}
			else {
				sb.append(ex + "\r\n");
			}

			if (sts != null && sts.length > 0) {
				for (StackTraceElement st : sts) {
					if (st != null) {
						sb.append("[ " + st.getFileName() + ":" + st.getLineNumber() + " ]\r\n");
					}
				}
			}

			Log.e(tag, sb.toString());
		}
	}

	public void e(Object str) {
		if (isDebug) {
			Error(str);
		}
	}

	public void e(Exception ex) {
		if (isDebug) {
			Error(ex);
		}
	}

	public void Debug(Object str) {
		if (logLevel <= Log.DEBUG) {
			String name = GetFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.d(tag, ls);
		}
	}

	public void d(Object str) {
		if (isDebug) {
			Debug(str);
		}
	}
}