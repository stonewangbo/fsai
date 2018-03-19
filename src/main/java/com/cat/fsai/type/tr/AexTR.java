package com.cat.fsai.type.tr;


import java.util.Arrays;
import java.util.Optional;

import com.cat.fsai.type.TR;


/**
 * HitbtcTR交易对
 * @author wangbo
 * @version Feb 20, 2018 9:25:34 PM
 */
public enum AexTR {
	ETH_CNY(TR.ETH_CNY),
	BCX_CNC(TR.BCX_CNC),
	ETH_CNC(TR.ETH_CNC),
	EOS_CNC(TR.EOS_CNC);

    private TR tr;

    private AexTR(TR tr) {
        this.tr = tr;
    }

    public static Optional<AexTR> searchByTr(TR tr){       
        return Arrays.stream(AexTR.values())
        		.filter(htr-> htr.tr==tr).findFirst();
    }
}
