package com.cat.fsai.type.tr;

import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;

/**
 * BinanceTR 币安交易对
 * @author wangbo
 * @version Feb 16, 2018 9:12:28 PM
 */
public enum BinanceTR {
	ETHUSDT(TR.ETH_USDT),	
	BTCUSDT(TR.BTC_USDT),
	LTCUSDT(TR.LTC_USDT),	
	ETHBTC(TR.ETH_BTC),
	ETCETH(TR.ETC_ETH),
	EOSETH(TR.EOS_ETH),
	HSRETH(TR.HSR_ETH),
	XRPETH(TR.XRP_ETH),
	NEOETH(TR.NEO_ETH);

    private TR tr;

    private BinanceTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<BinanceTR> searchByTr(TR tr){       
        return Arrays.stream(BinanceTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
