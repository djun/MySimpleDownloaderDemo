package com_dot_52djun.mysimpledownloaderdemo;

import com_dot_52djun.mysimpledownloaderdemo.MySimpleDownloader.MySimpleDownloader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	private MySimpleDownloader downloader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		downloader = MySimpleDownloader.getPublicDownloader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
