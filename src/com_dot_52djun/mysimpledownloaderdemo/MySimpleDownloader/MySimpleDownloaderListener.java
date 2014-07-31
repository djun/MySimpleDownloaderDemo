package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

//listener for the downloader
public interface MySimpleDownloaderListener {
	void onDownloadStarted(MySimpleDownloader downloader, long id);

	void onDownloadCompleted(MySimpleDownloader downloader, long id);

	void onDownloadCanceled(MySimpleDownloader downloader, long id);

	void onDownloading(MySimpleDownloader downloader, long id);
}