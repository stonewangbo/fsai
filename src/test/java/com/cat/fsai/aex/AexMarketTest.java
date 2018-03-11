package com.cat.fsai.aex;

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
		 }, TR.BCX_CNY);
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
		 downLatch.await(1000, TimeUnit.MILLISECONDS);
	 }
	 
}
