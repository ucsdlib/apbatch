package edu.ucsd.library.apbatch;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * @author lib-phnguyen
 *
 */
public class EditVendors extends HttpServlet {
	private static Logger log = Logger.getLogger(EditVendors.class);

	/* client's request for server to perform an action */
	private static final String REQUEST_ACTION = "EDIT_VENDORS_REQUEST";

	/* actions client can request */
	private static final String REQUEST_ADD = "ADD";
	private static final String REQUEST_FETCH = "FETCH";
	private static final String REQUEST_EDIT = "EDIT";
	private static final String REQUEST_DELETE = "DELETE";
	private static final String REQUEST_DISPLAY = "DISPLAY";

	/* data labels expected from the client */
	private static final String VENDOR_ID = "vendor_id";
	private static final String VENDOR_NAME = "vendor_name";
	private static final String VENDOR_CODE = "vendor_code";
	private static final String PAYEE_ID = "payee_id";
	private static final String ADDRESS_TYPE = "address_type";
	private static final String TAX_CODE = "tax_code";
	private static final String NOTES = "notes";

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response) {
		log.info("Determining what to do with request.");
		String requested_action = request.getParameter(REQUEST_ACTION);
		log.info("Client requested " + requested_action);

		if(requested_action.equalsIgnoreCase(REQUEST_ADD)) {
			handleAdd(request, response);
		} else if(requested_action.equalsIgnoreCase(REQUEST_FETCH)) {
			handleFetch(request, response);
		} else if(requested_action.equalsIgnoreCase(REQUEST_EDIT)) {
			handleEdit(request, response);
		} else if(requested_action.equalsIgnoreCase(REQUEST_DELETE)) {
			handleDelete(request, response);
		} else if(requested_action.equalsIgnoreCase(REQUEST_DISPLAY)) {
			handleDisplay(request, response);
		} else {
			log.info("Client's request cannot be handled.");
		}
	}

	public void handleAdd(HttpServletRequest request,HttpServletResponse response) {
		String vendor_name = request.getParameter(VENDOR_NAME);
		String vendor_code = request.getParameter(VENDOR_CODE);
		String payee_id = request.getParameter(PAYEE_ID);
		String address_type = request.getParameter(ADDRESS_TYPE);
		String tax_code = request.getParameter(TAX_CODE);
		String notes = request.getParameter(NOTES);

		vendor_name = vendor_name.trim();
		vendor_code = vendor_code.trim();
		payee_id = payee_id.trim();
		address_type = address_type.trim();
		tax_code = tax_code.trim();
		notes = notes.trim();

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(
					" SELECT COUNT(*) AS VENDOR_CODE_COUNT FROM VENDORS A "+
					" WHERE A.VENDCODE = ?");

			pstmt.setString(1, vendor_code);
			ResultSet rs_checkValidity = pstmt.executeQuery();
			rs_checkValidity.next();
			if(rs_checkValidity.getInt("VENDOR_CODE_COUNT") != 0) {
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(HttpServletResponse.SC_CONFLICT);

				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write("Duplicate Vendor Code.  Must be unique.");
				writer.close();

				conn.setAutoCommit(true);
				rs_checkValidity.close();
				pstmt.close();
				conn.close();
				log.info("INSERTION failed because of duplicate vendor code.");
				return;
			}

			pstmt = conn.prepareStatement(
					" SELECT DISTINCT A.VENDID " +
					" FROM VENDORS A " +
					" WHERE A.VENDID not in " +
						" (SELECT Z.VENDID " +
						" FROM VENDORS Z, VENDORS X " +
						" WHERE Z.VENDID < X.VENDID)"
					);
			ResultSet rsTmp = pstmt.executeQuery();
			rsTmp.next();
			int vendor_id = rsTmp.getInt("VENDID") + 1;
			rsTmp.close();
			pstmt.close();

			pstmt = conn.prepareStatement(
					" INSERT INTO VENDORS (VENDID, VENDCODE, NAME, " +
											"ADDR_TYPE, TAXCODE, " +
											" PAYEE_ID, NOTES) " +
					" VALUES (?,?,?,?,?,?,?) "
					);

			pstmt.setInt(1, vendor_id);
			pstmt.setString(2, vendor_code);
			pstmt.setString(3, vendor_name);
			pstmt.setString(4, address_type);
			pstmt.setString(5, tax_code);
			pstmt.setString(6, payee_id);
			pstmt.setString(7, notes);

			pstmt.executeUpdate();

			conn.commit();
			pstmt.close();
			conn.setAutoCommit(true);
			conn.close();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			PrintWriter writer = new PrintWriter(response.getOutputStream());
			JSONObject results = new JSONObject();
			String htmlStr = "";
			htmlStr += "<tr id=\"editVendors_checkbox_" + vendor_id + "\" tooltip=\"" +
			((notes == null || notes.length() == 0) ? "No comments" : notes) + "\">";
			htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editVendors_checkboxes\"" +
					" value=\"" + vendor_id + "\"></td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + vendor_code + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + vendor_name + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + payee_id + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + address_type + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + tax_code + "</td>";
			htmlStr += "</tr>";

			results.put("htmlStr", htmlStr);
			results.put("vendor_name", vendor_name);
			results.put("vendor_code", vendor_code);
			results.put("vendor_id", vendor_id);
			writer.write(results.toString());
			writer.close();

			log.info("Completing action for insertion of new vendor info.");
		} catch(SQLException se) {
			log.info(se.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete transaction for ADD");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		} catch(IOException ie) {
			log.info(ie.getMessage());
		}
	}

	public void handleFetch(HttpServletRequest request,HttpServletResponse response) {
		String vendor_id = request.getParameter(VENDOR_ID);

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(
					" SELECT * FROM VENDORS WHERE VENDID = ? ");

			pstmt.setInt(1, Integer.parseInt(vendor_id));

			ResultSet rsTmp = pstmt.executeQuery();
			rsTmp.next();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			PrintWriter writer = new PrintWriter(response.getOutputStream());
			JSONObject results = new JSONObject();
			results.put("vendor_id", vendor_id);
			results.put("vendor_name", rsTmp.getString("NAME"));
			results.put("vendor_code", rsTmp.getString("VENDCODE"));
			results.put("payee_id", rsTmp.getString("PAYEE_ID"));
			results.put("address_type", rsTmp.getString("ADDR_TYPE"));
			results.put("tax_code", rsTmp.getString("TAXCODE"));
			results.put("notes", rsTmp.getString("NOTES"));
			writer.write(results.toString());
			writer.close();

			rsTmp.close();
			pstmt.close();
			conn.close();

			log.info("Fetched vendor info.");
		} catch(SQLException se) {
			log.info(se.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete transaction for FETCH");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		} catch(IOException ie) {
			log.info(ie.getMessage());
		}
	}

	public void handleEdit(HttpServletRequest request,HttpServletResponse response) {
		String vendor_id = request.getParameter(VENDOR_ID);
		String vendor_name = request.getParameter(VENDOR_NAME);
		String vendor_code = request.getParameter(VENDOR_CODE);
		String payee_id = request.getParameter(PAYEE_ID);
		String address_type = request.getParameter(ADDRESS_TYPE);
		String tax_code = request.getParameter(TAX_CODE);
		String notes = request.getParameter(NOTES);

		vendor_id = vendor_id.trim();
		vendor_name = vendor_name.trim();
		vendor_code = vendor_code.trim();
		payee_id = payee_id.trim();
		address_type = address_type.trim();
		tax_code = tax_code.trim();
		notes = notes.trim();

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			conn.setAutoCommit(false);

			pstmt = conn.prepareStatement(
					" SELECT COUNT(*) AS VENDOR_CODE_COUNT FROM VENDORS A "+
					" WHERE A.VENDCODE = ? AND A.VENDID <> ? ");

			pstmt.setString(1, vendor_code);
			pstmt.setInt(2, Integer.parseInt(vendor_id));
			ResultSet rs_checkValidity = pstmt.executeQuery();
			rs_checkValidity.next();
			if(rs_checkValidity.getInt("VENDOR_CODE_COUNT") != 0) {
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(HttpServletResponse.SC_CONFLICT);

				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write("Duplicate Vendor Code.  Must be unique.");
				writer.close();

				conn.setAutoCommit(true);
				rs_checkValidity.close();
				pstmt.close();
				conn.close();
				log.info("EDIT failed because of duplicate vendor code.");
				return;
			}

			pstmt = conn.prepareStatement(
					" UPDATE VENDORS A " +
					" SET A.VENDCODE = ?, A.NAME = ?, A.ADDR_TYPE = ?, " +
					" A.TAXCODE = ?, A.PAYEE_ID = ?, A.NOTES = ? " +
					" WHERE A.VENDID = ? "
					);

			pstmt.setString(1, vendor_code);
			pstmt.setString(2, vendor_name);
			pstmt.setString(3, address_type);
			pstmt.setString(4, tax_code);
			pstmt.setString(5, payee_id);
			pstmt.setString(6, notes);
			pstmt.setInt(7, Integer.parseInt(vendor_id));

			pstmt.executeUpdate();

			conn.commit();
			conn.setAutoCommit(true);

			pstmt.close();
			conn.close();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			try {
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				JSONObject results = new JSONObject();
				String htmlStr = "";
				htmlStr += "<tr id=\"editVendors_checkbox_" + vendor_id + "\" tooltip=\"" +
				((notes == null || notes.length() == 0) ? "No comments" : notes) + "\">";
				htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editVendors_checkboxes\"" +
						" value=\"" + vendor_id + "\"></td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + vendor_code + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + vendor_name + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + payee_id + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + address_type + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + tax_code + "</td>";
				htmlStr += "</tr>";
				results.put("htmlStr", htmlStr);
				results.put("vendor_id", vendor_id);
				results.put("vendor_name", vendor_name);
				results.put("vendor_code", vendor_code);
				writer.write(results.toString());
				writer.close();
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}

			log.info("Updated vendor info.");
		} catch(SQLException se) {
			log.info(se.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete transaction for EDIT");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		} catch(IOException ioe) {
			log.info(ioe.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete transaction for EDIT");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		}
	}

	public void handleDelete(HttpServletRequest request,HttpServletResponse response) {
		/* an array VENDOR_IDs is expected in the form of a comma separated string
		 * EX: vendor_id : "1,2,3,4,...,n"
		 */
		String[] vendor_ids = ((String)request.getParameter(VENDOR_ID)).split(",");
		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement ps_delete = null;

		try {
			conn.setAutoCommit(false);

			ps_delete = conn.prepareStatement(
					" DELETE FROM VENDORS A WHERE A.VENDID = ? "
					);

			for(int i = 0; i < vendor_ids.length; ++i) {
				ps_delete.setInt(1, Integer.parseInt(vendor_ids[i]));
				ps_delete.executeUpdate();
			}

			conn.commit();
			conn.setAutoCommit(true);

			ps_delete.close();
			conn.close();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			try {
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				JSONObject results = new JSONObject();
				results.put("status", "OKAY");
				writer.write(results.toString());
				writer.close();
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}

			log.info("Deleted from database row " + (String)request.getParameter(VENDOR_ID));
		} catch(SQLException se) {
			log.info(se.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete transaction for DELETE");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		}
	}

	public void handleDisplay(HttpServletRequest request,HttpServletResponse response) {
		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(
					" SELECT VENDID, VENDCODE, NAME, PAYEE_ID, ADDR_TYPE, TAXCODE, NOTES " +
					" FROM VENDORS ORDER BY NAME, VENDCODE "
					);

			ResultSet dataSet = pstmt.executeQuery();
			String htmlStr = "<table id=\"editVendors_table\">" +
					"<thead>" +
						"<tr>" +
							"<th class=\"tc_align\"></th>" +
							"<th class=\"tc_align\">Vendor Code</th>" +
							"<th class=\"tc_align\">Vendor Name</th>" +
							"<th class=\"tc_align\">Payee ID</th>" +
							"<th class=\"tc_align\">Address Type</th>" +
							"<th class=\"tc_align\">Tax Code</th>" +
						"</tr>" +
					"</thead>" +
					"<tbody id=\"editVendors_table_body\">";

			while(dataSet.next()) {
				htmlStr += "<tr id=\"editVendors_checkbox_" + dataSet.getInt(1) + "\" tooltip=\"" +
					((dataSet.getString(7) == null || dataSet.getString(7).length() == 0) ? "No comments" : 
						dataSet.getString(7)) + "\">";

				htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editVendors_checkboxes\"" +
						" value=\"" + dataSet.getInt(1) + "\"></td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(2) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(3) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(4) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(5) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(6) + "</td>";

				htmlStr += "</tr>";
			}
			htmlStr += "</tbody></table>";

			dataSet.close();
			pstmt.close();
			conn.close();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			PrintWriter writer = new PrintWriter(response.getOutputStream());
			JSONObject results = new JSONObject();
			results.put("htmlStr", htmlStr);
			writer.write(results.toString());
			writer.close();

			log.info("Completed creating table of vendor info.");
		} catch(SQLException se) {
			log.info(se.getMessage());
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database failed to complete grabbing data for DISPLAY");
			} catch(IOException ie) {
				log.info(ie.getMessage());
			}
		} catch(IOException ie) {
			log.info(ie.getMessage());
		}
	}
}