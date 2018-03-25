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
import com.cat.fsai.inter.pojo.AsyncObject;
import com.cat.fsai.inter.pojo.DepthGroup;
import com.cat.fsai.inter.pojo.DepthItem;
import com.cat.fsai.inter.pojo.OrderItem;
import com.cat.fsai.task.pojo.TaskInfo;
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
	
	static Map<TR,TaskInfo> trTime = new HashMap<>();
	
	@Autowired
	private AexMarket aexMarket;
	
	@Scheduled(fixedRate = 1000*60*3)
	public synchronized void bcxSell()  {	
		
		try{
			doTr(TR.BCX_CNC,OrderType.Sell,11,4,0,0.0000);
		}catch(Exception e){
			logger.error("卖出BCX_CNC",e);
		}
		try{
			doTr(TR.EOS_CNC,OrderType.Buy,10.5,1,6,0.001);
		}catch(Exception e){
			logger.error("买入EOS_CNC",e);
		}
		
//		try{
//			doTr(TR.ETH_CNC,OrderType.Buy,10.5,0,6,0.007);
//		}catch(Exception e){
//			logger.error("买入ETH_CNC",e);
//		}
		
//		try{
//			doTr(TR.BCX_CNY,OrderType.Sell,11,4,0,0.007);
//		}catch(Exception e){
//			logger.error("卖出BCX_CNY出错",e);
//		}

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
		 
		 //开始查询行情
			
		 //开始计算价格		
		 AsyncObject<DepthGroup> dgs = new  AsyncObject<>(str+"查询市场深度",1,null);		
		 aexMarket.depth((dg,e)->{	
			 dgs.setE(e);
			 dgs.setObj(dg);
			 dgs.getCdl().countDown();;
		 }, tr);					
		 infolog(dgs.getName(),dgs.waitAndGet(2000));
		
		 BigDecimal price = price(dgs.getObj(),priceRound,type,OrderType.Sell==type?RoundingMode.UP:RoundingMode.DOWN,BigDecimal.valueOf(dis));				 
		
		 
		 //开始查询挂单
		 AsyncObject<List<OrderItem>> aoOI = new  AsyncObject<>(str+"查询挂单",1,new ArrayList<>());		
		 aexMarket.orderList(tr, (ol,e)->{			
			 if(ol!=null){
				 infolog("查询"+str+"挂单",ol);	
				 aoOI.getObj().addAll(ol);
			 }
			 aoOI.setE(e);
			 aoOI.getCdl().countDown();
		 });
		 List<OrderItem> orderList = aoOI.waitAndGet(9000);
		 
		// boolean[] hasOrder = new boolean[]{false};
		 AsyncObject<Boolean> hasOrder = new AsyncObject<>(str+"判断挂单信息",orderList.size(),false);
		//取消长时间未成交订单
		 if(orderList.size()>0){
			 logger.info("{}查询到之前未成交的挂单{}条,判断挂单信息",str,orderList.size());			
			 long now = System.currentTimeMillis();
			 //过滤超过7分钟仍未成交的挂单
			 orderList.stream().forEach(o->{		
				 TaskInfo taskInfo  = trTime.get(tr);
				 if(taskInfo!=null && taskInfo.getPrice().compareTo(price)==0){
					 hasOrder.setObj(true);
					 logger.info("{}挂单 oderid:{} 价格:{}未变化,无需取消挂单,已挂单时间:{}秒",str, o.getOrderId(),price,(now-taskInfo.getTime())/1000);
					 hasOrder.getCdl().countDown();		
				 }else if(taskInfo!=null && (now-taskInfo.getTime())<1000*60*15){
					 hasOrder.setObj(true);
					 logger.info("{}挂单 oderid:{} 目前时间{}秒 还未到取消时间范围",str, o.getOrderId(),(now-taskInfo.getTime())/1000);
					 hasOrder.getCdl().countDown();		
				 }else{
					 aexMarket.cancelOrder(o.getTr(), o.getOrderId(), (r,e)->{	
						 hasOrder.setE(e);
						 hasOrder.getCdl().countDown();
					 });
				 }
			 });
			 infolog(hasOrder.getName(),hasOrder.waitAndGet(5000));
		 }
		 if(hasOrder.getObj()){
			 logger.info("{} 当前有有效挂单,不进行交易",str);
			 return;
		 }
		 
		 //查询账户信息		
		 AsyncObject<AccountInfo> infos = new  AsyncObject<>(str+"查询账户信息",1,null);		
		 aexMarket.accountInfo((info,e)->{			
			 infos.setObj(info);
			 infos.setE(e);
			 infos.getCdl().countDown();
		 });		
		 infolog(infos.getName(),infos.waitAndGet(5000));
		
		 BigDecimal count = BigDecimal.valueOf(min).divide(price, countRound, RoundingMode.DOWN);
		 Coin targetCoin = OrderType.Sell==type?tr.getLeft():tr.getRight();		
		 BigDecimal minCoin = infos.getObj().getInfoMap().get(targetCoin).getAvail();
		 logger.info("{} 计算完毕 price:{} count:{} minCoin:{}",str,price,count,minCoin);
		 
		 //根据行情和账户信息进行挂单
		 if(OrderType.Sell==type?minCoin.compareTo(count)<0:minCoin.compareTo(BigDecimal.valueOf(min))<0){
			 logger.info("{} 账户余额:{} 不足,不进行交易",str,minCoin);
			 return;
		 }
			
		 logger.error("{}开始挂单, price:{} count:{}",str,price,count);
		 //开始挂单
		 CountDownLatch downLatch = new CountDownLatch(1);		 
		 aexMarket.sumbitOrder(tr,type, price,count,(ol,e)->{
			 if(ol!=null){
				 logger.info("{} submitOrder price:{} res:{}",str,price,JSONObject.toJSONString(ol));
				 trTime.put(tr, new TaskInfo(System.currentTimeMillis(),price));
			 }
			 if(e!=null){
				 logger.error("{} submitOrder BCX_CNY error:",str,e);
			 }
			 downLatch.countDown();
		 });
		 if(!downLatch.await(9000, TimeUnit.MILLISECONDS))throw new BussException(str+"挂单超时");			 
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
				return buyPrice.get().getPrice().multiply(BigDecimal.ONE.subtract(dis)).setScale(round, roundingMode);			
			case Sell:
				return sellPrice.get().getPrice().multiply(BigDecimal.ONE.add(dis)).setScale(round, roundingMode);			
			default:
				throw new ParamException("orderType:"+orderType+" 不支持");
		 }
	}
	
	private void infolog(String title,Object obj){
		if(obj!=null){
			String json = JSONObject.toJSONString(obj);
			logger.info("{} res:{}",title, json.length()>100?json.substring(0,90)+"..."+json.substring(json.length()-10, json.length()):json);
		}	
	}
}
