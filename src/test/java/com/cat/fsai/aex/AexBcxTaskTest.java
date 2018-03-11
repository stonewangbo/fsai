/**
 * 
 */
package com.cat.fsai.aex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cat.fsai.task.AexBcxTask;

/**
 * AexBcxTaskTest
 * @author wangbo
 * @version Mar 11, 2018 11:33:06 PM
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class AexBcxTaskTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	 @Autowired
	 private AexBcxTask aexBcxTask;
	 
	 @Test
	 public void task() throws Exception {
		 aexBcxTask.bcxSell();
	 }
	
}
