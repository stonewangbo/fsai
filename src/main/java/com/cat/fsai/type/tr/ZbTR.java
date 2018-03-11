package com.cat.fsai.type.tr;


import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;


/**
 * HitbtcTR交易对
 * @author wangbo
 * @version Feb 20, 2018 9:25:34 PM
 */
public enum ZbTR {
	eth_usdt(TR.ETH_USDT),
	btc_usdt(TR.BTC_USDT),
	ltc_usdt(TR.LTC_USDT),   
	eos_usdt(TR.EOS_USDT),   
	xrp_usdt(TR.XRP_USDT),   
	btn_usdt(TR.BTN_USDT),   
	eos_qc(TR.EOS_QC),   
	xrp_qc(TR.XRP_QC),   
	btn_qc(TR.BTN_QC),   
	eth_btc(TR.ETH_BTC),
	btn_btc(TR.BTN_BTC),
	xrp_btc(TR.XRP_BTC),
	eos_btc(TR.EOS_BTC);
	


    private TR tr;

    private ZbTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<ZbTR> searchByTr(TR tr){       
        return Arrays.stream(ZbTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
