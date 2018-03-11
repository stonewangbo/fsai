package com.cat.fsai.type.tr;


import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;


/**
 * HitbtcTR交易对
 * @author wangbo
 * @version Feb 20, 2018 9:25:34 PM
 */
public enum HitbtcTR {
    ETHUSD(TR.ETH_USDT),
    BTCUSD(TR.BTC_USDT),
    LTCUSD(TR.LTC_USDT),
    ZECUSD(TR.ZEC_USDT),
    ETHBTC(TR.ETH_BTC),
	ETCETH(TR.ETC_ETH),
    EOSETH(TR.EOS_ETH),  
    XRPETH(TR.XRP_ETH),
    NEOETH(TR.NEO_ETH);


    private TR tr;

    private HitbtcTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<HitbtcTR> searchByTr(TR tr){       
        return Arrays.stream(HitbtcTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
