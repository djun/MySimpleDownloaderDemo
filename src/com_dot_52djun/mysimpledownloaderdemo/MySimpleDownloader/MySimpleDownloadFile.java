package com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader;

//for storing those download file informations
public class MySimpleDownloadFile {

	private long id = INVALID_ID;
	private String url;
	private String localFilePath;
	private String title;
	private String description;

	private int fileLength = 0, downloadedLength = 0;
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

	public int getFileLength() {
		return fileLength;
	}

	public void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}

	public int getDownloadedLength() {
		return downloadedLength;
	}

	public void setDownloadedLength(int downloadedLength) {
		this.downloadedLength = downloadedLength;
	}

	public double getLastAvgDownloadSpeed() {
		return lastAvgDownloadSpeed;
	}

	public void setLastAvgDownloadSpeed(double lastAvgDownloadSpeed) {
		this.lastAvgDownloadSpeed = lastAvgDownloadSpeed;
	}

}
