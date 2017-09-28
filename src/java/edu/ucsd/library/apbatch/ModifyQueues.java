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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.NumberFormat;


/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class ModifyQueues extends HttpServlet {
	private static Logger log = Logger.getLogger( ModifyQueues.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray dummy = null;
	JSONArray pending = null;
	JSONArray deleted = null;
	JSONArray problemArr = null;
	JSONArray newPending = new JSONArray();
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String whichQueue = null;
	Double sumAmtObj = new Double(0);
	Double sumObj = new Double(0);
	String sumAmtObjString = null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}	
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		System.out.println("start deleting");
		//get SOLR query and callback
		log.info("MODIFYQUEUES: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
					session = request.getSession();
					 pending = (session.getAttribute("apbatchData") != null ) ? (JSONArray)session.getAttribute("apbatchData") : null;
					 deleted = (session.getAttribute("deletedData") != null ) ? (JSONArray)session.getAttribute("deletedData") : null;
					 problemArr =(session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
					 sumAmtObj = (session.getAttribute("SUM") != null ) ? (Double)session.getAttribute("SUM") : null;
					 log.info("MODIFYQUEUES: Found data from session");
					 log.info("deleted data - "+deleted);
					 //sumAmtObj = new Double(sumAmtObjString.substring(1));
					 //log.info("sumAmtObjString="+sumAmtObjString);
					// log.info("sumAmtObj="+sumAmtObj);
					if(pending == null ){
						log.error("There is no json data in the session");
						try {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
						} catch (IOException e) {
							log.error("There was an error sending error message back in response from ModifyQueues servlet", e);
							return;
						}
					}
					
					
					
					try{
						invoiceNoList = request.getParameter("invoiceArr");
						//whichQueue = request.getParameter("whichQueue");
						log.info("$$$$ invoiceNoList:"+invoiceNoList);
						//log.info("$$$$ whichQueue:"+whichQueue);
					}
					catch(Exception e2)
					{
						foundRequestData = false;
						log.error("NO data got from request in ModifyQueues servlet: P.O No is expected");
						try {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
						} catch (IOException e) {
							log.error("There was an error sending error message back in response from ModifyQueues servlet", e);
							return;
						}
						
					}
					
					if(foundSessionData && foundRequestData){
						try {
							
							log.info("$$$$ size of the apbatch queue in sesssion:"+pending.size());
							log.info("$$$$ size of the deleted queue in sesssion:"+deleted.size());
							String [] temp = invoiceNoList.split(",");
							JSONArray newApbatchArr = getNewArray(pending,temp);
							sumObj= updateSumSubtract(sumAmtObj,pending,temp);
							JSONArray deletedArr = getDeletedArray(pending,deleted,temp);
							results.put("SUM", sumObj);
							log.info("SUM : "+sumObj);
							results.put("rows", newApbatchArr);
							results.put("total", newApbatchArr.size());
							log.info("rowsize : "+newApbatchArr.size());
							results.put("delRows", deletedArr);
							results.put("delTotal", deletedArr.size());
							log.info("delRowssize : "+deletedArr.size());
							results.put("ProblemRows", problemArr);
							results.put("ProblemTotal", problemArr.size());
							results.put("result", "success");
							session.setAttribute("apbatchData", newApbatchArr);
							session.setAttribute("deletedData", deletedArr);
							session.setAttribute("problemData", problemArr);
							session.setAttribute("SUM", sumObj);
							response.setContentType("text/plain;charset=UTF-8");
							response.addHeader("Pragma", "no-cache");
							response.setStatus(200);
							PrintWriter writer = new PrintWriter(response.getOutputStream());
							writer.write(results.toString());
							writer.close();
							newApbatchArr = new JSONArray();
							deletedArr = new JSONArray();
							//temp = null;
							log.info("MODIFYQUEUE: END");
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.info("Error sending back queue data from MODIFYQUEUES", e);
						}
						
						
					}
					
		
					
	}
	public JSONArray getNewArray(JSONArray dummy, String[] temp)
	{
		log.info("Dummy size ="+dummy.size());
		boolean flag = false;
		JSONArray newPending = new JSONArray();
		int tokencount= temp.length;
		String voucherRecType = "";
		for(int i=0; i < dummy.size();i++)
		  {
			 if(i== (dummy.size()-1))  
			  {
				  JSONObject newTotalObj=updateTotals(dummy,temp);
				  newPending.add(newTotalObj);  
				  break;
			  }
			 flag = false;
			  JSONObject obj = (JSONObject)dummy.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				  voucherRecType = (obj.get("voucherNo") + "=" + obj.get("recType")).trim();
				  if(voucherRecType.equals(temp[j].trim()))
				  //if((obj.get("recType")).equals(temp[j]))
				  {
					  flag = true;
					  break;
				  }
			  }
			  
			 if(!flag)
			  {
				  newPending.add(obj);  
			  }
			 /* if((i== (dummy.size()-1))  && (!flag))
			  {
				  JSONObject newTotalObj=updateTotals(dummy,temp);
				  newPending.add(newTotalObj);   
			  }
			  else if ((i != (dummy.size()-1))  && (!flag))
			  {
				  newPending.add(obj);  
			  }*/
		  }
		log.info("newPending size after="+newPending.size());
		return newPending;
	}
	
	public JSONArray getDeletedArray(JSONArray dummy, JSONArray deleted,String[] temp)
	{
		boolean flag = false;
		//JSONArray newPending = new JSONArray();
		int tokencount= temp.length;
		String voucherRecType = "";
		for(int i=0; i < dummy.size();i++)
		  {
			 //flag = false;
			  JSONObject obj = (JSONObject)dummy.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				  voucherRecType = (obj.get("voucherNo") + "=" + obj.get("recType")).trim();
				  //if((obj.get("recType")).equals(temp[j]))
				  if(voucherRecType.equals(temp[j].trim()))
				  {
					  //flag = true;
					 // break;
					  log.info("obj voucher recType"+voucherRecType + " - tmp voucher+recType:" + temp[j]);
					  deleted.add(obj);  
				  }
			  }
			  
			 // if(!flag)
			 // {
			//	  newPending.add(obj);  
			//  }
			  
		  }
		return deleted;
	}
	
	public JSONObject updateTotals(JSONArray dummy,String[] temp)
	{
		int count= dummy.size();
		double tempAmt =0;
		double tempTax=0;
		double tempUseTax=0;
		double tempShip=0;
		double tempDisc =0;
		int tokencount= temp.length;
		JSONObject lastObj = (JSONObject)dummy.get(count-1);
		String amount =((String)lastObj.get("amount")).trim();
		String tax =((String)lastObj.get("tax")).trim();
		String useTax =((String)lastObj.get("useTax")).trim();
		String ship =((String)lastObj.get("ship")).trim(); 
		String discount =((String)lastObj.get("discount")).trim(); 
		
		
		tempAmt = Double.parseDouble(amount.substring(1));
		tempTax = Double.parseDouble(tax.substring(1));
		tempUseTax = Double.parseDouble(useTax.substring(1));
		tempShip = Double.parseDouble(ship.substring(1));
		tempDisc = Double.parseDouble(discount.substring(1));
		String voucherRecType = "";
		for(int i=0; i < dummy.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)dummy.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				  voucherRecType = (tempObj.get("voucherNo") + "=" + tempObj.get("recType")).trim();
				  if(voucherRecType.equals(temp[j].trim()))
				  //if((tempObj.get("recType")).equals(temp[j]))
				  {
					  	String amount2 =((String)tempObj.get("amount")).trim();
						String tax2 =((String)tempObj.get("tax")).trim();
						String useTax2 =((String)tempObj.get("useTax")).trim();
						String ship2 =((String)tempObj.get("ship")).trim(); 
						String discount2 =((String)tempObj.get("discount")).trim(); 
						double tempAmt2 = Double.parseDouble(amount2.substring(1));
						double tempTax2 = Double.parseDouble(tax2.substring(1));
						double tempUseTax2 = Double.parseDouble(useTax2.substring(1));
						double tempShip2 = Double.parseDouble(ship2.substring(1));
						double tempDisc2 = Double.parseDouble(discount2.substring(1));
						tempAmt = tempAmt -tempAmt2;
						tempTax = tempTax -tempTax2;
						tempUseTax = tempUseTax - tempUseTax2;
						tempShip = tempShip-tempShip2;
						tempDisc = tempDisc - tempDisc2;
				  }
			  }
			
			  
		  }	
		  NumberFormat twoDForm = new DecimalFormat("#0.00");
		  double newAmt= Double.valueOf(twoDForm.format(tempAmt));
		  String finalAmt = "$"+twoDForm.format(tempAmt);
		  double newTax= Double.valueOf(twoDForm.format(tempTax));
		  String finalTax= "$"+twoDForm.format(tempTax);
		  double newUseTax= Double.valueOf(twoDForm.format(tempUseTax));
		  String finalUseTax= "$"+twoDForm.format(tempUseTax);
		  double newShip= Double.valueOf(twoDForm.format(tempShip));
		  String finalShip= "$"+twoDForm.format(tempShip);
		  double newDisc= Double.valueOf(twoDForm.format(tempDisc));
		  String finalDisc= "$"+twoDForm.format(tempDisc);
		  
		  JSONObject obj = new JSONObject();
			obj.put("voucherNo", ((String)lastObj.get("voucherNo")).trim());
			obj.put("recType", " ");
			obj.put("fundCode", " ");
			obj.put("subfundNo"," ");
			obj.put("externalFund", " ");
			obj.put("paidDate", " ");
			obj.put("invDate", " ");
			obj.put("invNo", " ");
			obj.put("amount", finalAmt);
			obj.put("tax", finalTax);
			obj.put("useTax", finalUseTax);
			obj.put("ship", finalShip);
			obj.put("discount", finalDisc);
			obj.put("listPrice", " ");
			obj.put("lien"," ");
			obj.put("lienFlag", " ");
			obj.put("status", " ");
			obj.put("notes", " ");
			obj.put("vendorCode", " ");
			obj.put("altVendorCode", " ");
			obj.put("vendorName"," ");
			obj.put("taxcode", " ");
			
			return obj;
	}
	
	public Double updateSumSubtract(Double sumObj,JSONArray pending,String[] temp)
	{
		log.info("==========inside updateSumSubtract========== ");
		log.info("Previous sumObj="+sumObj);
		int tokencount= temp.length;
		double sum = Double.valueOf(sumObj);
		String voucherRecType = "";
		for(int i=0; i < pending.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)pending.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				  voucherRecType = (tempObj.get("voucherNo") + "=" + tempObj.get("recType")).trim();
				  //if((tempObj.get("recType")).equals(temp[j]))
				  if(voucherRecType.equals(temp[j].trim()))
				  {
					  	String amount2 =((String)tempObj.get("amount")).trim();
						String tax2 =((String)tempObj.get("tax")).trim();
						String useTax2 =((String)tempObj.get("useTax")).trim();
						String ship2 =((String)tempObj.get("ship")).trim(); 
						String discount2 =((String)tempObj.get("discount")).trim(); 
						double tempAmt2 = Double.parseDouble(amount2.substring(1));
						double tempTax2 = Double.parseDouble(tax2.substring(1));
						double tempUseTax2 = Double.parseDouble(useTax2.substring(1));
						double tempShip2 = Double.parseDouble(ship2.substring(1));
						double tempDisc2 = Double.parseDouble(discount2.substring(1));
						double tempSum= tempAmt2+ tempTax2+tempUseTax2+tempShip2+tempDisc2;
						log.info("tempSum="+tempSum);
						sum -= tempSum;
						log.info("new sum="+sum);
				  }
			  }
			
			  
		  }
		log.info("sum="+sum);
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		double sumMod= Double.valueOf(twoDForm.format(sum));

		return new Double(sumMod);
		
	}
}