package edu.ucsd.library.apbatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.text.*;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;
/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class GetProblemQueueData extends HttpServlet {
	private static Logger log = Logger.getLogger( GetProblemQueueData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray problem = null;
	JSONArray transArray = new JSONArray();
	JSONArray invoiceNoArray = new JSONArray();
	JSONArray invoiceNoteArray = new JSONArray();
	String patronRecNo = null;
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String username = null;
	
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	
		log.info("GetProblemQueueData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		//problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
	//username = (session.getAttribute("username") != null ) ? (String)session.getAttribute("username") : null;
		 problem =(session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		log.info("GetProblemQueueData: Found data from session");
		//log.info("GetProblemQueueData: username:"+username);
		
		if(problem == null ){
			log.error("There is no json data in the session");
			foundSessionData = false;
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetProblemQueueData servlet", e);
				return;
			}
		}
		
		
		try{
			invoiceNoList = request.getParameter("invoiceArr");
			log.info("$$$$ invoiceNoList:"+invoiceNoList);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in GetProblemQueueData servlet: voucher no is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetProblemQueueData servlet", e);
				return;
			}
			
		}
		
		
		
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				String [] temp = invoiceNoList.split(",");
				JSONObject data = getData(problem,temp);
				log.info("$$ data:"+data.equals(null));
				results.put("basicData", data);				
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				log.info("GetProblemQueueData: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from GetProblemQueueData", e);
			}
			
			}//end of if(foundSe
		
		
	}//end of post
	
	
	public JSONObject getData(JSONArray from,String[] temp)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		int tokencount= temp.length;
		//String patronRecNo = null;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((obj2.get("recType")).equals(temp[j]))
				  {
					  flag = true;
					  String voucherNo = (String)obj2.get("recType");
							   
					   newjObj.put("voucherNo", obj2.get("voucherNo"));
					   newjObj.put("recType",obj2.get("recType"));
					   newjObj.put("fundCode", obj2.get("fundCode"));
					   newjObj.put("subfundNo",  obj2.get("subfundNo"));
					   newjObj.put("externalFund",  obj2.get("externalFund"));
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
					   break;
				  }
			  }
			  
			 if(flag)
			 break;
			  
		  }
		return newjObj;
	}
}