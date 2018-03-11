package com.cat.fsai.cc.hibtc;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.MarketApi;
import com.cat.fsai.error.ApiException;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.TR;
import com.cat.fsai.type.tr.HitbtcTR;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HitbtcMarket
 * @author wangbo
 * @version Feb 20, 2018 9:13:23 PM
 */
@Service
public class HitbtcMarket implements MarketApi {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	final private String url = "https://api.hitbtc.com";

	final String depth = "/api/2/public/orderbook";

	private OkHttpClient client;
	
	@PostConstruct
	public void testPostConstruct() throws Exception {
		logger.info("http init");		

		client = new OkHttpClient.Builder().connectTimeout(1000, TimeUnit.MILLISECONDS)
				.writeTimeout(1000, TimeUnit.MILLISECONDS).readTimeout(2000, TimeUnit.MILLISECONDS).build();
		
	}

	@PreDestroy
	public void testPreDesstroy() throws Exception {
		logger.info("http close");		
	}

	@Override
	public void depth(DepthRes depthInfo, TR tr) {

		try {

			String tagetUrl = url + depth;
			Optional<HitbtcTR> ccTR = HitbtcTR.searchByTr(tr);
			if (depthInfo == null) {
				throw new ParamException(CCtype().getCn() + "DepthRes 不能为空");
			}
			if (!ccTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}
		
			
			Request request = new Request.Builder()
				        .url(tagetUrl + "/" + ccTR.get().name() )
				        .build();

			client.newCall(request).enqueue(new Callback() {

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					try{
				    if (!response.isSuccessful()) 
				    	throw new ApiException(CCtype().getCn() +"深度数据获取失败 " + response);
					String str = response.body().string();					
					logger.debug("content():{}", str);
					JSONObject json = JSONObject.parseObject(str);
					if (!StringUtils.isEmpty(json.getString("error")))
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str);						
				
						
					DepthGroup depthGroup = new DepthGroup();
					JSONArray buyArray = json.getJSONArray("bid");
					if (buyArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str);
					
					buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
							new DepthItem(((JSONObject) obj).getBigDecimal("price"), ((JSONObject) obj).getBigDecimal("size"))));
					JSONArray sellArray = json.getJSONArray("ask");
					if (sellArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str);
						
					sellArray.stream().forEach(obj -> depthGroup.getSell().add(
							new DepthItem(((JSONObject) obj).getBigDecimal("price"), ((JSONObject) obj).getBigDecimal("size"))));
					depthInfo.depth(depthGroup, null);
					}catch(Exception e){
						depthInfo.depth(null,e);
					}
				}

				@Override
				public void onFailure(Call call, IOException e) {
					depthInfo.depth(null, e);
				}			

			});
			
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}

	@Override
	public CC CCtype() {
		return CC.HitBTC;
	}
}
