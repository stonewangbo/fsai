package com.cat.fsai.type;

/**
 * 交易对
 * 
 * @author wangbo
 * @date 2/15/2018
 * @description
 */
public enum TR {
	ETH_USDT(Coin.ETH, Coin.USDT), 
	BTC_USDT(Coin.BTC, Coin.USDT),
	LTC_USDT(Coin.LTC, Coin.USDT),
	ZEC_USDT(Coin.ZEC, Coin.USDT),
	EOS_USDT(Coin.EOS, Coin.USDT),
	XRP_USDT(Coin.XRP, Coin.USDT),
	BTN_USDT(Coin.BTN, Coin.USDT),
	EOS_QC(Coin.EOS, Coin.QC),
	XRP_QC(Coin.XRP, Coin.QC),
	BTN_QC(Coin.BTN, Coin.QC),
	BCX_CNY(Coin.BCX, Coin.BitCNY),
	ETH_CNY(Coin.ETH, Coin.BitCNY),
	ETH_BTC(Coin.ETH, Coin.BTC), 
	EOS_BTC(Coin.EOS,Coin.BTC),
	XRP_BTC(Coin.XRP,Coin.BTC),
	BTN_BTC(Coin.BTN,Coin.BTC),
	ETC_ETH(Coin.ETC, Coin.ETH),
	EOS_ETH(Coin.EOS, Coin.ETH),
	HSR_ETH(Coin.HSR, Coin.ETH),
	XRP_ETH(Coin.XRP, Coin.ETH),
	NEO_ETH(Coin.NEO, Coin.ETH);

	final private Coin left;
	final private Coin right;

	private TR(Coin left, Coin right) {
		this.left = left;
		this.right = right;
	}

	public Coin getLeft() {
		return left;
	}

	public Coin getRight() {
		return right;
	}

}
