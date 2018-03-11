package com.cat.fsai.inter.pojo;

import java.math.BigDecimal;



/**
 * 深度数据明细
 * @author wangbo
 * @version
 */
public class DepthItem {
	
	private BigDecimal price;

	private BigDecimal amount;

	
	
	public DepthItem(BigDecimal price, BigDecimal amount) {
		super();
		this.price = price;
		this.amount = amount;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "DepthItem [price=" + price + ", amount=" + amount + "]";
	}
	
	
}
