package com.cat.fsai.cc;

import com.cat.fsai.inter.DepthRes;
import com.cat.fsai.type.CC;
import com.cat.fsai.type.TR;

/**
 * MarketApi 交易所统一接口
 * @author wangbo
 * @version Feb 16, 2018 9:51:41 PM
 */
public interface MarketApi {	
	
	/**
	 * 获取市场深度
	 * @param depthInfo
	 * @param tr
	 */
	void depth(DepthRes depthInfo,TR tr);
	
	/**
	 * 交易所
	 * @return
	 */
	CC CCtype();
}
