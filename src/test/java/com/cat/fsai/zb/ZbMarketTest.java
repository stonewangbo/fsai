/**
 * 
 */
package com.cat.fsai.zb;

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
import com.cat.fsai.cc.zb.ZbMarket;

/**
 * ZbMarketTest
 * @author wangbo
 * @version Mar 4, 2018 6:25:07 PM
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class ZbMarketTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	 @Autowired
	 private ZbMarket zbMarket;
	
	 @Test
	 public void accountInfo() throws InterruptedException {
		 CountDownLatch downLatch = new CountDownLatch(1);
		 zbMarket.accountInfo((info,error)->{
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
