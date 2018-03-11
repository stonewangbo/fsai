package com.cat.fsai.cc.huobi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import com.cat.fsai.type.tr.HuobiTR;

@Service
public class HuobiMarket implements MarketApi {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${huobi.market.url}")
	private String url;

	final String merged = "/detail/merged";

	final String depth = "/depth";

	final String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

	private CloseableHttpAsyncClient httpclient;

	@PostConstruct
	public void testPostConstruct() throws Exception {
		logger.info("http init");		

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(1000).build();

		httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		httpclient.start();
	}

	@PreDestroy
	public void testPreDesstroy() throws Exception {
		logger.info("http close");		
		httpclient.close();
	}

	@Override
	public void depth(DepthRes depthInfo, TR tr) {

		try {

			String tagetUrl = url + depth;
			Optional<HuobiTR> huobiTR = HuobiTR.searchByTr(tr);
			if (depthInfo == null) {
				throw new ParamException(CCtype().getCn() + "DepthRes 不能为空");
			}
			if (!huobiTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}

			HttpGet request = new HttpGet(tagetUrl + "?symbol=" + huobiTR.get().name() + "&type=step1");
			request.addHeader("agent", agent);

			httpclient.execute(request, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {
					try{
					String str = "";
					try {
						str = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines()
								.collect(Collectors.joining("\n"));
					} catch (Exception e) {
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:", e);
					}
					logger.debug("content():{}", str);
					JSONObject json = JSONObject.parseObject(str);
					if (!"ok".equals(json.getString("status"))) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str);
						
					JSONObject tick = json.getJSONObject("tick");
					if (tick == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  tick为空  res:" + str);
						
					DepthGroup depthGroup = new DepthGroup();
					JSONArray buyArray = tick.getJSONArray("bids");
					if (buyArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str);
					
					buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					JSONArray sellArray = tick.getJSONArray("asks");
					if (sellArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str);
						
					sellArray.stream().forEach(obj -> depthGroup.getSell().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					depthInfo.depth(depthGroup, null);
					}catch(Exception e){
						depthInfo.depth(null,e);
					}
				}

				@Override
				public void failed(final Exception ex) {
					depthInfo.depth(null, ex);
				}

				@Override
				public void cancelled() {
					depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  被取消 "));
				}

			});
			
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}

	@Override
	public CC CCtype() {
		return CC.Huobi;
	}
}
