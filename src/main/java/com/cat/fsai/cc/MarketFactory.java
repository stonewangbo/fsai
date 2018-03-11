package com.cat.fsai.cc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.cat.fsai.type.CC;

/**
 * MarketFactory 交易所工厂
 * @author wangbo
 * @version Feb 16, 2018 10:17:16 PM
 */
@Service
public class MarketFactory  implements ApplicationContextAware{
	/** 缓存*/
	private Map<CC,MarketApi> marketMap;
	
	private ApplicationContext applicationContext;
	
	@PostConstruct
	public void init(){
		marketMap = new HashMap<>();
		Map<String, MarketApi> beanMap = applicationContext.getBeansOfType(MarketApi.class);
		beanMap.values().stream().forEach(ma->marketMap.put(ma.CCtype(), ma));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;		
	}	

	/**
	 * 获取所有交易所
	 * @return
	 */
	public Collection<MarketApi> getMarketList(){
		return marketMap.values();
	}
	
	/**
	 * 获取特定交易所
	 * @param cc
	 * @return
	 */
	public MarketApi getMarketByCC(CC cc){
		return marketMap.get(cc);
	}

}
