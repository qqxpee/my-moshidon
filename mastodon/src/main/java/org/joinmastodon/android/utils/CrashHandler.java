package org.joinmastodon.android.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.joinmastodon.android.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	private static CrashHandler instance;
	private Thread.UncaughtExceptionHandler defaultHandler;
	private Context context;

	private CrashHandler() {}

	public static CrashHandler getInstance() {
		if (instance == null) {
			instance = new CrashHandler();
		}
		return instance;
	}

	public void init(Context context) {
		this.context = context.getApplicationContext();
		this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		Log.i(TAG, "UncaughtExceptionHandler initialized");
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			saveCrashInfoToFile(ex);
		} catch (Exception e) {
			Log.e(TAG, "Error saving crash info to file", e);
		}

		if (defaultHandler != null) {
			defaultHandler.uncaughtException(thread, ex);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	private void saveCrashInfoToFile(Throwable ex) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		// 1. Header with metadata
		sb.append("================ CRASH REPORT ================\n");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		sb.append("Time: ").append(sdf.format(new Date())).append("\n");
		sb.append("Package Name: ").append(context.getPackageName()).append("\n");
		sb.append("App Version Name: ").append(BuildConfig.VERSION_NAME).append("\n");
		sb.append("App Version Code: ").append(BuildConfig.VERSION_CODE).append("\n");
		sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n");
		sb.append("Device Model: ").append(Build.MODEL).append("\n");
		sb.append("Device Product: ").append(Build.PRODUCT).append("\n");
		sb.append("OS Version (Release): ").append(Build.VERSION.RELEASE).append("\n");
		sb.append("OS Version (SDK): ").append(Build.VERSION.SDK_INT).append("\n");
		sb.append("==============================================\n\n");

		// 2. Exception stack trace
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);

		// 3. Write to file in external files dir
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir != null) {
			File crashDir = new File(externalDir, "crashes");
			if (!crashDir.exists()) {
				crashDir.mkdirs();
			}
			SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
			String fileName = "crash_" + fileSdf.format(new Date()) + ".txt";
			File crashFile = new File(crashDir, fileName);
			
			FileOutputStream fos = new FileOutputStream(crashFile);
			fos.write(sb.toString().getBytes("UTF-8"));
			fos.close();
			
			Log.e(TAG, "Crash log successfully saved to: " + crashFile.getAbsolutePath());
		}
	}
}
