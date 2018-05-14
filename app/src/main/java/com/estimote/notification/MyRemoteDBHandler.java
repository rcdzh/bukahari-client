package com.estimote.notification;

import android.os.AsyncTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class MyRemoteDBHandler extends AsyncTask<String, Integer, String> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = null;
    private String dbUrl;
    private String userId;
    private Date clientTime;

    public MyRemoteDBHandler() {
        this.client = this.createAuthenticatedClient();
    }

    public MyRemoteDBHandler(String dbUrl) {
        this.client = this.createAuthenticatedClient();
        this.dbUrl = dbUrl;
    }

    public MyRemoteDBHandler(String userId, Date clientTime) {
        this.client = this.createAuthenticatedClient();
        this.userId = userId;
        this.clientTime = clientTime;
    }

    public MyRemoteDBHandler(String dbUrl, String userId, Date clientTime) {
        this.client = this.createAuthenticatedClient();
        this.dbUrl = dbUrl;
        this.userId = userId;
        this.clientTime = clientTime;
    }

    private OkHttpClient createAuthenticatedClient() {
        // build client with authentication information.
        return new OkHttpClient.Builder().authenticator(new Authenticator() {
            public Request authenticate(Route route, Response response) throws IOException {
            String credential = Credentials.basic(
                "bukahari@bukalapak.com",
                "nktb2018"
            );
            return response.request().newBuilder().header(
                "Authorization",
                credential
            ).build();
            }
        }).build();
    }

    public String getContent () throws IOException {
        return getContent(this.dbUrl);
    }

    public String getContent (String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String post() throws IOException {
        return post(this.dbUrl, toTimeJson());
    }

    public String post(String url, String json) throws IOException {
        System.out.println(json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String toTimeJson() {
        return toTimeJson(this.userId, this.clientTime.getTime()/1000);
    }

    public String toTimeJson(Date clientTime) {
        return toTimeJson(this.userId, clientTime.getTime()/1000);
    }

//    public String toTimeJson(String userId, Date clientTime) {
//        String timeParsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(clientTime);
//        return "{\"username\": \"" + userId + "\", \"time\": \"" + timeParsed + "\"}";
//    }

    public String toTimeJson(String userId, long clientTime) {
        return "{\"username\": \"" + userId + "\", \"time\": " + clientTime + "}";
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected String doInBackground(String... requests) {
        try {
            if (requests[0].equals("POST")) {
                String res = this.post();
                System.out.println(res);
                return res;
            } else if (requests[0].equals("GET")) {
                String res = this.getContent();
                System.out.println(res);
                return res;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

}
