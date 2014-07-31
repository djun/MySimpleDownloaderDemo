[MySimpleDownloader Demo] - DJun, 2014.4.9
	This demo is created by DJun, which can provide a simple http downloader with
 simple download management ("multi-thread" added in 2014.7.31) .


2014-7-31
1. Add a class called DownUtil (renamed to MyMultiThreadDownloader) original from CrazyIT.
   It's for multi-thread file download. I will modify it to adapt to MySimpleDownloader.
   Set threadNum <= 1 is as like downloading a file by a single thread.
2. Seperate the classes from MySimpleDownloader into several files (but delete MySimpleDownloaderHandler after that).
3. More changes please see them in the source code.

2014-4-14
1. MySimpleDownloader improved, add delay time between twice UI update, and add record of last average download speed.
2. Update MyDownloadManagerHelper code.
3. Minor bug fix.

2014-4-11
1. Add 2 new variables for recording file length and downloaded length in MySimpleDownloadFile.
   When downloading, file length will be gotten and downloaded length will be updated now.
2. When localFilePath is null in MySimpleDownloadFile, set it to cache folder.
3. Create class MyDownloadManagerHelper for helping to use DownloadManager from OS.
4. Change minSdkVersion to 9 in Manifest.

2014-4-10
1. Add 2 necessary uses-permission to Manifest.
2. Complete initial programming code for MySimpleDownloader and then improve it.

2014-4-9
1. Project created.