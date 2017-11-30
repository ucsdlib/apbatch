package edu.ucsd.library.apbatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Calendar;
import java.text.DecimalFormat;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.ucsd.library.util.sql.ConnectionManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;

//import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import edu.ucsd.library.shared.Mail;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import javax.servlet.http.HttpSession;

import edu.ucsd.library.util.sql.EmployeeInfo;
import javax.sql.DataSource;

//import javax.mail.internet.*;
//import javax.mail.*;
/**
 * 
 * @author lib-kdushamali
 *
 */
public class SendDataToServer  {
	private static Logger log = Logger.getLogger( SendDataToServer .class );
	
	static String commonFileName = null;
	  private String userEmail;
	 // private String serverName = "dail.ucsd.edu";
	//  private String userName = "kdushamali";
	//  private String password = "qeJ29aHE";
	// public static int chargeRecordCount =0;
	  static int personRecordCount =0;
	  static int entityRecordCount =0;
	  static String username = null;
	  private static String propertiesFilePath;
	  
	  public static JSONObject sendOutputFiles(HttpServletRequest request,HttpServletResponse response, JSONArray results,String password,String username,String FTPusername){
		  log.info("=======SendDataToServer BEGIN===================");
		  boolean finalFlag = false;
		  boolean transferSuccess = false;
		  boolean insertSuccess= false;
		  boolean removeSuccess = false;
		  boolean sessionSuccess= false;
		  String errorMsg = "";
		  String userName =null;
		  JSONObject removeObj = new JSONObject();
		  userName = username;
		  try {
			  InitialContext context = new InitialContext();
			  propertiesFilePath = (String)context.lookup("java:comp/env/apbatchFilePath");
			  log.info("$$$ propertiesFilePath:"+propertiesFilePath);
		  }
		  catch(NamingException nee)
		  {
			  log.info("$$$ NamingException:"+nee);
		  }
		  	//if(results.size() == 0)
		  	//	return false;
		  	
		 //System.out.println("results---------"+results);
		  
		 // String userID=getUserID(userName);
		 //boolean insertSuccess= insertDataToDatabase( results,userID);
		 //JSONObject chargeJobj = generateChargeFileContent(results);
		 
		 //String chargeFileContent =(String) chargeJobj.get("chargeBuffer");
		// String cCount= (String)chargeJobj.get("chargeRecordCount");
		 //int chargeRecordCount = Integer.parseInt(cCount);
		 //log.info("$$$$$  chargeFileContent: "+chargeFileContent.length());
		 //log.info("$$$$$  chargeRecordCount: "+chargeRecordCount);
		// String fullname = getFullname(userName);
		 // log.info("Fullname is:"+fullname);
		//  String emailcontent =getEmailContent(fullname,chargeRecordCount);
		  
		  // try to send the 3 output files to server
		  //boolean success = BillingUtility.processApbatchData(request, response, results, true);
		  HttpSession session = request.getSession();
		  String transmitData = (session.getAttribute("transmitData") != null ) ? (String)session.getAttribute("transmitData") : null;
		  
		  if(transmitData != null) {
			  log.info("---transmit Data ---"+transmitData);
			  //transferSuccess = sendReportFilesToServer(results,password,FTPusername,BillingUtility.outStream.toString()); //run transfer
			  transferSuccess = sendReportFilesToServer(results,password,FTPusername,transmitData); //run transfer
		  } else {
			  errorMsg += "Please build the txt file first.";			  
		  }
		  log.info("$$$$ transferSuccess: "+transferSuccess);
		
		  
		 if (transferSuccess)
		 {
			 //insert into database
			 //insertSuccess= insertDataToDatabase( results,userID);
			 log.info("$$$$ insertSuccess: "+insertSuccess);
			//success = sendReportFilesToServer(results,password,FTPusername,chargeFileContent); //run transfer
			 /*if (!insertSuccess)
			 {
				 log.info("$$$$ insertion failed so going to remove files:"); 
				 //remove files from FTP server
				 removeObj =removeFiles(username,password);
				 String strRemoveSuccess =(String) removeObj.get("removeFlag");
				 removeSuccess = Boolean.parseBoolean(strRemoveSuccess);
				 errorMsg += "Insertion to DB failed! \n";
			 }
			 	
			 if(transferSuccess && insertSuccess)
			 { finalFlag = true;
			   
			 }	
			 
			 if(removeSuccess)
			 {
				 log.info("$$$$ removeSuccess: "+removeSuccess);
				 finalFlag = false;
				 String removeError =(String)removeObj.get("removeError");
				 errorMsg +=  removeError; 
			 }		 
			*/
			 finalFlag = true;	 
			 if(finalFlag)
			 {   
			   String emailAddress = getUserEmail(request);
			   log.info("$$$$ Useremail:"+emailAddress);
		       String emailcontent =getEmailContent(results, countLines(transmitData), emailAddress);
		       
		       try {
		         System.out.println("start to send mail");
	             //Mail.sendMail(emailAddress,strArr , "AP BATCH XLI4",emailcontent, "smtp.ucsd.edu");
	             Mail.sendMail(emailAddress, getEmail() , "AP BATCH XLI4",emailcontent, "smtp.ucsd.edu");
	           } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
               } 
			 }
			 
			 errorMsg +="Output File Transfer success!\n";
		 }//end of if(transferSuccess)
       
		 else{
			 //remove files from FTP server because transfer failed
			 if(!errorMsg.equals("Please build the txt file first.")) {
				 removeObj =removeFiles(username,password);
				 removeSuccess = Boolean.parseBoolean(removeObj.get("removeFlag").toString());
				 log.info("tranfer failure: removeSuccess="+removeSuccess);
				 errorMsg += "Output File Transfer Failed! \n";
			 } else {
				 log.info("tranfer failure: "+errorMsg);
			 }
			 finalFlag = false; 
		 }
		 
