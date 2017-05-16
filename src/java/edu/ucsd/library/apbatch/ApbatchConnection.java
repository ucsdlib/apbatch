package edu.ucsd.library.apbatch;
import java.sql.*;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import edu.ucsd.library.util.sql.ConnectionManager;
import java.util.Properties;
import edu.ucsd.library.util.FileUtils;

public class ApbatchConnection{
	
	private static Logger log = Logger.getLogger( ApbatchConnection.class );
	
	public static Connection getTestConnection(){
		Connection conn = null;
		Properties dsProp = null;
		String filePath = System.getProperty("user.dir").replace("apbatch/","")+"/common/apbatch/datasource.properties";
		try {			
			dsProp = FileUtils.loadProperties(filePath);			
		} catch (Exception ioe) {
			System.out.println("Error loading properties file!-filePath:" + filePath);
		}
		try{			
			Class.forName("org.postgresql.Driver");	
		} catch(ClassNotFoundException e) {
			log.error("$$$ ClassNotFoundException in ApbatchConnection class",e);	
		}
			
		try {				 
			//conn = ConnectionManager.getConnection("apbatch-test");
			//conn=DriverManager.getConnection("jdbc:postgresql://abbott.ucsd.edu:5432/ap_user","ap_user","b#9K3LH21");
			conn=DriverManager.getConnection((String)dsProp.get("dataSourceURL"),(String)dsProp.get("dataSourceUser"),(String)dsProp.get("dataSourcePass"));
		} catch (SQLException e) {
			log.error("$$$ SQLException in ApbatchConnection class",e);
			System.out.println("$$$ SQLException in ApbatchConnection class getTestConnection" + e);
			e.printStackTrace();
		} 
			
		return conn;
		
	}
	
	public static Connection getConnection(){
		Connection conn = null;
		
		try{			
			Class.forName("org.postgresql.Driver");	

			}
			catch(ClassNotFoundException e)
			{
				log.error("$$$ ClassNotFoundException in ApbatchConnection class",e);	
			}
			
			try {				 
				 conn = ConnectionManager.getConnection("apbatch");
								
			} catch (SQLException e) {
				log.error("$$$ SQLException in ApbatchConnection class",e);
				System.out.println("$$$ SQLException in ApbatchConnection class" + e);
			} 
			 catch (NamingException e) {
				log.error("JNDI Lookup failed for DB2 connection", e);
			}

		
		return conn;
		
	}	
		
}