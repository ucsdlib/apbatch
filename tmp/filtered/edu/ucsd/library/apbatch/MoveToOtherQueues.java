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




/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class MoveToOtherQueues extends HttpServlet {
	private static Logger log = Logger.getLogger( MoveToOtherQueues.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray dummy = null;
	JSONArray deletedArray = null;
	JSONArray addedArray = null;
	JSONArray pending = null;
	JSONArray problem = null;
	JSONArray deleted = null;
	JSONArray newPending = new JSONArray();
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String whichQueue = null;
	Double sumAmtObj = new Double(0);
	Double sumObj = new Double(0);
	//String sumAmtObj = null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		log.info("MoveToOtherQueues: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
					session = request.getSession();
					 pending = (session.getAttribute("apbatchData") != null ) ? (JSONArray)session.getAttribute("apbatchData") : null;
					 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
					 deleted = (session.getAttribute("deletedData") != null ) ? (JSONArray)session.getAttribute("deletedData") : null;
					 sumAmtObj = (session.getAttribute("SUM") != null ) ? (Double)session.getAttribute("SUM") : null;
					log.info("MoveToOtherQueues: Found data from session");
					
					if((pending == null )|| (problem == null)){
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
			whichQueue = request.getParameter("whichQueue");
			log.info("$$$$ invoiceNoList:"+invoiceNoList);
			log.info("$$$$ whichQueue:"+whichQueue);
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in ModifyQueues servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from MoveToOtherQueues servlet", e);
				return;
			}
			
		}
		// if we got data from both session and request 
		// we are going to modify queues
		if(foundSessionData && foundRequestData){
			try {
				
				log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
				log.info("$$$$ size of the deleted queue in sesssion:"+deleted.size());
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				String [] temp = invoiceNoList.split(",");
				
				
				if(whichQueue.equals("P"))
				{
					log.info("$$$$$$$$$$ INSIDE queue = P");
					deletedArray = deleteRecords(pending,temp,"P");
					log.info("$$$$$$$$$$ Pending previous size:"+pending.size());
					log.info("$$$$$$$$$$ Pending After size:"+deletedArray.size());
					addedArray =addRecords(pending,problem,temp,"P");
					log.info("$$$$$$$$$$ Problem previous size:"+problem.size());
					log.info("$$$$$$$$$$ Problem After size:"+addedArray.size());
					sumObj= updateSumSubtract(sumAmtObj,pending,temp);				
					
					results.put("SUM", sumObj);
					results.put("rows", deletedArray);
					results.put("ProblemRows", addedArray);
					results.put("delRows", deleted);
					results.put("total",deletedArray.size());
					results.put("ProblemTotal",addedArray.size());
					results.put("delTotal",deleted.size());
					session.setAttribute("problemData", addedArray);
					session.setAttribute("apbatchData", deletedArray);
					session.setAttribute("deletedData", deleted);
					session.setAttribute("SUM", sumObj);
					
				}
				else if (whichQueue.equals("Q"))
				{
					deletedArray = deleteRecords(problem,temp,"Q");
					addedArray =addRecords(problem,pending,temp,"Q");
					 session.setAttribute("problemData", deletedArray);
					 session.setAttribute("apbatchData", addedArray);
					 session.setAttribute("deletedData", deleted);
					 sumObj= updateSumAdd(sumAmtObj,problem,temp);
					 results.put("SUM", sumObj);
					 results.put("ProblemRows", deletedArray);
						results.put("ProblemTotal", deletedArray.size());
						results.put("result", "success");
						results.put("rows", addedArray);
						results.put("total", addedArray.size());
						results.put("delRows", deleted);
						results.put("delTotal", deleted.size());
						session.setAttribute("SUM", sumObj);
						
				}
				
				log.info("$$$$ size of the deletedArray:"+deletedArray.size());
				log.info("$$$$ size of the addedArray:"+addedArray.size());
				//session.setAttribute("newPatronData", newPatron);
							
				//results.put("newPatron", newPatron);
				results.put("result", "success");
				results.put("whichQueue", whichQueue);
				//results.put("newPatronTotal",newPatron.size());
				//send response
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				deletedArray = null;
				addedArray = null;
				temp = null;
				log.info("MoveToOtherQueues: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from MoveToOtherQueues", e);
			}
		}
	}
	
	public JSONArray deleteRecords(JSONArray from,String[] temp,String queue)
	{
		log.info("========== Inside Deleterecords==========");
		int count=from.size();
		JSONObject modifiedLastObj = null;
		boolean flag = false;
		JSONArray newArray = new JSONArray();
		int tokencount= temp.length;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((obj.get("recType")).equals(temp[j]))
				  {
					  flag = true;
					  break;
				  }
			  }
			  
			  if(!flag)
			  {
				  newArray.add(obj);  
			  }
			  
		  }
		if(whichQueue.equals("P"))
		{
			 JSONObject lastObj = (JSONObject)from.get(count-1);
			 modifiedLastObj =updateTotalsPendingToProb(lastObj,from,temp);
			 if(modifiedLastObj!=null)
			 {
				 log.info("&&&&&& size of newarray in deleterecords:"+newArray.size());
				 int countNewArray= newArray.size();
				 newArray.remove(countNewArray-1);
				 newArray.add(modifiedLastObj);
				 
			 }
			
		}
		log.info("========== Inside Deleterecords END==========");
		
		return newArray;
	}
	
	public JSONArray addRecords(JSONArray from,JSONArray to,String[] temp,String queue)
	{
		log.info("==========Inside addRecords============");
		JSONArray newPendingArray =new JSONArray(); 
		 JSONObject modifiedLastObj = null;
		 int count=to.size();
		/*=========================
		for (int k=0;k<count-1;k++)
		{
			  JSONObject obj = (JSONObject)to.get(k);
			  newPendingArray.add(obj);
		}
		//=========================
		 
		 */
		
		 if(queue.equalsIgnoreCase("Q"))
		 {
			 log.info("==========Inside Q============");
			 log.info("");
			 for (int k=0;k<count-1;k++)
				{
					  JSONObject obj = (JSONObject)to.get(k);
					  newPendingArray.add(obj);
				}
			 JSONObject lastObj = (JSONObject)to.get(count-1);
			 
		 modifiedLastObj =updateTotalsProblemToPending(lastObj,from,temp);
	    // newPendingArray.add(modifiedLastObj);
		 }
		 else{
			 log.info("==========Inside else============");
			 for (int k=0;k<count;k++)
			{
			  JSONObject obj = (JSONObject)to.get(k);
			  newPendingArray.add(obj);
			}
			 log.info("==========new pending size=:"+newPendingArray.size());
			// JSONObject lastObj = (JSONObject)to.get(count-1);
			// modifiedLastObj =updateTotalsProblemToPending(lastObj,from,temp);
		 }
		
		
		boolean flag = false;
		//JSONArray newArray = to;
		int tokencount= temp.length;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((obj.get("recType")).equals(temp[j]))
				  {
					  flag = true;
					  newPendingArray.add(obj); 
				  }
			  }
			  
			  			  
		  }
		 
		 if(modifiedLastObj!=null)
		 {newPendingArray.add(modifiedLastObj);
		 modifiedLastObj =null;
		 }
		 log.info("==========new pending size FINAL=:"+newPendingArray.size());
		 log.info("==========Inside addRecords END============");
		 return newPendingArray;
			
	}
	
	public JSONObject updateTotalsPendingToProb(JSONObject lastObj,JSONArray problem,String[] temp)
	{
		int count= problem.size();
		double tempAmt =0;
		double tempTax=0;
		double tempUseTax=0;
		double tempShip=0;
		double tempDisc =0;
		int tokencount= temp.length;
		//JSONObject lastObj = (JSONObject)dummy.get(count-1);
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
		for(int i=0; i < problem.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)problem.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((tempObj.get("recType")).equals(temp[j]))
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
						tempAmt = tempAmt - tempAmt2;
						tempTax = tempTax - tempTax2;
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
	
	public JSONObject updateTotalsProblemToPending(JSONObject lastObj,JSONArray problem,String[] temp)
	{
		int count= problem.size();
		double tempAmt =0;
		double tempTax=0;
		double tempUseTax=0;
		double tempShip=0;
		double tempDisc =0;
		int tokencount= temp.length;
		//JSONObject lastObj = (JSONObject)dummy.get(count-1);
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
		for(int i=0; i < problem.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)problem.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((tempObj.get("recType")).equals(temp[j]))
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
						tempAmt = tempAmt + tempAmt2;
						tempTax = tempTax + tempTax2;
						tempUseTax = tempUseTax+ tempUseTax2;
						tempShip = tempShip+tempShip2;
						tempDisc = tempDisc + tempDisc2;
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
		int tokencount= temp.length;
		double sum = Double.valueOf(sumObj);
		for(int i=0; i < pending.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)pending.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((tempObj.get("recType")).equals(temp[j]))
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
						sum -= tempSum;
						
				  }
			  }
			
			  
		  }
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		double sumMod= Double.valueOf(twoDForm.format(sum));

		return new Double(sumMod);
		
	}
	
	public Double updateSumAdd(Double sumObj,JSONArray problem,String[] temp)
	{
		int tokencount= temp.length;
		double sum = Double.valueOf(sumObj);
		for(int i=0; i < problem.size();i++)
		  {
			
			  JSONObject tempObj = (JSONObject)problem.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((tempObj.get("recType")).equals(temp[j]))
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
						sum += tempSum;
						
				  }
			  }
			
			  
		  }
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		double sumMod= Double.valueOf(twoDForm.format(sum));

		return new Double(sumMod);
		
	}
}

