//(c)Copyright.2014.DJun.2014-4-9 Project Created.
package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

@SuppressLint("HandlerLeak")
public class MySimpleDownloader {

	private MySimpleDownloaderListener listener = null;

	// list for all download files
	private List<MySimpleDownloadFile> fileList;
	// map for id->file
	private Map<Long, MySimpleDownloadFile> fileMap;
	// map for download asynctasks
	private Map<Long, MySimpleDownloaderAsyncTask> taskMap;

	// a counter for generating download file id
	private int idCounter = 0;

	public MySimpleDownloader() {
		// initialize
		if (fileList == null || fileMap == null || taskMap == null) {
			fileList = new ArrayList<MySimpleDownloadFile>();
			fileMap = new ConcurrentHashMap<Long, MySimpleDownloadFile>();
			taskMap = new ConcurrentHashMap<Long, MySimpleDownloaderAsyncTask>();
		}
	}

	// add download task
	public synchronized long addDownloadTask(String url) {
		return addDownloadTask(url, null);
	}

	// add download task
	public synchronized long addDownloadTask(String url, String localFilePath) {
		return createDownloadFile(url, localFilePath).getId();
	}

	// start download task
	public synchronized void startDownloadTask(long id) {
		startDownloadTask(id, false);
	}

	// start download task
	public synchronized void startDownloadTask(long id, boolean overWrite) {
		MySimpleDownloadThread t = taskMap.get(id);
		if (t != null && !t.isInterrupted()) {
			return;
		}

		MySimpleDownloadFile dFile = fileMap.get(id);
		if (dFile != null) {
			MySimpleDownloadThread thread = new MySimpleDownloadThread(dFile,
					this, overWrite);
			threadMap.put(id, thread);

			thread.start();
		}
	}

	// start all download task
	public synchronized void startAllDownloadTasks() {
		startAllDownloadTasks(false);
	}

	// start all download task
	public synchronized void startAllDownloadTasks(boolean overWrite) {
		for (MySimpleDownloadFile dFile : fileList) {
			startDownloadTask(dFile.getId(), overWrite);
		}
	}

	// cancel download task
	public synchronized void cancelDownloadTask(long id) {
		MySimpleDownloaderAsyncTask t = taskMap.get(id);
		t.cancel(true); // TODO testing
	}

	// cancel all download task
	public synchronized void cancelAllDownloadTasks() {
		for (MySimpleDownloadFile dFile : fileList) {
			cancelDownloadTask(dFile.getId());
		}
	}

	// set download file info in one method
	public synchronized void setDownloadFileInfo(long id, String title,
			String description) {
		MySimpleDownloadFile dFile = fileMap.get(id);
		if (dFile != null) {
			dFile.setTitle(title);
			dFile.setDescription(description);
		}
	}

	// get the download file for other purpose
	public synchronized MySimpleDownloadFile getDownloadFile(long id) {
		return fileMap.get(id);
	}

	// private method for create download file and put it into the map
	private MySimpleDownloadFile createDownloadFile(String url,
			String localFilePath) {
		MySimpleDownloadFile dFile = new MySimpleDownloadFile(idCounter++, url,
				localFilePath);
		fileList.add(dFile);
		fileMap.put(dFile.getId(), dFile);

		return dFile;
	}

	// return a public downloader used for global
	public static MySimpleDownloader getPublicDownloader() {
		if (publicDownloader == null) {
			publicDownloader = new MySimpleDownloader();
		}
		return publicDownloader;
	}

	public MySimpleDownloaderListener getListener() {
		return listener;
	}

	public void setListener(MySimpleDownloaderListener listener) {
		this.listener = listener;
	}

