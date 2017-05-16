package edu.ucsd.library.apbatch;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class BillingHelperTest {
	  File file = null;
	  ProcessFile processor = null;
	  BillingHelper billing = null;
	  JSONObject results = null;
	
	  @Before
	  public void setUp() throws Exception {
		file=new File(System.getProperty("user.dir")+"/src/fixtures/WB100313.out");
	    processor = new ProcessFile(new FileInputStream(file));
	    processor.useTestConnection = true;
	    billing = new BillingHelper(true);
	  }

	  @Test
	  public void testProcessOutFile() throws IOException {	
		processor.processOutFile();
		results = processor.getProcessingResults();
		boolean success = billing.processApbatchDataFromFile((JSONArray)results.get("rows"));
		assertEquals("true",String.valueOf(success));
	  }

	  @Test
	  public void validateData() throws java.io.FileNotFoundException{	
		BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/tmp/APCHECK.txt"));;
		try {
		  int count = 1;
		  boolean isValidate = true;
		  String lineIn = null;
		  String firstLine = "01LIB ACC LIBtst  XLI4 00058612EU10008         00000000N 000000000000000000000000 00058612E000000000000";
		  while ((lineIn = in.readLine()) != null){
			if(count == 1 && !firstLine.equals(lineIn.trim())) {
				isValidate = false;
				assertFalse(isValidate);
			}
			if(lineIn.contains("error")) {
				isValidate = false;
				assertFalse(isValidate);
			}				
			count++;
		  } 
		  
		  assertEquals("true",String.valueOf(isValidate));
		} catch (java.io.FileNotFoundException fe) {
		  System.err.println("FileNotFoundException in validateData()"+fe);
		} catch (Exception e) { 
		  System.err.println("Exception in validateData()"+e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {}
		}
	
	  }
	  
	  @After
	  public void tearDown() {
		processor.useTestConnection = false;
		file = null;
		processor = null;
		results = null;
		billing = null;
	  }
}
