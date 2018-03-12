package com.cat.fsai.cc.aex;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import com.cat.fsai.inter.ApiRes;
import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.inter.OrderListRes;
import com.cat.fsai.inter.pojo.AccountInfo;
import com.cat.fsai.inter.pojo.AccountInfoItem;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.inter.pojo.OrderItem;
import com.cat.fsai.inter.pojo.StandRes;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.OrderType;
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
	final String accountInfo = "getMyBalance.php";
	final String orderList = "getOrderList.php";
	final String submitOrder = "submitOrder.php";
	final String cancelOrder = "cancelOrder.php";

	private OkHttpClient client;

	@Value("${aex.id}")
	public String ID;
	@Value("${aex.key}")
	public String key;
	@Value("${aex.skey}")
	public String skey;

	
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
	
	public void orderList(TR tr,OrderListRes orderListRes){
		try {
			String tagetUrl = url + orderList;			
			String now = System.currentTimeMillis() + "";

			RequestBody formBody = new FormBody.Builder()
					.add("key", key)
					.add("time", now)
					.add("md5", sign(now))
					.add("mk_type", tr.getRight().name())
					.add("coinname", tr.getLeft().name())
					.build();

			okhttpUtil.http(client, tagetUrl, null, formBody, (str, error) -> {
				try {
					if (error != null) {
						throw new ApiException(CCtype().getCn() + "查询挂单信息失败 ", error);
					}
					 List<OrderItem> res = new ArrayList<>();		
					//logger.info("str:{}", str);
					if(str.contains("no_order")){
						orderListRes.orderList(res, null);
						return;
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					JSONArray json = JSONArray.parseArray(str);		
					logger.info("json:{}", JSONObject.toJSONString(json));									 
					 json.stream().forEach(item->{
						 try {
						 JSONObject jo = (JSONObject)item;
						 OrderItem o = new OrderItem();
						 o.setOrderId(jo.getString("id"));
						 o.setType(jo.getString("type").equals("1")?OrderType.Buy:OrderType.Sell);
						 o.setTr(tr);
						 o.setPrice(jo.getBigDecimal("price"));
						 o.setAmount(jo.getBigDecimal("amount"));						
						 o.setTime(format.parse(jo.getString("time")));
						 res.add(o);
						 } catch (Exception e) {
						   throw new ApiException(e);						  
						}
					 });
					 
					orderListRes.orderList(res, null);
				} catch (Exception e) {
					orderListRes.orderList(null, new ApiException(CCtype().getCn() + "http res:"+str,e));
				}
			});
		} catch (Exception e) {
			orderListRes.orderList(null, e);
		}
	}
	
	public void sumbitOrder(TR tr,OrderType type,BigDecimal price,BigDecimal amount,ApiRes apiRes){
		try {
			String tagetUrl = url + submitOrder;			
			String now = System.currentTimeMillis() + "";

			RequestBody formBody = new FormBody.Builder()
					.add("key", key)
					.add("time", now)
					.add("md5", sign(now))
					.add("type", type==OrderType.Buy?"1":"2")
					.add("mk_type", tr.getRight().name())
					.add("price", price.toString())
					.add("amount", amount.toString())			
					.add("coinname", tr.getLeft().name())
					.build();

			okhttpUtil.http(client, tagetUrl, null, formBody, (str, error) -> {
				try {
					if (error != null) {
						throw new ApiException(CCtype().getCn() + "挂单失败 ", error);
					}
					StandRes res = new StandRes();		
					//logger.info("str:{}", str);
					if(str.contains("succ")){
						res.setSucess(true);
					}else{
						res.setSucess(false);
					}
					res.setMsg(str);
					apiRes.res(res, null);
				} catch (Exception e) {
					apiRes.res(null, new ApiException(CCtype().getCn() +tagetUrl+ "http res:"+str,e));
				}
			});
		} catch (Exception e) {
			apiRes.res(null, e);
		}
	}
	
	public void cancelOrder(TR tr,String orderId,ApiRes apiRes){
		try {
			String tagetUrl = url + cancelOrder;			
			String now = System.currentTimeMillis() + "";

			RequestBody formBody = new FormBody.Builder()
					.add("key", key)
					.add("time", now)
					.add("md5", sign(now))
					.add("mk_type", tr.getRight().name())
					.add("order_id", orderId)
					.add("coinname", tr.getLeft().name())					
					.build();

			okhttpUtil.http(client, tagetUrl, null, formBody, (str, error) -> {
				try {
					if (error != null) {
						throw new ApiException(CCtype().getCn() + "取消挂单失败 ", error);
					}
					StandRes res = new StandRes();		
					//logger.info("str:{}", str);
					if(str.contains("succ") || str.contains("overtime")){
						res.setSucess(true);
					}else{
						res.setSucess(false);
					}
					res.setMsg(str);
					apiRes.res(res, null);
				} catch (Exception e) {
					apiRes.res(null, new ApiException(CCtype().getCn() + "http res:"+str,e));
				}
			});
		} catch (Exception e) {
			apiRes.res(null, e);
		}
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
