package com.halflike.myapplication.nettool;

import android.util.Log;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;

public abstract class HttpResponse extends AsyncHttpResponseHandler {

	private final String TAG = "HttpResponse";

	public abstract void onSuccess(JSONObject json);
	
	public abstract void onFailure(int statusCode, String msg);
	
	@Override
	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		if (statusCode != 200) {
			onFailure(statusCode, "fail");
			return;
		}
		Log.d(TAG, "************Response info start**************");
		for(Header head : headers) {
			Log.d(TAG, head.getName() + ":" + head.getValue());
		}
		String resp = new String(responseBody);
		Log.d(TAG, resp);
		Log.d(TAG, "************Response info end**************");
		try {
			JSONObject json = new JSONObject(resp);
			onSuccess(json);
		} catch (JSONException e) {
			onFailure(statusCode, "Creat JSONObeject fail, the response is:" +
					new String(responseBody));
		} 
	}

	@Override
	public void onFailure(int statusCode, Header[] headers,
			byte[] responseBody, Throwable error) {
		onFailure(statusCode, error.getMessage());
	}

}
