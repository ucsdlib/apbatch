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
public class EditIndexes extends HttpServlet {
	private static Logger log = Logger.getLogger(EditIndexes.class);

	/* client's request for server to perform an action */
	private static final String REQUEST_ACTION = "EDIT_INDEXES_REQUEST";

	/* actions client can request */
	private static final String REQUEST_ADD = "ADD";
	private static final String REQUEST_FETCH = "FETCH";
	private static final String REQUEST_EDIT = "EDIT";
	private static final String REQUEST_DELETE = "DELETE";
	private static final String REQUEST_DISPLAY = "DISPLAY";

	/* data labels expected from the client */
	private static final String INDEX = "index";
	private static final String HIDDEN_INDEX = "hidden_index";
	private static final String FUND = "fund";
	private static final String ORG = "org";
	private static final String PROG = "prog";
	private static final String TITLE = "title";

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
		String index = request.getParameter(INDEX);
		String fund = request.getParameter(FUND);
		String org = request.getParameter(ORG);
		String prog = request.getParameter(PROG);
		String title = request.getParameter(TITLE);

		index = index.trim();
		fund = fund.trim();
		org = org.trim();
		prog = prog.trim();
		title = title.trim();

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(
					" SELECT COUNT(*) AS INDEX_COUNT FROM GLINDEX A "+
					" WHERE A.AC_INDEX = ?");

			pstmt.setString(1, index);
			ResultSet rs_checkValidity = pstmt.executeQuery();
			rs_checkValidity.next();
			if(rs_checkValidity.getInt("INDEX_COUNT") != 0) {
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(HttpServletResponse.SC_CONFLICT);

				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write("Duplicate AC_INDEX.  Must be unique.");
				writer.close();

				conn.setAutoCommit(true);
				rs_checkValidity.close();
				pstmt.close();
				conn.close();
				log.info("INSERTION failed because of duplicate AC_INDEX.");
				return;
			}

			pstmt = conn.prepareStatement(
					" INSERT INTO GLINDEX (AC_INDEX, AC_FUND, AC_ORG, AC_PROG, AC_TITLE) " +
					" VALUES (?,?,?,?,?) "
					);

			pstmt.setString(1, index);
			pstmt.setString(2, fund);
			pstmt.setString(3, org);
			pstmt.setString(4, prog);
			pstmt.setString(5, title);

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
			htmlStr += "<tr id=\"editIndexes_checkbox_" + index + "\">";
			htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editIndexes_checkboxes\"" +
					" value=\"" + index + "\"></td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + index + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + fund + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + org + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + prog + "</td>";
			htmlStr += "<td class=\"tc_border tc_align\">" + title + "</td>";
			htmlStr += "</tr>";

			results.put("htmlStr", htmlStr);
			results.put("index", index);
			writer.write(results.toString());
			writer.close();

			log.info("Completing action for insertion of new index info.");
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
		String index = request.getParameter(INDEX);

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(
					" SELECT * FROM GLINDEX WHERE AC_INDEX = ? ");

			pstmt.setString(1, index);

			ResultSet rsTmp = pstmt.executeQuery();
			rsTmp.next();

			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(HttpServletResponse.SC_OK);

			PrintWriter writer = new PrintWriter(response.getOutputStream());
			JSONObject results = new JSONObject();
			results.put("index", index);
			results.put("fund", rsTmp.getString("AC_FUND"));
			results.put("org", rsTmp.getString("AC_ORG"));
			results.put("prog", rsTmp.getString("AC_PROG"));
			results.put("title", rsTmp.getString("AC_TITLE"));
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
		String hidden_index = request.getParameter(HIDDEN_INDEX);
		String index = request.getParameter(INDEX);
		String fund = request.getParameter(FUND);
		String org = request.getParameter(ORG);
		String prog = request.getParameter(PROG);
		String title = request.getParameter(TITLE);

