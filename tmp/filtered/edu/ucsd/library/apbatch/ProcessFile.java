package edu.ucsd.library.apbatch;
import java.text.NumberFormat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import javax.naming.NamingException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import edu.ucsd.library.util.sql.ConnectionManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
/**
 * Logic implementation to process the file
 *
 * @author lib-kdushamali
 *
 */

public class ProcessFile {
	private static Logger log = Logger.getLogger( ProcessFile.class );
	private InputStream stream = null;
	private JSONArray outFileContent = new JSONArray();
	private JSONArray problemQueue = new JSONArray();
	private double amountSum =0;
	private double taxSum = 0;
	private double useTaxSum=0;
	private double shipSum =0;
	private double discSum =0;
	public boolean useTestConnection = false;
	
	public ProcessFile(InputStream stream) {
		this.stream = stream;

	}

	
	public boolean processOutFile() throws IOException {
		String voucherNo = null;
		String recType = null;
		String fundCode =null;
		String subfundNo = null;
		String externalFund =null;
		String paidDate = null;
		String invDate = null;
		String invNo =null;
		String amount = null;
		String tax = null;
		String useTax = null;
		String ship = null;
		String discount = null;
		String listPrice = null;
		String lien = null;
		String lienFlag = null;
		String status = null;
		String notes = null;
		String vendorCode = null;
		String altVendorCode = null;
		String vendorName = null;
		 String taxcode=null;
		boolean flagLastLine = false;
	
		boolean flagProblemQueue= false;
		double tempTax = 0;
		double tempAmt = 0;
		double tempShip =0;
		double tempDiscount = 0;
		double tempUseTax =0;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String text = "";
		
		while ((text = reader.readLine()) != null) {
			//System.out.println("Current billing record: "+text);
						
			String temp = text.trim();
	        int length = temp.length();
	       //log.info("$$$ length="+length);
	       if(length > 7){
	        voucherNo = temp.substring(0,7).trim();
	        if(voucherNo.equalsIgnoreCase("ALLDONE"))
	        	flagLastLine = true;
	       }
	       else
	       {
	    	   voucherNo = " ";
	       }
	       
	        //log.info("$$$ voucherNo="+voucherNo);
	        //===========================================
	        
	        if (flagLastLine)
	        {
	        	//may be the last line
	        	String tempStr= temp.replaceAll(" +", " "); 
	        	//log.info("$$$ tempStr="+tempStr);
	            String[] tempArr = tempStr.split(" ");
	            //log.info("$$$ LENGHT tempStr="+tempArr.length);
	            voucherNo   = "TOTAL";
	            recType   = " ";
	            fundCode   = " ";
	            subfundNo   = " ";
	            externalFund= " ";
	            paidDate= " ";
	            invDate= " ";
	            invNo= " ";
	            /*
	            amount= tempArr[1];
	            if(amount.equals("0"))
				{
	            	amount = "$0.00";
				}
				else{
					String tempStrAmt1 = "$"+amount.substring(0,amount.length()-2)+"." + amount.substring(amount.length()-2);
					log.info("tempStrDisc1:"+tempStrAmt1);
					amount =tempStrAmt1;
					
				}
	            log.info("$$$ amount="+amount);
	            tax= tempArr[2];
	            if(tax.equals("0"))
				{
				 tax = "$0.00";
				}
				else{
					String tempStrTax1 ="$"+tax.substring(0,tax.length()-2)+"." + tax.substring(tax.length()-2);
					log.info("tempStrtax1:"+tempStrTax1);
					tax = tempStrTax1;
				
				} 
	            log.info("$$$ tax="+tax);
	            useTax= tempArr[3];
	            if(useTax.equals("0"))
	 			{
	    			 useTax =  "$0.00";
	 			}
	 			else{
	 			String tempStrUseTax1 = "$"+useTax.substring(0,useTax.length()-2)+"." + useTax.substring(useTax.length()-2);
	 			log.info("tempStrUseTax1:"+tempStrUseTax1);
	 			useTax = tempStrUseTax1;
	 			
	 			}
	            log.info("$$$ useTax="+useTax);
	            ship = tempArr[4];
	            if(ship.equals("0"))
	 			{
	    			 ship = "$0.00";
	 			}
	 			else{
	 				String tempStrShip1 = "$"+ship.substring(0,ship.length()-2)+"." + ship.substring(ship.length()-2);
	 				log.info("tempStrShip1:"+tempStrShip1);
	 				ship = tempStrShip1;
	 				
	 			}
	            log.info("$$$ ship="+ship);
	            discount= tempArr[5];
	            if(discount.equals("0"))
				{
	    			discount = "$0.00";
				}
				else{
					String tempStrDisc1 = "$"+discount.substring(0,discount.length()-2)+"." + discount.substring(discount.length()-2);
					log.info("tempStrDisc1:"+tempStrDisc1);
					discount =tempStrDisc1;
					
				}
			*/	
	            //log.info("$$$ discount="+discount);
	           NumberFormat twoDForm = new DecimalFormat("#0.00");
	  		   String finalAmt = "$"+twoDForm.format(amountSum);
	  		   amount = finalAmt;
	  		   String finalTax = "$"+twoDForm.format(taxSum);
	  		   tax = finalTax;
	  		   String finalUseTax = "$"+twoDForm.format(useTaxSum);
	  		   useTax = finalUseTax;
	  		   String finalShip = "$"+twoDForm.format(shipSum);
	  		   ship = finalShip;
	  		  String finalDisc = "$"+twoDForm.format(discSum);
	  		   discount = finalDisc;    
	  		   
	  		   listPrice= " ";
	            //log.info("$$$ listPrice="+listPrice);
	            lien= " ";
	            lienFlag= " ";
	            status= " ";
	            notes= " ";
	            vendorCode= " ";
	            altVendorCode= " ";
	            vendorName= " ";
	            taxcode=" ";
	            
	           
		    	JSONObject obj = new JSONObject();
				obj.put("voucherNo", voucherNo);
				obj.put("recType", recType);
				obj.put("fundCode", fundCode);
				obj.put("subfundNo", subfundNo);
				obj.put("externalFund", externalFund);
				obj.put("paidDate", paidDate);
				obj.put("invDate", invDate);
				obj.put("invNo", invNo);
				obj.put("amount", amount);
				obj.put("tax", tax);
				obj.put("useTax", useTax);
				obj.put("ship", ship);
				obj.put("discount", discount);
				obj.put("listPrice", listPrice);
				obj.put("lien", lien);
				obj.put("lienFlag", lienFlag);
				obj.put("status", status);
				obj.put("notes", notes);
				obj.put("vendorCode", vendorCode);
				obj.put("altVendorCode", altVendorCode);
				obj.put("vendorName", vendorName);
				obj.put("taxcode", taxcode);			
				outFileContent.add(obj);
				continue;
	        }
	        else{
	        	
	        	
	       
	        if(length > 14){
	        	 recType   =temp.substring(7,14);
	        }
	        else
	        {
	        	 recType   = " ";
	        }	        
	      
	       //log.info("$$$ recType="+recType);
	       
	        if(length > 19){
	    	   fundCode =temp.substring(14,19).trim();
	        }
	        else
	        {
	        	fundCode   = " ";
	        }       
	    	if(fundCode ==" " || fundCode.length()==0 )
	    	{
	    		flagProblemQueue = true;
	    	}
	    	else
	    		flagProblemQueue = false;
	    	//log.info("$$$ fundCode="+fundCode);
	    	  if(length > 20){
	    		  subfundNo =temp.substring(19,20).trim();
		        }
		        else
		        {
		        	subfundNo   = " ";
		        }     	
	    	
	    	//log.info("$$$ subfundNo="+subfundNo);
	    	
	    	  if(length > 40){
	    			externalFund=temp.substring(20,40).trim();
	    	  }
	    	  else
	    	  {
	    		  externalFund= " ";
	    	  }
	    	//log.info("$$$ externalFund="+externalFund);
	    	 if(length > 48){
	    		 paidDate =temp.substring(40,48).trim();
	    	  }
	    	  else
	    	  {
	    		  paidDate= " ";
	    	  }    	
	    	
	    	//log.info("$$$ paidDate="+paidDate);
	    	 if(length > 56){
	    		 invDate=temp.substring(48,56).trim();
	    	  }
	    	  else
	    	  {
	    		  invDate= " ";
	    	  }
	    		    	
	    	//log.info("$$$ invDate="+invDate);
	    	 if(length > 72){
	    		 invNo =temp.substring(56,72).trim();
	    	  }
	    	  else
	    	  {
	    		  invNo= " ";
	    	  }    	
	       
	    	//System.out.println("$$$ invNo="+invNo);
	    	 if(length > 86){
	    		 amount =temp.substring(72,86).trim();
	    		 String tempStrAmt = null;
	    		 String strAmt = null;
	    		 if(amount.equals("0"))
	 			{
	    			 amount = "$0.00";
	    			 strAmt ="0";
	 			}
	    		 else if(amount.length()== 2)
				 {
	    			 tempStrAmt ="$0."+amount;
	    			 strAmt = "0."+amount;
						//log.info("tempStrAmt:"+tempStrAmt);
						amount = tempStrAmt; 
				 }else if(amount.length()== 1)
				 {
	    			 tempStrAmt ="$0.0"+amount;
	    			 strAmt = "0.0"+amount;
						//log.info("tempStrAmt:"+tempStrAmt);
						amount = tempStrAmt; 
				 }
	 			else{
	 				tempStrAmt = "$"+amount.substring(0,amount.length()-2)+"." + amount.substring(amount.length()-2);
	 				strAmt = amount.substring(0,amount.length()-2)+"." + amount.substring(amount.length()-2);
	 				//log.info("tempStrAmt:"+tempStrAmt);
	 				amount=tempStrAmt;
	 				//log.info("tempAmt:"+tempAmt);
	 			}
	    		 tempAmt = Double.parseDouble(strAmt);
	    	  }
	    	  else
	    	  {
	    		  amount= " ";
	    	  }
	    	
	    	 //System.out.println("$$$ amount="+amount); 
			
			 if(length > 100){
				 tax=temp.substring(86,100).trim();
				
				 String tempStrTax = null;
				 String strTax = null;
				 if(tax.equals("0"))
					{
					 tax = "$0.00";
					 strTax ="0";
					}
				 else if(tax.length()== 2)
				 {
					  tempStrTax ="$0."+tax;
					  strTax ="0."+tax;
						//log.info("tempStrtax:"+tempStrTax);
						tax = tempStrTax; 
				 }
				 else if(tax.length()== 1)
				 {
					  tempStrTax ="$0.0"+tax;
					  strTax ="0.0"+tax;
						//log.info("tempStrtax:"+tempStrTax);
						tax = tempStrTax; 
				 }				 
					else{
						tempStrTax ="$"+tax.substring(0,tax.length()-2)+"." + tax.substring(tax.length()-2);
						strTax =tax.substring(0,tax.length()-2)+"." + tax.substring(tax.length()-2);
						//log.info("tempStrtax:"+tempStrTax);
						tax = tempStrTax;
					
					} 
				 tempTax = Double.parseDouble(strTax);
	    	  }
	    	  else
	    	  {
	    		  tax= " ";
	    	  }
					
	    	//log.info("$$$ tax="+tax);
	    	
	    	 if(length > 114){
	    		 useTax =temp.substring(100,114).trim();
	    		
	    		 String tempStrUseTax = null;
	    		 String strUseTax = null;
	    		 if(useTax.equals("0"))
	 			{
	    			 useTax =  "$0.00";
	    			 strUseTax ="0";
	 			}
	    		 else if(useTax.length()== 2)
				 {
	    			 tempStrUseTax ="$0."+useTax;
	    			 strUseTax = "0."+useTax;
						//log.info("tempStrUseTax:"+tempStrUseTax);
						useTax = tempStrUseTax; 
				 } 
	 			else{
	 			tempStrUseTax = "$"+useTax.substring(0,useTax.length()-2)+"." + useTax.substring(useTax.length()-2);
	 			strUseTax =useTax.substring(0,useTax.length()-2)+"." + useTax.substring(useTax.length()-2);
	 			//log.info("tempStrUseTax:"+tempStrUseTax);
	 			useTax = tempStrUseTax;
	 			
	 			}
	    		 tempUseTax = Double.parseDouble(strUseTax);
	    	  }
	    	  else
	    	  {
	    		  useTax= " ";
	    	  }
						
	    	//log.info("$$$ useTax="+useTax);
	    	 if(length > 128){
	    		 ship =temp.substring(114,128).trim();
	    		
	    		 String tempStrShip = null;
	    		 String strShip = null;
	    		 if(ship.equals("0"))
	 			{
	    			 ship = "$0.00";
	    			 strShip ="0";
	 			}
	    		 else if(ship.length()== 2)
				 {
	    			 tempStrShip ="$0."+ship;
	    			 strShip= "0."+ship;
						//log.info("tempStrShip:"+tempStrShip);
						ship = tempStrShip; 
				 }else if(ship.length()== 1) {
	    			 tempStrShip ="$0.0"+ship;
	    			 strShip= "0.0"+ship;
						//log.info("tempStrShip:"+tempStrShip);
						ship = tempStrShip; 
				 }
	 			else{
	 				tempStrShip = "$"+ship.substring(0,ship.length()-2)+"." + ship.substring(ship.length()-2);
	 				strShip =ship.substring(0,ship.length()-2)+"." + ship.substring(ship.length()-2);
	 				//log.info("tempStrShip:"+tempStrShip);
	 				ship = tempStrShip;
	 				
	 			}
	    		 if(strShip.contains("-")) {
	    			 return false;
	    		 } else	{
	    		   tempShip = Double.parseDouble(strShip);
	    		 }
	    	  }
	    	  else
	    	  {
	    		  ship= " ";
	    	  }
	    			
	    	//log.info("$$$ ship="+ship);
	    	if(length > 142){
	    		discount=temp.substring(128,142).trim();
	    		
	    		String tempStrDisc = null;
	    		String strDisc =null;
	    		//log.info("first discount:"+discount);
	    		if(discount.equals("0"))
				{
	    			discount = "$0.00";
	    			strDisc ="0";
				}
	    		 else if(discount.length()== 2)
				 {
	    			 tempStrDisc ="$0."+discount;
	    			 strDisc ="0."+discount;
					 discount = tempStrDisc; 
				 }
				else{
					if(discount.length()>1) {
						tempStrDisc = "$"+discount.substring(0,discount.length()-2)+"." + discount.substring(discount.length()-2);
						strDisc = discount.substring(0,discount.length()-2)+"." + discount.substring(discount.length()-2);
						discount =tempStrDisc;
					} else if(discount.length()== 1){
						tempStrDisc = "$0.0" + discount;
						strDisc = "0.0" + discount;
						//log.info("2c -tempStrDisc:"+tempStrDisc);						
					}
					discount =tempStrDisc;
				}
	    		tempDiscount = Double.parseDouble(strDisc);
	    	  }
	    	  else
	    	  {
	    		  discount= " ";
	    	  }
	    	    			
	    	//log.info("$$$ discount="+discount);
	    	if(length > 156){
	    		listPrice =temp.substring(142,156).trim();
	    	  }
	    	  else
	    	  {
	    		  listPrice= " ";
	    	  }
	    			
	    	//log.info("$$$ listPrice="+listPrice);
	    	if(length > 170){
	    		lien=temp.substring(156,170).trim();
	    	  }
	    	  else
	    	  {
	    		  lien= " ";
	    	  }   	
			
	    	//log.info("$$$ lien="+lien);
	    	
	    	if(length > 171){
	    		lienFlag =temp.substring(170,171).trim();
	    	  }
	    	  else
	    	  {
	    		  lienFlag= " ";
	    	  }  
	    	
	    		
	    	//log.info("$$$ lienFlag="+lienFlag);
	    	if(length > 172){
	    		status =temp.substring(171,172).trim();
	    	  }
	    	  else
	    	  {
	    		  status= " ";
	    	  } 
	    	 	
	    	//log.info("$$$ status="+status);
	    	
	    	if(length > 207){
	    		notes=temp.substring(172,207).trim();
	    	  }
	    	  else
	    	  {
	    		  notes= " ";
	    	  } 
	    	 	
	    	//log.info("$$$ notes="+notes);
	    	
	    	if(length > 212){
	    		vendorCode=temp.substring(207,212).trim();
	    	  }
	    	  else
	    	  {
	    		  vendorCode= " ";
	    	  } 
	    	 	
	    	
	    	//log.info("$$$ vendorCode="+vendorCode);
	    	
	    	if(length > 222){
	    		altVendorCode=temp.substring(212,222).trim();
	    	  }
	    	  else
	    	  {
	    		  altVendorCode= " ";
	    	  } 
	    		
			
			//log.info("$$$ altVendorCode="+altVendorCode);
	    	
			if(length > 222){
				vendorName=temp.substring(222,length).trim();
	    	  }
	    	  else
	    	  {
	    		  vendorName= " ";
	    	  } 
					
			//System.out.println("$$$ vendorName="+vendorName);
			
			//===============================
			if(!flagProblemQueue)
			{
				amountSum +=tempAmt;
				taxSum += tempTax;
				useTaxSum += tempUseTax;
				shipSum += tempShip;
				discSum +=tempDiscount;
			}
			//===============================
			
	        }//end of else for  if (voucherNo.length() < 3 && length >20)
	        //System.out.println("heyyy");
	        taxcode= getTaxCode(vendorCode.toUpperCase());
	        //System.out.println("$$$ taxcode="+taxcode + "="+flagProblemQueue);
			JSONObject obj = new JSONObject();
			obj.put("voucherNo", voucherNo);
			obj.put("recType", recType);
			obj.put("fundCode", fundCode);
			obj.put("subfundNo", subfundNo);
			obj.put("externalFund", externalFund);
			obj.put("paidDate", paidDate);
			obj.put("invDate", invDate);
			obj.put("invNo", invNo);
			obj.put("amount", amount);
			obj.put("tax", tax);
			obj.put("useTax", useTax);
			obj.put("ship", ship);
			obj.put("discount", discount);
			obj.put("listPrice", listPrice);
			obj.put("lien", lien);
			obj.put("lienFlag", lienFlag);
			obj.put("status", status);
			obj.put("notes", notes);
			obj.put("vendorCode", vendorCode);
			obj.put("altVendorCode", altVendorCode);
			obj.put("vendorName", vendorName);
			obj.put("taxcode", taxcode);
			/*if(fundCode != null)
			{
				if(fundCode.length()>1) 
				outFileContent.add(obj);
				
				//else if (flagLastLine)
				//{
				//	outFileContent.add(obj);
					
				//}
				else
				{
					problemQueue.add(obj);
				}
				
			}
			else
			{
				problemQueue.add(obj);
			}
			*/
			if(flagProblemQueue)
			{
				problemQueue.add(obj);
			}
			else
			{
				outFileContent.add(obj);
			}
			
			
					
		}//end of while
		return true;
	}//end of processOutFile()
	
