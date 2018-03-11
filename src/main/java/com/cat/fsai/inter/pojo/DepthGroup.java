package com.cat.fsai.inter.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 深度数据
 * @author wangbo
 */
public class DepthGroup {
	/** 卖出深度数据*/
	private List<DepthItem> sell = new ArrayList<>();;
	/** 购买深度数据*/
	private List<DepthItem> buy = new ArrayList<>();
	
	
	public List<DepthItem> getSell() {
		return sell;
	}
	public void setSell(List<DepthItem> sell) {
		this.sell = sell;
	}
	public List<DepthItem> getBuy() {
		return buy;
	}
	public void setBuy(List<DepthItem> buy) {
		this.buy = buy;
	}
	@Override
	public String toString() {
		return "DepthGroup [sell=" + sell + ", buy=" + buy + "]";
	}
	
	
}
