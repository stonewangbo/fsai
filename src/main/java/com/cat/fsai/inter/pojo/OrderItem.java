package com.cat.fsai.inter.pojo;

import java.math.BigDecimal;
import java.util.Date;

import com.cat.fsai.type.OrderType;
import com.cat.fsai.type.TR;

/**
 * OrderItem
 * @author wangbo
 * @version Mar 11, 2018 8:00:30 PM
 */
public class OrderItem {
	/** id*/
	private String orderId;
	/** */
	private OrderType type;
	/** 交易类型*/
	private TR tr;
	/** 数量*/
	private BigDecimal amount;
	/** 价格*/
	private BigDecimal price;
	/** 下单时间*/
	private Date time;
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public OrderType getType() {
		return type;
	}
	public void setType(OrderType type) {
		this.type = type;
	}
	public TR getTr() {
		return tr;
	}
	public void setTr(TR tr) {
		this.tr = tr;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "OrderItem [orderId=" + orderId + ", type=" + type + ", tr=" + tr + ", amount=" + amount + ", price="
				+ price + "]";
	}
	
	
}
