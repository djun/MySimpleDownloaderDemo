//(c)Copyright.2014.DJun.2014-4-9 Project Created.
package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
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
		return createDownloadThread(url, localFilePath).getId();
	}

	// start download task
	public synchronized void startDownloadTask(long id) {
		MySimpleDownloaderAsyncTask t = taskMap.get(id);
		if (t != null) {
			t.execute();
		}
	}

	// start all download task
	public synchronized void startAllDownloadTasks() {
		for (MySimpleDownloadFile dFile : fileList) {
			startDownloadTask(dFile.getId());
		}
	}

	// cancel download task
	public synchronized void cancelDownloadTask(long id) {
		MySimpleDownloaderAsyncTask t = taskMap.get(id);
		if (t != null) {
			t.cancel(true); // TODO testing
		}
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
	private MySimpleDownloadFile createDownloadThread(String url,
			String localFilePath) {
		MySimpleDownloaderAsyncTask t = new MySimpleDownloaderAsyncTask(
				idCounter++, url, localFilePath, listener, this);
		MySimpleDownloadFile f = t.getMyFile();
		fileList.add(f);
		fileMap.put(f.getId(), f);

		return f;
	}

	public MySimpleDownloaderListener getListener() {
		return listener;
	}

	public void setListener(MySimpleDownloaderListener listener) {
		this.listener = listener;
	}

	public class MySimpleDownloaderAsyncTask extends
			AsyncTask<Void, Double, Void> {

		private MySimpleDownloaderListener listener;
		private MySimpleDownloader downloader;

		private MyMultiThreadDownloader thread;

		public MySimpleDownloaderAsyncTask(long id, String url,
				String localFilePath, MySimpleDownloaderListener listener,
				MySimpleDownloader downloader) {
			this.listener = listener;
			this.downloader = downloader;

			this.thread = new MyMultiThreadDownloader(id, url, localFilePath);
		}

		public MySimpleDownloadFile getMyFile() {
			return thread.getMySimpleDownloadFile();
		}

		// Void可以不用专门写一个这个
		// public void execute() {
		// this.execute(..);
		// }

		@Override
		protected void onPreExecute() {
			if (listener != null) {
				listener.onDownloadStarted(downloader, getMyFile());
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (thread != null) {
					thread.download();

					double rate = 0;
					while (rate < 1f || !thread.isInterrupted()) {
						if (thread != null) {
							rate = thread.getCompletedRate();
						}
						publishProgress(rate);

						Thread.sleep(MyMultiThreadDownloader.SPEED_UPDATE_DELAY);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Double... values) {
			System.out.println("file:" + getMyFile().getId() + ",rate="
					+ values[0]);// debug

			if (listener != null) {
				listener.onDownloading(downloader, getMyFile());
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if (listener != null) {
				listener.onDownloadCompleted(downloader, getMyFile());
			}
		}

		@Override
		protected void onCancelled() {
			if (listener != null) {
				listener.onDownloadCanceled(downloader, getMyFile());
			}
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
