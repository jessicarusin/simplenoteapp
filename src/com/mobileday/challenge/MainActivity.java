package com.mobileday.challenge;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {
	Button button;
	EditText text;
	ListView listView;
	String result = "";
	ArrayList<String> notesList;
	ArrayAdapter<String> notesAdptr;
	final String tag = "Notes Response: ";
	String apiURL = "http://54.235.108.219/notes";
	//hardcoded user id - it should be based on user logged into system
	int userId = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		notesList = new ArrayList<String>();
		notesAdptr = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, notesList);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(notesAdptr);

		addListenerOnButton();
		populateNotes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void addListenerOnButton() {

		button = (Button) findViewById(R.id.button1);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				text = (EditText) findViewById(R.id.editText1);
				// add the Note to cloud DB (MongoDB using REST API call)
				addNote(text.getText().toString(), userId);
				// re-populate the notes from cloud storage
				populateNotes();
				// reset text field
				text.setText("");
			}

		});

	}

	/**
	 * @param text
	 *            This Method turns the text into a JSON Object and then calls a
	 *            post REST API call to update the value in cloud storage
	 * @return
	 */
	public boolean addNote(String text, int userId) {
		// only do something if the text has a value
		if (text != null && text.length() > 0) {
			JSONObject js = new JSONObject();
			try {
				js.put("text", text);
				js.put("userId", userId);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new PostNotes().execute(js.toString());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method calls the GET REST API to return all notes for a userId
	 */
	public void populateNotes() {
		new GetNotes().execute(apiURL + "/" + userId );
	}

	/**
	 * @param url
	 * @return
	 * This method builds an http GET request to call the URL passed in
	 */
	public String GET(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse result = null;
		String responseString = "";

		try {
			result = httpclient.execute(request);
			HttpEntity entity = result.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}

		Log.i(tag, responseString);
		return responseString;
	}

	/**
	 * @param url
	 * @param data
	 * @return
	 * This method builds a http post request to call the URL passed in with data passed in
	 */
	public HttpResponse POST(String url, String data) {
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new StringEntity(data));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		HttpClient httpclient = new DefaultHttpClient();
		try {
			return httpclient.execute(httpPost);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 * @author jessica This class makes the REST API call GET request for all
	 *         notes
	 */
	private class GetNotes extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String response = GET(params[0]);
			return response;

		}

		@Override
		protected void onPostExecute(String result) {
			JSONArray arrayOfNotes = null;
			try {
				arrayOfNotes = new JSONArray(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (arrayOfNotes != null) {
				Log.i(tag, "There are " + arrayOfNotes.length() + " notes.");

				// clear the current list of notes
				notesList.clear();
				for (int i = 0; i < arrayOfNotes.length(); i++) {
					JSONObject json_obj = null;
					String name = null;
					try {
						json_obj = arrayOfNotes.getJSONObject(i);
						name = json_obj.getString("text");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// add the notes to the list
					notesList.add(0, name);
				}
				// notify the adapter that the list has changed
				notesAdptr.notifyDataSetChanged();
			}
		}
	}

	/**
	 * @author jessica
	 * This class is responsible for posting new notes to the Notes REST API
	 *
	 */
	private class PostNotes extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... params) {
			return POST(apiURL, params[0]);
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			// Do something with result check for errors etc.
		}

	}

}
