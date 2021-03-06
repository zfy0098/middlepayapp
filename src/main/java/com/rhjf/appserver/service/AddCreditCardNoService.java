package com.rhjf.appserver.service;

import java.util.Map;

import com.rhjf.appserver.constant.Constant;
import com.rhjf.appserver.constant.RespCode;
import com.rhjf.appserver.db.LoginUserDAO;
import com.rhjf.appserver.db.UserBankCardDAO;
import com.rhjf.appserver.model.RequestData;
import com.rhjf.appserver.model.ResponseData;
import com.rhjf.appserver.model.LoginUser;
import com.rhjf.appserver.util.EhcacheUtil;
import com.rhjf.appserver.util.LoggerTool;
import com.rhjf.appserver.util.UtilsConstant;
import com.rhjf.appserver.util.auth.AuthUtil;
import com.rhjf.appserver.util.auth.Author;



/**
 *   用户添加新用卡卡号
 * @author hadoop
 *
 */
public class AddCreditCardNoService {

	private LoggerTool log = new LoggerTool(this.getClass());
	
	public void addCreditCardNo(LoginUser user , RequestData request , ResponseData response){
        //  查询商户结算卡信息
		Map<String,Object> userBankCard = LoginUserDAO.getUserBankCard(user.getID());

        String settleCreditCard = request.getCreditCardNo();

        /**
         *  用户信用卡 卡号鉴权
         */
        Map<String,String> map = AuthUtil.authentication(UtilsConstant.ObjToStr(userBankCard.get("AccountName")),UtilsConstant.ObjToStr(settleCreditCard)
        		,UtilsConstant.ObjToStr(user.getIDCardNo()) , UtilsConstant.ObjToStr(userBankCard.get("PayerPhone")));
        
        if(!Author.SUCESS_CODE.equals(map.get("respCode").toString())){
            log.info("商户：" + user.getLoginID() + "信用鉴权没有通过 , 卡号:" + settleCreditCard);
            response.setRespCode(RespCode.BankCardInfoErroe[0]);
            response.setRespDesc(RespCode.BankCardInfoErroe[1]);
            return ;
        }

        UserBankCardDAO.addCreditCardNo(new Object[]{settleCreditCard , user.getID()});

        EhcacheUtil ehcache = EhcacheUtil.getInstance();
        ehcache.clear(Constant.cacheName);
        
        response.setRespCode(RespCode.SUCCESS[0]);
        response.setRespDesc(RespCode.SUCCESS[1]);

	}
}
