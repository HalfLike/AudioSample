package com.halflike.myapplication.nettool;

import com.halflike.myapplication.logger.Logger;
import com.loopj.android.http.AsyncHttpClient;

public class HttpManager {

	public static HttpManager instance;
	public AsyncHttpClient mClient;

	public HttpManager() {
		mClient = new AsyncHttpClient();
	}
	
	public static HttpManager getInstance() {
		if (instance == null) {
			synchronized (HttpManager.class) {
				if (instance == null) {
					instance = new HttpManager();
				}
			}
		}
		return instance;
	}
	
	public void get(HttpRequest req, HttpResponse resp) {
		if (mClient != null) {
			mClient.get(req.getUrl(), resp);
		}
	}
	
	public void post(HttpRequest req, HttpResponse resp) {
		if (mClient != null) {
			mClient.post(null, req.getUrl(), req.getHeaders(), req.getBody(), null, resp);
		}
	}
	
}
