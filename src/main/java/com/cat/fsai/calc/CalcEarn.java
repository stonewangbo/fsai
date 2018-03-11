package com.cat.fsai.calc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cat.fsai.error.BussException;
import com.cat.fsai.inter.pojo.AllDepth;
import com.cat.fsai.type.CC;

/**
 * CalcEarn 计算利润
 * 
 * @author wangbo
 * @version Feb 18, 2018 12:12:54 PM
 */
@Service
public class CalcEarn {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * @param allDepth
	 * @param amount
	 */
	public void calc(AllDepth allDepth, BigDecimal amount) {
		Map<CC, BigDecimal> buyAmount = Collections.synchronizedMap(new HashMap<>());
		Map<CC, BigDecimal> buyPrice = Collections.synchronizedMap(new HashMap<>());
		Map<CC, BigDecimal> sellAmount = Collections.synchronizedMap(new HashMap<>());
		Map<CC, BigDecimal> sellPrice = Collections.synchronizedMap(new HashMap<>());

		// 计算各平台实际能成交的价格
		allDepth.getAllDepth().entrySet().parallelStream().forEach(dp -> {
			buyAmount.put(dp.getKey(), BigDecimal.ZERO);
			dp.getValue().getBuy().stream().sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())).forEach(di -> {
				if (buyPrice.containsKey(dp.getKey()))
					return;
				// 计算当前深度是否可以购买
				buyAmount.put(dp.getKey(), buyAmount.get(dp.getKey()).add(di.getAmount()));
				// 满足购买数量要求
				if (buyAmount.get(dp.getKey()).compareTo(amount) >= 0) {
					buyPrice.put(dp.getKey(), di.getPrice());
				}
			});
			;
			sellAmount.put(dp.getKey(), BigDecimal.ZERO);
			dp.getValue().getSell().stream().sorted((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())).forEach(di -> {
				if (sellPrice.containsKey(dp.getKey()))
					return;
				// 计算当前深度是否可以售出
				sellAmount.put(dp.getKey(), sellAmount.get(dp.getKey()).add(di.getAmount()));
				// 满足售出数量要求
				if (sellAmount.get(dp.getKey()).compareTo(amount) >= 0)
					sellPrice.put(dp.getKey(), di.getPrice());
			});
			;
		});

		// 开始计算，获取买单出价最高的平台
		Optional<Entry<CC, BigDecimal>> highBuy = buyPrice.entrySet().stream()
				.sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).findFirst();
		if (!highBuy.isPresent()) {
			throw new BussException("未计算出最高买入订单价格,可能市场深度不足");
		}
		// 获取最低价格售出平台
		Optional<Entry<CC, BigDecimal>> lowSell = sellPrice.entrySet().stream()
				.sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue())).findFirst();
		if (!lowSell.isPresent()) {
			throw new BussException("未计算出最低售出价格,可能市场深度不足");
		}
		BigDecimal earn = highBuy.get().getValue().subtract(lowSell.get().getValue());
		// 利润高于平台手续费
		if (earn.compareTo(BigDecimal.ZERO) > 0
				&& (earn.divide(highBuy.get().getValue(), 10, BigDecimal.ROUND_HALF_DOWN)).compareTo((lowSell.get()
						.getKey().getFee().add(highBuy.get().getKey().getFee())).add(BigDecimal.valueOf(0.003))) > 0) {
			logger.error("tr:{}收益预测,数量:{} 购买平台:{} 价格:{} 售出平台:{} 价格:{} 差价:{} 比例:{}%", allDepth.getTr(),
					amount, lowSell.get().getKey().getCn(), lowSell.get().getValue(), highBuy.get().getKey().getCn(),
					highBuy.get().getValue(), earn,
					(earn.divide(highBuy.get().getValue(), 10, BigDecimal.ROUND_HALF_DOWN))
							.multiply(BigDecimal.valueOf(100)));
		}

		logger.info("tr:{}收益预测,数量:{} 购买平台:{} 价格:{} 售出平台:{} 价格:{} 差价:{} 比例:{}%", allDepth.getTr(), amount,
				lowSell.get().getKey().getCn(), lowSell.get().getValue(), highBuy.get().getKey().getCn(),
				highBuy.get().getValue(), earn,
				(earn.divide(highBuy.get().getValue(), 10, BigDecimal.ROUND_HALF_DOWN))
				.multiply(BigDecimal.valueOf(100)));
	}
}
