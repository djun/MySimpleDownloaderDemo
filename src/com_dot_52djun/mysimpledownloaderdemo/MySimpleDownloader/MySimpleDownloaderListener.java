package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

//listener for the downloader
public interface MySimpleDownloaderListener {
	void onDownloadStarted(MySimpleDownloader downloader,
			MySimpleDownloadFile file);

	void onDownloadCompleted(MySimpleDownloader downloader,
			MySimpleDownloadFile file);

	void onDownloadCanceled(MySimpleDownloader downloader,
			MySimpleDownloadFile file);

	void onDownloading(MySimpleDownloader downloader, MySimpleDownloadFile file);
}