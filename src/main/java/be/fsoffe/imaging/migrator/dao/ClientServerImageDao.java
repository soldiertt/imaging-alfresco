package be.fsoffe.imaging.migrator.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Dao to access client/server database.
 * 
 * @author jbourlet
 *
 */
public class ClientServerImageDao {
	
	private static final String SQL_CS_DOCUMENT_IS_LINKED = "select count(*) from image WHERE nrimage = ?";
	
	private static final String SQL_CS_DOCUMENT_PROCESSED_BY = "select personne from migr WHERE docid = ? and personne <> ''";
	
	private DataSource dataSource;

	/**
	 * Check if the document is linked in client/server database, table images.
	 * @param legacyDocId the original document name
	 * @return linked status
	 * @throws SQLException if error occurs
	 */
	public boolean documentIsLinked(String legacyDocId) throws SQLException {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean isLinked = false;
		
		stmt = connection.prepareStatement(SQL_CS_DOCUMENT_IS_LINKED);
		stmt.setString(1, legacyDocId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next() && rs.getInt(1) > 0) {
			isLinked = true;
		}

		rs.close();
		connection.close();
		stmt.close();
	
		return isLinked;
	}
	
	/**
	 * Set the datasource.
	 * @param dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Get field 'Personne' from new 'migr' table in C/S Schema.
	 * 
	 * @param legacyDocId
	 * @return personne as processedBy user or null if no personne were found.
	 * @throws SQLException 
	 */
	public String getProcessedBy(String legacyDocId) throws SQLException {
		String processedBy = null;
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		
		stmt = connection.prepareStatement(SQL_CS_DOCUMENT_PROCESSED_BY);
		stmt.setString(1, legacyDocId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			processedBy = rs.getString(1);
		}

		rs.close();
		connection.close();
		stmt.close();
	
		return processedBy;
	}
	
}
