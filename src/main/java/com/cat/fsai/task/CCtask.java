package com.cat.fsai.task;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cat.fsai.calc.CalcEarn;
import com.cat.fsai.cc.MarketService;
import com.cat.fsai.inter.pojo.AllDepth;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.TR;

@Component
public class CCtask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MarketService marketService;

	@Autowired
	private CalcEarn calcEarn;

	@Scheduled(fixedRate = 1000*13)
	public synchronized void getDepth() throws InterruptedException {
		Stream.of(TR.values()).filter(tr -> tr.getRight()!=Coin.USDT
				&&tr.getRight()!=Coin.QC
				&&tr.getRight()!=Coin.BitCNY).sequential()
				.forEach(tr -> {
					try {
						AllDepth allDepth = marketService.getAllDepth(tr);
						logger.info("===cc task 获取深度数据：{}", allDepth.getAllDepth().size());
						calcEarn.calc(allDepth, BigDecimal.ONE);
					} catch (Exception e) {
						logger.warn("计算交易对:{} 发生错误:", tr, e);
					}
				});

	}

}
