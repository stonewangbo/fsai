package com.cat.fsai.cc.binance;

import java.math.BigDecimal;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.error.ApiException;
import com.cat.fsai.error.NetException;
import com.cat.fsai.inter.KLineRes;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.inter.pojo.KLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cat.fsai.cc.MarketApi;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.TR;
import com.cat.fsai.type.tr.BinanceTR;
import org.springframework.util.StringUtils;

/**
 * BinanceMarket 币安交易所实现
 * 
 * @author wangbo
 * @version Feb 16, 2018 10:31:13 PM
 */
@Service
@Slf4j
public class BinanceMarket implements MarketApi {


	@Value("${binance.market.url}")
	private String url;

	@Value("${api.proxy.host}")
	private String proxyHost;

	@Value("${api.proxy.port}")
	private Integer proxyPort;

	final String depth = "/api/v1/depth";

	final String klines = "/api/v3/klines";

	final String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";


	private HttpClient httpClient;

	@PostConstruct
	public void init() throws Exception {
		log.info("{} http init",CCtype().getCn());

		var builder =  HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(Duration.ofSeconds(2));
		if(!StringUtils.isEmpty(proxyHost)){
			log.info("{} API接口，识别到代理配置 {}:{} 将使用代理模式进行连接",CCtype().getCn(),proxyHost,proxyPort);
			builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
		}
		httpClient = builder.build();

	}

	@PreDestroy
	public void clean() throws Exception {
		log.info("http close");

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

		URI uri = new URIBuilder(tagetUrl)
					.addParameter("symbol",  marketTR.get().name())
					.addParameter("limit",  "20")
					.build();
		HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.timeout(Duration.ofSeconds(5))
					.header("Content-Type", "application/json")
					.header("agent",agent)
					.GET().build();
		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(body -> {
					String str = body;
					log.debug("content():{}", str);
					JSONObject json = JSONObject.parseObject(str);
					if (json.getString("code") != null) {
						depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + str));
						return  "fail";
					}
					DepthGroup depthGroup = new DepthGroup();
					JSONArray buyArray = json.getJSONArray("bids");
					if (buyArray == null) {
						depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str));
						return "fail";
					}
					buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					JSONArray sellArray = json.getJSONArray("asks");
					if (sellArray == null) {
						depthInfo.depth(null, new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str));
						return "fail";
					}
					sellArray.stream().forEach(obj -> depthGroup.getSell().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					depthInfo.depth(depthGroup, null);
					return "OK";})
				.exceptionally(err -> {
					depthInfo.depth(null,
									new NetException(CCtype().getCn() + "深度数据获取失败:",err));
					return "fallback";
				});
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}

	public void klines(KLineRes kLineRes, Date startTime, Date endTime, TR tr){
		try {
			String tagetUrl = url + klines;
			Optional<BinanceTR> marketTR = BinanceTR.searchByTr(tr);
//		if (depthInfo == null) {
//			throw new ParamException(CCtype().getCn()+"DepthRes 不能为空");
//		}
			if (!marketTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}

			/**
			 m -> 分钟; h -> 小时; d -> 天; w -> 周; M -> 月
			 1m
			 3m
			 5m
			 15m
			 30m
			 1h
			 2h
			 4h
			 6h
			 8h
			 12h
			 1d
			 3d
			 1w
			 1M
			 */
			URI uri = new URIBuilder(tagetUrl)
					.addParameter("symbol", marketTR.get().name())
					.addParameter("interval", "1m")
					.addParameter("startTime",String.valueOf(startTime.getTime()))
					.addParameter("endTime", String.valueOf(endTime.getTime()))
					.addParameter("limit", "500")
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(uri)
					.timeout(Duration.ofSeconds(5))
					.header("Content-Type", "application/json")
					.header("agent", agent)
					.GET().build();
			httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApply(HttpResponse::body)
					.thenApply(body -> {
						/**
						 [
						 [
						 1499040000000,      // 开盘时间
						 "0.01634790",       // 开盘价
						 "0.80000000",       // 最高价
						 "0.01575800",       // 最低价
						 "0.01577100",       // 收盘价(当前K线未结束的即为最新价)
						 "148976.11427815",  // 成交量
						 1499644799999,      // 收盘时间
						 "2434.19055334",    // 成交额
						 308,                // 成交笔数
						 "1756.87402397",    // 主动买入成交量
						 "28.46694368",      // 主动买入成交额
						 "17928899.62484339" // 请忽略该参数
						 ]
						 ]
						 */
						//log.debug("body():{}", body);
						// 解析JSON
						List<JSONArray> arrayList = JSONArray.parseArray(body, JSONArray.class);
						//log.debug("arrayList:{}", arrayList);
						List<KLine> kines = new ArrayList<>();
						for(var array:arrayList){
							KLine kline =  new KLine();
							kline.setStartTime(new Date(array.getLong(0)));
							kline.setBeginPr(new BigDecimal(array.getString(1)));
							kline.setHighPr(new BigDecimal(array.getString(2)));
							kline.setLowPr(new BigDecimal(array.getString(3)));
							kline.setFinishPr(new BigDecimal(array.getString(4)));
							kline.setTradeAmt(new BigDecimal(array.getString(5)));
							kline.setEndTime(new Date(array.getLong(6)));
							kline.setTurnover(new BigDecimal(array.getString(7)));
							kline.setTradeCount(array.getInteger(8));
							kline.setBuyAmt(new BigDecimal(array.getString(9)));
							kline.setBuyTurnover(new BigDecimal(array.getString(10)));
							kines.add(kline);
						}
						kLineRes.kLine(kines,null);
						return "OK";
					}).exceptionally(err -> {
						kLineRes.kLine(null,err);
						return "fallback";
					});
		}catch (Exception e){
			kLineRes.kLine(null,e);
		}
	}

	@Override
	public CC CCtype() {
		return CC.Binance;
	}

}