	/*
	 * ==============================================
	 * This method will return the JSON object
	 * ==============================================
	 */
	
	public JSONObject getProcessingResults() {
		JSONObject results = new JSONObject();
		boolean hasData = false; // make sure there is at least data in one
		// queue
		JSONArray deletedArr = new JSONArray();
		//JSONArray problemArr = new JSONArray();
		results.put("rows", outFileContent);
		results.put("total", outFileContent.size());
		results.put("delRows", deletedArr);
		results.put("delTotal", deletedArr.size());
		results.put("ProblemRows", problemQueue);
		results.put("ProblemTotal", problemQueue.size());
		double sum= amountSum+taxSum+useTaxSum+shipSum+discSum;
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		double sumMod= Double.valueOf(twoDForm.format(sum));
		 Double dObj = new Double(sumMod);
		 /*log.info("$$$ amountSum="+amountSum);
		 log.info("$$$ taxSum="+taxSum);
		 log.info("$$$ useTaxSum="+useTaxSum);
		 log.info("$$$ shipSum="+shipSum);
		 log.info("$$$ discSum="+discSum);
		 log.info("$$$ sum="+sumMod);
		 log.info("$$$ dObj="+dObj);*/
		results.put("SUM",dObj );
		//results.put("SUM",sumMod );
		//log.info("rowsize : "+outFileContent.size());
		results.put("result", "success");
		return results;
	}
	
	private String getTaxCode(String vendcode)
	{
		String tax = null;
		InitialContext ctx = null;
		DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		/*try {
			ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/apbatch");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			
			log.error("$$$ Naming Exception",e);
			
			
		} 
		*/
		try{
			 // con = ds.getConnection();
			//  con = ConnectionManager.getConnection("apbatch");
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 
			  con = ApbatchConnection.getConnection();
			
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT TAXCODE FROM VENDORS WHERE VENDCODE LIKE '"+vendcode+"%'");
			  while(rs.next())
			  {
				  tax = rs.getString(1);
				 // log.info(" $$$$  TAX ="+tax);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException",e);			
		} 
 	
		
		return tax;
	}
	
	private String checkFundCode(String vendcode)
	{
		String tax = null;
		InitialContext ctx = null;
		DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		
		try{
			 // con = ds.getConnection();
			//  con = ConnectionManager.getConnection("apbatch");
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT TAXCODE FROM VENDORS WHERE VENDCODE LIKE '"+vendcode+"%'");
			  while(rs.next())
			  {
				  tax = rs.getString(1);
				 // log.info(" $$$$  TAX ="+tax);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException",e);			
		} 
 	
		
		return tax;
	}
	
}//end of class