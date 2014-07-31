package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

import android.os.Handler;
import android.os.Message;

//this handler class used to receive messages sent from download threads
public class MySimpleDownloaderHandler extends Handler {

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
