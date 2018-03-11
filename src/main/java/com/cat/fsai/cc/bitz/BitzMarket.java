package com.cat.fsai.cc.bitz;

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
import com.cat.fsai.type.tr.BitzTR;

/**
 * BinanceMarket 币安交易所实现
 * 
 * @author wangbo
 * @version Feb 16, 2018 10:31:13 PM
 */
@Service
public class BitzMarket implements MarketApi {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${bitz.market.url}")
	private String url;

	final String depth = "/api_v1/depth";

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

			Optional<BitzTR> marketTR = BitzTR.searchByTr(tr);
			if (depthInfo == null) {
				throw new ParamException(CCtype().getCn() + "DepthRes 不能为空");
			}
			if (!marketTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}

			HttpGet request = new HttpGet(tagetUrl + "?coin=" + marketTR.get().name() + "&limit=20");
			request.addHeader("agent", agent);

			httpclient.execute(request, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {
					try {
						String str = "";
						try {
							str = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines()
									.collect(Collectors.joining("\n"));
						} catch (Exception e) {
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:", e);
						}
						logger.debug("content():{}", str);
						JSONObject json = JSONObject.parseObject(str);
						if (json.getString("code") == null || !"0".equals(json.getString("code")))
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str);

						if (json.getString("msg") == null || !"Success".equals(json.getString("msg")))
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str);

						DepthGroup depthGroup = new DepthGroup();
						JSONObject jdata = json.getJSONObject("data");
						if (jdata == null)
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  data为空  res:" + str);
						JSONArray buyArray = jdata.getJSONArray("bids");
						if (buyArray == null)
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str);

						buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
								new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
						JSONArray sellArray = jdata.getJSONArray("asks");
						if (sellArray == null)
							throw new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str);
						sellArray.stream().forEach(obj -> depthGroup.getSell().add(
								new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
						depthInfo.depth(depthGroup, null);
					} catch (Exception e) {
						depthInfo.depth(null, e);
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
		} catch (ParamException e) {
			depthInfo.depth(null, e);
		} catch (Exception e) {
			depthInfo.depth(null, new ApiException(CCtype().getCn() + "获取深度数据TR:" + tr, e));
		}
	}

	@Override
	public CC CCtype() {
		return CC.Bitz;
	}

}
