package com.cat.fsai.inter.pojo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cat.fsai.type.CC;
import com.cat.fsai.type.TR;

/**
 * AllDepth 所有市场交易深度数据
 * @author wangbo
 * @version Feb 17, 2018 8:17:57 PM
 */
public class AllDepth {
	private Map<CC,DepthGroup> allDepth = Collections.synchronizedMap(new HashMap<>());
	
	private TR tr;

	
	

	public AllDepth(TR tr) {
		super();
		this.tr = tr;
	}

	public Map<CC, DepthGroup> getAllDepth() {
		return allDepth;
	}
	

	public TR getTr() {
		return tr;
	}



	public void setTr(TR tr) {
		this.tr = tr;
	}



	@Override
	public String toString() {
		return "AllDepth [allDepth=" + allDepth + "]";
	}
	
	
}
