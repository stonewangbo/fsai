package com.cat.fsai;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.calc.CalcEarn;
import com.cat.fsai.cc.MarketService;
import com.cat.fsai.inter.pojo.AllDepth;
import com.cat.fsai.task.CCtask;
import com.cat.fsai.type.TR;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class CCtaskTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private CCtask cctask;
	
	@Autowired
	private MarketService marketService;
	
	@Autowired
	private CalcEarn calcEarn;

	@Test
	public void depth() throws Exception {		
		AllDepth allDepth = marketService.getAllDepth(TR.EOS_ETH);
		logger.info("===cc task 获取深度数据：{}",JSONObject.toJSONString(allDepth.getAllDepth()));		
		calcEarn.calc(allDepth, BigDecimal.valueOf(1));
	}
	
	@Test
	public void test() throws Exception {	
		//for(int i=0;i<3;i++){
			cctask.getDepth();
		//	logger.info("==== i:{} end",i);
		//}
	}

}
