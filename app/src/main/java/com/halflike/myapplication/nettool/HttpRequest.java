package com.halflike.myapplication.nettool;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequest {
	
	public static String mDomain = "http://54.64.185.246";
	public String mActionUrl = "";

	public Header[] mHeaders;
	public HttpEntity mEntity;
	public HashMap mUrlParams = new HashMap<String, String>();

	public final String TAG = "HttpRequest";

	public HttpRequest() {
	}
	
	public HttpRequest(String action) {
		this.mActionUrl = action;
	}
	
	public String getUrl() {
		Log.d(TAG, mDomain + mActionUrl + getGetMethodParams());
		return mDomain + mActionUrl + getGetMethodParams();
	}

	public String getGetMethodParams() {
		if (mUrlParams == null || mUrlParams.size() == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("?");
		Iterator iter = mUrlParams.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			builder.append(entry.getKey() + "=" + entry.getValue() + "&");
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	public Header[] getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Header[] headers) {
		mHeaders = headers;
	}

	public HttpEntity getBody() {
		try {
			Log.d(TAG, EntityUtils.toString(mEntity));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mEntity;
	}

	public void setBody(String body) {
		try {
			mEntity = new ByteArrayEntity(body.getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void put(String key, String value) {
		mUrlParams.put(key, value);
	}

}
