package com.cat.fsai.inter;

import com.cat.fsai.inter.pojo.AccountInfo;

/**
 * AccountRes
 * @author wangbo
 * @version Mar 4, 2018 6:38:07 PM
 */
@FunctionalInterface
public interface AccountRes {
	/**
	 * @param accountInfo
	 */
	void accountInfo(AccountInfo accountInfo,Exception e);
}
