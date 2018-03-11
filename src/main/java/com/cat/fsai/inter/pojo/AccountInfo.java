/**
 * 
 */
package com.cat.fsai.inter.pojo;

import java.util.HashMap;
import java.util.Map;

import com.cat.fsai.type.Coin;

/**
 * AccountInfo
 * @author wangbo
 * @version Mar 4, 2018 6:39:01 PM
 */
public class AccountInfo {
	Map<Coin,AccountInfoItem> infoMap = new HashMap<>();

	public Map<Coin, AccountInfoItem> getInfoMap() {
		return infoMap;
	}
	
}
