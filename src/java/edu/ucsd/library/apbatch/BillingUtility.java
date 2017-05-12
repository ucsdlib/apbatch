package edu.ucsd.library.apbatch;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;
import edu.ucsd.library.util.sql.EmployeeInfo;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * 
 * @author lib-kdushamali
 * @author tchu
 */
public class BillingUtility {
	static class ByPayeeIDAddressType implements java.util.Comparator {
		public int compare(Object a, Object b) {
			JSONObject obj = (JSONObject)a;
			String vendCode =((String)obj.get("vendorCode")).trim();
			String payeeId =getPayeeId(vendCode);
			String addressType=getAddressType(vendCode);

			JSONObject obj2 = (JSONObject)b;
			String vendCode2 = ((String)obj2.get("vendorCode")).trim();
			String payeeId2 = getPayeeId(vendCode2);
			String addressType2 = getAddressType(vendCode2);

			return (payeeId + addressType).compareTo(payeeId2 + addressType2);
		}
	}
	private static Logger log = Logger.getLogger( BillingUtility .class );
	public static long docSeqNum = 0;
	
	public static StringBuffer outStream = null;
	//public static String taxCode = null;
	public static boolean  flagWroteFirstLine = false;	
	public static boolean  needNewHeaderLine = false;
	public static Vector payeeIdVector = new Vector();
	public static boolean useTestConnection = false;
	public static boolean processApbatchData(HttpServletRequest request,HttpServletResponse response, JSONArray results, boolean forTransfer){
		HttpSession session = request.getSession();
		String fileName = "APCHECK.txt";		
		if(!forTransfer) {
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment;filename="+fileName);
			response.setHeader("Cache-Control", "no-store,no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
		}
		JSONArray rows = new JSONArray();
		rows = results;

		java.util.Collections.sort(rows, new ByPayeeIDAddressType());

		JSONArray temp = new JSONArray();
		String invoiceNo = null;
		String username = null;
		String vendCode = null;
		outStream= new StringBuffer();
		flagWroteFirstLine = false;	
		Vector errorVec = new Vector();
		try {
			java.security.Principal pObj = request.getUserPrincipal();
			username = pObj.getName();
			System.out.println("USER NAME:"+ username);
			System.out.println("result size:"+results.size());
			JSONObject firstObj = (JSONObject)rows.get(0);
			String firstVoucherNo =((String)firstObj.get("voucherNo")).trim();			
			System.out.println("$$$ First voucher no:"+firstVoucherNo);
			vendCode =((String)firstObj.get("vendorCode")).trim();
			//taxCode =((String)firstObj.get("taxcode")).trim();	
			//taxCode = getTaxCode(vendCode.toUpperCase());
			//log.info("$$$ taxCode:"+taxCode);
			System.out.println("first vendor code:"+vendCode + "-tax code"+getTaxCode(vendCode.toUpperCase()));
			
			JSONObject lastObj = (JSONObject)rows.get(rows.size()-2);
			String lastVoucherNo =((String)lastObj.get("voucherNo")).trim();
			System.out.println("last vendor code:"+((String)lastObj.get("vendorCode")).trim() + "-tax code"+getTaxCode(((String)lastObj.get("vendorCode")).trim().toUpperCase()));
			System.out.println("$$$ Last voucher no:"+lastVoucherNo);
			String tempVenderCode = null;
			PrintWriter out = null;
			if(!forTransfer) {
				out = new PrintWriter(response.getOutputStream());
			}			
			String tempAccCode = null; 
			boolean isValidate = validateData(rows,errorVec);
			if(isValidate) {
				System.out.println("$$$ rows:"+rows.size());
			for(int i = 0; i < rows.size(); i++){
				JSONObject obj = (JSONObject)rows.get(i);
				//System.out.println("$$$$$ rows:"+i+"="+obj.toString());
//System.out.println(i+":tempVenderCode:"+tempVenderCode);
				if (i == 0)
				{
					temp.add(obj);
					invoiceNo =((String)obj.get("invNo")).trim();
					tempVenderCode =((String)obj.get("vendorCode")).trim();
					   //log.info("i==0 invoice no:"+invoiceNo);
					
				}
				else
				{					
					//compare for same invoice no
					String tempInvoiceNo =((String)obj.get("invNo")).trim();
					 //log.info("tempInvoiceNo:"+tempInvoiceNo);
					if(getPayeeId(((String)obj.get("vendorCode")).trim()) == null) {
						System.out.println("Missing VedorCode: "+obj.toString());
						errorVec.add("Missing VedorCode: "+obj.toString());
					}
					if(invoiceNo.equals(tempInvoiceNo))
					{
						temp.add(obj);
					}
					else{
						// write already in records
						//assign the new voucher no to temp string
						JSONArray newArr = arrangeRecords(temp);
						boolean flag = writeRecords(newArr,username,tempVenderCode);
						temp = new JSONArray();
						invoiceNo =((String)obj.get("invNo")).trim();
						temp.add(obj);
					}
				}
			//outStream.write(firstVoucherNo);
			///outStream.write("\r\n");
			///outStream.write(lastVoucherNo);
			//outStream.write("\r\n");
			
			
				
			
			
			} //end of for
			}
			if(temp.size() != 0)
			{				
				JSONArray newArr2 = arrangeRecords(temp);
				boolean flag2 = writeRecords(newArr2,username,tempVenderCode);
			}
			if( rows.size() != 0) {
				setDocSeqNo (docSeqNum);
			}
			String strStream = outStream.toString();
			
			//log.info("%%%%  Stream ="+strStream);
			if(out != null) {
				if(errorVec.size() > 0) {
					StringBuffer tmpStringBuffer = new StringBuffer();
					tmpStringBuffer.append("Error processing file\n");
					for(int m = 0; m < errorVec.size(); m++) {
						tmpStringBuffer.append(errorVec.elementAt(m).toString()+"\n");
					}
					out.write(tmpStringBuffer.toString());
					out.flush();
					out.close();
					return false;
				} else {
					out.write(strStream);
					out.flush();
					out.close();
				}
			}
			if(strStream != null && strStream.length() > 0)
				session.setAttribute("transmitData",strStream);
			pObj = null;
			needNewHeaderLine = false;
			payeeIdVector = new Vector();
		}catch (IOException e) {
			log.error("Unable to generate report file", e);
			log.info("BILLING UTILITY ERROR");
			needNewHeaderLine = false;
			return false;
		}
		//log.info("BILLING UTILITY RETURNIN TRUE ");
		return true;
		
		
		
		
	}

	public static boolean processApbatchDataFromFile(JSONArray results){
		String fileName = "APCHECK.txt";
		JSONArray rows = new JSONArray();
		useTestConnection = true;
		rows = results;

		java.util.Collections.sort(rows, new ByPayeeIDAddressType());

		JSONArray temp = new JSONArray();
		String invoiceNo = null;
		String username = null;
		String vendCode = null;
		outStream= new StringBuffer();
		flagWroteFirstLine = false;	
		Vector errorVec = new Vector();
		try {
			username = "tst";
			//log.info("USER NAME:"+ username);
			//log.info("result size:"+results.size());
			JSONObject firstObj = (JSONObject)rows.get(0);
			String firstVoucherNo =((String)firstObj.get("voucherNo")).trim();			
			//log.info("$$$ First voucher no:"+firstVoucherNo);
			vendCode =((String)firstObj.get("vendorCode")).trim();
			//taxCode =((String)firstObj.get("taxcode")).trim();	
			//taxCode = getTaxCode(vendCode.toUpperCase());
			//log.info("$$$ taxCode:"+taxCode);
			//System.out.println("first vendor code:"+vendCode + "-tax code"+getTaxCode(vendCode.toUpperCase()));
			
			JSONObject lastObj = (JSONObject)rows.get(rows.size()-2);
			String lastVoucherNo =((String)lastObj.get("voucherNo")).trim();
			//System.out.println("last vendor code:"+((String)lastObj.get("vendorCode")).trim() + "-tax code"+getTaxCode(((String)lastObj.get("vendorCode")).trim().toUpperCase()));
			//log.info("$$$ Last voucher no:"+lastVoucherNo);
			String tempVenderCode = null;
			File f = new File("tmp/"+fileName);
			if(!f.exists())
			    f.createNewFile();
			else {
				f.delete();
				f.createNewFile();
			}
			PrintWriter out = new PrintWriter(new FileOutputStream(f));
		
			String tempAccCode = null; 
			boolean isValidate = validateData(rows,errorVec);
			if(isValidate) {
			  for(int i = 0; i < rows.size(); i++){
				JSONObject obj = (JSONObject)rows.get(i);
			
//System.out.println(i+":tempVenderCode:"+tempVenderCode);
				if (i == 0)
				{
					temp.add(obj);
					invoiceNo =((String)obj.get("invNo")).trim();
					tempVenderCode =((String)obj.get("vendorCode")).trim();
					   //log.info("i==0 invoice no:"+invoiceNo);
					
				}
				else
				{					
					//compare for same invoice no
					String tempInvoiceNo =((String)obj.get("invNo")).trim();
					 //log.info("tempInvoiceNo:"+tempInvoiceNo);
					if(getPayeeId(((String)obj.get("vendorCode")).trim()) == null) {
						System.out.println("Missing VedorCode:"+obj.toString());
						errorVec.add("Missing VedorCode "+obj.toString());						
					}
					if(invoiceNo.equals(tempInvoiceNo))
					{
						temp.add(obj);
					}
					else{
						// write already in records
						//assign the new voucher no to temp string
						JSONArray newArr = arrangeRecords(temp);
						boolean flag = writeRecords(newArr,username,tempVenderCode);
						temp = new JSONArray();
						invoiceNo =((String)obj.get("invNo")).trim();
						temp.add(obj);
					}
				}
			//outStream.write(firstVoucherNo);
			///outStream.write("\r\n");
			///outStream.write(lastVoucherNo);
			//outStream.write("\r\n");
			
			
				
			
			
			  } //end of for
			//}
				if(temp.size() != 0)
				{				
					JSONArray newArr2 = arrangeRecords(temp);
					boolean flag2 = writeRecords(newArr2,username,tempVenderCode);
				}
				if( rows.size() != 0) {
					setDocSeqNo (docSeqNum);
				}
		    }
			String strStream = outStream.toString();
			
			//log.info("%%%%  Stream ="+strStream);
			if(out != null) {
				if(errorVec.size() > 0) {
					StringBuffer tmpStringBuffer = new StringBuffer();
					tmpStringBuffer.append("Error processing file\n");
					for(int m = 0; m < errorVec.size(); m++) {
						tmpStringBuffer.append(errorVec.elementAt(m).toString()+"\n");
					}
					out.write(tmpStringBuffer.toString());
					out.flush();
					out.close();
					return false;
				} else {
					out.write(strStream);
					out.flush();
					out.close();
				}
			}

			needNewHeaderLine = false;
			payeeIdVector = new Vector();
			useTestConnection = false;
		}catch (IOException e) {
			log.error("Unable to generate report file", e);
			log.info("BILLING UTILITY ERROR");
			needNewHeaderLine = false;
			useTestConnection = false;
			return false;
		}
		//log.info("BILLING UTILITY RETURNIN TRUE ");
		return true;
		
		
		
		
	}
	public static boolean validateData(JSONArray rows, Vector errorVec) {
		String tempAccCode = null; 
		for(int i = 0; i < rows.size()-1; i++){
			JSONObject obj = (JSONObject)rows.get(i);
			if(!((String)obj.get("voucherNo")).equals("TOTAL")){
				tempAccCode =((String)obj.get("externalFund")).trim();
				
			    int index =tempAccCode.lastIndexOf("LIB");
			    			  
			   String accCode = null;
			   accCode = tempAccCode.substring(index,index+7); 
						   
			   if(getFundCode(accCode) == null) {
				  
				   errorVec.add("Missing AccountCode:"+accCode);
			   } 
			   if(getPayeeId(((String)obj.get("vendorCode")).trim()) == null) {
				   errorVec.add("Missing VedorCode:"+(String)obj.get("vendorCode"));
			   }
			}
		}
		if(errorVec.size() > 0)
			return false;
		else
			return true;
	}
	public static boolean writeRecords(JSONArray temp,String username,String venderCode)
	{ 
	  String invNo =null;
	  String oldAccCode = null;
	  double tempTax = 0;
	  double total = 0;
	  double totalAmt = 0;
	  double tempUseTax = 0;
	  double tempShip = 0;
	  double tempDisc = 0;
	  double tempListPrice = 0;
	  double tempAmt = 0;
		String tempInvNo = null;
		String tempAccCode = null;
		String vendCode = null;
		String invDate = null;
		String prevInvoiceNo = null;
		String tmpPayeeId = null;
	
	    boolean flagWroteSeconeLine = false;
	    String taxCode =null;
	   // String taxCode =getTaxCode(venderCode.toUpperCase());	
	    //System.out.println("venderCode:"+venderCode+" taxCode:"+taxCode);
	    //log.info("=========INSIDE  writeRecords==================");
	    //log.info("^^^^ User name:"+username);
	    
		for(int i = 0; i < temp.size(); i++){
			
			JSONObject obj = (JSONObject)temp.get(i);			
			 tempInvNo =((String)obj.get("invNo")).trim();
			 tempAccCode =((String)obj.get("externalFund")).trim();
			 vendCode =((String)obj.get("vendorCode")).trim();
			 invDate =((String)obj.get("invDate")).trim();	
			 taxCode = getTaxCode(vendCode.toUpperCase());
			 //System.out.println(i+"vendCode"+vendCode+"-"+taxCode+"-"+tempAccCode);
		   int index =tempAccCode.lastIndexOf("LIB");
		   int indexHy = tempAccCode.lastIndexOf("-");
		   //log.info("index:"+index);
		 //  log.info("indexHy:"+indexHy);
		   String accCode = null;
		  /* if(indexHy > 0)
		    accCode = tempAccCode.substring(index,indexHy);	           
		   else
			accCode = tempAccCode.substring(index); */
		   accCode = tempAccCode.substring(index,index+7); 
		   //log.info("accCode:"+accCode);
		  // log.info("=========flagWroteFirstLine================="+flagWroteFirstLine); 
		   tmpPayeeId = getPayeeId(vendCode);
		   if(!payeeIdVector.contains(tmpPayeeId)) {
				payeeIdVector.add(tmpPayeeId);
				needNewHeaderLine = true;
			} else
				needNewHeaderLine = false;
		   
		   if (!(flagWroteFirstLine) || needNewHeaderLine)
		   {
			 /*  boolean flagN = writeRecordTypeN(vendCode,username);
			   flagWroteFirstLine = true;
				log.info("flagN:"+flagN);
				*/
			   //log.info("=========INSIDE  !flagWroteFirstLine=================="); 
			 //----------- REC TYPE N-----
				String userName = "LIB"+username.substring(0,3)+"  ";
				String payeeId =getPayeeId(vendCode);
				//log.info("&&&&payeeId : "+payeeId);
				String addressType=getAddressType(vendCode);
				outStream.append("01"); //2- uni code
				outStream.append("LIB ACC "); //8 - Prog name
				outStream.append(userName);//8 -userid
				//outStream.append("XLI2"); //4- origin code
				outStream.append("XLI4"); //4- origin code
				outStream.append(" "+payeeId);//10 - vendor code
				outStream.append(addressType);//2 address type
				outStream.append("0008");//4 doc type seq no
				outStream.append(" ");//1 - grouping indicator blank
				outStream.append("        ");//8 doc no:blanks
				outStream.append("0000");//4 - item no
				outStream.append("0000");//4 - Acc seq no
				outStream.append("N");//1- rec type
				outStream.append(" ");//1-debit balance indicator
				outStream.append("000000000000");//12-debit balance amt
				outStream.append("000000000000");//12-credit balance amt
				outStream.append(" "+payeeId);//10 - vendor code
				outStream.append("000000");//6 -federal withholding %
				outStream.append("000000");//6 -state withholding %
				//filler
				for(int l=0; l<103;l++)
				{
					outStream.append(" ");
				}
				outStream.append("\r\n");
				flagWroteFirstLine = true;
		   }
		   //log.info("=========flagWroteFirstLine AFTER================="+flagWroteFirstLine); 
		   if (!(flagWroteSeconeLine))
		   {
			   //log.info("=========INSIDE  !flagWroteSeconeLine=================="); 
			//boolean flagO = writeRecO(vendCode,tempInvNo,username,invDate);
			//log.info("!!!!!!!!!!!! flagO:"+flagO);
			   String amtStr = null;
				String userName = "LIB"+username.substring(0,3)+"  ";
				String payeeId =getPayeeId(vendCode);
				String addressType=getAddressType(vendCode);
				String docSeqNo = null;
				long finalSeqNo =0;
				if(docSeqNum == 0){
					docSeqNo = getDocSeqNo();
					long docSeqNoLong= Long.parseLong(docSeqNo);
					//log.info("LASTDOCNUM ="+docSeqNoLong);	
					docSeqNum = docSeqNoLong;
					finalSeqNo =docSeqNum;
				}
				else
				{
					finalSeqNo=docSeqNum+1;
					docSeqNum=finalSeqNo;
				}
				 String invoiceDate = getInvoiceDate(invDate);
				
				    
				    
				outStream.append("01"); //2- uni code
				outStream.append("LIB ACC "); //8 - Prog name
				outStream.append(userName);//8 -userid
				//outStream.append("XLI2"); //4- origin code
				outStream.append("XLI4"); //4- origin code
				outStream.append(" "+payeeId);//10 - vendor code
				outStream.append(addressType);//2 address type
				outStream.append("0008");//4 doc type seq no
				outStream.append("1");// grouping indicator
				outStream.append(""+finalSeqNo);//8-doc seq no from database
				outStream.append("0001");//4
				outStream.append("0001");//4
				outStream.append("O");//1-rec type
				outStream.append(""+finalSeqNo);//8-doc seq no from database
				outStream.append("0008");//4 doc type seq no
				outStream.append("1");//1-grouping indicator
				outStream.append("N");//1-recurring indicator
				outStream.append("N");//1- 1099 indicator
				outStream.append("         ");//9 -1099 report id
				outStream.append(addressType);//2 address type
				outStream.append(invoiceDate);//8 -inv date
				//outStream.append(tempInvNo);
				if(tempInvNo.length()>9) {
					outStream.append(tempInvNo.substring(tempInvNo.length()-9));//9-inv no
				} else {
					outStream.append(tempInvNo);
				}
				//log.info("invoice no in writeRecO:"+tempInvNo);
				String tmpLength = String.valueOf(finalSeqNo);
				//tmpLength = tmpLength.length();
				int tmp2 = 16 - (tmpLength.length()*2);
				int tmp3 = 105 + tmp2;
				//int tmp3 = 101 + tmp2 + (2*(tmpLength.length()-1));
				for(int m = 0; m < 9 - tempInvNo.length(); m++) {
					outStream.append(" ");
				}
				outStream.append("  ");//2 adjustment code
				/*for(int n = 0; n < tmp2; n++) {
					outStream.append("+");
				}*/
				int k;
				for(k=0; k< 105 + tmp2;k++)
				{
					
						outStream.append(" ");
				}
				outStream.append("\r\n");
			flagWroteSeconeLine = true;
		   }
			//-----------get amount-----------------
			String amount =((String)obj.get("amount")).trim();
			/*if(amount.equals("0"))
			{
				tempAmt = 0;
			}
			else{*/
				String tempStrAmt = amount.substring(1);
				//log.info("tempStrAmt:"+tempStrAmt);
				tempAmt = Double.parseDouble(tempStrAmt);
				//log.info("tempAmt:"+tempAmt);
			//}
		
			//---------tax-------------------
			String tax =((String)obj.get("tax")).trim();
			/*if(tax.equals("0"))
			{
				tempTax = 0;
			}
			else{*/
				String tempStrTax = tax.substring(1);
				//log.info("tempStrtax:"+tempStrTax);
				tempTax = Double.parseDouble(tempStrTax);
				//log.info("tempTax:"+tempTax);
			
		
			//---------useTax-------------------
			String useTax =((String)obj.get("useTax")).trim();
			/*if(useTax.equals("0"))
			{
				tempUseTax = 0;
			}
			else{*/
			String tempStrUseTax = useTax.substring(1);
			//log.info("tempStrUseTax:"+tempStrUseTax);
			tempUseTax = Double.parseDouble(tempStrUseTax);
			//System.out.println("tempUseTax:"+tempUseTax);
			//log.info("tempUseTax:"+tempUseTax);
			//}
			//---------ship-------------------
			String ship =((String)obj.get("ship")).trim();
			/*if(ship.equals("0"))
			{
				tempShip = 0;
			}
			else{*/
				String tempStrShip = ship.substring(1);
				//log.info("tempStrShip:"+tempStrShip);
				tempShip = Double.parseDouble(tempStrShip);
				//log.info("tempShip:"+tempShip);
			
			
			
			//---------discount-------------------
			String discount =((String)obj.get("discount")).trim();
			/*if(discount.equals("0"))
			{
				tempDisc = 0;
			}
			else{*/
				String tempStrDisc = discount.substring(1);
				//log.info("tempStrDisc:"+tempStrDisc);
				tempDisc = Double.parseDouble(tempStrDisc);
				//log.info("tempDisc:"+tempDisc);
			
			
			
			//---------List Price-------------------
			String listPrice =((String)obj.get("listPrice")).trim();
			if(listPrice.equals("0"))
			{
				tempListPrice = 0;
			}
			else{
				String tempStrListPrice = listPrice.substring(0,listPrice.length()-2)+"." + listPrice.substring(listPrice.length()-2);
				//log.info("tempStrListPrice:"+tempStrListPrice);
				tempListPrice = Double.parseDouble(tempStrListPrice);
				//log.info("tempListPrice:"+tempListPrice);
			}
			
			
		
			
			if(i == 0)
			{invNo = tempInvNo;	
			oldAccCode =accCode;
			totalAmt +=  tempAmt + tempTax + tempShip +tempDisc;
			//log.info("Inside i == 0)");
			//log.info("totalAmt ="+totalAmt);
			}//if (i ==0)
			else
			{
				/*log.info("Inside else of i==0");
				log.info("invNo ="+invNo);
				log.info("tempInvNo ="+tempInvNo);
				log.info("oldAccCode ="+oldAccCode);
				log.info("accCode ="+accCode);
				log.info("totalAmt ="+totalAmt);*/
				
				
				if(oldAccCode.equals(accCode)) 
				{//both matching
					totalAmt +=  tempAmt + tempTax + tempShip +tempDisc;
					//log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^"+totalAmt);
				}
				else if (!(oldAccCode.equals(accCode)))  
				{
					
						
						// write the records for same invoice and previous acc code
						boolean writeFirstFlag = writeFirstPart(vendCode,totalAmt,oldAccCode,tempInvNo,username,taxCode);
						//log.info("&&&&&&&&&&&&& writeFirstFlag ="+writeFirstFlag);
						if (!(writeFirstFlag))
						return false;
						invNo = tempInvNo;	
						oldAccCode =accCode;
						//vendCode =null;
						totalAmt = 0;
						totalAmt +=  tempAmt + tempTax + tempShip +tempDisc;
						//log.info("^^^^^^^^else part^^^^^^^^^^^"+totalAmt);
						
				
					
					
				}
			
				
				
			}
			
			
		}
		
		flagWroteSeconeLine = false;
		
		if (totalAmt != 0)
		{
			 /*log.info("=========INSIDE if (totalAmt != 0)==================");
			 log.info("vendCode ="+vendCode);
				log.info("totalAmt ="+totalAmt);
				log.info("oldAccCode ="+oldAccCode);
				log.info("tempInvNo ="+tempInvNo);*/
				//log.info("accCode ="+accCode);
			// write the records for same invoice and previous acc code
			boolean writeFirstFlag = writeFirstPart(vendCode,totalAmt,oldAccCode,tempInvNo,username,taxCode);
			//log.info("&&&&&&&&&&&&& writeFirstFlag ="+writeFirstFlag);
			if (!(writeFirstFlag))
			return false;
		}
		//write the lastdoc no into database
		//write the batches to database
		return true;
	}
	
	
	
	
	
	/**
	 * ======================================================================
	 * THis method will write the record type O,N and first part of P
	 * if there are more than
	 * one account codes
	 * =====================================================================
	 */
	public static boolean writeFirstPart(String vendCode,double totalAmt,String oldAccCode,String tempInvNo,String username,String taxCode){
		//log.info("&&&&&&&&&&&&& writeFirstPart &&&&&&&&&&&&&&&&&&&&&&&&&");
		// log.info("^^^^ User name:"+username);
		//log.info("&&&&&  ACC CODE="+oldAccCode);
		String amtStr = null;
		String userName = "LIB"+username.substring(0,3)+"  ";
		String payeeId =getPayeeId(vendCode);
		String addressType=getAddressType(vendCode);
		
		//----------Rec Type P---------------
		
	    String fundCode =getFundCode(oldAccCode);
	    String orgCode =getOrgCode(oldAccCode);
	    String progCode =getProgCode(oldAccCode);
	    
	    DecimalFormat twoDForm = new DecimalFormat("#.##");
		double newTotalAmt= Double.valueOf(twoDForm.format(totalAmt));

	    String amtt =""+newTotalAmt;
	    //log.info("0000000000000000totalAmt="+newTotalAmt);
		int indexDot = amtt.indexOf(".");
		
		if(amtt.length() > indexDot+2)
			 amtStr = amtt.substring(0,indexDot)+amtt.substring(indexDot+1,indexDot+3);
			else
			{
				amtStr = amtt.substring(0,indexDot)+amtt.substring(indexDot+1);
				amtStr += "0";
			}
		//log.info("amtStr="+amtStr);
		
		outStream.append("01"); //2- uni code
		outStream.append("LIB ACC "); //8 - Prog name
		outStream.append(userName);//8 -userid
		outStream.append("XLI4"); //4- origin code
		//outStream.append("XLI2"); //4- origin code
		outStream.append(" "+payeeId);//10 - vendor code
		outStream.append(addressType);//2 address type
		outStream.append("0008");//4 doc type seq no
		outStream.append("1");// grouping indicator
		outStream.append(""+docSeqNum);//8-doc seq no from database
		outStream.append("0001");//4
		outStream.append("0001");//4
		outStream.append("P");//1-rec type
		outStream.append("0001");//4-Item no
		outStream.append("0001");//4-Seq no
		outStream.append("A");//1-chart of acc code
		if(oldAccCode.length() <10)
		{
			for(int k=oldAccCode.length();k<10;k++)
				oldAccCode = oldAccCode+" ";
		}
		//log.info("oldAccCode"+oldAccCode.length());
		outStream.append(oldAccCode);//10 acc index code
		outStream.append(fundCode);//6 -fund code
		outStream.append(orgCode);//6 //org code
		outStream.append("649200");//6-acc code //hard coded for now.might hav to get from db
		outStream.append(progCode);//6-Prog code
		outStream.append("      ");//6-activity code
		outStream.append("      ");//6-location code
		
		if(amtStr.length() < 12)
		 {
			 for(int i =amtStr.length();i<12;i++)
				 amtStr = "0"+amtStr;
			 
		 }
		outStream.append(amtStr);//12 -approvbed amt
		//log.info("~~~~~~~~~~~~~~~~~amtStr-----"+amtStr);
		outStream.append("000000000000");////12 -discount amt
		outStream.append("000000000000");//12-tax amt //check
		outStream.append("000000000000");//12-Additional charge
		outStream.append("AD02");//4 -aproved amt rule
		outStream.append("DISB");//4 - dicount rule
		outStream.append("DIST");//4 - tax rule
		outStream.append("    ");//4 -additional charge rule
		outStream.append("          ");//10 - doc ref no //check
		outStream.append(" ");//1- liquidation idicator
		outStream.append("        ");//8-project code
		outStream.append(" ");//1 - sales/usetax indicator
		outStream.append(taxCode);//3-tax rate code
		String tmpLength = String.valueOf(docSeqNum);
		for(int i=0; i<8+(8-tmpLength.length());i++)
		{
			outStream.append(" ");
		}
		outStream.append("\r\n");
		
		
		
		
		
		return true;
	}
	
	/**
	 * ======================================================================
	 * THis method will append the record type P
	 * if there are more than
	 * one account codes
	 * =====================================================================
	 */
	/*public static boolean writeSecondPart(String vendCode,double totalAmt,String oldAccCode,String tempInvNo,PrintWriter outStream,String username){
		String amtStr = null;
		//----------Rec Type P---------------
		String userName = "LIB"+username.substring(0,3)+"  ";
		String payeeId =getPayeeId(vendCode);
		String addressType=getAddressType(vendCode);
	    String fundCode =getFundCode(oldAccCode);
	    String orgCode =getOrgCode(oldAccCode);
	    String progCode =getProgCode(oldAccCode);
	    String amtt =""+totalAmt;
		int indexDot = amtt.indexOf(".");
		
		if(amtt.length() > indexDot+3)
		 amtStr = amtt.substring(0,indexDot)+amtt.substring(indexDot+1,indexDot+3);
		else
		{
			amtStr = amtt.substring(0,indexDot)+amtt.substring(indexDot+1);
			amtStr = amtStr + "0";
		}
		log.info("amtStr in writesecondPart="+amtStr);
		
		outStream.append("01"); //2- uni code
		outStream.append("LIB ACC "); //8 - Prog name
		outStream.append(userName);//8 -userid
		//outStream.append("XLI2"); //4- origin code
		outStream.append("XLI1"); //4- origin code
		outStream.append(" "+payeeId);//10 - vendor code
		outStream.append(addressType);//2 address type
		outStream.append("0008");//4 doc type seq no
		outStream.append("1");// grouping indicator
		outStream.append(""+docSeqNum);//8-doc seq no from database
		outStream.append("0001");//4
		outStream.append("0001");//4
		outStream.append("P");//1-rec type
		outStream.append("0001");//4-Item no
		outStream.append("0001");//4-Seq no
		outStream.append("A");//1-chart of acc code
		if(oldAccCode.length() <10)
		{
			for(int k=oldAccCode.length();k<10;k++)
				oldAccCode = oldAccCode+" ";
		}
		log.info("oldAccCode"+oldAccCode.length());
		outStream.append(oldAccCode);//10 acc index code
		outStream.append(fundCode);//6 -fund code
		outStream.append(orgCode);//6 //org code
		outStream.append("649200");//6-acc code //hard coded for now.might hav to get from db
		outStream.append(progCode);//6-Prog code
		outStream.append("      ");//6-activity code
		outStream.append("      ");//6-location code
		 if(amtStr.length() < 12)
		 {
			 for(int i =amtStr.length();i<12;i++)
				 amtStr = "0"+amtStr;
			 
		 }
		outStream.append(amtStr);//12 -approvbed amt
		log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@amtStr-----"+amtStr);
		outStream.append("000000000000");////12 -discount amt
		outStream.append("000000000000");//12-tax amt //check
		outStream.append("000000000000");//12-Additional charge
		outStream.append("AD02");//4 -aproved amt rule
		outStream.append("DISB");//4 - dicount rule
		outStream.append("DIST");//4 - tax rule
		outStream.append("    ");//4 -additional charge rule
		outStream.append("          ");//10 - doc ref no //check
		outStream.append(" ");//1- liquidation idicator
		outStream.append("        ");//8-project code
		outStream.append(" ");//1 - sales/usetax indicator
		outStream.append(taxCode);//3-tax rate code
		for(int i=0; i<8;i++)
		{
			outStream.append(" ");
		}
		outStream.append("\r\n");
		
		return true;
	}
	
	*/
	public static boolean writeRecordTypeN(String vendCode,String username){
		//log.info("&&&&&&&&&&&&& writeRecordTypeN &&&&&&&&&&&&&&&&&&&&&&&&&");
		// log.info("^^^^ User name:"+username);
		
		//----------- REC TYPE N-----
		String userName = "LIB"+username.substring(0,3)+"  ";
		String payeeId =getPayeeId(vendCode);
		//log.info("&&&&payeeId : "+payeeId);
		String addressType=getAddressType(vendCode);
		outStream.append("01"); //2- uni code
		outStream.append("LIB ACC "); //8 - Prog name
		outStream.append(userName);//8 -userid
		//outStream.append("XLI2"); //4- origin code
		outStream.append("XLI4"); //4- origin code
		outStream.append(" "+payeeId);//10 - vendor code
		outStream.append(addressType);//2 address type
		outStream.append("0008");//4 doc type seq no
		outStream.append(" ");//1 - grouping indicator blank
		outStream.append("        ");//8 doc no:blanks
		outStream.append("0000");//4 - item no
		outStream.append("0000");//4 - Acc seq no
		outStream.append("N");//1- rec type
		outStream.append(" ");//1-debit balance indicator
		outStream.append("000000000000");//12-debit balance amt
		outStream.append("000000000000");//12-credit balance amt
		outStream.append(" "+payeeId);//10 - vendor code
		outStream.append("000000");//6 -federal withholding %
		outStream.append("000000");//6 -state withholding %
		//filler
		for(int i=0; i<103;i++)
		{
			outStream.append(" ");
		}
		outStream.append("\r\n");
		
		//log.info("&&&&outStream : "+outStream.toString());
		return true;
	}
	
	/**
	 * GET PAYEE_ID for vendor
	 */
	private static String getPayeeId(String vendcode)
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String payeeId = null;
		 String vendCode = vendcode.toUpperCase();
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 			
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT PAYEE_ID FROM VENDORS WHERE VENDCODE LIKE '"+vendCode+"%'");
			  while(rs.next())
			  {
				  payeeId = rs.getString(1);
				  
				 // log.info(" $$$$  payeeId ="+payeeId);
			  }
			  if(payeeId == null)
				  System.out.println("NULL PAYEE ID:"+vendcode);
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getPayeeId()",e);			
		} 
	
		
		return payeeId;
	}
	
	/**
	 * GET address type for vendor
	 */
	private static String getAddressType(String vendcode)
	{
		//DataSource ds = null;
		InitialContext ctx = null;
		DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String addressType = null;
		 String vendCode = vendcode.toUpperCase();
		try{
			
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT addr_type FROM VENDORS WHERE VENDCODE LIKE '"+vendCode+"%'");
			  while(rs.next())
			  {
				  addressType = rs.getString(1);
				  //log.info(" $$$$  addressType ="+addressType);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getAddressType()",e);			
		} 
	
		
		return addressType;
	}
	/**
	 * SET Document Seq no 
	 */
	private static void setDocSeqNo(long newSeqNo)
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String docNo = null;
		PreparedStatement pstmt = null;
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 
			  con = ApbatchConnection.getConnection();
			  pstmt = con.prepareStatement("UPDATE APCNTRL SET LASTDOCNUM = ? WHERE LASTDOCNUM = ?");
			  pstmt.clearParameters();
			  pstmt.setInt(1, (int)newSeqNo+1);
			  pstmt.setInt(2, Integer.parseInt(getDocSeqNo()));
			  pstmt.execute();
			 
			  //log.info(" $$$$  oldSeqNo:"+ getDocSeqNo()+ " newSeqNo:"+newSeqNo);
			  //rs.close();
			 
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in setDocSeqNo()",e);			
		} finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(con != null)
				  con.close();
			} catch (SQLException e) {	
				log.error("$$$ SQLException closing connection in setDocSeqNo()",e);			
			}
		}
	}
	
	/**
	 * GET Document Seq no 
	 */
	private static String getDocSeqNo()
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String docNo = null;
		
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 			
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT LASTDOCNUM FROM APCNTRL");
			  while(rs.next())
			  {
				  docNo = rs.getString(1);
				  //log.info(" $$$$  docNo ="+docNo);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getDocSeqNo()",e);			
		} 
	
		
		return docNo;
	}
	/**
	 * GET invoice date
	 */
	private static String getInvoiceDate(String invDate)
	{
		//invDate will be on the format: 07-31-09
		 String monthStr = invDate.substring(0,2);
		 String dateStr = invDate.substring(3,5);
		 String yearStr = invDate.substring(6,8);
		 String finalStr = "20"+yearStr+monthStr+dateStr;
		 //log.info("finalStr ="+finalStr);
		 return finalStr;
		
	}
	
	/**
	 * GET Fund Code
	 */
	private static String getFundCode(String oldAccCode)
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String fundCode = null;
		
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 			
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT AC_FUND FROM GLINDEX WHERE AC_INDEX = '"+oldAccCode+"'");
			  while(rs.next())
			  {
				  fundCode = rs.getString(1);
				  //log.info(" $$$$  fundCode ="+fundCode);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getFundCode()",e);			
		} 
	
		
		return fundCode;
	}
	
	
	/**
	 * GET Org Code
	 */
	private static String getOrgCode(String oldAccCode)
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String orgCode = null;
		
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 			
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT AC_ORG FROM GLINDEX WHERE AC_INDEX = '"+oldAccCode+"'");
			  while(rs.next())
			  {
				  orgCode = rs.getString(1);
				  //log.info(" $$$$  orgCode ="+orgCode);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getOrgCode()",e);			
		} 
	
		
		return orgCode;
	}
	

	/**
	 * GET Prog Code
	 */
	private static String getProgCode(String oldAccCode)
	{
		//DataSource ds = null;
		ResultSet rs =null;
		Connection con = null;
		Statement stmt = null;
		String progCode = null;
		
		try{
			if(useTestConnection == true)
			  con = ApbatchConnection.getTestConnection();
			else 			
			  con = ApbatchConnection.getConnection();
			  stmt = con.createStatement();
			  rs = stmt.executeQuery("SELECT AC_PROG FROM GLINDEX WHERE AC_INDEX = '"+oldAccCode+"'");
			  while(rs.next())
			  {
				  progCode = rs.getString(1);
				  //log.info(" $$$$  progCode ="+progCode);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException in getProgCode()",e);			
		} 
	
		
		return progCode;
	}
	
	/**
	 * get tax code
	 */
	
	private static String getTaxCode(String vendcode)
	{
		String tax = null;
		InitialContext ctx = null;
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
				  //log.info(" $$$$  TAX ="+tax);
			  }
			  rs.close();
			  con.close();
		}
		catch (SQLException e) {	
			log.error("$$$ SQLException",e);			
		} 
 	
		
		return tax;
	}
	
	/**
	 * THis method sorts the  records in the JSON array
	 */
	private static JSONArray arrangeRecords(JSONArray rows)
	{
	  String invoiceNo = null;
	  String accountCode2 = null;
	  String accountCode = null;
	  JSONArray newArr = new JSONArray();
	  Vector v = new Vector();
		for(int i = 0; i< rows.size();i++)
		{		
			JSONObject row = (JSONObject)rows.get(i);
			if(!((String)row.get("voucherNo")).equals("TOTAL")){
				String accCode =((String)row.get("externalFund")).trim();
				int index =accCode.lastIndexOf("LIB");
				//int indexHy = accCode.indexOf("-");
				int indexHy = accCode.lastIndexOf("-");
				/*if(indexHy >0)
				{
				 accountCode = accCode.substring(index,indexHy);
				}
				else
				{
					accountCode = accCode.substring(index);	
				}*/
				accountCode = accCode.substring(index,index+7); 
				//log.info(" $$$$-------------  arrangeRecords");
				//log.info(" $$$$  accountCode ="+accountCode);
				if(v.size() >= 1)
				{
					boolean status = checkAccCode(accountCode,v);
					if(status)
					{
						continue;
					}
					else
					{
						v.add(accountCode);
						newArr.add(row);
					}
				}
				else{
					newArr.add(row);
					v.add(accountCode);
				}
				
				
				
				
				for(int j=i+1; j< rows.size();j++)
				{
					JSONObject row2 = (JSONObject)rows.get(j);
					String accCode2 =((String)row2.get("externalFund")).trim();
					int index2 =accCode2.lastIndexOf("LIB");
					//int indexHy2 = accCode2.indexOf("-");
					int indexHy2 = accCode2.lastIndexOf("-");
	
					/*if(indexHy2 > 0)
					accountCode2 = accCode2.substring(index2,indexHy2);
					else
					accountCode2 = accCode2.substring(index2);*/
					accountCode2 = accCode2.substring(index,index+7); 
					if(accountCode.equalsIgnoreCase(accountCode2))
					{
						newArr.add(row2);
					}
					
					
					
				}
			}
			
			
		}
		//log.info(" $$$$  newArr ="+newArr.size());
		for(int k =0; k<newArr.size();k++)
		{
			JSONObject js = (JSONObject)newArr.get(k);
			String accCode3 =((String)js.get("externalFund")).trim();
			//log.info("content :"+accCode3);
		}
		return newArr;
	}
	
	 private static boolean checkAccCode(String accCode, Vector arr)
	 {
		 boolean flag = false;
		 for(int i =0;i<arr.size();i++)
		 {
			 String jObj= (String) arr.get(i);
			 if(jObj.equalsIgnoreCase(accCode))
			 {
				 
				 flag = true;
				 break;
			 }
		 }
		 return flag;
	 }
	 
	
}