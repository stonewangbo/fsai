package com.cat.fsai.cc.aex;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import com.cat.fsai.type.tr.AexTR;
import com.cat.fsai.util.http.OKhttpUtil;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * ZbMarket
 * 
 * @author wangbo
 * @version Mar 3, 2018 9:58:11 PM
 * 
 * 
 */
@Service
public class AexMarket implements MarketApi {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	final private String url = "https://api.aex.com/";

	final String depth = "depth.php";

	private OkHttpClient client;

	@Value("${aex.id}")
	public String ID;
	@Value("${aex.key}")
	public String key;
	@Value("${aex.skey}")
	public String skey;

	final String accountInfo = "getMyBalance.php";

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
			Optional<AexTR> ccTR = AexTR.searchByTr(tr);
			if (depthInfo == null) {
				throw new ParamException(CCtype().getCn() + "DepthRes 不能为空");
			}
			if (!ccTR.isPresent()) {
				throw new ParamException(CCtype().getCn() + "TR:" + tr + " 没有有效的交易对");
			}
			@SuppressWarnings("serial")
			Map<String, String> param = new HashMap<String, String>() {
				{
					put("c", tr.getLeft().name().toLowerCase());
					put("mk_type", tr.getRight().name().toLowerCase());
				}
			};

			okhttpUtil.http(client, tagetUrl, param, null, (str, error) -> {
				try {
					if (error != null) {
						throw new ApiException(CCtype().getCn() + "深度数据获取失败 ", error);
					}
					logger.debug("content():{}", str);
					JSONObject json = JSONObject.parseObject(str);

					DepthGroup depthGroup = new DepthGroup();
					JSONArray buyArray = json.getJSONArray("bids");
					if (buyArray == null)
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  bids为空  res:" + str);

					buyArray.stream().forEach(obj -> depthGroup.getBuy().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					JSONArray sellArray = json.getJSONArray("asks");
					if (sellArray == null)
						throw new ApiException(CCtype().getCn() + "深度数据获取失败  asks为空  res:" + str);

					sellArray.stream().forEach(obj -> depthGroup.getSell().add(
							new DepthItem(((JSONArray) obj).getBigDecimal(0), ((JSONArray) obj).getBigDecimal(1))));
					depthInfo.depth(depthGroup, null);

				} catch (Exception e) {
					depthInfo.depth(null, e);
				}
			});
		} catch (Exception e) {
			depthInfo.depth(null, e);
		}
	}

	public void accountInfo(AccountRes accountRes) {
		try {
			String tagetUrl = url + accountInfo;			
			String now = System.currentTimeMillis() + "";

			RequestBody formBody = new FormBody.Builder()
					.add("key", key)
					.add("time", now)
					.add("md5", sign(now))
					.build();

			okhttpUtil.http(client, tagetUrl, null, formBody, (str, error) -> {
				try {
					if (error != null) {
						throw new ApiException(CCtype().getCn() + "个人信息获取失败 ", error);
					}
					//logger.info("str:{}", str);
					 Map<String,BigDecimal> json = JSONObject.parseObject(str,new TypeReference<Map<String,BigDecimal>>(){});
					 logger.info("json:{}", JSONObject.toJSONString(json));
					 AccountInfo res = new AccountInfo();
					 String balance = "_balance";
					 String lock = "_balance_lock";
					 Arrays.stream(Coin.values()).forEach(c->{
						 BigDecimal avail = json.get(c.name().toLowerCase()+balance);
						 BigDecimal freez = json.get(c.name().toLowerCase()+lock);
						 if(avail!=null||freez!=null){
							 AccountInfoItem accountInfoItem = new AccountInfoItem();
						     accountInfoItem.setAvail(avail);
						     accountInfoItem.setFreez(freez);
						     res.getInfoMap().put(c, accountInfoItem);
						 }						 
					 });					
					accountRes.accountInfo(res, null);
				} catch (Exception e) {
					accountRes.accountInfo(null, new ApiException(CCtype().getCn() + "http res:"+str,e));
				}
			});
		} catch (Exception e) {
			accountRes.accountInfo(null, e);
		}
	}
	
	public void orderList(){
		
	}
	
	private String sign(String time) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		return DatatypeConverter.printHexBinary(
				md.digest((key + "_" + ID + "_" + skey + "_" + time).getBytes())).toUpperCase();
	}

	@Override
	public CC CCtype() {
		return CC.Aex;
	}
}
