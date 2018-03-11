package com.cat.fsai.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.zb.ZbMarket;
import com.cat.fsai.error.BussException;
import com.cat.fsai.inter.pojo.AccountInfo;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.TR;

@Component
public class ZbBtnTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ZbMarket zbMarket;
	
	@SuppressWarnings("serial")
	//@Scheduled(fixedRate = 1000*60*5)
	public synchronized void btnSell() throws Exception {
		zbSellBuy(Coin.BTN,Coin.EOS,BigDecimal.valueOf(10));
		zbSellBuy(Coin.BTN,Coin.XRP,BigDecimal.valueOf(10));
	}
	
	public void zbSellBuy(Coin sell,Coin buy,BigDecimal sellCount) throws Exception{
		TR[] trList= Arrays.stream(TR.values()).filter(tr->tr.getLeft()==sell||tr.getLeft()==buy).toArray(TR[]::new);
				//Arrays.asList(TR.BTN_BTC,TR.BTN_QC,TR.BTN_USDT,TR.EOS_BTC,TR.EOS_QC,TR.EOS_USDT);
		CountDownLatch downLatch = new CountDownLatch(trList.length);
		Map<TR,BigDecimal> vMap = new HashMap<>(); 
		Arrays.stream(trList).forEach(tr->zbMarket.depth((dg,e)->{
			if(e!=null){
				logger.warn("获取深度数据失败tr:{} {}",tr,e.getMessage());
				downLatch.countDown();
				return;
			}
			//处理深度数据,返回需要的
			if(tr.getLeft()==sell){
				//获取卖出价格
				Optional<DepthItem> sellPrice = dg.getBuy().stream().sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())).findFirst();
				if(!sellPrice.isPresent()){
					logger.warn("深度不足 tr:{} dg:{}",tr,dg);					
				}else{
					logger.info("tr:{} 获取卖出价格:{} 整体深度:{}",tr,sellPrice.get().getPrice(), dg.getBuy());
					vMap.put(tr, sellPrice.get().getPrice());
				}
			}
			//处理深度数据,返回需要的
			if(tr.getLeft()==buy){
				//获取买入价格
				Optional<DepthItem> sellPrice = dg.getSell().stream().sorted((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())).findFirst();
				if(!sellPrice.isPresent()){
					logger.warn("深度不足 tr:{} dg:{}",tr,dg);					
				}else{
					logger.info("tr:{} 获取买入价格:{} 整体深度:{}",tr,sellPrice.get().getPrice(), dg.getSell());
					vMap.put(tr, sellPrice.get().getPrice());
				}
			}
			downLatch.countDown();
		},tr));
		
		downLatch.await(5, TimeUnit.SECONDS);		
		Map<Coin,BigDecimal> tempCoin = new HashMap<Coin,BigDecimal>(){{put(Coin.QC, null);put(Coin.BTC, null);put(Coin.USDT, null);}};
		tempCoin.entrySet().stream().forEach(et->{
			    BigDecimal[] arry = new BigDecimal[2];
				vMap.entrySet().stream().filter(it->it.getKey().getRight()==et.getKey())
				.forEach(it2->{
					if(it2.getKey().getLeft()==sell)arry[0]=it2.getValue();
					if(it2.getKey().getLeft()==buy)arry[1]=it2.getValue();
							});
				if(arry[0]==null)throw new BussException("sell:"+sell+" buy:"+buy+" et:"+et+" vMap:"+vMap+" sell未获取到");
				if(arry[1]==null)throw new BussException("sell:"+sell+" buy:"+buy+" et:"+et+" vMap:"+vMap+" buy未获取到");
				tempCoin.put(et.getKey(), arry[0].divide(arry[1], 10, RoundingMode.HALF_UP));
			});	
		
		logger.info("tempCoin:{}",tempCoin);
		Optional<Entry<Coin, BigDecimal>> res= tempCoin.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).findFirst();
		logger.info("sell:{} buy:{} res:{}",sell,buy,res.get());		
		
		AccountInfo info =  accountinfo();
		
		
	}
	
	private AccountInfo accountinfo() throws Exception{
		 AccountInfo[] res =new AccountInfo[1];
		 Exception[] e = new Exception[1];
		 //判断账户余额
		 CountDownLatch downLatch = new CountDownLatch(1);
		 zbMarket.accountInfo((info,error)->{			
			 if(info!=null){
				 logger.info("accountInfo:{}",JSONObject.toJSONString(info));
				 res[0] = info;
			 }
			 if(error!=null){
				e[0] = error;
			 }
			 downLatch.countDown();
		 });
		 downLatch.await(1000, TimeUnit.MILLISECONDS);
		 if(e[0]!=null){
			 throw e[0];
		 }
		 return res[0];
	}
}
