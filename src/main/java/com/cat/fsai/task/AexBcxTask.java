/**
 * 
 */
package com.cat.fsai.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.cat.fsai.cc.aex.AexMarket;
import com.cat.fsai.error.BussException;
import com.cat.fsai.error.ParamException;
import com.cat.fsai.inter.pojo.AccountInfo;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.inter.pojo.OrderItem;
import com.cat.fsai.type.Coin;
import com.cat.fsai.type.OrderType;
import com.cat.fsai.type.TR;

/**
 * AexBcxTask
 * @author wangbo
 * @version Mar 11, 2018 6:45:43 PM
 */
@Component
public class AexBcxTask {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	static Map<TR,Long> trTime = new HashMap<>();
	
	@Autowired
	private AexMarket aexMarket;
	
	@Scheduled(fixedRate = 1000*60*3)
	public synchronized void bcxSell()  {	
		
		try{
			doTr(TR.BCX_CNC,OrderType.Sell,11,4,0,0.007);
		}catch(Exception e){
			logger.error("BCX_CNC出借",e);
		}
		try{
			doTr(TR.EOS_CNC,OrderType.Buy,10.5,1,6,0.007);
		}catch(Exception e){
			logger.error("买入EOS_CNC",e);
		}
		
		try{
			doTr(TR.BCX_CNY,OrderType.Sell,11,4,0,0.007);
		}catch(Exception e){
			logger.error("卖出BCX_CNY出错",e);
		}
		try{
			doTr(TR.ETH_CNY,OrderType.Buy,10.5,0,6,0.007);
		}catch(Exception e){
			logger.error("买入ETH_CNY",e);
		}
	}
	
	
	/**
	 * @param tr 交易对
	 * @param type 交易方向
	 * @param min 最小成交额
	 * @param priceRound 订单价格精度
	 * @param countRound 订单数量精度
	 * @param dis 订单溢价比例
	 * @throws Exception
	 */
	private void doTr(TR tr,OrderType type,double min,int priceRound,int countRound,double dis) throws Exception{
		 String str = "tr:"+tr+" type:"+type; 
		 //开始查询挂单
		 CountDownLatch cdOl = new CountDownLatch(1);
		 List<OrderItem> orderList = new ArrayList<>();
		 aexMarket.orderList(tr, (ol,e)->{
			 infolog("查询"+str+"挂单",ol,e);	
			 if(ol!=null){
				 orderList.addAll(ol);
			 }
			 cdOl.countDown();
		 });
		 if(!cdOl.await(9000, TimeUnit.MILLISECONDS))throw new BussException("查询挂单超时");
		 boolean[] hasOrder = new boolean[]{false};
		//取消长时间未成交订单
		 if(orderList.size()>0){
			 logger.info("{}查询到之前未成交的挂单{}条,判断挂单信息",str,orderList.size());
			 CountDownLatch cdoc = new CountDownLatch(orderList.size());
			 long now = System.currentTimeMillis();
			 //过滤超过7分钟仍未成交的挂单
			 orderList.stream().forEach(o->{
				 
				 if(o.getType()==OrderType.Sell && (now-getTime(tr))<1000*60*15){
					 hasOrder[0] = true;
					 logger.info("{}挂单 oderid:{} 目前时间{}秒 还未到取消时间范围",str, o.getOrderId(),(now-getTime(tr))/1000);
					 cdoc.countDown();		
				 }else if(o.getType()==OrderType.Buy && (now-getTime(tr))<1000*60*15) {
					 hasOrder[0] = true;
					 logger.info("{}挂单 oderid:{} 目前时间{}秒 还未到取消时间范围",str, o.getOrderId(),(now-getTime(tr))/1000);
					 cdoc.countDown();					
				 }else{
					 aexMarket.cancelOrder(o.getTr(), o.getOrderId(), (r,e)->{
						 infolog("撤销订单  OrderId:"+o.getOrderId(),r,e);							
						 cdoc.countDown();
					 });
				 }
			 });
			 if(!cdoc.await(5000, TimeUnit.MILLISECONDS))throw new BussException(str+"撤销订单超时");	
		 }
		 
		 //查询账户信息
		 AccountInfo[] infos  = new AccountInfo[1]; 
		 CountDownLatch cdai = new CountDownLatch(1);
		 aexMarket.accountInfo((info,error)->{
			 infolog("查询账户信息",info,error);
			 infos[0] = info;
			 cdai.countDown();
		 });
		 if(!cdai.await(5000, TimeUnit.MILLISECONDS))throw new BussException(str+"查询账户信息超时");
		 
		 if(hasOrder[0]){
			 logger.info("{} 当前有在时效范围内挂单,不进行交易",str);
			 return;
		 }
		//开始查询行情
		
		 //bcx 数量满足要求,开始计算价格
		 DepthGroup[] dgs = new  DepthGroup[1];
		 CountDownLatch downLatchBcx = new CountDownLatch(1);
		 aexMarket.depth((dg,e)->{		
			 infolog("查询BCX_CNY深度",dg,e);
			 dgs[0] = dg;
			 downLatchBcx.countDown();
		 }, tr);	
		 if(!downLatchBcx.await(2000, TimeUnit.MILLISECONDS))throw new BussException(str+"查询深度超时");	
		
		 BigDecimal price = price(dgs[0],priceRound,type,OrderType.Sell==type?RoundingMode.UP:RoundingMode.DOWN,BigDecimal.valueOf(dis));
		 BigDecimal count = BigDecimal.valueOf(min).divide(price, countRound, RoundingMode.DOWN);
		 Coin targetCoin = OrderType.Sell==type?tr.getLeft():tr.getRight();		
		 BigDecimal minCoin = infos[0].getInfoMap().get(targetCoin).getAvail();
		 //根据行情和账户信息进行挂单
		 if(OrderType.Sell==type?minCoin.compareTo(count)<0:minCoin.compareTo(BigDecimal.valueOf(min))<0){
			 logger.info("{} 账户余额:{} 不足,不进行交易",str,minCoin);
			 return;
		 }
			
		 logger.error("{}开始挂单, price:{} count:{}",str,price,count);
		 //开始挂单
		 CountDownLatch downLatch = new CountDownLatch(1);
		 aexMarket.sumbitOrder(tr,type, price,count,(ol,error)->{
			 if(ol!=null){
				 logger.info("{} submitOrder price:{} res:{}",str,price,JSONObject.toJSONString(ol));
				 trTime.put(tr, System.currentTimeMillis());
			 }
			 if(error!=null){
				 logger.error("{} submitOrder BCX_CNY error:",str,error);
			 }
			 downLatch.countDown();
		 });
		 if(!downLatch.await(9000, TimeUnit.MILLISECONDS))throw new BussException(str+"挂单超时");			 
	}
	
