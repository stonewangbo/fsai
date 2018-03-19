package com.cat.fsai.aex;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.aex.AexMarket;
import com.cat.fsai.type.OrderType;
import com.cat.fsai.type.TR;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class AexMarketTest {
	 private Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	 @Autowired
	 private AexMarket aexMarket;
	 
	 @Test
	 public void depth() throws InterruptedException{
		 CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.depth((dg,e)->{
			 logger.info("dg:{}",dg);
			 downLatch.countDown();
		 }, TR.BCX_CNC);
		 downLatch.await(2, TimeUnit.SECONDS);		
	 }
	 
	 @Test
	 public void accountInfo() throws InterruptedException {
		 CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.accountInfo((info,error)->{
			 if(info!=null){
				 logger.info("accountInfo:{}",JSONObject.toJSONString(info));
			 }
			 if(error!=null){
				 logger.error("accountInfo error:",error);
			 }
			 downLatch.countDown();
		 });
		 downLatch.await(2000, TimeUnit.MILLISECONDS);
	 }
	 
	 @Test
	 public void orderList() throws InterruptedException {
		 CountDownLatch downLatch = new CountDownLatch(1);
		 long now = System.currentTimeMillis();
		 aexMarket.orderList(TR.BCX_CNC, (ol,error)->{
			 if(ol!=null){
				 logger.info("orderList:{}",JSONObject.toJSONString(ol));
			 }
			 if(error!=null){
				 logger.error("orderList error:",error);
			 }
			 //过滤超过十分钟仍未成交的挂单
			 ol.stream().forEach(o->{
				 logger.info("diff :{}s",(now-o.getTime().getTime())/1000);
					 
			 });
			 downLatch.countDown();
		 });
		 downLatch.await(2000, TimeUnit.MILLISECONDS);
	 }
	 
	 @Test
	 public void submitOrder() throws InterruptedException {
		 CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.sumbitOrder(TR.BCX_CNC,OrderType.Sell, BigDecimal.valueOf(0.0125),BigDecimal.valueOf(1000),(ol,error)->{
			 if(ol!=null){
				 logger.info("submitOrder:{}",JSONObject.toJSONString(ol));
			 }
			 if(error!=null){
				 logger.error("submitOrder error:",error);
			 }
			 downLatch.countDown();
		 });
		 downLatch.await(2000, TimeUnit.MILLISECONDS);
	 }
	 
	 @Test
	 public void cancelOrder() throws InterruptedException {
		 CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.cancelOrder(TR.ETH_CNY,"517588", (ol,error)->{
			 if(ol!=null){
				 logger.info("orderList:{}",JSONObject.toJSONString(ol));
			 }
			 if(error!=null){
				 logger.error("orderList error:",error);
			 }
			 downLatch.countDown();
		 });
		 downLatch.await(2000, TimeUnit.MILLISECONDS);
	 }
	 
}
