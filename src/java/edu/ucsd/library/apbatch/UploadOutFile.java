package edu.ucsd.library.apbatch;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.log4j.Logger;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import edu.ucsd.library.util.sql.EmployeeInfo;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * @author Chandana Kapugama Arachchige
 *
 */
public class UploadOutFile extends HttpServlet {
	private static Logger log = Logger.getLogger( UploadOutFile.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	    boolean foundInputFile = true;
		JSONObject results = null;
		JSONArray pending = null;
		String errorMsg = null;
		
		try {
			
			MultipartParser mp = new MultipartParser(request,10000 * 1024 * 1024); // 10MB
			Part part = null;
			ParamPart paramPart = null;
			FilePart filePart = null;
			boolean successfulProcessing = true;
			while ((part = mp.readNextPart()) != null) {
				String name = part.getName();
				if (part.isParam()) {
					
				}else if (part.isFile()) {
					foundInputFile = true;
					log.info("Found attachment file for Ap Batch application");
					filePart = (FilePart) part;
					ProcessFile processor = new ProcessFile(filePart.getInputStream());
					successfulProcessing = processor.processOutFile(); //perform processing
					results = processor.getProcessingResults();
					//pending = processor.getPendingQueue();
					//problem =processor.getProblemQueue();
					//newPatron=processor.getNewPatronQueue();
				}
			}
			if(!successfulProcessing) {
				errorMsg = "There was an error in the attached file. Negative shipping charge found.";
				log.error(errorMsg);				
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMsg);
				} catch (IOException e) {
					log.error(e);
					return;
				}
			}
		} catch (IOException e1) {
			foundInputFile = false;
			errorMsg = "There was an error retrieving the attached file in UploadBillingFile servlet";
			log.error("There was an error retrieving the attached file in UploadBillingFile servlet");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no file attached");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from UploadBillingFile servlet", e);
				return;
			}
		}
		if(foundInputFile){
			try {
				if(results.get("result") == "fail"){
					log.error("There is no json data to return from servlet UploadOutFile servlet");
					try {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no results");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//errorMsg =
						log.error("There was an error sending error message back in response from UploadOutFile servlet", e);
						return;
					}
				}
				if(foundInputFile)
					results.put("success","true");
				else					
					results.put("success","false");
				
				//set json object in session variable
				HttpSession session = request.getSession(true);
				log.info("$$$$$ session null or not:"+(session == null));
				
					 
				JSONArray temp = (JSONArray)results.get("rows");
				JSONArray tempDeleted = (JSONArray)results.get("delRows");
				JSONArray tempProblem = (JSONArray)results.get("ProblemRows");
				Double sum = (Double)results.get("SUM");
				log.info("$$$$$ size of the rows:"+temp.size());
				session.setAttribute("apbatchData", temp);
				session.setAttribute("deletedData", tempDeleted);
				session.setAttribute("problemData", tempProblem);
				session.setAttribute("SUM", sum);
				
				//log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
				//send response
				response.setContentType("text/html");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Error sending back AP Batch data", e);
			}
		}
		
		
		
		
		
	}
	
	
}