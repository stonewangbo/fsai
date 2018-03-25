package com.cat.fsai.task.pojo;

import java.math.BigDecimal;

public class TaskInfo {
	private Long time;
	private BigDecimal price;
	public TaskInfo(Long time, BigDecimal price) {
		super();
		this.time = time;
		this.price = price;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	
}
