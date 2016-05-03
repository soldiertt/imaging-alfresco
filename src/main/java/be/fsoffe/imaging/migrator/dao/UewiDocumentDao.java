package be.fsoffe.imaging.migrator.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import be.fsoffe.imaging.migrator.Environment;
import be.fsoffe.imaging.migrator.model.ImagingDocument;
import be.fsoffe.imaging.migrator.model.InDateFormat;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Dao class to access Uewi database.
 * 
 * @author jbourlet
 *
 */
public class UewiDocumentDao {

	private static final Log LOGGER = LogFactory.getLog(UewiDocumentDao.class);
	
	// NO WIP
	private static final String SQL_UEWI_SELECT_DOCUMENTS_TO_PROCESS = "SELECT exp.objectid, exp.docname, exp.has_atu, obj.static,"
				+ " obj.wip_count FROM alfresco_export exp, object obj, attributes attr WHERE exp.objectid=obj.id"
				+ " AND exp.objectid=attr.object_id AND (attr.doctype NOT LIKE 'NOTTOKEEP%' OR attr.doctype IS NULL) AND exp.status=10 AND obj.wip_count=0"
				+ " ORDER BY exp.OBJECTID";
	// WIP DOCUMENTS
	private static final String SQL_UEWI_SELECT_DOCUMENTS_TO_PROCESS_WIP = "SELECT exp.objectid, exp.docname, exp.has_atu, obj.static,"
			+ " obj.wip_count FROM alfresco_export exp, object obj, attributes attr WHERE exp.objectid=obj.id"
			+ " AND exp.objectid=attr.object_id AND (attr.doctype NOT LIKE 'NOTTOKEEP%' OR attr.doctype IS NULL) AND exp.status=10 AND obj.wip_count > 0"
			+ " ORDER BY exp.OBJECTID";
	
	private static final String SQL_UEWI_UPDATE_STATUS = "UPDATE alfresco_export SET status=?, docname=? WHERE objectid = ?";
	
	private static final String SQL_UEWI_UPDATE_IMPORTSTATUS = "UPDATE alfresco_export SET import_status=? WHERE objectid = ?";
	
	// LIST OF DOCUMENTS TO UPDATE
	private static final String SQL_UEWI_SELECT_DOCUMENTS_TO_UPDATE = "SELECT exp.objectid, exp.docname, exp.has_atu, obj.static,"
			+ " obj.wip_count, attr.scandate, attr.dossiernr, attr.object_class FROM alfresco_export exp, object obj, attributes attr WHERE exp.objectid=obj.id"
			+ " AND exp.objectid=attr.object_id AND (attr.doctype NOT LIKE 'NOTTOKEEP%' OR attr.doctype IS NULL) AND exp.status=11";
	
	// LIST OF DOCUMENTS TO COMPLETE IMPORTS
	private static final String SQL_UEWI_SELECT_DOCUMENTS_TO_COMPLETE_IMPORTS = "SELECT exp.objectid, exp.docname, exp.has_atu, obj.static,"
			+ " obj.wip_count, attr.scandate, attr.dossiernr, attr.object_class FROM alfresco_export exp, object obj, attributes attr WHERE exp.objectid=obj.id"
			+ " AND exp.objectid=attr.object_id AND exp.status in (11,110) and exp.import_status = 0";
		
	private int batchSize;
	private DataSource dataSource;
	
	/**
	 * Get the imaging documents matching the criteria for migration.
	 * @return list of imaging documents
	 * @throws SQLException if error occurs
	 */
	public List<ImagingDocument> getDocumentsToProcess(boolean wipMode) throws SQLException {
		List<ImagingDocument> documentsToProcess = new ArrayList<ImagingDocument>();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		String sql = null;
		if (wipMode) {
			sql = SQL_UEWI_SELECT_DOCUMENTS_TO_PROCESS_WIP;
		} else {
			sql = SQL_UEWI_SELECT_DOCUMENTS_TO_PROCESS;
		}
		stmt = connection.prepareStatement(sql);
		stmt.setMaxRows(batchSize);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			ImagingDocument docRow = new ImagingDocument(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getString(4), rs.getInt(5));
			documentsToProcess.add(docRow);
		}
		rs.close();
		connection.close();
		stmt.close();
		
