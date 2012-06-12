package com.qburst.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;

public class QLog implements Thread.UncaughtExceptionHandler {

	static Application app = null;
	private static Thread.UncaughtExceptionHandler defaultUEH;
	private static QLog mQLogInstatnce;

	private static String QLOG_BASE_URL = "http://example.com/summarylog/";
	private static String QLOG_DETAILED_BASE_URL = "http://example.com/detailedreport/";
	private static String LOG_MEGGASE_TAG = "log_msg";
	private static String MODEL_TAG = "model";
	private static String BRAND_TAG = "brand";
	private static String VERSION_TAG = "version";
	private static String TIMESTAMP_TAG = "timestamp";
	private static String APP_ID_TAG = "app_id";
	private static String SEVERITY_TAG = "severity";
	private static String AVAILABLE_MEMORY_TAG = "avail_mem";
	private static String TOTAL_MEMORY_TAG = "total_mem";
	private static String NETWORK_STATUS_TAG = "status";

	private static String SEVERITY_DEBUG = "debug";
	private static String SEVERITY_WARNING = "warning";
	private static String SEVERITY_ERROR = "error";
	private static String SEVERITY_INFORMATION = "information";
	private static String SEVERITY_CRITICAL = "critical";

	private QLog() {
		super();
	}

	public static synchronized QLog getInstance() {

		if (mQLogInstatnce == null) {

			synchronized (QLog.class) {

				if (mQLogInstatnce == null) {
					mQLogInstatnce = new QLog();
				}

			}

		}
		return mQLogInstatnce;
	}

	/*
	 * For implementation, initialize QLog by calling QLog.setupLogging(this) in
	 * Application class.
	 */

	public static void setupLogging(Application application) {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		app = application;
		Thread.setDefaultUncaughtExceptionHandler(QLog.getInstance());
	}

	public static void v(java.lang.String tag, java.lang.String msg) {
		android.util.Log.v(tag, msg);
	}

	public static void v(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.v(tag, msg, tr);
	}

	public static void d(java.lang.String tag, java.lang.String msg) {
		android.util.Log.d(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, false);
	}

	public static void d(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.d(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, false);
	}

	public static void d(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.d(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, isDetailedReport);
	}

	public static void w(java.lang.String tag, java.lang.String msg) {
		android.util.Log.w(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_WARNING, false);
	}

	public static void w(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.w(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_WARNING, false);
	}

	public static void w(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.w(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_WARNING, isDetailedReport);
	}

	public static void e(java.lang.String tag, java.lang.String msg) {
		android.util.Log.e(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_ERROR, false);
	}

	public static void e(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.e(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_ERROR, false);
	}

	public static void e(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.e(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_ERROR, isDetailedReport);
	}

	public static void i(java.lang.String tag, java.lang.String msg) {
		android.util.Log.i(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, false);
	}

	public static void i(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.i(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, false);
	}

	public static void i(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.i(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, isDetailedReport);
	}

	public void uncaughtException(Thread t, Throwable e) {
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString() + "\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i = 0; i < arr.length; i++) {
			report += "    " + arr[i].toString() + "\n";
		}
		report += "-------------------------------\n\n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "--------- Cause ---------\n\n";
		Throwable cause = e.getCause();
		if (cause != null) {
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i = 0; i < arr.length; i++) {
				report += "    " + arr[i].toString() + "\n";
			}
		}
		report += "-------------------------------\n\n";

		postLog(report, SEVERITY_CRITICAL, true);

		defaultUEH.uncaughtException(t, e);

	}

	private static void postLog(String report, String severity,
			boolean isDetailedReport) {
		String responseString = "";
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost();

			if (isDetailedReport)
				request.setURI(new URI(QLOG_DETAILED_BASE_URL));
			else
				request.setURI(new URI(QLOG_BASE_URL));

			request.setHeader("Content-Type", "text/xml; charset=utf-8");
			StringEntity se = new StringEntity(getPostParamsForReport(report,
					severity, isDetailedReport));
			// request.setParams(getPostParamsForReport(report, severity,
			// isDetailedReport));
			request.setEntity(se);
			HttpResponse response = client.execute(request);
			InputStream inputStream = response.getEntity().getContent();
			responseString = collectDataString(new InputStreamReader(
					inputStream, "UTF-8"));

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	protected static String getPostParamsForReport(String report,
			String severity, boolean isDetailedReport) {

		// phone model
		String PhoneModel = android.os.Build.MODEL;
		// Android version
		String AndroidVersion = android.os.Build.VERSION.RELEASE;
		// Brand
		String Brand = android.os.Build.BRAND;

		long timestamp = System.currentTimeMillis() / 1000L;

		String appId = app.getPackageName();

		String postBody = new String();

		postBody += "&" + LOG_MEGGASE_TAG + "=" + report;

		postBody += "&" + MODEL_TAG + "=" + PhoneModel;

		postBody += "&" + BRAND_TAG + "=" + Brand;

		postBody += "&" + VERSION_TAG + "=" + AndroidVersion;

		postBody += "&" + TIMESTAMP_TAG + "=" + String.valueOf(timestamp);

		postBody += "&" + APP_ID_TAG + "=" + appId;

		postBody += "&" + SEVERITY_TAG + "=" + severity;

		if (isDetailedReport) {
			postBody += "&" + AVAILABLE_MEMORY_TAG + "="
					+ String.valueOf(getAvailableInternalMemorySize());
			postBody += "&" + TOTAL_MEMORY_TAG + "="
					+ String.valueOf(getTotalInternalMemorySize());

			NetworkInfo info = (NetworkInfo) ((ConnectivityManager) app
					.getApplicationContext().getSystemService(
							Context.CONNECTIVITY_SERVICE))
					.getActiveNetworkInfo();

			postBody += "&" + NETWORK_STATUS_TAG + "=" + info.getTypeName();

		}
		return postBody;

	}

	protected static String collectDataString(InputStreamReader isr) {
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String aLine = null;
		do {
			try {
				aLine = br.readLine();
				if (aLine != null)
					sb.append(aLine.trim());
			} catch (IOException e) {
			}
		} while (aLine != null);
		return sb.toString();
	}

	// Memory Info
	protected static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	protected static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

}
