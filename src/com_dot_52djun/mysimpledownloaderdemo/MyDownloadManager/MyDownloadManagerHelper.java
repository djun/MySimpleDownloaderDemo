//(c)Copyright.2014.DJun.2014-4-9 Project Created.
package com_dot_52djun.mysimpledownloaderdemo.MyDownloadManager;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader.MySimpleDownloader.MySimpleDownloadFile;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class MyDownloadManagerHelper {

	private static MyDownloadManagerHelper publicHelper;

	private Context mContext;
	private DownloadManager dManager;

	private MyDownloadHelperHanlder handler = new MyDownloadHelperHanlder(null);
	private MyDownloadHelperListener listener = null;

	// map for id->request
	private Map<Long, DownloadManager.Request> requestMap;

	private MyDownloadChangeObserver mdcObserver;
	private MyDownloadCompletedReceiver mdcReceiver;

	public static final long INVALID_DOWNLOAD_ID = -1L;

	public static MyDownloadManagerHelper getPublicHelper(Context context) {
		if (publicHelper == null) {
			if (context == null) {
				return null;
			}

			publicHelper = new MyDownloadManagerHelper(context);
		}
		return publicHelper;
	}

	public DownloadManager getPublicManager() {
		return dManager;
	}

	public MyDownloadHelperListener getListener() {
		return listener;
	}

	public void setListener(MyDownloadHelperListener listener) {
		this.listener = listener;
		handler.setListener(this.listener);
	}

	protected MyDownloadManagerHelper(Context context) {
		this.mContext = context;
		if (mContext != null) {
			this.dManager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}

		if (requestMap == null) {
			requestMap = new ConcurrentHashMap<Long, DownloadManager.Request>();
		}

		// register download content observer
		mdcObserver = new MyDownloadChangeObserver(mContext, this, handler);
		mdcObserver.register();

		// register download completed receiver
		mdcReceiver = new MyDownloadCompletedReceiver();
		mContext.registerReceiver(mdcReceiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	public DownloadManager.Request getRequest(String url, String local) {
		if (url == null || local == null) {
			return null;
		}

		ensureDestinationFolderIsExisted(local);

		DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
		r.setDestinationUri(Uri.parse(local));
		return r;
	}

	public DownloadManager.Request getRequest(Uri url, Uri local) {
		if (url == null || local == null) {
			return null;
		}

		ensureDestinationFolderIsExisted(URI.create(local.toString()));

		DownloadManager.Request r = new DownloadManager.Request(url);
		r.setDestinationUri(local);
		return r;
	}

	public void ensureDestinationFolderIsExisted(String local) {
		if (local == null) {
			return;
		}

		File lf = new File(local);
		File pf = lf.getParentFile();
		pf.mkdirs();
	}

	public void ensureDestinationFolderIsExisted(URI local) {
		if (local == null) {
			return;
		}

		try {
			File lf = new File(local);
			File pf = lf.getParentFile();
			pf.mkdirs();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public long enqueue(DownloadManager.Request request) {
		if (request == null) {
			return INVALID_DOWNLOAD_ID;
		}

		long downloadId = INVALID_DOWNLOAD_ID;
		if (dManager != null) {
			downloadId = dManager.enqueue(request);
		}

		if (downloadId != INVALID_DOWNLOAD_ID) {
			requestMap.put(downloadId, request);
		}

		return downloadId;
	}

	public int[] getBytesAndStatus(long downloadId) {
		int[] bytesAndStatus = new int[] { -1, -1, 0 };
		DownloadManager.Query query = new DownloadManager.Query()
				.setFilterById(downloadId);
		Cursor c = null;
		try {
			c = dManager.query(query);
			int i1 = c
					.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
			int i2 = c
					.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
			int i3 = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
			if (c != null && c.moveToFirst()) {
				bytesAndStatus[0] = c.getInt(i1);
				bytesAndStatus[1] = c.getInt(i2);
				bytesAndStatus[2] = c.getInt(i3);
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return bytesAndStatus;
	}

	private class MyDownloadCompletedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			long completeDownloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, INVALID_DOWNLOAD_ID);
			if (requestMap.containsKey(completeDownloadId)) {
				// TODO send message
			}
		}
	};

	private class MyDownloadChangeObserver extends ContentObserver {

		private Context mContext;
		private MyDownloadManagerHelper helper;
		private MyDownloadHelperHanlder handler;
		private long lastUIUpdateTime = 0;

		private static final long UI_UPDATE_DELAY = 500L;
		private static final String CONTENT_URI = "content://downloads/my_downloads";

		public MyDownloadChangeObserver(Context context,
				MyDownloadManagerHelper helper, MyDownloadHelperHanlder handler) {
			super(handler);
			this.handler = handler;

			lastUIUpdateTime = System.currentTimeMillis();
		}

		public void register() {
			mContext.getContentResolver().registerContentObserver(
					Uri.parse(CONTENT_URI), true, this);
		}

		public void unregister() {
			mContext.getContentResolver().unregisterContentObserver(this);
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUIUpdateTime >= UI_UPDATE_DELAY) {
				// send message
				if (handler != null) {
					handler.sendMessage(
							MyDownloadHelperHanlder.DOWNLOAD_INFO_UPDATED,
							helper);
				}

				lastUIUpdateTime = currentTime;
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private class MyDownloadHelperHanlder extends Handler {

		private MyDownloadHelperListener listener;

		public static final int UNDEFINED = 0;
		public static final int DOWNLOAD_INFO_UPDATED = 1;
		public static final int DOWNLOAD_COMPLETED = 2;

		public class MyDownloadMsgInfo {
			MyDownloadManagerHelper helper;
			int byteDownloaded;
			int totalSize;
			int status;
		}

		public MyDownloadHelperHanlder(MyDownloadHelperListener l) {
			this.listener = l;
		}

		@SuppressWarnings("unused")
		public MyDownloadHelperListener getListener() {
			return listener;
		}

		public void setListener(MyDownloadHelperListener listener) {
			this.listener = listener;
		}

		@Override
		public void handleMessage(Message msg) {
			MyDownloadMsgInfo info = null;
			if (msg.obj != null && msg.obj instanceof MyDownloadMsgInfo) {
				info = (MyDownloadMsgInfo) msg.obj;
			}

			if (info != null && listener != null) {
				// TODO
				switch (msg.what) {
				case DOWNLOAD_INFO_UPDATED: {
					break;
				}
				case DOWNLOAD_COMPLETED: {
					break;
				}
				case UNDEFINED:
				default: {
					// do nothing
				}
				}
			}
		}

		public void sendMessage(int messageType, MyDownloadManagerHelper helper) {
			// TODO
			MyDownloadMsgInfo info = new MyDownloadMsgInfo();
			info.helper = helper;

			Message msg = Message.obtain();
			msg.what = messageType;
			msg.obj = info;

			this.sendMessage(msg);
		}

	}

	public interface MyDownloadHelperListener {

		void onDownloading(MyDownloadManagerHelper helper); // TODO

		void onDownloadCompleted(MyDownloadManagerHelper helper); // TODO

	}

	@Override
	protected void finalize() throws Throwable {
		// when this object destroy, unregister the observer and receiver
		if (mdcObserver != null && mContext != null) {
			mdcObserver.unregister();
		}
		if (mdcReceiver != null && mContext != null) {
			mContext.unregisterReceiver(mdcReceiver);
		}

		super.finalize();
	}

}