		return documentsToProcess;
	}

	/**
	 * Update the document status in the migration table.
	 * 
	 * @param imgDoc the imaging document
	 * @param status the new status
	 * @param docName the document name in alfresco
	 * @return success status
	 */
	public boolean updateDocumentStatus(ImagingDocument imgDoc, int status, String docName, Environment env) {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean updateOK = false;
		
		try {
			stmt = connection.prepareStatement(SQL_UEWI_UPDATE_STATUS);
			if (Environment.DEV.equals(env) || Environment.TEST.equals(env)) {
				status = status + 30;
			}
			stmt.setInt(1, status);
			stmt.setString(2, docName);
			stmt.setString(3, imgDoc.getLegacyDocId());
			int rowAffected = stmt.executeUpdate();
			updateOK = (rowAffected == 1);
			connection.commit();
			connection.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.error("Cannot update record status !", e);
		}
		
		return updateOK;
	}
	
	/**
	 * Update the document import status in the migration table.
	 * 
	 * @param imgDoc the imaging document
	 * @param status the new status
	 * @param docName the document name in alfresco
	 * @return success status
	 */
	public boolean updateDocumentImportStatus(ImagingDocument imgDoc, int status, Environment env) {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean updateOK = false;
		
		try {
			stmt = connection.prepareStatement(SQL_UEWI_UPDATE_IMPORTSTATUS);
			if (Environment.DEV.equals(env) || Environment.TEST.equals(env)) {
				status = status + 30;
			}
			stmt.setInt(1, status);
			stmt.setString(2, imgDoc.getLegacyDocId());
			int rowAffected = stmt.executeUpdate();
			updateOK = (rowAffected == 1);
			connection.commit();
			connection.close();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.error("Cannot update record status !", e);
		}
		
		return updateOK;
	}
	
	/**
	 * Set the dataSource.
	 * @param dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the batch size.
	 * @param batchSize to set
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * List of documents to update - post migration.
	 * 
	 * @return a list of imaging document
	 * @throws SQLException 
	 */
	public List<ImagingDocument> getDocumentsToUpdate() throws SQLException {
		List<ImagingDocument> documentsToUpdate = new ArrayList<ImagingDocument>();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		String sql = SQL_UEWI_SELECT_DOCUMENTS_TO_UPDATE;
		PreparedStatement stmt = connection.prepareStatement(sql);
		stmt.setMaxRows(batchSize);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			ImagingDocument docRow = new ImagingDocument(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getString(4), rs.getInt(5));
			String docInDateStr = rs.getString(6);
			String[] dateFormats = {"yyyyMMddHHmmss", "yyyy-MM-dd"};
			
			if (docInDateStr != null && !"".equals(docInDateStr.trim())) {
				InDateFormat inDateFormat = null;
				boolean formatOK = false;
				for (int i = 0; i < dateFormats.length; i++) {
					SimpleDateFormat sdf = new SimpleDateFormat(dateFormats[i]);
					try {
						sdf.parse(docInDateStr);
						formatOK = true;
						if (i == 0) {
							inDateFormat = InDateFormat.LONG;
						} else if (i == 1) {
							inDateFormat = InDateFormat.SHORT;
						}
						break;
					} catch (java.text.ParseException e) {
						//Unable to parse this date with this format (try next format)
						formatOK = false;
					}
				}
				if (formatOK) {
					docRow.setDocInDateFormat(inDateFormat);
				}
			}
			
			docRow.setWorkflowDossierNr(rs.getString(7));
			docRow.setDocClass(rs.getString(8)); //Original document class, not mapped
			documentsToUpdate.add(docRow);
		}
		rs.close();
		connection.close();
		stmt.close();
		
		return documentsToUpdate;
	}
	
	/**
	 * List of documents to complete Imports - post migration.
	 * 
	 * @return a list of imaging document
	 * @throws SQLException 
	 */
	public List<ImagingDocument> getDocumentsToCompleteImports() throws SQLException {
		List<ImagingDocument> documentsToComplete = new ArrayList<ImagingDocument>();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		String sql = SQL_UEWI_SELECT_DOCUMENTS_TO_COMPLETE_IMPORTS;
		PreparedStatement stmt = connection.prepareStatement(sql);
		stmt.setMaxRows(batchSize);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()) {
			ImagingDocument docRow = new ImagingDocument(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getString(4), rs.getInt(5));
			documentsToComplete.add(docRow);
		}
		rs.close();
		connection.close();
		stmt.close();
		
		return documentsToComplete;
	}
}
