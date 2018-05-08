package com.estimote.notification;

import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyRemoteDBHandler {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    private String dbUrl;
    private String userId;
    private Date clientTime;

    public MyRemoteDBHandler() {
        //empty public constructor
    }

    public MyRemoteDBHandler(String userId, Date clientTime) {
        this.userId = userId;
        this.clientTime = clientTime;
    }

    public MyRemoteDBHandler(String dbUrl, String userId, Date clientTime) {
        this.dbUrl = dbUrl;
        this.userId = userId;
        this.clientTime = clientTime;
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
        Request request = new Request.Builder().url(url).post(RequestBody.create(JSON, json)).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String toTimeJson(){
        return toTimeJson(this.userId, this.clientTime);
    }

    public String toTimeJson(Date clientTime){
        return toTimeJson(this.userId, clientTime.getTime());
    }

    public String toTimeJson(String userId, Date clientTime){
        return toTimeJson(userId, clientTime.getTime());
    }

    public String toTimeJson(String userId, long clientTime){
        return "{\"user_id\": \"" + userId + "\", \"client_check_time\": " + clientTime + "}";
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
