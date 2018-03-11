package com.cat.fsai.cc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cat.fsai.error.ParamException;
import com.cat.fsai.error.SystemException;
import com.cat.fsai.inter.pojo.AllDepth;
import com.cat.fsai.type.TR;

/**
 * MarketService 交易市场数据获取
 * 
 * @author wangbo
 * @version Feb 17, 2018 8:15:37 PM
 */
@Service
public class MarketService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MarketFactory factroy;

	/**
	 * 获取所有深度数据
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public AllDepth getAllDepth(TR tr) {
		logger.info("===getAllDepth start tr:{}",tr);
		Collection<MarketApi> list = factroy.getMarketList();
		// 使用栅栏等待所有交易所交易数据返回
		CountDownLatch downLatch = new CountDownLatch(list.size());
		AllDepth res = new AllDepth(tr);
		LocalDateTime start = LocalDateTime.now();
		// 并发查询所有交易所深度数据
		list.parallelStream().forEach(market -> market.depth((dg, e) -> {
			if (dg != null){
				res.getAllDepth().put(market.CCtype(), dg);
				logger.info("{}获取{}交易数据成功,耗时:{}",market.CCtype().getCn(),tr,Duration.between(start, LocalDateTime.now()));
			}
			if (e != null){
				if(e instanceof ParamException){
					logger.info("{}交易对:{}当前不支持:{}",market.CCtype().getCn(),tr,e.getMessage());
				}else{
					logger.warn("{}获取{}交易数据异常", market.CCtype().getCn(),tr, e);				
				}
			}
			downLatch.countDown();
		}, tr)
		);
		boolean allgetintime=false; 
		try {
			allgetintime = downLatch.await(700, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			throw new SystemException("中断",e1);
		}
		logger.info("===获取所有交易所{}深度数据耗时:{} 是否全部按时返回:{}", tr,Duration.between(start, LocalDateTime.now()),allgetintime);
		return res;
	}
}
