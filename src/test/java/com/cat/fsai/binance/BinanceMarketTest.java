package com.cat.fsai.binance;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.binance.BinanceMarket;
import com.cat.fsai.type.OrderType;
import com.cat.fsai.type.TR;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class BinanceMarketTest {
	 private Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	 @Autowired
	 private BinanceMarket binanceMarket;
	 
	 @Test
	 public void depth() throws InterruptedException{
		 CountDownLatch downLatch = new CountDownLatch(1);
		 binanceMarket.depth((dg, e)->{
			 logger.info("dg:{}",dg);
			 downLatch.countDown();
		 }, TR.BTC_USDT);
		 downLatch.await(2, TimeUnit.SECONDS);		
	 }
	 

}
