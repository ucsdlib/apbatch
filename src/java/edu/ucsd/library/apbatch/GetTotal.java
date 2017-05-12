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
import java.text.NumberFormat;




/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class GetTotal extends HttpServlet {
	private static Logger log = Logger.getLogger( GetTotal.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	Double sumObj = new Double(0);
	JSONObject results =new JSONObject() ;
	HttpSession session = null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		log.info("GetTotal: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
		session = request.getSession();
		sumObj = (session.getAttribute("SUM") != null ) ? (Double)session.getAttribute("SUM") : null;
		try {
			if(sumObj == null)
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Total sum is not available in session!");	
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error : sumObj is null", e);
		}
		 try {
			 double d = sumObj.doubleValue();
			  NumberFormat nf = NumberFormat.getCurrencyInstance();
			  String s=nf.format(d);
		  results.put("SUM", s);
		 log.info("$$$$ SUM oBJ:"+sumObj);
		 response.setContentType("text/html");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
			log.info("GetTotal: $$$$$$$$$ END $$$$$$$$$$$$$$");
		 }
		 catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Error in sending data =>GetTotal servlet", e);
			}
	}
	
}