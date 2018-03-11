package com.cat.fsai.util.http;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cat.fsai.error.NetException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class OKhttpUtil {

	public void http(OkHttpClient client, String url,Map<String,String> param,RequestBody body,HttpRes httpRes) {
		HttpUrl.Builder builder =  HttpUrl.parse(url).newBuilder();	
		if(param!=null){
			param.entrySet().stream().forEach(item->builder.addQueryParameter(item.getKey(), item.getValue()));
		}
		HttpUrl httpurl =builder.build();
		Builder builder2 = new Request.Builder().url(httpurl);
		if(body!=null){
			builder2 = builder2.post(body);
		}
		 Request request = builder2.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				 if (!response.isSuccessful()) {
					 httpRes.parseRes(null, new NetException(url+" http失败 " + response));
					 return;
				 }
				 try{
					 httpRes.parseRes( response.body().string(),null);
				 }catch(Exception e){
					 httpRes.parseRes(null, new NetException(url+" http失败 " ,e));
				 }
			}

			@Override	
			public void onFailure(Call call, IOException e) {
				 httpRes.parseRes(null, new NetException(url+" http失败 " , e));
			}			

		});	
	
	}
}
