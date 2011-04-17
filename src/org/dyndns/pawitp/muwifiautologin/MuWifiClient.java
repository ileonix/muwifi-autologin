package org.dyndns.pawitp.muwifiautologin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class MuWifiClient {

	static final String REDIRECT_PAGE_PATTERN = "Form used by registered users to login";
	static final String LOGIN_SUCCESSFUL_PATTERN = "External Welcome Page"; // not regex
	static final String FORM_USERNAME = "user";
	static final String FORM_PASSWORD = "password";
	static final String FORM_URL = "https://securelogin.arubanetworks.com/auth/index.html/u";
	static final int CONNECTION_TIMEOUT = 2000;
	static final int SOCKET_TIMEOUT = 2000;
	
	private String mUsername;
	private String mPassword;
	private HttpClient mHttpClient;
	
	public MuWifiClient(String username, String password) {
		mUsername = username;
		mPassword = password;
		
		mHttpClient = new DefaultHttpClient();
		HttpParams params = mHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
	}
	
	public boolean loginRequired() throws IOException {
		HttpGet httpget = new HttpGet("http://www.google.com/");
		HttpResponse response = mHttpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		Scanner scanner = new Scanner(is);
		String found = scanner.findWithinHorizon(REDIRECT_PAGE_PATTERN, 0);
		scanner.close();
		if (found == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public void login() throws IOException, LoginException {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair(FORM_USERNAME, mUsername));
		formparams.add(new BasicNameValuePair(FORM_PASSWORD, mPassword));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		HttpPost httppost = new HttpPost(FORM_URL);
		httppost.setEntity(entity);
		HttpResponse response = mHttpClient.execute(httppost);
		String strRes = EntityUtils.toString(response.getEntity());
		
		if (strRes.contains(LOGIN_SUCCESSFUL_PATTERN)) {
			// login successful
		} else {
			throw new LoginException(strRes);
		}
	}
	
}