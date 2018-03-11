package com.cat.fsai.type;

import java.math.BigDecimal;

/**
 * CC 支持的市场
 * @author wangbo
 * @version Feb 16, 2018 8:55:15 PM
 */
public enum CC {
	/** 火币 */
	Huobi("火币(huobi.pro)",0.002),
	/** 币安 */
	Binance("币安(binance.com)",0.001),
	/** Okex */
	Okex("(okex.com)",0.002),
	/**HitBTC */
	HitBTC("(hitbtc.com)",0.001),
	/**ZB */
	Zb("(zb.com)",0.001),
	/** 币创 */
	Bitz("币创(bit-z.com)",0.002),
	/** 比特时代*/
	Aex("比特时代(aex.com)",0.0005);
	
	final private String cn;
	
	/** 交易手续费*/
	final private BigDecimal fee;

	private CC(String cn,double fee) {
		this.cn = cn;
		this.fee = BigDecimal.valueOf(fee);
	}

	public String getCn() {
		return cn;
	}

	public BigDecimal getFee() {
		return fee;
	}	
	
	
}
