package com.cat.fsai.zb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cat.fsai.task.ZbBtnTask;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ZbBtnTaskTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	 @Autowired
	 private ZbBtnTask zbBtnTask;
	 
	 @Test
	 public void btnSell() throws Exception {
		 zbBtnTask.btnSell();
	 }
}
