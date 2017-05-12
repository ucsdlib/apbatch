package edu.ucsd.library.apbatch;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;
/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class EditProblemQueueData extends HttpServlet {
	private static Logger log = Logger.getLogger( EditProblemQueueData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray deleted = null;
	JSONArray pending = null;
	JSONArray problem = null;
	HttpSession session = null;
	String invoiceNumString = null;
	String  recType= null;
	String fundCode= null;
	String extFundCode = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		
		log.info("EditProblemQueueData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		deleted = (session.getAttribute("deletedData") != null ) ? (JSONArray)session.getAttribute("deletedData") : null;
		 pending = (session.getAttribute("apbatchData") != null ) ? (JSONArray)session.getAttribute("apbatchData") : null;
		String txtInvDate= null,txtInvNo= null,txtAmount= null,txtVoucher= null,txtPONumber= null,txtPaidDate= null,txtTax= null,
		       txtUseTax= null,txtShip= null,txtDiscount= null,txtVendorCode= null,txtVendName= null,txtTaxCode= null;		 
		log.info("EditProblemQueueData: Found data from session");
		
		if(problem == null ){
			foundSessionData = false;
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditProblemQueueData servlet", e);
				return;
			}
		}
		try{
			recType = request.getParameter("recType");
			log.info("$$$$ recType:"+recType);
			fundCode = request.getParameter("fundCode");
			log.info("$$$$ fundCode:"+fundCode);
			extFundCode= request.getParameter("extFundCode");
			log.info("$$$$ extFundCode:"+extFundCode);			
			txtInvDate= request.getParameter("txtInvDate");
			log.info("$$$$ txtInvDate:"+txtInvDate);
			txtInvNo= request.getParameter("txtInvNo");
			log.info("$$$$ txtInvNo:"+txtInvNo);
			txtAmount= request.getParameter("txtAmount");
			log.info("$$$$ txtAmount:"+txtAmount);
			txtVoucher= request.getParameter("txtVoucher");
			log.info("$$$$ txtVoucher:"+txtVoucher);
			txtPONumber= request.getParameter("txtPONumber");
			log.info("$$$$ txtPONumber:"+txtPONumber);			
			txtPaidDate= request.getParameter("txtPaidDate");
			log.info("$$$$ txtPaidDate:"+txtPaidDate);			
			txtTax= request.getParameter("txtTax");
			log.info("$$$$ txtTax:"+txtTax);			
			txtUseTax= request.getParameter("txtUseTax");
			log.info("$$$$ txtUseTax:"+txtUseTax);			
			txtShip= request.getParameter("txtShip");
			log.info("$$$$ txtShip:"+txtShip);			
			txtDiscount= request.getParameter("txtDiscount");
			log.info("$$$$ txtDiscount:"+txtDiscount);			
			txtVendorCode= request.getParameter("txtVendorCode");
			log.info("$$$$ txtVendorCode:"+txtVendorCode);		
			txtVendName= request.getParameter("txtVendName");
			log.info("$$$$ txtVendName:"+txtVendName);
			txtTaxCode= request.getParameter("txtTaxCode");
			log.info("$$$$ txtTaxCode:"+txtTaxCode);
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in EditProblemQueueData servlet: recType and fundCode is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditProblemQueueData servlet", e);
				return;
			}
			
		}
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				 JSONArray data = getData(problem,recType,fundCode,extFundCode,txtInvDate,txtInvNo,txtAmount,txtVoucher,
						 txtPONumber,txtPaidDate,txtTax,txtUseTax,txtShip,txtDiscount,txtVendorCode,txtVendName,txtTaxCode);
				 //test
				 session.setAttribute("problemData", data);
				 session.setAttribute("apbatchData", pending);
				 session.setAttribute("deletedData", deleted);
				// results.put("problemData", data);
				 results.put("ProblemRows", data);
					results.put("ProblemTotal", data.size());
					results.put("result", "success");
					results.put("rows", pending);
					results.put("total", pending.size());
					results.put("delRows", deleted);
					results.put("delTotal", deleted.size());
					response.setContentType("text/plain;charset=UTF-8");
					response.addHeader("Pragma", "no-cache");
					response.setStatus(200);
					PrintWriter writer = new PrintWriter(response.getOutputStream());
					writer.write(results.toString());
					writer.close();
					pending = new JSONArray();
					deleted = new JSONArray();
					//temp = null;
					log.info("MODIFYQUEUE: END");
			}
			 catch (Exception e) {
					// TODO Auto-generated catch block
					log.info("Error sending back queue data from EditProblemQueueData", e);
				}
				
		}
	}
		
	public JSONArray getData(JSONArray from,String recType,String fundCode,String extFundCode)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		JSONArray newArr = new JSONArray();
		log.info("$$$$$ SIZE OF THE PROBLEM ARRAY:"+ from.size());
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			
				 
				  if((obj2.get("recType")).equals(recType))
				  {
					 
					  newjObj.put("voucherNo", obj2.get("voucherNo"));
					   newjObj.put("recType",obj2.get("recType"));
					   newjObj.put("fundCode", fundCode);
					   newjObj.put("subfundNo",  obj2.get("subfundNo"));
					   newjObj.put("externalFund",  extFundCode);
					   newjObj.put("paidDate",  obj2.get("paidDate"));
					   newjObj.put("invDate",  obj2.get("invDate"));
					   newjObj.put("invNo",  obj2.get("invNo"));
					   newjObj.put("amount", obj2.get("amount"));
					   newjObj.put("tax",  obj2.get("tax"));
					   newjObj.put("useTax",  obj2.get("useTax"));
					   newjObj.put("ship",  obj2.get("ship"));
					   newjObj.put("discount",  obj2.get("discount"));
					   newjObj.put("listPrice",  obj2.get("listPrice"));
					   newjObj.put("lien",  obj2.get("lien"));
					   newjObj.put("lienFlag",  obj2.get("lienFlag"));
					   newjObj.put("status",  obj2.get("status"));
					   newjObj.put("notes", obj2.get("notes"));
					   newjObj.put("vendorCode",  obj2.get("vendorCode"));
					   newjObj.put("altVendorCode",  obj2.get("altVendorCode"));
					   newjObj.put("vendorName",  obj2.get("vendorName"));
					   newjObj.put("taxcode",  obj2.get("taxcode"));
				
                      newArr.add(newjObj);
                      newjObj = new JSONObject();
                      log.info("$$$$ newobj invoice no:"+ newjObj.get("invoiceNo"));
                      log.info("$$$$ ADDED PID CHAGE REOCRD $$$$");
                      
				  }
				  else
				  {
					  newArr.add(obj2);
					 
				  }
			  
			  
			
			  
		  }
		 log.info("$$$$$ SIZE OF THE newArr ARRAY:"+ newArr.size());
		return newArr;
	}

	public JSONArray getData(JSONArray from,String recType,String fundCode,String extFundCode, 
			String txtInvDate,String txtInvNo,String txtAmount,String txtVoucher,String txtPONumber,
			String txtPaidDate,String txtTax,String txtUseTax,String txtShip,String txtDiscount,String txtVendorCode,String txtVendName,String txtTaxCode)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		JSONArray newArr = new JSONArray();
		log.info("$$$$$ SIZE OF THE PROBLEM ARRAY:"+ from.size());
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			
				 
				  if((obj2.get("recType")).equals(recType))
				  {
					 
					  newjObj.put("voucherNo", txtVoucher);
					   newjObj.put("recType",txtPONumber);
					   newjObj.put("fundCode", fundCode);
					   newjObj.put("subfundNo",  obj2.get("subfundNo"));
					   newjObj.put("externalFund",  extFundCode);
					   newjObj.put("paidDate",  txtPaidDate);
					   newjObj.put("invDate",  txtInvDate);
					   newjObj.put("invNo",  txtInvNo);
					   newjObj.put("amount", txtAmount);
					   newjObj.put("tax",  txtTax);
					   newjObj.put("useTax",  txtUseTax);
					   newjObj.put("ship",  txtShip);
					   newjObj.put("discount",  txtDiscount);
					   newjObj.put("listPrice",  obj2.get("listPrice"));
					   newjObj.put("lien",  obj2.get("lien"));
					   newjObj.put("lienFlag",  obj2.get("lienFlag"));
					   newjObj.put("status",  obj2.get("status"));
					   newjObj.put("notes", obj2.get("notes"));
					   newjObj.put("vendorCode",  txtVendorCode);
					   newjObj.put("altVendorCode",  obj2.get("altVendorCode"));
					   newjObj.put("vendorName",  txtVendName);
					   newjObj.put("taxcode",  txtTaxCode);
				
                      newArr.add(newjObj);
                      newjObj = new JSONObject();
                      log.info("$$$$ newobj invoice no:"+ newjObj.get("invoiceNo"));
                      log.info("$$$$ ADDED PID CHAGE REOCRD $$$$");
                      
				  }
				  else
				  {
					  newArr.add(obj2);
					 
				  }
			  
			  
			
			  
		  }
		 log.info("$$$$$ SIZE OF THE newArr ARRAY:"+ newArr.size());
		return newArr;
	}
	
	}