		hidden_index = hidden_index.trim();
		index = index.trim();
		fund = fund.trim();
		org = org.trim();
		prog = prog.trim();
		title = title.trim();

		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement pstmt = null;

		try {
			conn.setAutoCommit(false);

			pstmt = conn.prepareStatement(
					" SELECT COUNT(*) AS INDEX_COUNT FROM GLINDEX A "+
					" WHERE A.AC_INDEX = ? AND A.AC_INDEX <> ?");

			pstmt.setString(1, index);
			pstmt.setString(2, hidden_index);
			ResultSet rs_checkValidity = pstmt.executeQuery();
			rs_checkValidity.next();
			if(rs_checkValidity.getInt("INDEX_COUNT") != 0) {
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(HttpServletResponse.SC_CONFLICT);

				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write("Duplicate AC_INDEX.  Must be unique.");
				writer.close();

				conn.setAutoCommit(true);
				rs_checkValidity.close();
				pstmt.close();
				conn.close();
				log.info("EDIT failed because of duplicate AC_INDEX.");
				return;
			}

			pstmt = conn.prepareStatement(
					" UPDATE GLINDEX A " +
					" SET A.AC_INDEX = ?, A.AC_FUND = ?, A.AC_ORG = ?, " +
					" A.AC_PROG = ?, A.AC_TITLE = ? " +
					" WHERE A.AC_INDEX = ? "
					);

			pstmt.setString(1, index);
			pstmt.setString(2, fund);
			pstmt.setString(3, org);
			pstmt.setString(4, prog);
			pstmt.setString(5, title);
			pstmt.setString(6, hidden_index);

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
				htmlStr += "<tr id=\"editIndexes_checkbox_" + index + "\">";
				htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editIndexes_checkboxes\"" +
						" value=\"" + index + "\"></td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + index + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + fund + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + org + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + prog + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + title + "</td>";
				htmlStr += "</tr>";
				results.put("index", index);
				results.put("hidden_index", hidden_index);
				results.put("htmlStr", htmlStr);
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
		/* an array AC_INDEX is expected in the form of a comma separated string
		 * EX: index : "1,2,3,4,...,n"
		 */
		String[] indexes = ((String)request.getParameter(INDEX)).split(",");
		Connection conn = ApbatchConnection.getConnection();
		PreparedStatement ps_delete = null;

		try {
			conn.setAutoCommit(false);

			ps_delete = conn.prepareStatement(
					" DELETE FROM GLINDEX A WHERE A.AC_INDEX = ? "
					);

			for(int i = 0; i < indexes.length; ++i) {
				ps_delete.setString(1, indexes[i]);
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

			log.info("Deleted from database row " + (String)request.getParameter(INDEX));
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
					" SELECT AC_INDEX, AC_FUND, AC_ORG, AC_PROG, AC_TITLE " +
					" FROM GLINDEX ORDER BY AC_INDEX "
					);

			ResultSet dataSet = pstmt.executeQuery();
			String htmlStr = "<table id=\"editIndexes_table\">" +
					"<thead>" +
						"<tr>" +
							"<th class=\"tc_align\"></th>" +
							"<th class=\"tc_align\">Index</th>" +
							"<th class=\"tc_align\">Fund</th>" +
							"<th class=\"tc_align\">Organization</th>" +
							"<th class=\"tc_align\">Program</th>" +
							"<th class=\"tc_align\">Description</th>" +
						"</tr>" +
					"</thead>" +
					"<tbody id=\"editIndexes_table_body\">";

			while(dataSet.next()) {
				htmlStr += "<tr id=\"editIndexes_checkbox_" + dataSet.getString(1) + "\">";

				htmlStr += "<td>" + "<input type=\"checkbox\" name=\"editIndexes_checkboxes\"" +
						" value=\"" + dataSet.getString(1) + "\"></td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(1) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(2) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(3) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(4) + "</td>";
				htmlStr += "<td class=\"tc_border tc_align\">" + dataSet.getString(5) + "</td>";

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

			log.info("Completed creating table of index info.");
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