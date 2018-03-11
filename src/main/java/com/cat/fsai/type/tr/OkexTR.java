package com.cat.fsai.type.tr;


import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;

/**
 * 火币交易对
 * @author: wangbo
 * @date: 2/15/2018
 * @description:
 */
public enum OkexTR {
    eth_usdt(TR.ETH_USDT),
    btc_usdt(TR.BTC_USDT),
    ltc_usdt(TR.LTC_USDT),
    zec_usdt(TR.ZEC_USDT),
    eth_btc(TR.ETH_BTC),
	etc_eth(TR.ETC_ETH),
    eos_eth(TR.EOS_ETH),
    hsr_eth(TR.HSR_ETH),
    xpr_eth(TR.XRP_ETH),
    neo_eth(TR.NEO_ETH);


    private TR tr;

    private OkexTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<OkexTR> searchByTr(TR tr){       
        return Arrays.stream(OkexTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
