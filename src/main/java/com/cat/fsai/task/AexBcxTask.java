/**
 * 
 */
package com.cat.fsai.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.aex.AexMarket;
import com.cat.fsai.error.BussException;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.pojo.AccountInfo;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.inter.pojo.OrderItem;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.OrderType;
import com.cat.fsai.type.TR;

/**
 * AexBcxTask
 * @author wangbo
 * @version Mar 11, 2018 6:45:43 PM
 */
@Component
public class AexBcxTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AexMarket aexMarket;
	
	@Scheduled(fixedRate = 1000*60*7)
	public synchronized void bcxSell() throws Exception {		
		 BigDecimal cnyMin = BigDecimal.valueOf(11);
		
		 //开始查询挂单
		 CountDownLatch downLatch2 = new CountDownLatch(2);
		 List<OrderItem> orderList = new ArrayList<>();
		 Arrays.asList(TR.BCX_CNY,TR.ETH_CNY).forEach(tr->{
			 aexMarket.orderList(tr, (ol,e)->{
				 infolog("查询"+tr+"挂单",ol,e);	
				 if(ol!=null){
					 orderList.addAll(ol);
				 }
				 downLatch2.countDown();
			 });
		 });
		 if(!downLatch2.await(2000, TimeUnit.MILLISECONDS))throw new BussException("查询挂单超时");	
		 
		 boolean[] hasOrder = new boolean[]{false,false};
		 //取消长时间未成交订单
		 if(orderList.size()>0){
			 logger.info("查询到之前未成交的挂单{}条,判断挂单信息",orderList.size());
			 CountDownLatch downLatch3 = new CountDownLatch(orderList.size());
			 long now = System.currentTimeMillis();
			 //过滤超过7分钟仍未成交的挂单
			 orderList.stream().forEach(o->{
				 if((now-o.getTime().getTime())<1000*60*15){
					 if(o.getTr()==TR.BCX_CNY) hasOrder[0] = true;
					 if(o.getTr()==TR.ETH_CNY) hasOrder[1] = true;
					 logger.info("挂单 oderid:{} 目前时间{}秒 还未到取消时间范围", o.getOrderId(),(now-o.getTime().getTime())/1000);
					 downLatch3.countDown();					
				 }else{
					 aexMarket.cancelOrder(o.getTr(), o.getOrderId(), (r,e)->{
						 infolog("撤销订单  OrderId:"+o.getOrderId(),r,e);	
						 downLatch3.countDown();
					 });
				 }
			 });
			 if(!downLatch3.await(7000, TimeUnit.MILLISECONDS))throw new BussException("撤销订单超时");	
		 }
		 
		 //查询账户信息
		 AccountInfo[] infos  = new AccountInfo[1]; 
		 CountDownLatch downLatch4 = new CountDownLatch(1);
		 aexMarket.accountInfo((info,error)->{
			 infolog("查询账户信息",info,error);
			 infos[0] = info;
			 downLatch4.countDown();
		 });
		 if(!downLatch4.await(2000, TimeUnit.MILLISECONDS))throw new BussException("查询账户信息超时");
		
		 //开始查询行情
		 if(!hasOrder[0]){
			 //bcx 数量满足要求,开始计算价格
			 DepthGroup[] dgs = new  DepthGroup[1];
			 CountDownLatch downLatchBcx = new CountDownLatch(1);
			 aexMarket.depth((dg,e)->{		
				 infolog("查询BCX_CNY深度",dg,e);
				 dgs[0] = dg;
				 downLatchBcx.countDown();
			 }, TR.BCX_CNY);	
			 if(!downLatchBcx.await(2000, TimeUnit.MILLISECONDS))throw new BussException("查询BCX_CNY深度超时");	
			
			 BigDecimal price = price(dgs[0],4,OrderType.Sell,RoundingMode.UP);
			 BigDecimal count = cnyMin.divide(price, 0, RoundingMode.DOWN);
			 //根据行情和账户信息进行挂单
			 if(infos[0].getInfoMap().get(Coin.BCX).getAvail().compareTo(count)>0){
				
				 logger.error("开始挂单卖出BCX_CNY, price:{} count{}",price,count);
				 //开始挂单
				 CountDownLatch downLatch = new CountDownLatch(1);
				 aexMarket.sumbitOrder(TR.BCX_CNY,OrderType.Sell, price,count,(ol,error)->{
					 if(ol!=null){
						 logger.info("submitOrder BCX_CNY price:{} res:{}",price,JSONObject.toJSONString(ol));
					 }
					 if(error!=null){
						 logger.error("submitOrder BCX_CNY error:",error);
					 }
					 downLatch.countDown();
				 });
				 if(!downLatch.await(2000, TimeUnit.MILLISECONDS))throw new BussException("挂单卖出BCX_CNY超时");	
			 }
		 }
		 
		
		 //判断是否可以进行eth买入挂单
		 if(!hasOrder[1] && infos[0].getInfoMap().get(Coin.BitCNY).getAvail().compareTo(cnyMin)>0){
			//bcx 数量满足要求,开始计算价格
			 DepthGroup[] dgs = new  DepthGroup[1];
			 CountDownLatch downLatchEth = new CountDownLatch(1);
			 aexMarket.depth((dg,e)->{		
				 infolog("查询ETH_CNY深度",dg,e);
				 dgs[0] = dg;
				 downLatchEth.countDown();
			 }, TR.ETH_CNY);	
			 if(! downLatchEth.await(2000, TimeUnit.MILLISECONDS))throw new BussException("查询ETH_CNY深度超时");		
			 BigDecimal price = price(dgs[0],0,OrderType.Buy,RoundingMode.DOWN);
			 BigDecimal count = cnyMin.divide(price, 6, RoundingMode.DOWN);
			 logger.error("开始挂单买入ETH_CNY, price:{} count:{}",price,count);
			 //开始挂单
			 CountDownLatch downLatch = new CountDownLatch(1);
			 aexMarket.sumbitOrder(TR.ETH_CNY,OrderType.Buy, price,count,(ol,error)->{
				 if(ol!=null){
					 logger.info("submitOrder ETH_CNY price:{} count:{} res:{}",price,count,JSONObject.toJSONString(ol));
				 }
				 if(error!=null){
					 logger.error("submitOrder ETH_CNY error:",error);
				 }
				 downLatch.countDown();
			 });
			 if(! downLatch.await(2000, TimeUnit.MILLISECONDS))throw new BussException("挂单买入ETH_CNY超时");	
		 }
	}
	
	private BigDecimal price(DepthGroup dg,int round,OrderType orderType,RoundingMode roundingMode) throws Exception{
		 //排序获取最高买入价格,最低卖出价格
		 Optional<DepthItem> buyPrice = dg.getBuy().stream().sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())).findFirst();
		 Optional<DepthItem> sellPrice = dg.getSell().stream().sorted((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())).findFirst();
		 if(!buyPrice.isPresent()|| !sellPrice.isPresent()){
			 throw new Exception("无法计算出深度数据:"+dg);
		 }		
		
		 //原取深度中间值算法,废弃,改为取深度当前方向,加零头算法
		 BigDecimal  midPrice = (buyPrice.get().getPrice().add(sellPrice.get().getPrice())).divide(BigDecimal.valueOf(2),round,roundingMode);
		 switch(orderType){
			case Buy:
				return midPrice.multiply(BigDecimal.valueOf(0.997)).setScale(round, roundingMode);			
			case Sell:
				return midPrice.multiply(BigDecimal.valueOf(1.01)).setScale(round, roundingMode);			
			default:
				throw new ParamException("orderType:"+orderType+" 不支持");
		 }
	}
	
	private void infolog(String title,Object obj,Exception e){
		if(obj!=null){
			logger.info("{} res:{}",title,JSONObject.toJSONString(obj));
		}
		if(e!=null){
			logger.warn("{} error:",title,e);
		}
	}
}
