package com.rhjf.appserver.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rhjf.appserver.constant.Constant;
import com.rhjf.appserver.constant.RespCode;
import com.rhjf.appserver.constant.StringEncoding;
import com.rhjf.appserver.db.LoginUserDB;
import com.rhjf.appserver.db.TradeDB;
import com.rhjf.appserver.model.Fee;
import com.rhjf.appserver.model.PayOrder;
import com.rhjf.appserver.model.TabLoginuser;
import com.rhjf.appserver.service.NotifyService;
import com.rhjf.appserver.util.EhcacheUtil;
import com.rhjf.appserver.util.LoggerTool;
import com.rhjf.appserver.util.MD5;
import com.rhjf.appserver.util.RabbitmqSend;
import com.rhjf.appserver.util.UtilsConstant;

import net.sf.json.JSONObject;

/**
 *   信用卡还款交易通知
 * @author hadoop
 *
 */
@Controller
@RequestMapping("/creditcardrepay")
@ResponseBody
public class CreditCardRepayNotityController {

LoggerTool logger = new LoggerTool(this.getClass());
	
	@Autowired
	private NotifyService notifyService;
	
	@RequestMapping("")
	public Object notify(HttpServletRequest request) { 
		
		Map<String,String> map2 = new HashMap<String,String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				if (paramValue.length() != 0) {
					map2.put(paramName, paramValue);
				}
			}
		}
		if(map2==null||map2.isEmpty()){
			logger.info("回调报文为空");
			return  RespCode.notifyfail;
		}
		logger.info("接收上游回调, 回调内容:" + map2.toString()); 
		
		StringBuffer text = new StringBuffer("#");
		/**  拼接加密字符串  **/
		for (String key : Constant.notifyParams) {
			if(map2.containsKey(key)){
				String value = map2.get(key);
				if(UtilsConstant.strIsEmpty(value)||"sign".equals(key)){ 
					continue;
				}
				text.append(value);
				text.append("#");
			}
		}
		/** 查询商户信息  **/
		String merchantID = map2.get("r1_merchantNo");
		String paytype = "4";
		
		Map<String,Object> map =  TradeDB.getMerchantInfo(merchantID,paytype);
		/**  计算签名 **/
		String serverSign = MD5.sign(text.append(map.get("SignKey")).toString(), StringEncoding.UTF_8);
		String reqSign = map2.get("sign");
		
		if(!serverSign.equals(reqSign)){
			logger.info("平台计算签名：" + serverSign + ", 通知上传签名：" + reqSign);
			return RespCode.notifyfail;
		}

		/** 获取订单号  **/
		String orderNumber = map2.get("r2_orderNumber");

		/**  查询订单信息 **/
		PayOrder order = TradeDB.getPayOrderInfo(orderNumber);
		
		if(order==null){
			logger.info("订单号：" + orderNumber + "未查到订单信息");
			return RespCode.notifyfail;
		}
		
		if(Constant.payRetCode.equals(order.getPayRetCode())){
			logger.info("订单号：" + orderNumber + "已经成功支付");
			return RespCode.notifySuccess;
		}
		
		String retCode = map2.get("retCode");
		String orderStatus = map2.get("r8_orderStatus");
		
		logger.info("订单：" + orderNumber + "状态为 : retCode= " + retCode + " , orderStatus=" + orderStatus);
		
		if((Constant.payRetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))||
				(Constant.T0RetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))){
			logger.info("订单 ：" + orderNumber  + "支付成功");
			String retMsg = "支付成功";
			
			retCode = Constant.payRetCode;
			
			if(map2.containsKey("retMsg")){
				retMsg = map2.get("retMsg");
			}
			
			TabLoginuser loginUser = null;
			try {
				loginUser = LoginUserDB.getLoginuserInfo(order.getUserID());
			} catch (Exception e) {
				logger.info(e.getMessage()); 
				return RespCode.notifyfail;
			}
			
			Fee fee = notifyService.calProfit(orderNumber ,order, loginUser);
			
			if(fee==null){
				logger.info("订单：" +  orderNumber + "计算手续费失败");
				return RespCode.notifyfail;
			}
			
			int updateRet = TradeDB.updatePayOrderPayRetCode(new Object[]{retCode ,retMsg ,fee.getMerchantFee() , 0  , order.getID()});
			if(updateRet < 1){
				logger.info("订单号：" + orderNumber + "更新数据库失败"); 
				return RespCode.notifyfail;
			}
			// ID,UserID,TradeID,Fee,AgentID,AgentProfit,TwoAgentID,TwoAgentProfit,DistributeProfit,PlatformProfit
			int x = TradeDB.saveProfit(new Object[]{
					UtilsConstant.getUUID(),loginUser.getID(), order.getID() ,fee.getMerchantFee(),null,0,
					null,0,
					0,fee.getPlatformProfit(),fee.getPlatCostFee()
			});
			if(x < 1){
				logger.info("订单号:"  + orderNumber + "保存收益记录失败");
				return RespCode.notifyfail;
			}
			
			try {
				JSONObject mq = new JSONObject();
				mq.put("orderNumber", orderNumber);
				mq.put("dfType", "Trade");
				RabbitmqSend.sendMessage(mq.toString());
			} catch (Exception e) {
				logger.error("执行mq发送队列消息异常：" + e.getMessage() ,  e); 
			}
			
			EhcacheUtil ehcache = EhcacheUtil.getInstance();
			ehcache.clear(Constant.cacheName);
		}
		return RespCode.notifySuccess;
	}
}