		 JSONObject finalObj = new JSONObject();
		 finalObj.put("finalFlag",finalFlag);
		 finalObj.put("errorMsg",errorMsg);
		 log.info("$$$$ finalFlag: "+finalFlag);
		 log.info("$$$$ errorMsg: "+errorMsg);
         log.info("=======SendDataToServer END===================");
         return finalObj;
	  }//end of run()
	
	  public static boolean sendReportFilesToServer(JSONArray results,String password,String username,String fileContent){
		    boolean flag = false;
			boolean flag1 = true;
			//boolean flag2 = true;
			//boolean flag3 = true;
			boolean retValue1 = false;
			 //boolean retValue2 = false;
			// boolean retValue3 = false;
			//FTPClient ftp;
	       // String serverName = "adcom.ucsd.edu";
	       //  String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
	         // generating file name
	         Calendar cal = Calendar.getInstance();
			    int day = cal.get(Calendar.DATE);
		        int month = cal.get(Calendar.MONTH) + 1;
		        int year = cal.get(Calendar.YEAR);
		        log.info("YEAR:"+year);
		        log.info("month:"+month);
		        log.info("day:"+day);
            										 
             double date1 = toJulian(new int[]{year,month,day});
		        double date2 = toJulian(new int[]{year,1,1});
		        int dif = (int) (date1-date2+1);
		        log.info("dif: " + dif + " days.");
             
		        String strYear =""+year;
		        String strDiff =""+dif;
		        for(int i=strDiff.length(); i < 3 ;i++)
		         {
		         	strDiff = "0"+strDiff;
		         }
		         
		      String filename = "D"+strYear.substring(2)+strDiff;
              log.info("File name: " + filename);  
	         
             retValue1 = sendFileToServer(results,password,username,fileContent,filename);
             //retValue2 =sendPersonFileToServer(results,password,username, filename); 
             //retValue3 = sendEntityFileToServer(results,password,username,filename);
           
             if (!retValue1){
				 flag1 = false;
				 log.info("Charge File uploaded FAILED");
			 }
				  
             else  
             {
            	 log.info("Charge File uploaded successful.");
            	 
             }
			/* if (!retValue2){
				 flag3 = false;
				 log.info("Person File uploaded FAILED");
			 }					  
             else {
            	 
            	 log.info("Person File uploaded successful.");
             }
            	  
			 if (!retValue3){
				 flag3 = false;
				 log.info("Entity File uploaded FAILED");
			 }
				  
             else{
            	 log.info("Entity File uploaded successful.");	            	
             }*/
			 
			 if(flag1)
			 {
				 flag = true;
			 }
			 else
				 flag = false;
			
			 log.info("FLAG1 := " +flag1);
			// log.info("FLAG2 := " +flag2);
			 //log.info("FLAG3 := " +flag3);
			 log.info("FLAG := " +flag);
			 
			 log.info("$$$$ FLAG is :"+flag);
			 return flag;
			 
			 
			 
			 /*
	         
	         
	         
	         
	         
	         
	         
	  	    ftp = new FTPClient();
	  	    ftp.setRemoteVerificationEnabled(false); 
	        log.info("username and password:"+username+" "+password);
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);        
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			   // JSONObject chargeJobj = generateChargeFileContent(results);
	           // String bufferChargeFile = generateChargeFileContent(results);
			    String bufferChargeFile =  chargeFileContent;
	            String bufferPersonFile = generatePersonFileContent();
	            String bufferEntityFile = generateEntityFileContent();
	            log.info("$$$$$ OUT content:"+bufferChargeFile);
	           try{ 
	            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
	            log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
	            PrintWriter out = new PrintWriter(htmlStream);
	            log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
	            out.write(bufferChargeFile);
	            log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
	            out.flush();
	            out.close();
				
	            
				 boolean retValue1 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream.toByteArray()));
				 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$");
				 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
				 
	             
	             
	             boolean flagP = ftp.changeWorkingDirectory("/SISP/ARD2502/LIBCIR/PERSON/");
		           log.info("flagP is:"+flagP);        
		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
		           
		          if(flagP)
		          {
		        	  ByteArrayOutputStream htmlStream1 = new ByteArrayOutputStream();
						 PrintWriter out1 = new PrintWriter(htmlStream1);
			             out1.write(bufferPersonFile);
			             out1.flush();
			             out1.close();
	             retValue2 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream1.toByteArray()));
	             log.info("$$$$$$$$$ RETVALUE2:"+retValue2);
	            
	             
		          }
		          boolean flagE = ftp.changeWorkingDirectory("/SISP/ARD2502/LIBCIR/ENTITY/");
		           log.info("flagE is:"+flagE);        
		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
		          
		           if(flagE)
			          {
		        	   ByteArrayOutputStream htmlStream2 = new ByteArrayOutputStream();
			             PrintWriter out2 = new PrintWriter(htmlStream2);
						 out2.write(bufferEntityFile);
			             out2.flush();
			             out2.close();
		           retValue3 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream2.toByteArray()));
				  log.info("$$$$$$$$$ RETVALUE3:"+retValue3);
			          }
				 if (!retValue1){
					 flag1 = false;
					 log.info("Charge File uploaded FAILED");
				 }
					  
	             else  
	             {
	            	 log.info("Charge File uploaded successful.");
	            	 
	             }
				 if (!retValue2){
					 flag3 = false;
					 log.info("Person File uploaded FAILED");
				 }					  
	             else {
	            	 
	            	 log.info("Person File uploaded successful.");
	             }
	            	  
				 if (!retValue3){
					 flag3 = false;
					 log.info("Entity File uploaded FAILED");
				 }
					  
	             else{
	            	 log.info("Entity File uploaded successful.");	            	
	             }
				 
				 if(flag1 && flag2 && flag3)
				 {
					 flag = true;
				 }
				 else
					 flag = false;
				
				 log.info("FLAG1 := " +flag1);
				 log.info("FLAG2 := " +flag2);
				 log.info("FLAG3 := " +flag3);
				 log.info("FLAG := " +flag);
				 ftp.logout();
				 ftp.disconnect();
			}//end of try
			 catch (FTPConnectionClosedException e)
		        {
				 flag1 = false;
		            log.info("Server closed connection.");
		           
		        }
		        catch (IOException e)
		        {
		        	 flag1 = false;
		        	 log.info("IO Exception %%%%%%%%%%%");
		        }
		        finally
		        {
		            if (ftp.isConnected())
		            {
		                try
		                {
		                    ftp.disconnect();
		                }
		                catch (IOException f)
		                {
		                    // do nothing
		                }
		            }
		        }
			 log.info("$$$$ FLAG is :"+flag);
			 return flag;
			 */
			}//end of sendReportFilesToServer()

	

	  
	  public static String getEmailContent(JSONArray results, int recordLength, String emailAddress)
	  {
			log.info("$$$$$$$$ INSIDE GETEMAIL");
			String amount = null, tax = null, useTax = null, discount = null, ship = null; 
			int total = 0;
			
			for(int i = 0; i< results.size();i++)
			{		
				JSONObject obj = (JSONObject)results.get(i);
				if(((String)obj.get("voucherNo")).equals("TOTAL")){
					amount =((String)obj.get("amount")).trim().replace(".","").replace("$","");
					tax =((String)obj.get("tax")).trim().replace(".","").replace("$","");
					useTax =((String)obj.get("useTax")).trim().replace(".","").replace("$","");
					ship =((String)obj.get("ship")).trim().replace(".","").replace("$","");
					discount =((String)obj.get("discount")).trim().replace(".","").replace("$","");
				}
			}
			
			total = Integer.parseInt(amount) + Integer.parseInt(tax) + Integer.parseInt(useTax) + Integer.parseInt(ship) + Integer.parseInt(discount);
			//totalCharges = Double.valueO;
	  		//DecimalFormat twoDForm = new DecimalFormat("#.##");
			//totalCharges = Double.valueOf(twoDForm.format(total));
			
			BigDecimal totalCharges = new BigDecimal(total).movePointLeft(2);
			
		/*  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
		  String todayStr = shortDf.format(new Date());
		  StringBuffer out= new StringBuffer();
		  String [] temp = todayStr.split("/");
			String fp = temp[0];
			if(fp.length()<2)
			{
				fp = "0"+fp;
			}
			String sp = temp[1];
			if(sp.length()<2)
			{
				sp = "0"+sp;
			}
			String tp = temp[2];
			if(tp.length()<2)
			{
				tp = "0"+tp;
			}	
			String today = tp+fp+sp;	
  */ // 1/28
			DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
			String todayStr = shortDf.format(new Date());
			 StringBuffer out= new StringBuffer();
		    Calendar cal = Calendar.getInstance();
		    int day = cal.get(Calendar.DATE);
	        int month = cal.get(Calendar.MONTH) + 1;
	        int year = cal.get(Calendar.YEAR);
	        log.info("YEAR:"+year);
	        log.info("month:"+month);
	        log.info("day:"+day);
      										 
	        double date1 = toJulian(new int[]{year,month,day});
	        double date2 = toJulian(new int[]{year,1,1});
	        int dif = (int) (date1-date2+1);
	        log.info("dif: " + dif + " days.");
       
	        String strYear =""+year;
	        String strDiff =""+dif;
	        for(int i=strDiff.length(); i < 3 ;i++)
	         {
	         	strDiff = "0"+strDiff;
	         }
		         
		      String today = strYear.substring(2)+strDiff;
		
			  out.append("DEPARTMENT:         UCSD Library");
			  out.append("\r\n");
			  if(emailAddress.startsWith("ssayavanh")) {
				  out.append("CONTACT NAME:       Sone Sayavanh");
				  out.append("\r\n");
				  out.append("EMAIL ADDRESS:      ssayavanh@ucsd.edu"); 
				  out.append("\r\n");
				  out.append("CONTACT PHONE:      858-534-7751");
				  out.append("\r\n");
				  out.append("CONTACT MAILCODE:   0175A");
			  } 
			  out.append("\r\n");
			  out.append("\r\n");
			  out.append("INPUT FILENAME:     FISP.XLI4.APCHECKS.INPUT");
			  out.append("\r\n");
			  out.append("FILE DESCRIPTION:   BATCH FILE FOR "+todayStr);
			  out.append("\r\n");	  
			  out.append("RECORD:             "+recordLength);  //need RECORD
			  out.append("\r\n");
			  out.append("RECORD LENGTH:      206");  //need record length
			  out.append("\r\n");
			  out.append("TOTAL:              $"+totalCharges);  //need total
			  out.append("\r\n");	  
			  out.append("DATE WHEN INPUT FILE IS TO BE PROCESSED: "+todayStr);
			  out.append("\r\n");
			  out.append("WHAT PROCESS DOES FILE GO INTO: APCHECKWRITE");
			  out.append("\r\n");	  
			  out.append("\r\n");
			  out.append("ADDITIONAL COMMENTS:");
			  out.append("\r\n");	  
			  out.append("\r\n");
			  out.append("PRODUCTION CONTROL INFORMATION");
			  out.append("\r\n");	  
			  out.append("==============================");
			  out.append("\r\n");
			  out.append("(FOR PRODUCTION CONTROL USE)");	  
			  out.append("\r\n");
			  out.append("\r\n");
			  out.append("\r\n");	
			  out.append("\r\n");
			  out.append("PRODUCTION LIBRARY/MEMBER NAME:");	  
			  out.append("\r\n");
			  out.append("\r\n");
			  out.append("PRODUCTION DATASET NAME");	
			  out.append("\r\n");	  
			  out.append("\r\n");
			  out.append("Thank you");
			  out.append("\r\n");
			  
			  return out.toString();
	  
		  
	  }
	  
	  
	   //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	   public static boolean insertDataToDatabase(JSONArray pending,String userID)
	   {
		   Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			String userid = null;
			
			String itemNum = null;
			 int maxTransID = 0;
			 int chargeNO = 0;
			 String barcode = null;
			 
			 
			 boolean flag = true;
			try {
				conn = ConnectionManager.getConnection("apbatch");
				stmt = conn.createStatement();
				
			}  catch (SQLException e) {
				// TODO Auto-generated catch block
				flag = false;
				log.error("SQLException in creating the connection", e);
			 } catch (NamingException e) {
				log.error("JNDI Lookup failed for DB2 connection", e);
			}
		  
		   for(int i = 0; i < pending.size(); i++){
				JSONObject row = (JSONObject)pending.get(i);
				String  invNumber =((String)row.get("invoiceNo")).trim();
				String pid =((String)row.get("pid")).trim();
				String  invoiceDate =((String)row.get("date")).trim();
				String  chargeLoc =((String)row.get("loc")).trim();
				String  chargeTypee =((String)row.get("chargeType")).trim();
				String  chargeFee =((String)row.get("amount1")).trim();
				String  processingFee =((String)row.get("amount2")).trim();
				String  billingFees =((String)row.get("amount3")).trim();
				String  patronNumber =((String)row.get("patronRecordNo")).trim();
				String  barcodeTemp =((String)row.get("itemBarcode")).trim();
				String title =((String)row.get("title")).trim();
				String callNo =((String)row.get("callNo")).trim();
				String pName =((String)row.get("name")).trim();
				String aff =((String)row.get("patronAffliation")).trim();
				String pType =((String)row.get("patronType")).trim();
				
				
				log.info("invNumber: "+invNumber);
				log.info("invoiceDate "+invoiceDate);
				log.info("chargeLoc: "+chargeLoc);
				log.info("chargeTypee: "+chargeTypee);
				log.info("processingFee: "+processingFee);
				log.info("billingFees: "+billingFees);
				log.info("patronNumber: "+patronNumber);
				log.info("barcodeTemp: "+barcodeTemp);
				log.info("title: "+title);
				log.info("callNo: "+callNo);
				log.info("name: "+pName);
				log.info("aff: "+aff);
				log.info("pType: "+pType);
				
				 //===================================
				 double chargeTotal = Double.parseDouble(chargeFee.substring(1));	
				 log.info("chargeTotal"+chargeTotal);
				 double finalAmtCharge = 0;
				 /*
				 if(chargeTotal >0)
		           {
		         String strAmt = ""+chargeTotal;
		         int index = strAmt.indexOf(".");
		         String newStr = strAmt.substring(0,index);
		         log.info("newStr"+newStr);
		         String newStr2 = newStr.substring(0,newStr.length()-2)+"."+newStr.substring(newStr.length()-2);
		         log.info("newStr2"+newStr2);
		         finalAmtCharge = Double.parseDouble(newStr2);
		        
					  finalAmtCharge =  chargeTotal;
		           }
				  else
				  {	  finalAmtCharge =  chargeTotal;}
				  */
				  finalAmtCharge =  chargeTotal;
				  log.info("finalAmtCharge"+finalAmtCharge);
				  //===================================
				  double finalAmtprocessingFee = 0;
				 double processingFeeTotal = Double.parseDouble(processingFee.substring(1));	
				 log.info("processingFeeTotal"+processingFeeTotal);
				/* if(processingFeeTotal >0)
		           {
		         String strAmt1 = ""+processingFeeTotal;
		         int index1 = strAmt1.indexOf(".");
		         String newStr1 = strAmt1.substring(0,index1);
		         log.info("newStr1"+newStr1);
		         String newStr3 = newStr1.substring(0,newStr1.length()-2)+"."+newStr1.substring(newStr1.length()-2);
		         log.info("newStr3"+newStr3);
		         finalAmtprocessingFee = Double.parseDouble(newStr3);
		         //log.info("finalAmtprocessingFee"+finalAmtprocessingFee);
		           }
				
				 else {	  finalAmtprocessingFee =  processingFeeTotal;}
				 */
				 finalAmtprocessingFee =  processingFeeTotal;
				 log.info("finalAmtprocessingFee"+finalAmtprocessingFee);
				  //========================================
				 
				  double finalAmtBillingFees = 0;
					 double billingFeesTotal = Double.parseDouble(billingFees.substring(1));	
					 log.info("billingFeesTotal"+billingFeesTotal);
					/*
					  if(billingFeesTotal >0)
			           {
			         String strAmt4 = ""+billingFeesTotal;
			         int index1 = strAmt4.indexOf(".");
			         String newStr4 = strAmt4.substring(0,index1);
			         log.info("newStr4"+newStr4);
			         String newStr5 = newStr4.substring(0,newStr4.length()-2)+"."+newStr4.substring(newStr4.length()-2);
			         log.info("newStr5"+newStr5);
			         finalAmtBillingFees = Double.parseDouble(newStr5);
			         //log.info("finalAmtBillingFees"+finalAmtprocessingFee);
			           }
					
					 else {	  finalAmtBillingFees =  billingFeesTotal;}
					 */
					 finalAmtBillingFees =  billingFeesTotal;
					 log.info("finalAmtBillingFees"+finalAmtBillingFees);
					  //========================================
				 
				
				//String  invoiceDate =((String)row.get("date")).trim();
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				 if (barcodeTemp.length() > 2)
				 {
					 int t= barcodeTemp.indexOf("b");
					 if(t == 0)
					 {
						 barcode = barcodeTemp.substring(1); 
							
					 }
					 else
					 {
						 barcode = barcodeTemp; 
					 }
					// barcode = barcodeTemp.substring(1); 
				
				
				 log.info("barcode: "+barcode);
				try {
					 rs = stmt.executeQuery(" SELECT ITEMNO FROM ITEMS WHERE BARCODE="+ "'"+barcode+"'");
					 
					while (rs.next()) {
						itemNum = rs.getString(1);
					log.info("ITEMNO: "+itemNum);
					}
					
					
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("NumberFormatException", e);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("SQLException", e);
				} 
				
				 }//end  if (barcodeTemp.length() > 2)
				 else{
					 //if no barcode insert a record to ITEMS table and then use that new itemno
					 try{
						  rs = stmt.executeQuery("SELECT MAX(itemNo) FROM ITEMS");
							long maxItemNo =0;
							while (rs.next()) {
								maxItemNo = rs.getLong(1);
							 System.out.println("itemNo:"+maxItemNo);
							// System.out.println("byeee");
							}
							
							 PreparedStatement pstmt = conn.prepareStatement(
									    "INSERT INTO ITEMS ( ITEMNO,BARCODE, TITLE,CALLNUMBER ) " +
									    " values (?, ?, ?, ?)");
	                        
							 
							    pstmt.setLong( 1, (maxItemNo+1) );
							    pstmt.setString( 2,barcodeTemp ); 
							    pstmt.setString( 3, title ); 
							    pstmt.setString( 4, callNo);
							    log.info("+++++++ INSERTING INTO ITEMS +++++++++++++");
							    log.info("$$$$$ (maxItemNo+1): "+(maxItemNo+1));
							    log.info("$$$$$ barcodeTemp: "+(barcodeTemp+1));
							    log.info("$$$$$ title: "+title);
							    log.info("$$$$$ callNo: "+callNo);
							    
							    pstmt.execute();
							    
							    conn.commit();
							  
							    long newItemlong = maxItemNo+1;
							    itemNum = ""+newItemlong;
							   log.info("$$$$$ new iTem no was: "+newItemlong);	
							   log.info("$$$$$$$$   Inserted the record to ITEMS table...");	
					 }
					 catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							flag = false;
							log.error("NumberFormatException", e);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							flag = false;
							log.error("SQLException", e);
						} 
						
				 }//end of else part for if(barcode
				 
				 
				//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				try{
				rs = stmt.executeQuery(" SELECT MAX(TRANSACTIONNO) FROM TRANSACTIONS");
				
				while (rs.next()) {
					   maxTransID = rs.getInt(1);
					   log.info("max item id: "+maxTransID);
				}
				
				
				  rs = stmt.executeQuery("SELECT CHARGETYPE FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"+ "'" + chargeTypee + "%'");
					
					while (rs.next()) {
						chargeNO = rs.getInt(1);
						log.info("chargeNO: "+chargeNO);
					}
				
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("NumberFormatException", e);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("SQLException", e);
				} 
				//======= 01/28 insert new patrons====================
				try{
					String patronNoN = patronNumber.substring(1);
				
				rs = stmt.executeQuery(" SELECT count(*)FROM PATRONS WHERE PATRONNO ="+"'" +patronNoN+"'");
			    int count1 = 0;
				while (rs.next()) {
					 count1 = rs.getInt(1);				
					 log.info("count1="+count1);
				}
				int maxPatronId =0;
				if(count1 == 0)
				{
					// get maximum patron id and insert the patron to Patron table
					rs = stmt.executeQuery(" SELECT MAX(PATRONID) FROM PATRONS");
					
					while (rs.next()) {
						maxPatronId = rs.getInt(1);
						 log.info("maxPatronId: "+maxPatronId);
					}
					
					//get patron no substrin 1
					PreparedStatement pstmt = conn.prepareStatement(
						    "INSERT INTO PATRONS ( PATRONNO,PID,PATRONNAME,PATRONTYPE,AFFILIATION,PATRONID,NOTES) " +
						    " values (?, ?, ?, ?, ? ,?,?)");
					
					pstmt.setString( 1,patronNumber.substring(1));
				    pstmt.setString( 2,pid ); 
				    pstmt.setString( 3, pName ); 
				    pstmt.setInt( 4, Integer.parseInt(pType));
				    pstmt.setInt( 5, Integer.parseInt(aff));
				    pstmt.setInt( 6, (maxPatronId+1));
				    pstmt.setString( 7, "" ); 
				    log.info("+++++++ INSERTING INTO PATRONS +++++++++++++");
				    log.info("$$$$$ patronNumber: "+patronNumber.substring(1));
				    log.info("$$$$$ pid: "+pid);
				    log.info("$$$$$ pName: "+pName);
				    log.info("$$$$$ pType: "+ Integer.parseInt(pType));
				    log.info("$$$$$ Aff: "+Integer.parseInt(aff));
				    log.info("$$$$$ (maxPatronId+1): "+(maxPatronId+1));
				    
				    pstmt.execute();					    
				    log.info("executed  good 1...");
				}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					 log.info("SQLException  when inserting patrons...");
					 log.error("SQLException", e);
					flag = false;
				}
				//==========insert part=================
				try {
					String patronNumberNew = patronNumber.substring(1);
					  PreparedStatement pstmt = conn.prepareStatement(
							    "INSERT INTO TRANSACTIONS ( TRANSACTIONNO,INVOICENO, INVOICEDATE,CHARGELOCATION,CHARGETYPE,CHARGE,PROCESSINGFEE,BILLINGFEE,PATRONNO,ITEMNO,ADDEDDATE,PROCESSDATE,USERID ) " +
							    " values (?, ?, ?, ?, ? ,?,?,?,?,?,?,?,?)");
					 
					  String strInvDate ="20"+invoiceDate.substring(0,2)+"-"+invoiceDate.substring(2,4)+"-"+invoiceDate.substring(4);
					  log.info("strInvDate"+strInvDate);
					  java.sql.Date jsqlD = java.sql.Date.valueOf( strInvDate );
					  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
					  String todayStr = shortDf.format(new Date());
					  String [] temp = todayStr.split("/");
					  String newArray = "20"+temp[2]+"-"+temp[0]+"-"+temp[1];
					  System.out.println("newarray:"+newArray);
					  java.sql.Date when = java.sql.Date.valueOf( newArray);
					  log.info("when:"+when);
					  if (itemNum == null)
					  {
						  itemNum = "000000";
					  }
					    pstmt.setLong( 1, (maxTransID+1) );
					    pstmt.setString( 2,invNumber ); 
					    pstmt.setDate( 3, jsqlD ); 
					    pstmt.setString( 4, chargeLoc);
					    pstmt.setInt( 5, chargeNO);
					    pstmt.setDouble( 6, finalAmtCharge);
					    pstmt.setDouble( 7, finalAmtprocessingFee ); 
					    pstmt.setDouble(8, finalAmtBillingFees ); 
					    pstmt.setString(9, patronNumberNew ); 
					    pstmt.setString(10, itemNum ); 
					  	pstmt.setDate( 11,when ); 
					    pstmt.setDate( 12, when );
					    pstmt.setString( 13, userID );
					   					    
					   // conn.commit();
					    log.info("+++++++ INSERTING INTO TRANSACTIONS +++++++++++++");
					    log.info("$$$$$  (maxTransID+1) : "+ (maxTransID+1) );
					    log.info("$$$$$ invNumber: "+invNumber);
					    log.info("$$$$$ jsqlD: "+jsqlD);
					    log.info("$$$$$ chargeLoc: "+chargeLoc);
					    log.info("$$$$$ finalAmtCharge: "+finalAmtCharge);
					    log.info("$$$$$ finalAmtprocessingFee: "+finalAmtprocessingFee);
					    log.info("$$$$$ finalAmtBillingFees: "+finalAmtBillingFees);
					    log.info("$$$$$ patronNumberNew: "+patronNumberNew);
					    log.info("$$$$$ itemNum: "+itemNum);
					    log.info("$$$$$ when: "+when);
					    log.info("$$$$$ userid: "+userID);
					    
					    pstmt.execute();
					    log.info("executed  good...");
					  
					  
					  
					  
				
		   } catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				flag = false;
				log.error("NumberFormatException", e);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				 log.info("SQLException  333333...");
				 log.error("SQLException", e);
				flag = false;
				try{ if (conn != null) {
				        conn.rollback();
				        
				      }
				
				}
				 catch (SQLException eg) { log.info("Connection rollback MAIN...");
				 log.error("SQLException", eg);
				 }
			} 
				
				
				
				
		   }//end of for
		   
		  try{
			   conn.commit();
			   conn.close();
			   
		   } catch (SQLException e) {
				// TODO Auto-generated catch block
				flag = false;
				try{ if (conn != null) {
				        conn.rollback();
				        conn.close();
				      }
				
				}
				 catch (SQLException eg) { System.out.println("Connection rollback MAIN...");
				 log.error("SQLException", eg);
				 }
			} 
	   log.info("flag from insert:"+flag);
		   return flag;
	   }
	   
	//============================================================
   // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
  //================================================================
	  private static String getFourthCharacter(char c)
	  {
	  	String code = null;
	  	switch (c) {
	  	case '8': code = "L";
	  			  break;
	  	case '9': code = "L";
	  			  break;
	  	case 'a': code = "C";
	  			  break;
	  	case 'b': code = "B";
	  	  		  break;
	  	case 'c': code = "C";
	  	  		  break;
	  	case 'e': code = "E";
	  	  		  break;
	  	case 'f': code = "C";
	  	  		  break;
	  	case 'g': code = "G";
	  			  break;
	  	case 'i': code = "R";
	  			  break;
	  	case 'l': code = "L";
	  	  		  break;
	  	case 'm': code = "M";
	  	  		  break;
	  	case 's': code = "I";
	  	  		  break;
	  	case 'o': code = "L";
	  	  		  break;
	  	case 'O': code = "L";
	  			  break;
	  	}
	  	return code;
	  }

	  private static String getSixthCharacter(int c)
	  {
	  	String s = null;
	  	if (c == 1 || c == 23 || c == 24 || c == 25 || c == 26 || c == 27 || c == 32 || c == 33 || c == 34 || c == 35 || c == 36 || c == 37 || c == 38 || c == 39 || c == 40 || c == 41 || c == 44)
	  		s= "X";
	  	else if (c == 2 || c == 42)
	  		s = "G";
	  	else if (c == 3 || c == 22)
	  		s = "Z";
	  	else if (c == 4 || c == 5 || c == 6 || c == 7 || c == 8 || c == 9 || c == 11 || c == 19 || c == 43)
	  		s = "O";
	  	else if (c == 16 || c == 17)
	  		s = "S";
	  	
	  	return s;
	  }

	  private static char getLastCharPositive(char c){
	  	char newC = ' ';
	  	switch (c){
	  	case '0' : newC = '{';break;
	  	case '1' : newC = 'A';break;
	  	case '2' : newC = 'B';break;
	  	case '3' : newC = 'C';break;
	  	case '4' : newC = 'D';break;
	  	case '5' : newC = 'E';break;
	  	case '6' : newC = 'F';break;
	  	case '7' : newC = 'G';break;
	  	case '8' : newC = 'H';break;
	  	case '9' : newC = 'I';break;
	  	
	  	}
	  	return newC;
	  }

	  private static char getLastCharNegative(char c){
	  	char newC = ' ';
	  	switch (c){
	  	case '0' : newC = '}';break;
	  	case '1' : newC = 'J';break;
	  	case '2' : newC = 'K';break;
	  	case '3' : newC = 'L';break;
	  	case '4' : newC = 'M';break;
	  	case '5' : newC = 'N';break;
	  	case '6' : newC = 'O';break;
	  	case '7' : newC = 'P';break;
	  	case '8' : newC = 'Q';break;
	  	case '9' : newC = 'R';break;
	  	
	  	}
	  	return newC;
	  }
	  private static String getRecordCount(int noOfRecords){
	  	
	  	String str = ""+ noOfRecords;
	  	int len = str.length();
	  	String finalString = str;
	  	for(int i =0 ;i <6-len; i++ )
	  	{
	  		finalString = "0"+finalString; 
	  		
	  	}
	  	
	  	return finalString;
	  	
	  }
	  private static String getTotalCharges(double totalCharges){
	  	String finalString = null;
	  	if (totalCharges < 0)
	  	{
	  		totalCharges = totalCharges - (totalCharges *2);
	  		log.info("totalCharges :"+totalCharges);
	
	  		DecimalFormat twoDForm = new DecimalFormat("#.##");
			totalCharges = Double.valueOf(twoDForm.format(totalCharges));
			log.info("totalCharges New :"+totalCharges);
			 String str = ""+ totalCharges;
			 log.info("TOTAL: ="+str);
		  	   int w= str.indexOf(".");
			   log.info("str:"+str);
			   log.info("w:"+w);
			   String sTemp2=str.substring(w+1);
			   log.info("sTemp2"+sTemp2);
			   if(sTemp2.length()< 2)
			   {
				   //that menas only one deciamal point
				   str = str + "0";
				   log.info("BILLING UTILITY inside <:"+str);
			   }
			   log.info("TOTAL new: ="+str);
	  		
	  		int p= str.indexOf(".");
	  		String s2= str.substring(0,p)+ str.substring(p+1);
	  		finalString = s2;
	  		//	int len = s2.length();
	  		
	  	//=========2/18======chandana====
	  		
	  		 char lastChar = finalString.charAt(finalString.length()-1);
		  	 log.info("if(amount<0): lastChar:"+lastChar);
		  	 char newLastChar = getLastCharNegative(lastChar);
		  	  log.info("BILLING UTILITY newLastChar:"+newLastChar);
		    // remove last char
		  	String s1 = finalString.substring(0,finalString.length()-1);
		  	log.info("BILLING UTILITY s1:"+s1);
		  	finalString = s1+newLastChar;
		  	log.info("FINAL finalString:"+finalString);		
		  	int len = finalString.length();
	  		//================================= 		
		  	//for(int i =0 ;i <10 -len; i++ )
	  		for(int i =0 ;i <11 -len; i++ )
	  		{
	  			finalString = "0"+finalString; 
	  			
	  		}
	  		//finalString += "}";
	  	}
	  	
	  	else
	  	{
	  		log.info("totalCharges :"+totalCharges);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			totalCharges = Double.valueOf(twoDForm.format(totalCharges));
			log.info("totalCharges New :"+totalCharges);
	  		String str = ""+ totalCharges;
	  		log.info("TOTAL: ="+str);
	  		int w= str.indexOf(".");
	  	   log.info("str:"+str);
	  	   log.info("w:"+w);
	  	   String sTemp2=str.substring(w+1);
	  	   log.info("sTemp2"+sTemp2);
	  	   if(sTemp2.length()< 2)
	  	   {
	  		   //that menas only one deciamal point
	  		   str = str + "0";
	  		   log.info("BILLING UTILITY inside <:"+str);
	  	   }
	  		int p= str.indexOf(".");
	  		String s2= str.substring(0,p)+ str.substring(p+1);
	  		finalString = s2;
	 	    //=========2/18======chandana====
	  		
	 		 char lastChar = finalString.charAt(finalString.length()-1);
		  	 log.info("if(amount<0): lastChar:"+lastChar);
		  	 char newLastChar = getLastCharPositive(lastChar);
		  	  log.info("BILLING UTILITY newLastChar:"+newLastChar);
		    // remove last char
		  	String s1 = finalString.substring(0,finalString.length()-1);
		  	log.info("BILLING UTILITY s1:"+s1);
		  	finalString = s1+newLastChar;
		  	log.info("FINAL finalString:"+finalString);		
		  	int len = finalString.length();
	 		//================================= 
			
	  		//for(int i =0 ;i <10 -len; i++ )	
	  		for(int i =0 ;i <11 -len; i++ )  		
	  	  		{
	  			finalString = "0"+finalString; 
	  			
	  		}
	  		//finalString += "{";
	  	}
	  	
	  	
	  	return finalString;
	  	
	  }
	
	  public static JSONObject removeFiles(String username,String password)
      {
		    FTPSClient ftp = null;
	        boolean flag1 = true;
            JSONObject obj =new JSONObject();
            int reply;
			String removeError = null;
			boolean  deleteFlagCharge=true;
		   // boolean  deleteFlagPerson=false;
			//boolean  deleteFlagEntity=false;
			  log.info("============Inside removeFiles================");
	         
	  	    //ftp = new FTPSClient();
	  	   // ftp.setRemoteVerificationEnabled(false); 
	  	 
	  	/*try {
			   try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
	  	  log.info("username and password:"+username+" "+password);
	  	  ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);        
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			*/ 
		
				//delete the file
				//deleteFlagCharge = ftp.deleteFile( "CHARGES.txt") ;
				//deleteFlagEntity = ftp.deleteFile( "PERSON.txt") ;
				//deleteFlagEntity = ftp.deleteFile( "ENTITY.txt") ;
				deleteFlagCharge=deleteChargeFileFromServer(password,username);
				//deleteFlagPerson=deletePersonFileFromServer(password,username);
				//deleteFlagCharge=deleteEntityFileFromServer(password,username);
				
				if(!deleteFlagCharge)
				{
					removeError="INPUT.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}
				/*if(!deleteFlagPerson)
				{
					removeError +="PERSON.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}
				if(!deleteFlagCharge)
				{
					removeError += "ENTITY.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}*/
				
				 //ftp.logout();
				// ftp.disconnect();
				
		    
	            
			boolean removeFlag = false;
			//if ( deleteFlagCharge && deleteFlagPerson && deleteFlagEntity )
			if ( deleteFlagCharge)
			{
				removeFlag = true;
			}
			
			obj.put("removeFlag",removeFlag);
			return obj;
      
  }
	  
	  public static  java.sql.Date getCurrentJavaSqlDate() {
		    java.util.Date today = new java.util.Date();
		    return new java.sql.Date(today.getTime());
		  }

	  
	  public static double toJulian(int[] ymd) {
			int JGREG= 15 + 31*(10+12*1582);
		 double HALFSECOND = 0.5;

			   int year=ymd[0];
			   int month=ymd[1]; // jan=1, feb=2,...
			   int day=ymd[2];    
			   int julianYear = year;
			   if (year < 0) julianYear++;
			   int julianMonth = month;
			   if (month > 2) {
			     julianMonth++;
			   }
			   else {
			     julianYear--;
			     julianMonth += 13;
			   }
			   
			   double julian = (java.lang.Math.floor(365.25 * julianYear)
			        + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
			   if (day + 31 * (month + 12 * year) >= JGREG) {
			     // change over to Gregorian calendar
			     int ja = (int)(0.01 * julianYear);
			     julian += 2 - ja + (0.25 * ja);
			   }
			   return java.lang.Math.floor(julian);
			 }
	  
	  
	  
	  
	  
	  
	  public static boolean sendFileToServer(JSONArray results,String password,String username,String fileContent,String filename){
		  FTPSClient ftp=null;
	     /* String serverName = "adcom.ucsd.edu";
	      String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
	      String newFileName = "'SISP.ARD2502.LIBCIR.CHARGE."+filename+"'";
	      
		 //======= Test FTP===============  
		  String serverName =  "dail.ucsd.edu";
	         String pathname = "/pub/data2/ftp/";
	        
	         //======= END Test FTP=============== */
		  //String newFileName = "'FISP.XLI1.APCHECKS."+filename+"'";
		  String newFileName = "'FISP.XLI4.APCHECKS.INPUT'";
		  String serverName = null;
		  String pathname = null;
		  try{
	      InitialContext context = new InitialContext();
	      serverName =
	          (String)context.lookup("java:comp/env/apBatchServer/hostname");
	      pathname = 
	         	 (String)context.lookup("java:comp/env/apBatchServer/path");
	      log.info("$$$ fileName:"+newFileName);
	      log.info("$$$ serverName:"+serverName);
	      log.info("$$$ pathname:"+pathname);
	      pathname = pathname+"INPUT";
	      log.info("$$$ pathnameMod:"+pathname);
		  }
		  catch(NamingException nee)
		  {
			  log.info("$$$ NamingException:"+nee);
		  }
	        // String newFileName = "CHARGES.txt";
	         String protocol = "SSL";
	         boolean retValue1 =  false;
	         log.info("$$$$$$ BEGIN  Sending Charge file :)");
	     /*============================+++++++++++++++++++++++++++=======================
		        try{
		        	 log.info("$$$ 1111111111111111");
		        	 ftp = new FTPSClient("SSL",false);
		        	 ftp.setRemoteVerificationEnabled(false);
		        	 log.info("$$$222222222");
		         }
			  	   catch(NoSuchAlgorithmException ne)
			  	   {
			  		 log.info("$$$$$ NoSuchAlgorithmException"); 
			  	   }
				try {
				   int reply;
				   log.info("$$$ BEFORE CONNECTING TO SERVER");
		           ftp.connect(serverName);
		           log.info("$$$ 333333333333333333");
		           log.info("Connected to " + serverName + ".");
		           reply = ftp.getReplyCode();
		           log.info("$$$ reply:"+reply);
		           ftp.execPBSZ(0);
		           ftp.execPROT("P");
		           ftp.login(username, password);
		           log.info("$$$ LOGGED TO THE SERVER");
		          
		         //  log.info("Logged in with the usernmae and password "+username+" "+password);
		           ftp.enterLocalPassiveMode(); 
		          // ftp.setDataTimeout(600000000);
		          
		           
		           if (!FTPReply.isPositiveCompletion(reply))
		           {
		               ftp.disconnect();
		               System.err.println("FTP server refused connection.");
		               System.exit(1);
		           }
		           ftp.changeToParentDirectory();
		           boolean flagg = ftp.changeWorkingDirectory(pathname);
		           log.info("Flaggg is:"+flagg);    
		           //ftp.execPBSZ( 0 );
		           //ftp.execPROT( "P" );

		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
				}
				 catch (IOException e)
			        {
					 log.info("$$$$$ IO Exception in connecting/ logging to the server");
			        } 
				//=============++++++++++++++++++++++++++++++++++++==============================
				  
				  */
	            try {
						 ftp = new FTPSClient("SSL",false);
						 ftp.setRemoteVerificationEnabled(false);
					 try {
						 log.info("$$$ BEFORE CONNECTING TO SERVER");
						 ftp.connect(serverName);
						
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log.info("Connected to " + serverName + ".");
				        int  reply = ftp.getReplyCode();
				        log.info("$$$ reply:"+reply);
				        try {
							ftp.execPBSZ(0);
						} catch (SSLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				         try {
							ftp.execPROT("P");
						} catch (SSLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        try {
							ftp.login(username, password);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log.info("$$$ LOGGED TO THE SERVER");
				       
				       
						try{
						ftp.enterLocalPassiveMode(); 
						 ftp.changeToParentDirectory();
				           boolean flagg = ftp.changeWorkingDirectory(pathname);
				           log.info("Flaggg is:"+flagg);    
				           ftp.setFileType(FTP.ASCII_FILE_TYPE,FTP.CARRIAGE_CONTROL_TEXT_FORMAT);
				           //ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
						} catch (IOException e)
				        {
							 log.info("$$$$$ IO Exception ");
					        } 
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
				    String bufferChargeFile =  fileContent;
				    
				    log.info("$$$$$ OUT content:"+bufferChargeFile);
			           try{ 
			        	
			            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
			            //log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
			            PrintWriter out = new PrintWriter(htmlStream);
			            //log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
			            out.write(bufferChargeFile);
			            //log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
			            out.flush();
			            out.close();
						
			            
						 retValue1 =  ftp.storeFile(newFileName, new ByteArrayInputStream(htmlStream.toByteArray()));
						 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$"+ftp.getReplyString());
						 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
				 
						 ftp.logout();
						 ftp.disconnect();
					}//end of try
					 catch (FTPConnectionClosedException e)
				        {
						      log.info("Server closed connection.");
				           
				        }
				        catch (IOException e)
				        {
				        	 e.printStackTrace();
				        	 log.info("IO Exception %%%%%%%%%%%");
				        }
				        finally
				        {
				            if (ftp.isConnected())
				            {
				                try
				                {
				                    ftp.disconnect();
				                }
				                catch (IOException f)
				                {
				                    // do nothing
				                }
				            }
				        }
				    
				        log.info("$$$$$$ END  Sending Charge file :)"+retValue1);
				return   retValue1;	    
				    
				    
	  }
	  
	  	  
  public static boolean deleteChargeFileFromServer(String password,String username){
	  FTPSClient ftp=null;
     /*String serverName = "adcom.ucsd.edu";
      String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
      */
	  Calendar cal = Calendar.getInstance();
	    int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH) + 1;
      int year = cal.get(Calendar.YEAR);
      log.info("YEAR:"+year);
      log.info("month:"+month);
      log.info("day:"+day);
  										 
   double date1 = toJulian(new int[]{year,month,day});
      double date2 = toJulian(new int[]{year,1,1});
      int dif = (int) (date1-date2+1);
      log.info("dif: " + dif + " days.");
   
      String strYear =""+year;
      String strDiff =""+dif;
      for(int i=strDiff.length(); i < 3 ;i++)
       {
       	strDiff = "0"+strDiff;
       }
       
      //String filename = "D"+strYear.substring(2)+strDiff;
      //log.info("File name: " + filename);  
      //String newFileName = "'SISP.ARD2502.LIBCIR.CHARGE."+filename+"'";
      
      String newFileName = "'FISP.XLI4.APCHECKS.INPUT'";
     /*
	  String serverName =  "dail.ucsd.edu";
      String pathname = "/pub/data2/ftp/"; */
      // String newFileName = "CHARGE.txt";
       String serverName = null;
 	  String pathname = null;
 	  try{
       InitialContext context = new InitialContext();
       serverName =
           (String)context.lookup("java:comp/env/apBatchServer/hostname");
           pathname = 
          	 (String)context.lookup("java:comp/env/apBatchServer/path");
       log.info("$$$ serverName:"+serverName);
       log.info("$$$ pathname:"+pathname);
       pathname = pathname+"INPUT";
       log.info("$$$ pathnameMod:"+pathname);
       
 	  }
 	  catch(NamingException nee)
 	  {
 		  log.info("$$$ NamingException:"+nee);
 	  }
       
      String protocol = "SSL";
	  boolean retValue1 =  false;
         log.info("$$$$$$ BEGIN  Deleting Input file :)");
	  	 /* 
	        try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	         //  log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ( 0 );
	           ftp.execPROT( "P" );       
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			 */
         //==== new========
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg+"-pathname"+pathname);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE, FTP.CARRIAGE_CONTROL_TEXT_FORMAT);
	           //ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
         
         //======================
			  try{
				    retValue1 = ftp.deleteFile(newFileName) ;
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  deleting INPUT file :)"+retValue1);
			return   retValue1;	    
			    
			    
  }
  

  public static boolean deleteEntityFileFromServer(String password,String username){
	  FTPSClient ftp=null;

	  Calendar cal = Calendar.getInstance();
	  int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH) + 1;
      int year = cal.get(Calendar.YEAR); 		 
      double date1 = toJulian(new int[]{year,month,day});
      double date2 = toJulian(new int[]{year,1,1});
      int dif = (int) (date1-date2+1);
      log.info("dif: " + dif + " days.");   
      String strYear =""+year;
      String strDiff =""+dif;
      for(int i=strDiff.length(); i < 3 ;i++)
       {
       	strDiff = "0"+strDiff;
       }
       
      String filename = "D"+strYear.substring(2)+strDiff;
      log.info("File name: " + filename);  
      String newFileName = "'SISP.ARD2502.LIBCIR.ENTITY."+filename+"'";
     
     /* String serverName =  "dail.ucsd.edu";
      String pathname = "/pub/data2/ftp/"; */
      // String newFileName = "ENTITY.txt";
       
       String serverName = null;
 	  String pathname = null;
 	  try{
       InitialContext context = new InitialContext();
       serverName =
       (String)context.lookup("java:comp/env/billingServer/hostname");
       pathname = 
      	 (String)context.lookup("java:comp/env/billingServer/path");
       log.info("$$$ serverName:"+serverName);
       log.info("$$$ pathname:"+pathname);
       pathname = pathname+"ENTITY";
       log.info("$$$ pathnameMod:"+pathname);
 	  }
 	  catch(NamingException nee)
 	  {
 		  log.info("$$$ NamingException:"+nee);
 	  }
       
       
      String protocol = "SSL";
	  boolean retValue1 =  false;
         log.info("$$$$$$ BEGIN  Deleting Entity file :)");
	  /*	  
	        try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	         //  log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ( 0 );
	           ftp.execPROT( "P" );    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
*/
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE, FTP.CARRIAGE_CONTROL_TEXT_FORMAT);
	           //ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			  try{
				    retValue1 = ftp.deleteFile(newFileName) ;
				    log.info("$$$$$ ret val delealeting entity file: "+retValue1);
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  deleting Entity file :)"+retValue1);
			return   retValue1;	    
			    
			    
  }
	  
  public static int countLines(String str) {
	    int lines = 0;
	    int pos = 0;
	    while ((pos = str.indexOf("\n", pos) + 1) != 0) {
	        lines++;
	    }
	    return lines;
	}
	  
  public static String[] getEmail()
  {
	  String[] email = null;
	  BufferedReader is = null;

	  try {
		  is = new BufferedReader(new FileReader(propertiesFilePath + "apbatch.properties"));
		  String lineIn = is.readLine();
		  if(lineIn != null) {
			  String[] temp = lineIn.split("=");
			  email = temp[1].split(",");
		  }
	  } catch (IOException e) {
		  System.err.println("Exception in getEmail():"+e);
	  } finally {
		  try {
			  if (is != null)
				  is.close();
		  } catch (Exception e) {}
	  }

	  return email; 
  }	  
	
  public static String getUserEmail(HttpServletRequest request){
		DataSource dsSourceAuth;
		EmployeeInfo emp;
		String email = null;
		String remoteUser = request.getRemoteUser();
		try {
			Context initCtx = new InitialContext();
			dsSourceAuth = (DataSource)initCtx.lookup("java:comp/env/jdbc/authzt");
			emp = EmployeeInfo.lookup( dsSourceAuth, remoteUser);
			email = emp.getEmail();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(email == null)
			email = "ssayavanh@ucsd.edu";
		return email;
  }
}//end of class