	public class MySimpleDownloaderAsyncTask extends
			AsyncTask<Object, Double, MySimpleDownloadFile> {

		private Object[] params;
		private MySimpleDownloaderListener listener;
		private MySimpleDownloader downloader;

		public MySimpleDownloaderAsyncTask(long id, String url,
				String localFilePath, MySimpleDownloaderListener listener,
				MySimpleDownloader downloader) {
			this.params = new Object[] { id, url, localFilePath };
			this.listener = listener;
			this.downloader = downloader;
		}

		public void execute() {
			this.execute(params);
		}

		@Override
		protected void onPreExecute() {
			if (listener != null) {
				listener.onDownloadStarted(downloader, id);
			}
		}

		@Override
		protected MySimpleDownloadFile doInBackground(Object... params) {
			return null;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(MySimpleDownloadFile result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

	}

	// thread for downloading a file
	// private class MySimpleDownloadThread extends Thread {
	//
	// private MySimpleDownloadFile dFile;
	// private MySimpleDownloader downloader;
	// private boolean overWrite = false;
	//
	// private MySimpleDownloaderHandler handler;
	//
	// private static final int READ_TIMEOUT = 9999;
	// private static final int CACHE_SIZE = 4 * 1024;
	// private static final long UI_UPDATE_DELAY = 500L;
	//
	// private long lastUIUpdateTime = 0;
	// private int currentDownloadedLength = 0;
	//
	// public MySimpleDownloadThread(MySimpleDownloadFile file,
	// MySimpleDownloader downloader, boolean overWrite) {
	// this.dFile = file;
	// this.downloader = downloader;
	// this.overWrite = overWrite;
	//
	// this.handler = this.downloader.getHandler();
	// }
	//
	// @Override
	// public void run() {
	// // remove this for not running runnable object set to this thread
	// // super.run();
	//
	// // send download started message to handler
	// if (this.handler != null) {
	// this.handler.sendMessage(
	// MySimpleDownloaderHandler.DOWNLOAD_STARTED,
	// downloader,
	// dFile == null ? MySimpleDownloadFile.INVALID_ID : dFile
	// .getId());
	// }
	//
	// boolean canceled = false;
	// try {
	// // check if necessary infomation not found in the
	// // MySimpleDownloadFile
	// if (dFile == null || dFile.getUrl() == null) {
	// canceled = true;
	// System.out.println("download file info null!");// debug
	// } else {
	// HttpURLConnection conn = null;
	// InputStream input = null;
	// OutputStream output = null;
	//
	// try {
	// if (dFile.getLocalFilePath() == null) {
	// // when localFilePath is null, set it to cache
	// // folder
	// String url = dFile.getUrl();
	// String fileName = url.substring(url
	// .lastIndexOf("/") + 1);
	// dFile.setLocalFilePath(Environment
	// .getDownloadCacheDirectory()
	// .getAbsolutePath()
	// + "/" + fileName);
	// }
	// File file = new File(dFile.getLocalFilePath());
	// // create parent directories
	// file.getParentFile().mkdirs();
	// // check if over write this file
	// boolean overwritten = false;
	// if (file.exists()) {
	// System.out.println("file exists: "
	// + file.getAbsolutePath());// debug
	// if (overWrite) {
	// file.delete();
	// overwritten = true;
	// } else {
	// canceled = true;
	// }
	// } else {
	// overwritten = true;
	// }
	//
	// if (overwritten) {
	// file.createNewFile();
	//
	// URL url = new URL(dFile.getUrl());
	// conn = (HttpURLConnection) url.openConnection();
	// conn.setReadTimeout(READ_TIMEOUT);
	// conn.connect();
	// input = conn.getInputStream();
	//
	// // get remote file length
	// dFile.setFileLength(conn.getContentLength());
	// dFile.setDownloadedLength(0);
	//
	// boolean interrupted = false;
	//
	// output = new FileOutputStream(file);
	// byte[] buffer = new byte[CACHE_SIZE]; // cache
	// lastUIUpdateTime = System.currentTimeMillis();
	// currentDownloadedLength = 0;
	// while (input.read(buffer) != -1
	// && !(interrupted = isInterrupted())) {
	// // write data to output
	// output.write(buffer);
	//
	// // update download speed
	// currentDownloadedLength += Math.min(
	// dFile.getFileLength()
	// - dFile.getDownloadedLength(),
	// CACHE_SIZE);
	//
	// // update downloaded length info
	// int d = dFile.getDownloadedLength();
	// d = Math.min(d + buffer.length,
	// dFile.getFileLength());
	// dFile.setDownloadedLength(d);
	//
	// // send downloading message to handler (with
	// // delay)
	// long currentTime = System.currentTimeMillis();
	// if (currentTime - lastUIUpdateTime >= UI_UPDATE_DELAY) {
	// // calculate the average download speed
	// double avgSpeed = currentDownloadedLength
	// * 1.0f
	// / (currentTime - lastUIUpdateTime);
	// dFile.setLastAvgDownloadSpeed(avgSpeed);
	//
	// // send message
	// if (this.handler != null) {
	// this.handler
	// .sendMessage(
	// MySimpleDownloaderHandler.DOWNLOADING,
	// downloader,
	// dFile == null ? MySimpleDownloadFile.INVALID_ID
	// : dFile.getId());
	// }
	//
	// lastUIUpdateTime = currentTime;
	// }
	// }
	// output.flush();
	//
	// canceled = interrupted;
	// }
	// } catch (MalformedURLException e) {
	// canceled = true;
	//
	// e.printStackTrace();
	// System.out.println("malformed url error!: "
	// + dFile.getUrl());// debug
	// } catch (IOException e) {
	// canceled = true;
	//
	// e.printStackTrace();
	// System.out.println("io error!: "
	// + dFile.getLocalFilePath());// debug
	// } finally {
	// // close all streams and connections
	// try {
	// if (output != null) {
	// output.close();
	// }
	// if (input != null) {
	// input.close();
	// }
	// if (conn != null) {
	// conn.disconnect();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// } finally {
	// // send download completed or canceled message to handler
	// if (this.handler != null) {
	// int messageType = canceled ? MySimpleDownloaderHandler.DOWNLOAD_CANCELED
	// : MySimpleDownloaderHandler.DOWNLOAD_COMPLETED;
	// this.handler.sendMessage(messageType, downloader,
	// dFile == null ? MySimpleDownloadFile.INVALID_ID
	// : dFile.getId());
	// }
	// }
	// }
	// }

}
