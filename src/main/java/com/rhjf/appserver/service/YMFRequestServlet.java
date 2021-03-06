package com.rhjf.appserver.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rhjf.appserver.util.DateUtil;
import com.rhjf.appserver.util.LoggerTool;
import com.rhjf.appserver.util.UtilsConstant;

@WebServlet("/YMFRequest")
public class YMFRequestServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7422128056249437318L;

	LoggerTool logger = new LoggerTool(this.getClass());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		
		
		logger.info("接收到页面的请求");

		/** 用户编号ID **/
		String userID = req.getParameter("userID");
		String merName = req.getParameter("merName");
		String amount = req.getParameter("amount");
		String ymfCode = req.getParameter("ymfCode");
		String paychannel = req.getParameter("paychannel");
		String tradeDate = DateUtil.getNowTime(DateUtil.yyyyMMdd);
		String tradeTime = DateUtil.getNowTime(DateUtil.HHmmss);
		String orderNumber = UtilsConstant.getOrderNumber();

		req.setAttribute("userID", userID);
		req.setAttribute("merName", merName);
		req.setAttribute("amount", amount);
		req.setAttribute("ymfCode", ymfCode);
		req.setAttribute("tradeDate", tradeDate);
		req.setAttribute("tradeTime", tradeTime);
		req.setAttribute("orderNumber", orderNumber);
		req.setAttribute("paychannel", paychannel);
		
		req.getRequestDispatcher("/YMFTrade").forward(req, resp);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
}
