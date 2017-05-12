package edu.ucsd.library.apbatch;
import java.sql.*;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import edu.ucsd.library.util.sql.ConnectionManager;
public class ApbatchConnection{
	
	private static Logger log = Logger.getLogger( ApbatchConnection.class );
	
	public static Connection getTestConnection(){
		Connection conn = null;
		
		try{			
			Class.forName("oracle.jdbc.driver.OracleDriver");	

			}
			catch(ClassNotFoundException e)
			{
				log.error("$$$ ClassNotFoundException in ApbatchConnection class",e);	
			}
			
			try {				 
				conn = ConnectionManager.getConnection("apbatch-test");
			} catch (SQLException e) {
				log.error("$$$ SQLException in ApbatchConnection class",e);
				System.out.println("$$$ SQLException in ApbatchConnection class getTestConnection" + e);
				e.printStackTrace();
			} catch (NamingException e) {
				log.error("JNDI Lookup failed for DB2 connection", e);
			}
		return conn;
		
	}
	
	public static Connection getConnection(){
		Connection conn = null;
		
		try{			
			Class.forName("oracle.jdbc.driver.OracleDriver");	

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