/**
 * 
 */
package com.cat.fsai.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cat.fsai.cc.aex.AexMarket;
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
	
	@Scheduled(fixedRate = 1000*60*5)
	public synchronized void bcxSell() throws Exception {
		//开始查询行情
		CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.depth((dg,e)->{
			 logger.info("dg:{}",dg);
			 downLatch.countDown();
		 }, TR.BCX_CNY);
		 downLatch.await(2, TimeUnit.SECONDS);		
	}
}
