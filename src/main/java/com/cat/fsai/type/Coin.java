package com.cat.fsai.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Coin 币种类型
 * @author wangbo
 * @version Feb 18, 2018 6:26:33 PM
 */
public enum Coin {
	ETH,
	ETC,
	LTC,
	ZEC,
	BTC,
	BTN,
	BCX,
	EOS,
	XRP,
	HSR,
	NEO,
	USDT,
	QC,
	BitCNY,
	CNC;
	
	static final Map<String,Coin> map = new HashMap<>();
	
	static{
		Arrays.stream(Coin.values()).forEach(item->map.put(item.name().toUpperCase(), item));
	}
	
	static public Optional<Coin> find(String name){
		return Optional.ofNullable(map.get(name.toUpperCase()));
	}
}
