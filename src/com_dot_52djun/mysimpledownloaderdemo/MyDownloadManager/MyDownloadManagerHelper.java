//(c)Copyright.2014.DJun.2014-4-9 Project Created.
package com_dot_52djun.mysimpledownloaderdemo.MyDownloadManager;

import java.io.File;
import java.net.URI;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

public class MyDownloadManagerHelper {

	private static MyDownloadManagerHelper publicHelper;

	private Context mContext;
	private DownloadManager dManager;

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

	protected MyDownloadManagerHelper(Context context) {
		this.mContext = context;
		if (mContext != null) {
			this.dManager = (DownloadManager) mContext
					.getSystemService(Context.DOWNLOAD_SERVICE);
		}
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

		return downloadId;
	}
}
