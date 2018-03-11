package com.cat.fsai.cc.zb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.MarketApi;
import com.cat.fsai.error.ApiException;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.AccountRes;
import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.inter.pojo.AccountInfo;
import com.cat.fsai.inter.pojo.AccountInfoItem;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.TR;
import com.cat.fsai.type.tr.ZbTR;
import com.cat.fsai.util.http.OKhttpUtil;

import okhttp3.OkHttpClient;


/**
 * ZbMarket
 * @author wangbo
 * @version Mar 3, 2018 9:58:11 PM
 * 
 * 
 */
@Service
public class ZbMarket implements MarketApi {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	final private String url = "http://api.zb.com/";

	final String depth = "data/v1/depth";

	private OkHttpClient client;
	
	@Value("${zb.access}")
	public String ACCESS_KEY;
	@Value("${zb.secret}")
	public String SECRET_KEY;
	
	final private String TRADE_URL = "https://trade.zb.com/api/";
	
	final String accountInfo = "getAccountInfo";
	
	@Autowired
	private OKhttpUtil okhttpUtil;
	
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
			Optional<ZbTR> ccTR = ZbTR.searchByTr(tr);
			if (depthInfo == null) {
				throw new ParamException(CCtype().getCn() + "DepthRes 不能为空");
			}
			if (!ccTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}
			@SuppressWarnings("serial")
			Map<String, String> param = new HashMap<String, String>() {
				{
					put("market", ccTR.get().name());
					put("size", "10");
				}
			};
			
			okhttpUtil.http(client, tagetUrl, param,null, (str,error)->{
				try{
					if(error!=null){
						throw new ApiException(CCtype().getCn() +"深度数据获取失败 " ,error);
					}
					logger.debug("content():{}", str);
					JSONObject json = JSONObject.parseObject(str);
					if (!StringUtils.isEmpty(json.getString("code")))
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  res:" + subError(str));						
				
						
					DepthGroup depthGroup = new DepthGroup();
					JSONArray buyArray = json.getJSONArray("bids");
					if (buyArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + subError(str));
					
					buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					JSONArray sellArray = json.getJSONArray("asks");
					if (sellArray == null) 
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + subError(str));
						
					sellArray.stream().forEach(obj -> depthGroup.getSell().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					depthInfo.depth(depthGroup, null);
					
				}catch(Exception e){
					depthInfo.depth(null,e);
				}
			});
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}
	
	private String subError(String str){
		if(str!=null){
			return str.substring(0, str.length()>100?100:str.length());
		}else{
			return "null";
		}
	}
	
	public void accountInfo(AccountRes accountRes){
		String tagetUrl = TRADE_URL + accountInfo;
		@SuppressWarnings("serial")
		Map<String, String> param = new HashMap<String, String>() {
			{
				put("method", "getAccountInfo");				
			}
		};
		sign(param);
		okhttpUtil.http(client, tagetUrl, param,null, (str,error)->{
			try{
				logger.debug("str:{}",str);
				JSONObject json = JSONObject.parseObject(str);				
				if (!StringUtils.isEmpty(json.getString("code")))
					throw new ApiException(CCtype().getCn() + "账户数据获取失败  res:" + str);		
				
				AccountInfo res = new AccountInfo();
				JSONObject result = json.getJSONObject("result");
				if (result == null) 
					throw new ApiException(CCtype().getCn() + "账户数据获取失败  result为空  res:" + str);
				JSONArray coins = result.getJSONArray("coins");
				if (coins == null) 
					throw new ApiException(CCtype().getCn() + "账户数据获取失败  coins为空  res:" + str);
				coins.stream().forEach(obj->{
						JSONObject jo = (JSONObject)obj;
						Optional<Coin> coin = Coin.find(jo.getString("enName"));
						if(!coin.isPresent()){
							logger.debug(CCtype().getCn() + "账户数据获取失败  enName找不到对应的coin  res:" + jo.getString("enName"));
							return;
						}
						AccountInfoItem accountInfoItem = new AccountInfoItem();
						accountInfoItem.setAvail(jo.getBigDecimal("available"));
						accountInfoItem.setFreez(jo.getBigDecimal("freez"));
						res.getInfoMap().put(coin.get(), accountInfoItem);;
					});
				accountRes.accountInfo(res, null);
			}catch(Exception e){
				accountRes.accountInfo(null,e);
			}
		});
	}
	
	/**
	 * 请求加密
	 * 
	 * @param params
	 * @return
	 */
	private void sign(Map<String, String> params) {
		params.put("accesskey", ACCESS_KEY);// 这个需要加入签名,放前面
		String digest = EncryDigestUtil.digest(SECRET_KEY);

		String sign = EncryDigestUtil.hmacSign(MapSort.toStringMap(params), digest); // 参数执行加密
		// 加入验证
		params.put("sign", sign);
		params.put("reqTime", System.currentTimeMillis() + "");		
		
	}

	@Override
	public CC CCtype() {
		return CC.Zb;
	}
}
