package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Message;

public class MySimpleDownloader {

	private static MySimpleDownloader publicDownloader;

	private MySimpleDownloaderHandler handler;
	private MySimpleDownloaderListener listener;

	// list for all download files
	private List<MySimpleDownloadFile> fileList;
	// map for id->file
	private Map<Long, MySimpleDownloadFile> fileMap;
	// map for download threads
	private Map<Long, MySimpleDownloadThread> threadMap;

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
		if (handler != null) {
			handler.setListener(this.listener);
		}
	}

	// thread for downloading a file
	private class MySimpleDownloadThread extends Thread {

		private MySimpleDownloadFile file;
		private MySimpleDownloaderHandler handler;

		public MySimpleDownloadThread(MySimpleDownloadFile file,
				MySimpleDownloaderHandler handler) {
			this.file = file;
			this.handler = handler;
		}

		@Override
		public void run() {
			// remove this for not running runnable object set to this thread
			// super.run();

			// TODO
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

			switch (msg.what) {
			case DOWNLOAD_STARTED: {
				if (info != null && listener != null) {
					listener.onDownloadStarted(info.downloader, info.id);
				}
				break;
			}
			case DOWNLOADING: {
				if (info != null && listener != null) {
					listener.onDownloading(info.downloader, info.id);
				}
				break;
			}
			case DOWNLOAD_COMPLETED: {
				if (info != null && listener != null) {
					listener.onDownloadCompleted(info.downloader, info.id);
				}
				break;
			}
			case DOWNLOAD_CANCELED: {
				if (info != null && listener != null) {
					listener.onDownloadCanceled(info.downloader, info.id);
				}
				break;
			}
			case UNDEFINED:
			default: {
				// do nothing
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
	interface MySimpleDownloaderListener {
		void onDownloadStarted(MySimpleDownloader downloader, long id);

		void onDownloadCompleted(MySimpleDownloader downloader, long id);

		void onDownloadCanceled(MySimpleDownloader downloader, long id);

		void onDownloading(MySimpleDownloader downloader, long id);
	}

	// for storing those download file informations
	public class MySimpleDownloadFile {

		private long id;
		private String url;
		private String localFilePath;
		private String title;
		private String description;

		public MySimpleDownloadFile(long id, String url) {
			init(id, url, null);
		}

		public MySimpleDownloadFile(long id, String url, String localFilePath) {
			init(id, url, localFilePath);
		}

		// initialize the necessary variables
		private void init(long id, String url, String localFilePath) {
			setId(id);
			setUrl(url);
			setLocalFilePath(localFilePath);
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getLocalFilePath() {
			return localFilePath;
		}

		public void setLocalFilePath(String localFilePath) {
			this.localFilePath = localFilePath;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}
}
