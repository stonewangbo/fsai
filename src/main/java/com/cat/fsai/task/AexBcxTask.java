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
	
	@Scheduled(fixedRate = 1000*60*20)
	public synchronized void bcxSell() throws Exception {
		 BigDecimal bcxMin = BigDecimal.valueOf(1000);
		 BigDecimal cnyMin = BigDecimal.valueOf(20);
		
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
		 downLatch2.await(2000, TimeUnit.MILLISECONDS);	
		 //取消长时间未成交订单
		 if(orderList.size()>0){
			 logger.info("查询到之前未成交的挂单{}条,判断挂单信息",orderList.size());
			 CountDownLatch downLatch3 = new CountDownLatch(orderList.size());
			 long now = System.currentTimeMillis();
			 //过滤超过十分钟仍未成交的挂单
			 orderList.stream().forEach(o->{
				 if((now-o.getTime().getTime())<1000*60*10){
					 downLatch3.countDown();
					 return;
				 }
				 aexMarket.cancelOrder(o.getTr(), o.getOrderId(), (r,e)->{
					 infolog("撤销订单:",r,e);					
				 });
				 downLatch3.countDown();
			 });
			 downLatch3.await(2000, TimeUnit.MILLISECONDS);	
		 }
		 
		 //查询账户信息
		 AccountInfo[] infos  = new AccountInfo[1]; 
		 CountDownLatch downLatch4 = new CountDownLatch(1);
		 aexMarket.accountInfo((info,error)->{
			 infolog("查询账户信息",info,error);
			 infos[0] = info;
			 downLatch4.countDown();
		 });
		 downLatch4.await(2000, TimeUnit.MILLISECONDS);
		 
		 
		
		 //开始查询行情
		
		 
		 //根据行情和账户信息进行挂单
		 if(infos[0].getInfoMap().get(Coin.BCX).getAvail().compareTo(bcxMin)>0){
			 //bcx 数量满足要求,开始计算价格
			 DepthGroup[] dgs = new  DepthGroup[1];
			 CountDownLatch downLatchBcx = new CountDownLatch(1);
			 aexMarket.depth((dg,e)->{		
				 infolog("查询BCX_CNY深度",dg,e);
				 dgs[0] = dg;
				 downLatchBcx.countDown();
			 }, TR.BCX_CNY);	
			 downLatchBcx.await(2000, TimeUnit.MILLISECONDS);	
			 
			
			 BigDecimal price = price(dgs[0],4,RoundingMode.UP);
			 logger.error("开始挂单卖出BCX_CNY, price:{}",price);
			 //开始挂单
			 CountDownLatch downLatch = new CountDownLatch(1);
			 aexMarket.sumbitOrder(TR.BCX_CNY,OrderType.Sell, price,bcxMin,(ol,error)->{
				 if(ol!=null){
					 logger.info("submitOrder BCX_CNY price:{} res:{}",price,JSONObject.toJSONString(ol));
				 }
				 if(error!=null){
					 logger.error("submitOrder BCX_CNY error:",error);
				 }
				 downLatch.countDown();
			 });
			 downLatch.await(2000, TimeUnit.MILLISECONDS);
		 }
		 //判断是否可以进行eth买入挂单
		 if(infos[0].getInfoMap().get(Coin.BitCNY).getAvail().compareTo(cnyMin)>0){
			//bcx 数量满足要求,开始计算价格
			 DepthGroup[] dgs = new  DepthGroup[1];
			 CountDownLatch downLatchEth = new CountDownLatch(1);
			 aexMarket.depth((dg,e)->{		
				 infolog("查询ETH_CNY深度",dg,e);
				 dgs[0] = dg;
				 downLatchEth.countDown();
			 }, TR.ETH_CNY);	
			 downLatchEth.await(2000, TimeUnit.MILLISECONDS);	
			 BigDecimal price = price(dgs[0],0,RoundingMode.DOWN);
			 BigDecimal count = cnyMin.divide(price, 6, RoundingMode.DOWN);
			 logger.error("开始挂单买入ETH_CNY, price:{} count:{}",price,count);
			 //开始挂单
			 CountDownLatch downLatch = new CountDownLatch(1);
			 aexMarket.sumbitOrder(TR.ETH_CNY,OrderType.Buy, price,count,(ol,error)->{
				 if(ol!=null){
					 logger.info("submitOrder BCX_CNY price:{} count:{} res:{}",price,count,JSONObject.toJSONString(ol));
				 }
				 if(error!=null){
					 logger.error("submitOrder BCX_CNY error:",error);
				 }
				 downLatch.countDown();
			 });
			 downLatch.await(2000, TimeUnit.MILLISECONDS);
		 }
	}
	
	private BigDecimal price(DepthGroup dg,int round,RoundingMode roundingMode) throws Exception{
		 Optional<DepthItem> buyPrice = dg.getBuy().stream().sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())).findFirst();
		 Optional<DepthItem> sellPrice = dg.getSell().stream().sorted((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())).findFirst();
		 if(!buyPrice.isPresent()|| !sellPrice.isPresent()){
			 throw new Exception("无法计算出深度数据:"+dg);
		 }
		return (buyPrice.get().getPrice().add(sellPrice.get().getPrice())).divide(BigDecimal.valueOf(2),round,roundingMode);
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
