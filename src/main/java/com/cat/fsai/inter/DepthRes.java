package com.cat.fsai.inter;

import com.cat.fsai.inter.pojo.DepthGroup;

/**
 * DepthRes
 * @author wangbo
 * @version Feb 15, 2018 9:06:46 PM
 */
@FunctionalInterface
public interface DepthRes {
	
	/**
	 * 获得深度数据
	 * @param depthGroup
	 */
	void depth(DepthGroup depthGroup,Exception e);
}
