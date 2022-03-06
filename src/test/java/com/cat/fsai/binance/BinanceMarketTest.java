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
import java.text.SimpleDateFormat;
import java.util.Date;
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
			 logger.info("dg:{} \r\n error:{}",dg,e);
			 downLatch.countDown();
		 }, TR.BTC_USDT);
		 downLatch.await(2, TimeUnit.SECONDS);		
	 }

	@Test
	public void kines() throws Exception{
		CountDownLatch downLatch = new CountDownLatch(1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTime = format.parse("2022-01-01 00:00:00");
		Date endTime = format.parse("2022-01-01 00:05:00");
		binanceMarket.klines(startTime,endTime, TR.BTC_USDT);
		downLatch.await(2, TimeUnit.SECONDS);
	}
	 

}
