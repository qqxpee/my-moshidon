package org.joinmastodon.android.utils;

import android.util.Base64;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MailSender {
	private static final String TAG = "MailSender";

	public interface Callback {
		void onSuccess();
		void onError(Exception e);
	}

	public static void sendEmailAsync(String host, int port, String username, String password, boolean useSSL, String from, String to, String subject, String body, Callback callback) {
		new Thread(() -> {
			try {
				sendEmail(host, port, username, password, useSSL, from, to, subject, body);
				Log.d(TAG, "Email sent successfully to: " + to);
				if (callback != null) {
					callback.onSuccess();
				}
			} catch (Exception e) {
				Log.e(TAG, "Error sending email", e);
				if (callback != null) {
					callback.onError(e);
				}
			}
		}).start();
	}

	public static void sendEmail(String host, int port, String username, String password, boolean useSSL, String from, String to, String subject, String body) throws Exception {
		Socket socket;
		if (useSSL) {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			socket = factory.createSocket(host, port);
		} else {
			socket = new Socket(host, port);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		OutputStream outputStream = socket.getOutputStream();

		readResponse(reader); // Read greeting

		sendCmd(outputStream, "EHLO " + host);
		readResponse(reader);

		if (username != null && !username.isEmpty()) {
			sendCmd(outputStream, "AUTH LOGIN");
			readResponse(reader);

			sendCmd(outputStream, Base64.encodeToString(username.getBytes("UTF-8"), Base64.NO_WRAP));
			readResponse(reader);

			sendCmd(outputStream, Base64.encodeToString(password.getBytes("UTF-8"), Base64.NO_WRAP));
			readResponse(reader);
		}

		sendCmd(outputStream, "MAIL FROM:<" + from + ">");
		readResponse(reader);

		sendCmd(outputStream, "RCPT TO:<" + to + ">");
		readResponse(reader);

		sendCmd(outputStream, "DATA");
		readResponse(reader);

		// Send headers and body
		String emailContent = "From: " + from + "\r\n" +
				"To: " + to + "\r\n" +
				"Subject: " + subject + "\r\n" +
				"Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
				body + "\r\n.\r\n";
		outputStream.write(emailContent.getBytes("UTF-8"));
		outputStream.flush();
		readResponse(reader);

		sendCmd(outputStream, "QUIT");
		readResponse(reader);

		socket.close();
	}

	private static void sendCmd(OutputStream os, String cmd) throws Exception {
		os.write((cmd + "\r\n").getBytes("UTF-8"));
		os.flush();
	}

	private static void readResponse(BufferedReader reader) throws Exception {
		String line = reader.readLine();
		if (line == null) {
			throw new Exception("SMTP server closed connection");
		}
		Log.d(TAG, "SMTP response: " + line);
		if (line.startsWith("4") || line.startsWith("5")) {
			throw new Exception("SMTP error: " + line);
		}
		// Multi-line responses are of the format: 250-something
		while (line.length() >= 4 && line.charAt(3) == '-') {
			line = reader.readLine();
			Log.d(TAG, "SMTP response: " + line);
		}
	}
}
