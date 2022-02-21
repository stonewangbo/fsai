package com.cat.fsai.cc.binance;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.MarketApi;
import com.cat.fsai.error.ApiException;
import com.cat.fsai.error.NetException;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.TR;
import com.cat.fsai.type.tr.BinanceTR;

/**
 * BinanceMarket 币安交易所实现
 * 
 * @author wangbo
 * @version Feb 16, 2018 10:31:13 PM
 */
@Service
public class BinanceMarket implements MarketApi {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${binance.market.url}")
	private String url;

	final String depth = "/api/v1/depth";

	final String klines = "/api/v3/klines";

	final String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

	private HttpClient httpClient;

	@PostConstruct
	public void init() throws Exception {
		logger.info("http init");
		httpClient = new HttpClient(new SslContextFactory());
		httpClient.setConnectTimeout(2000);
		httpClient.setAddressResolutionTimeout(1000);
		httpClient.start();
	}

	@PreDestroy
	public void clean() throws Exception {
		logger.info("http close");
		httpClient.stop();

	}

	@Override
	public void depth(DepthRes depthInfo, TR tr) {
		try {
		String tagetUrl = url + depth;
		Optional<BinanceTR> marketTR = BinanceTR.searchByTr(tr);
		if (depthInfo == null) {
			throw new ParamException(CCtype().getCn()+"DepthRes 不能为空");
		}
		if (!marketTR.isPresent()) {
			throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
		}
		httpClient.newRequest(tagetUrl).agent(agent).param("symbol", marketTR.get().name()).param("limit", "20")
				.onResponseContent(new ContentListener() {
					@Override
					public void onContent(Response response, ByteBuffer content) {
						String str = new String(content.array());
						logger.debug("content():{}", str);
						JSONObject json = JSONObject.parseObject(str);
						if (json.getString("code") != null) {
							depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str));
							return;
						}
						DepthGroup depthGroup = new DepthGroup();
						JSONArray buyArray = json.getJSONArray("bids");
						if (buyArray == null) {
							depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str));
							return;
						}
						buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
								new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
						JSONArray sellArray = json.getJSONArray("asks");
						if (sellArray == null) {
							depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str));
							return;
						}
						sellArray.stream().forEach(obj -> depthGroup.getSell().add(
								new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
						depthInfo.depth(depthGroup, null);
					}
				}).send(new CompleteListener() {
					@Override
					public void onComplete(Result arg0) {
						if (!arg0.isSucceeded()) {							
							depthInfo.depth(null,
									new NetException(CCtype().getCn() + "深度数据获取失败  res:" + arg0.getFailure()));
						}
					}
				});
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}

	public void klines(Date startTime,Date endTime,TR tr){
		String tagetUrl = url + depth;
		Optional<BinanceTR> marketTR = BinanceTR.searchByTr(tr);
//		if (depthInfo == null) {
//			throw new ParamException(CCtype().getCn()+"DepthRes 不能为空");
//		}
		if (!marketTR.isPresent()) {
			throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
		}

	//	httpClient.newRequest(tagetUrl).agent(agent).param("symbol", marketTR.get().name()).param("limit", "20")

	}

	@Override
	public CC CCtype() {
		return CC.Binance;
	}

}
