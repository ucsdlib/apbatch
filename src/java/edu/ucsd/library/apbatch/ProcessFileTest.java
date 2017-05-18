package edu.ucsd.library.apbatch;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class ProcessFileTest {
  File file = null;
  ProcessFile processor = null;
  JSONObject results = null;
  String sum = "", total = "", result = "", problemTotal = "";
  @Before
  public void setUp() throws Exception {
	file=new File(System.getProperty("user.dir")+"/src/fixtures/WB100313.out");
    processor = new ProcessFile(new FileInputStream(file));
    processor.useTestConnection = true;
	sum = "17450.3"; ;
	total = "193";
	result = "success";
	problemTotal = "0";
  }

  @Test
  public void testProcessOutFile() throws IOException {	
	processor.processOutFile();
	results = processor.getProcessingResults();
    assertEquals(sum,String.valueOf(results.get("SUM")));
    assertEquals(total,String.valueOf(results.get("total")));
    assertEquals(problemTotal,String.valueOf(results.get("ProblemTotal")));
    assertEquals(result,results.get("result"));
  }
  
  @After
  public void tearDown() {
	processor.useTestConnection = false;
	file = null;
	processor = null;
	results = null;
    sum = ""; ;
	total = "";
	result = "";
	problemTotal = "";
  }
}
