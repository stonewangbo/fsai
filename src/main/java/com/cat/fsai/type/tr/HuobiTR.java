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
public enum HuobiTR {
    ethusdt(TR.ETH_USDT),
    btcusdt(TR.BTC_USDT),
    ltcusdt(TR.LTC_USDT),
    zecusdt(TR.ZEC_USDT),
    ethbtc(TR.ETH_BTC),
    hsreth(TR.HSR_ETH),
    eoseth(TR.EOS_ETH);


    private TR tr;

    private HuobiTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<HuobiTR> searchByTr(TR tr){       
        return Arrays.stream(HuobiTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
