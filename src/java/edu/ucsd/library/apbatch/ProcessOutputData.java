package edu.ucsd.library.apbatch;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
/**
 * 
 * @author lib-kdushamali
 *
 */
public class ProcessOutputData extends HttpServlet {
	private static Logger log = Logger.getLogger( ProcessOutputData.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONArray results = (session.getAttribute("apbatchData") != null ) ? (JSONArray)session.getAttribute("apbatchData") : null;
		
		if(results == null){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ProcessOutputData servlet", e);
				return;
			}
		}
		log.info("ProcessOutputData size of results:"+results.size());
		//try and generate output text file
		log.info("PROCESSOUTPUTDATA SIZE OF ARRAY:"+ results.size());
		boolean success = BillingUtility.processApbatchData(request, response, results, false);
		log.info("ProcessOutputData boolean success:"+success);

		if(!success){
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to generate report file");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ProcessOutputData servlet", e);
				return;
			}
		} 


		
		
	}
	
	
}