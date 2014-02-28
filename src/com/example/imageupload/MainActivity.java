package com.example.imageupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int GALLERY_RESULT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		Toast.makeText(this, requestCode + " " + GALLERY_RESULT, 1).show();

		switch (requestCode) {
		case GALLERY_RESULT:
			if (data != null) {
				Uri img = data.getData();

				String path = this.getPath(img);

				Log.i("Path", path);

				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();

				StrictMode.setThreadPolicy(policy);

				request(path);

				Toast.makeText(this, path + " Path ", 0).show();
			}
			break;
		default:
			break;
		}
	}

	public void request(String file) {
		File f = new File(file);

		DefaultHttpClient client = new DefaultHttpClient();

		HttpPost postRequest = new HttpPost("http://172.27.99.39/file");

		MultipartEntity entity = new MultipartEntity();
		entity.addPart("upload",new FileBody(f) );
		
		postRequest.setEntity(entity);
		
		try {

			HttpResponse response = client.execute(postRequest);
			StatusLine status = response.getStatusLine();

			if (status.getStatusCode() == HttpStatus.SC_OK) {

				String result = getStringFromInputStream(response.getEntity()
						.getContent());

				Log.i("Resultado de request ", result);
			} else {
				String result = getStringFromInputStream(response.getEntity()
						.getContent());

				Log.i("Resultado de request Error " + status.getStatusCode(),
						result);
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getStringFromInputStream(InputStream inputStream) {

		BufferedReader buffer = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			buffer = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public String getPath(Uri uriFile) {

		String[] proj = { MediaStore.Images.Media.DATA };

		CursorLoader loader = new CursorLoader(this, uriFile, proj, null, null,
				null);

		Cursor cursor = loader.loadInBackground();

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public void load(View btn) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, GALLERY_RESULT);
	}

}
