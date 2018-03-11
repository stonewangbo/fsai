/**
 * 
 */
package com.cat.fsai.inter;

import java.util.List;

import com.cat.fsai.inter.pojo.OrderItem;

/**
 * OrderListRes
 * @author wangbo
 * @version Mar 11, 2018 7:58:39 PM
 */
@FunctionalInterface
public interface OrderListRes {
	/**
	 * @param accountInfo
	 */
	void orderList(List<OrderItem> orderList,Exception e);
}
