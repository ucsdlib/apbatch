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

import edu.ucsd.library.util.sql.ConnectionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class CheckExtFundCode extends HttpServlet {
	private static Logger log = Logger.getLogger( CheckExtFundCode.class );
	String extFundCode= null;
	JSONObject results =new JSONObject() ;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("CheckExtFundCode: Begin");
		try{
			extFundCode = request.getParameter("extFundCode");
			//whichQueue = request.getParameter("whichQueue");
			log.info("$$$$ extFundCode:"+extFundCode);
			
		}
		catch(Exception e2)
		{
			
			log.error("NO data got from request in ModifyQueues servlet: P.O No is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ModifyQueues servlet", e);
				return;
			}
			
		}
		
		
		try{
			
			String accountCode2 = null;
			accountCode2 = BillingUtility.getAccountCode(extFundCode);
			int count = checkExtFundCodeCount(accountCode2);
			log.info("count: "+count);
			results.put("TOT", count);
			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
			log.info("CheckExtFundCode: END");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Error sending back queue data from CheckExtFundCode", e);
		}
		
	}

	private int checkExtFundCodeCount(String fundCode) {
      
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
		try {
			 conn = ApbatchConnection.getConnection();
			  stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT count(*) FROM GLINDEX WHERE AC_INDEX = '"+fundCode+"'");
						
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
			}
			conn.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("NumberFormatException", e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("SQLException", e);
		} 

	return count;

	}
}