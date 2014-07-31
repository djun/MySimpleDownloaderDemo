package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyMultiThreadDownloader {

	private MySimpleDownloadFile myFile;
	private int threadNum;
	private MultiDownloadThread[] threads;
	private int fileSize;

	private boolean downloadStarted = false;

	// constructor without title & description
	public MyMultiThreadDownloader(long id, String url, String targetFile,
			int threadNum) {
		init(id, url, targetFile, threadNum);
	}

	// constructor with title & description
	public MyMultiThreadDownloader(long id, String url, String targetFile,
			String title, String description, int threadNum) {
		init(id, url, targetFile, threadNum);
		myFile.setTitle(title);
		myFile.setDescription(description);
	}

	private void init(long id, String url, String targetFile, int threadNum) {
		myFile = new MySimpleDownloadFile(id, url, targetFile);
		this.threadNum = threadNum;

		threads = new MultiDownloadThread[threadNum];
	}

	public MySimpleDownloadFile getMySimpleDownloadFile() {
		return this.myFile;
	}

	// start the download process!
	public synchronized void download() throws Exception {
		if (!downloadStarted) {
			downloadStarted = true;

			// setup HttpURLConnection for get file size
			URL url = new URL(myFile.getUrl());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			// setup local file for multi thread downloader
			fileSize = conn.getContentLength();
			conn.disconnect();
			int currentPartSize = fileSize / threadNum + 1;
			RandomAccessFile file = new RandomAccessFile(
					myFile.getLocalFilePath(), "rw");
			file.setLength(fileSize);
			file.close();

			// make all the threads for download this file
			for (int i = 0; i < threadNum; i++) {
				int startPos = i * currentPartSize;
				RandomAccessFile currentPart = new RandomAccessFile(
						myFile.getLocalFilePath(), "rw");
				currentPart.seek(startPos);
				threads[i] = new MultiDownloadThread(startPos, currentPartSize,
						currentPart);
				threads[i].start();
			}
		}
	}

	// for getting known that the completed rate of this downloading file
	public synchronized double getCompletedRate() {
		int sumSize = 0;
		for (int i = 0; i < threadNum; i++) {
			sumSize += threads[i].length;
		}

		return sumSize * 1.0 / fileSize;
	}

	private class MultiDownloadThread extends Thread {
		private int startPos;
		private int currentPartSize;
		private RandomAccessFile currentPart;
		public int length;

		public MultiDownloadThread(int startPos, int currentPartSize,
				RandomAccessFile currentPart) {
			this.startPos = startPos;
			this.currentPartSize = currentPartSize;
			this.currentPart = currentPart;
		}

		@Override
		public void run() {
			try {
				// setup HttpURLConnection for downloading file in this thread
				URL url = new URL(myFile.getUrl());
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty(
						"Accept",
						"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				InputStream inStream = conn.getInputStream();
				// skip to the start position and start downloading
				// TODO skip() has bug in Android 4.2?
				inStream.skip(this.startPos);
				byte[] buffer = new byte[1024];
				int hasRead = 0;
				while (length < currentPartSize
						&& (hasRead = inStream.read(buffer)) != -1) {
					currentPart.write(buffer, 0, hasRead);
					length += hasRead;
				}
				currentPart.close();
				inStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
