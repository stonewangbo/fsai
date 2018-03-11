package com.cat.fsai.type.tr;

import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;

/**
 * bit-z 币安交易对
 * @author wangbo
 * @version Feb 16, 2018 9:12:28 PM
 */
public enum BitzTR {
	eth_usdt(TR.ETH_USDT),
	btc_usdt(TR.BTC_USDT);

    private TR tr;

    private BitzTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<BitzTR> searchByTr(TR tr){      
    	//return Optional.empty();
        return Arrays.stream(BitzTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
