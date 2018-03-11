/**
 * 
 */
package com.cat.fsai.calc.pojo;

import java.math.BigDecimal;

/**
 * FeeInfo
 * @author wangbo
 * @version Feb 18, 2018 6:50:42 PM
 */
public class FeeInfo {
	final private BigDecimal minAmount;
	
	final private BigDecimal withdrawFee;

	public FeeInfo(double minAmount, double withdrawFee) {
		super();
		this.minAmount = BigDecimal.valueOf(minAmount);
		this.withdrawFee = BigDecimal.valueOf(withdrawFee);
	}

	public BigDecimal getMinAmount() {
		return minAmount;
	}

	public BigDecimal getWithdrawFee() {
		return withdrawFee;
	}
	
	
}
