//(c)Copyright.2014.DJun.2014-4-9 Project Created.
package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class MySimpleDownloader {

	private static MySimpleDownloader publicDownloader = null;

	private MySimpleDownloaderHandler handler = new MySimpleDownloaderHandler(
			null);
	private MySimpleDownloaderListener listener = null;

	// list for all download files
	private List<MySimpleDownloadFile> fileList;
	// map for id->file
	private Map<Long, MySimpleDownloadFile> fileMap;
	// map for download threads
	private Map<Long, MySimpleDownloadThread> threadMap;

	// a counter for generating download file id
	private int idCounter = 0;

	public MySimpleDownloader() {
		// initialize
		if (fileList == null || fileMap == null || threadMap == null) {
			fileList = new ArrayList<MySimpleDownloadFile>();
			fileMap = new ConcurrentHashMap<Long, MySimpleDownloadFile>();
			threadMap = new ConcurrentHashMap<Long, MySimpleDownloader.MySimpleDownloadThread>();
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
		MySimpleDownloadThread t = threadMap.get(id);
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
		MySimpleDownloadThread t = threadMap.get(id);
		if (t != null && !t.isInterrupted()) {
			t.interrupt();
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

	public MySimpleDownloaderHandler getHandler() {
		return handler;
	}

	public MySimpleDownloaderListener getListener() {
		return listener;
	}

	public void setListener(MySimpleDownloaderListener listener) {
		this.listener = listener;
		handler.setListener(this.listener);
	}

	// thread for downloading a file
	private class MySimpleDownloadThread extends Thread {

		private MySimpleDownloadFile dFile;
		private MySimpleDownloader downloader;
		private boolean overWrite = false;

		private MySimpleDownloaderHandler handler;

		private static final int READ_TIMEOUT = 9999;
		private static final int CACHE_SIZE = 4 * 1024;
		private static final long UI_UPDATE_DELAY = 500L;

		private long lastUIUpdateTime = 0;
		private int currentDownloadedLength = 0;

		public MySimpleDownloadThread(MySimpleDownloadFile file,
				MySimpleDownloader downloader, boolean overWrite) {
			this.dFile = file;
			this.downloader = downloader;
			this.overWrite = overWrite;

			this.handler = this.downloader.getHandler();
		}

		@Override
		public void run() {
			// remove this for not running runnable object set to this thread
			// super.run();

			// send download started message to handler
			if (this.handler != null) {
				this.handler.sendMessage(
						MySimpleDownloaderHandler.DOWNLOAD_STARTED,
						downloader,
						dFile == null ? MySimpleDownloadFile.INVALID_ID : dFile
								.getId());
			}

			boolean canceled = false;
			try {
				// check if necessary infomation not found in the
				// MySimpleDownloadFile
				if (dFile == null || dFile.getUrl() == null) {
					canceled = true;
					System.out.println("download file info null!");// debug
				} else {
					HttpURLConnection conn = null;
					InputStream input = null;
					OutputStream output = null;

					try {
						if (dFile.getLocalFilePath() == null) {
							// when localFilePath is null, set it to cache
							// folder
							String url = dFile.getUrl();
							String fileName = url.substring(url
									.lastIndexOf("/") + 1);
							dFile.setLocalFilePath(Environment
									.getDownloadCacheDirectory()
									.getAbsolutePath()
									+ "/" + fileName);
						}
						File file = new File(dFile.getLocalFilePath());
						// create parent directories
						file.getParentFile().mkdirs();
						// check if over write this file
						boolean overwritten = false;
						if (file.exists()) {
							System.out.println("file exists: "
									+ file.getAbsolutePath());// debug
							if (overWrite) {
								file.delete();
								overwritten = true;
							} else {
								canceled = true;
							}
						} else {
							overwritten = true;
						}

						if (overwritten) {
							file.createNewFile();

							URL url = new URL(dFile.getUrl());
							conn = (HttpURLConnection) url.openConnection();
							conn.setReadTimeout(READ_TIMEOUT);
							conn.connect();
							input = conn.getInputStream();

							// get remote file length
							dFile.setFileLength(conn.getContentLength());
							dFile.setDownloadedLength(0);

							boolean interrupted = false;

							output = new FileOutputStream(file);
							byte[] buffer = new byte[CACHE_SIZE]; // cache
							lastUIUpdateTime = System.currentTimeMillis();
							currentDownloadedLength = 0;
							while (input.read(buffer) != -1
									&& !(interrupted = isInterrupted())) {
								// write data to output
								output.write(buffer);

								// update download speed
								currentDownloadedLength += Math.min(
										dFile.getFileLength()
												- dFile.getDownloadedLength(),
										CACHE_SIZE);

								// update downloaded length info
								int d = dFile.getDownloadedLength();
								d = Math.min(d + buffer.length,
										dFile.getFileLength());
								dFile.setDownloadedLength(d);

								// send downloading message to handler (with
								// delay)
								long currentTime = System.currentTimeMillis();
								if (currentTime - lastUIUpdateTime >= UI_UPDATE_DELAY) {
									// calculate the average download speed
									double avgSpeed = currentDownloadedLength
											* 1.0f
											/ (currentTime - lastUIUpdateTime);
									dFile.setLastAvgDownloadSpeed(avgSpeed);

									// send message
									if (this.handler != null) {
										this.handler
												.sendMessage(
														MySimpleDownloaderHandler.DOWNLOADING,
														downloader,
														dFile == null ? MySimpleDownloadFile.INVALID_ID
																: dFile.getId());
									}

									lastUIUpdateTime = currentTime;
								}
							}
							output.flush();

							canceled = interrupted;
						}
					} catch (MalformedURLException e) {
						canceled = true;

						e.printStackTrace();
						System.out.println("malformed url error!: "
								+ dFile.getUrl());// debug
					} catch (IOException e) {
						canceled = true;

						e.printStackTrace();
						System.out.println("io error!: "
								+ dFile.getLocalFilePath());// debug
					} finally {
						// close all streams and connections
						try {
							if (output != null) {
								output.close();
							}
							if (input != null) {
								input.close();
							}
							if (conn != null) {
								conn.disconnect();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} finally {
				// send download completed or canceled message to handler
				if (this.handler != null) {
					int messageType = canceled ? MySimpleDownloaderHandler.DOWNLOAD_CANCELED
							: MySimpleDownloaderHandler.DOWNLOAD_COMPLETED;
					this.handler.sendMessage(messageType, downloader,
							dFile == null ? MySimpleDownloadFile.INVALID_ID
									: dFile.getId());
				}
			}
		}
	}

	// this handler class used to receive messages sent from download threads
	private class MySimpleDownloaderHandler extends Handler {

		private MySimpleDownloaderListener listener;

		public static final int UNDEFINED = 0;
		public static final int DOWNLOAD_STARTED = 1;
		public static final int DOWNLOADING = 2;
		public static final int DOWNLOAD_COMPLETED = 3;
		public static final int DOWNLOAD_CANCELED = 4;

		public class MyMsgInfo {
			MySimpleDownloader downloader;
			long id;
		}

		public MySimpleDownloaderHandler(MySimpleDownloaderListener l) {
			this.listener = l;
		}

		@SuppressWarnings("unused")
		public MySimpleDownloaderListener getListener() {
			return listener;
		}

		public void setListener(MySimpleDownloaderListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleMessage(Message msg) {
			MyMsgInfo info = null;
			if (msg.obj != null && msg.obj instanceof MyMsgInfo) {
				info = (MyMsgInfo) msg.obj;
			}

			if (info != null && listener != null) {
				switch (msg.what) {
				case DOWNLOAD_STARTED: {
					listener.onDownloadStarted(info.downloader, info.id);
					break;
				}
				case DOWNLOADING: {
					listener.onDownloading(info.downloader, info.id);
					break;
				}
				case DOWNLOAD_COMPLETED: {
					listener.onDownloadCompleted(info.downloader, info.id);
					break;
				}
				case DOWNLOAD_CANCELED: {
					listener.onDownloadCanceled(info.downloader, info.id);
					break;
				}
				case UNDEFINED:
				default: {
					// do nothing
				}
				}
			}
		}

		public void sendMessage(int messageType, MySimpleDownloader downloader,
				long id) {
			MyMsgInfo info = new MyMsgInfo();
			info.downloader = downloader;
			info.id = id;

			Message msg = Message.obtain();
			msg.what = messageType;
			msg.obj = info;

			this.sendMessage(msg);
		}
	}

	// listener for the downloader
	public interface MySimpleDownloaderListener {
		void onDownloadStarted(MySimpleDownloader downloader, long id);

		void onDownloadCompleted(MySimpleDownloader downloader, long id);

		void onDownloadCanceled(MySimpleDownloader downloader, long id);

		void onDownloading(MySimpleDownloader downloader, long id);
	}

}
