package com.cat.fsai;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class CompTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void test(){
		List<Integer> list= Arrays.asList(2,1,5,4);
		logger.info("====start:{}",JSONObject.toJSONString(list));
		list.stream().sorted((o1,o2)->o1.compareTo(o2)).forEach(i->logger.info("{}",i));
		//logger.info("order:{}",JSONObject.toJSONString(res));
	}
}
