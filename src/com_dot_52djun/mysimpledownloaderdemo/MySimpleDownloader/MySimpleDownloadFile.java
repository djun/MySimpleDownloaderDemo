package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

//for storing those download file informations
public class MySimpleDownloadFile {

	private long id = INVALID_ID;
	private String url;
	private String localFilePath;
	private String title;
	private String description;

	private int fileLength = 0, downloadedLength = 0;
	private double completedRate = 0;
	private double lastAvgDownloadSpeed = 0;

	public static final long INVALID_ID = -1;

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

	public synchronized void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public synchronized void setUrl(String url) {
		this.url = url;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public synchronized void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public String getTitle() {
		return title;
	}

	public synchronized void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public synchronized void setDescription(String description) {
		this.description = description;
	}

	public int getFileLength() {
		return fileLength;
	}

	public synchronized void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}

	public int getDownloadedLength() {
		return downloadedLength;
	}

	public synchronized void setDownloadedLength(int downloadedLength) {
		this.downloadedLength = downloadedLength;
	}

	public double getCompletedRate() {
		return completedRate;
	}

	public synchronized void setCompletedRate(double completedRate) {
		this.completedRate = completedRate;
	}

	public double getLastAvgDownloadSpeed() {
		return lastAvgDownloadSpeed;
	}

	public synchronized void setLastAvgDownloadSpeed(double lastAvgDownloadSpeed) {
		this.lastAvgDownloadSpeed = lastAvgDownloadSpeed;
	}

}
