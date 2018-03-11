/**
 * 
 */
package com.cat.fsai.inter.pojo;

import java.math.BigDecimal;

/**
 * AccountInfoItem
 * @author wangbo
 * @version Mar 4, 2018 6:39:19 PM
 */
public class AccountInfoItem {
	BigDecimal avail;
	BigDecimal freez;
	
	public BigDecimal getAvail() {
		return avail;
	}
	public void setAvail(BigDecimal avail) {
		this.avail = avail;
	}
	public BigDecimal getFreez() {
		return freez;
	}
	public void setFreez(BigDecimal freez) {
		this.freez = freez;
	}
	
	
}
