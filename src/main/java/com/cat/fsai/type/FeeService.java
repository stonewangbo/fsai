package com.cat.fsai.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cat.fsai.calc.pojo.FeeInfo;
import com.cat.fsai.error.ParamException;

/**
 * FeeService 手续费
 * @author wangbo
 * @version Feb 18, 2018 6:39:44 PM
 */
@Service
public class FeeService {
	/** 提现首先手续费*/
	static Map<CC,Map<Coin,FeeInfo>> withdrawFee;
	static{
		withdrawFee = new HashMap<>();
		//火币
		Map<Coin,FeeInfo> huobi = new HashMap<>();
		withdrawFee.put(CC.Huobi, huobi);
		huobi.put(Coin.USDT, new FeeInfo(200,20));
		huobi.put(Coin.BTC, new FeeInfo(0.01,0.001));
		huobi.put(Coin.ETH, new FeeInfo(0.015,0.01));
		huobi.put(Coin.EOS, new FeeInfo(1.5,0.5));
		//币安
		Map<Coin,FeeInfo> binance = new HashMap<>();
		withdrawFee.put(CC.Binance, binance);
		binance.put(Coin.USDT, new FeeInfo(100,2));
		binance.put(Coin.BTC, new FeeInfo(0.002,0.001));
		binance.put(Coin.ETH, new FeeInfo(0.1,0.01));
		binance.put(Coin.EOS, new FeeInfo(1.4,1));
		//币创
		Map<Coin,FeeInfo> bitz = new HashMap<>();
		withdrawFee.put(CC.Bitz, bitz);
		bitz.put(Coin.USDT, new FeeInfo(100,30));
		//bitz.put(Coin.BTC, new FeeInfo(0.002,0.001)); 比特币是比例收取
		bitz.put(Coin.ETH, new FeeInfo(0.1,0.01));
		bitz.put(Coin.EOS, new FeeInfo(1.4,1));
	}
	
	/**
	 * 提现手续费
	 * @param cc
	 * @param coin
	 * @return
	 */
	public FeeInfo getWithdrawFee(CC cc,Coin coin){
		if(!withdrawFee.containsKey(cc)){
			throw new ParamException("交易所:"+cc+" 未配置任何手续费信息");
		}
		Map<Coin,FeeInfo> coinMap = withdrawFee.get(cc);
		if(!coinMap.containsKey(coin)){
			throw new ParamException("交易所:"+cc+" 未配置或不支持"+coin+"提现");
		}	
		return coinMap.get(coin);
	}
}
