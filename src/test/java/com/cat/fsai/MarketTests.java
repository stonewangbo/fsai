package com.cat.fsai;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.MarketService;
import com.cat.fsai.type.TR;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class MarketTests {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MarketService marketService;

	@Test
	public void depth() throws Exception {		
		logger.info("===res:{}",JSONObject.toJSONString(marketService.getAllDepth(TR.ETH_USDT)));
	}

	
}