	private long getTime(TR tr){
		Long res = trTime.get(tr);
		if(res==null)res = 0l;
		return res;
	}
	
	private BigDecimal price(DepthGroup dg,int round,OrderType orderType,RoundingMode roundingMode,BigDecimal dis) throws Exception{
		 //排序获取最高买入价格,最低卖出价格
		 Optional<DepthItem> buyPrice = dg.getBuy().stream().sorted((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())).findFirst();
		 Optional<DepthItem> sellPrice = dg.getSell().stream().sorted((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())).findFirst();
		 if(!buyPrice.isPresent()|| !sellPrice.isPresent()){
			 throw new Exception("无法计算出深度数据:"+dg);
		 }		
		
		 //原取深度中间值算法,废弃,改为取深度当前方向,加零头算法
		 BigDecimal  midPrice = (buyPrice.get().getPrice().add(sellPrice.get().getPrice())).divide(BigDecimal.valueOf(2));
		 switch(orderType){
			case Buy:
				return midPrice.multiply(BigDecimal.ONE.subtract(dis)).setScale(round, roundingMode);			
			case Sell:
				return midPrice.multiply(BigDecimal.ONE.add(dis)).setScale(round, roundingMode);			
			default:
				throw new ParamException("orderType:"+orderType+" 不支持");
		 }
	}
	
	private void infolog(String title,Object obj,Exception e){
		if(obj!=null){
			logger.info("{} res:{}",title,JSONObject.toJSONString(obj));
		}
		if(e!=null){
			logger.warn("{} error:",title,e);
		}
	}
}
