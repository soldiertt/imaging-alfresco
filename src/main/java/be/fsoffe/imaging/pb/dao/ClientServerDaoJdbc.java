package be.fsoffe.imaging.pb.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import be.fsoffe.imaging.pb.exception.ImagingPBException;
import be.fsoffe.imaging.pb.mapper.LinkContextMapper;
import be.fsoffe.imaging.pb.model.Keyword;
import be.fsoffe.imaging.pb.model.LinkContext;

/**
 * DAO to manage operations on client server database.
 * 
 * @author jbourlet
 *
 */
public class ClientServerDaoJdbc implements Serializable {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = -5876425835999614953L;

	private static final Log LOGGER = LogFactory
			.getLog(ClientServerDaoJdbc.class);

	private transient DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Read the link context store in the C/S database.
	 * 
	 * @param userName
	 *            the actual logged in user
	 * @return all link info
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public List<LinkContext> readContextForUser(String userName)
			throws ImagingPBException {

		List<LinkContext> contexts = new ArrayList<LinkContext>();

		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
		PreparedStatement stmt5 = null;
		PreparedStatement stmt6 = null;

		try {
			stmt = connection.prepareStatement(
					CSConstants.SQL_VALID_CONTEXT_FOR_USER_1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, userName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				LinkContext lc = LinkContextMapper.mapLinkContext(rs);
				if (lc.getRefEmployer() != null) {
					stmt2 = connection.prepareStatement(
							CSConstants.SQL_EMPLOYER_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt2.setLong(1, lc.getRefEmployer());
					ResultSet rs2 = stmt2.executeQuery();
					if (rs2.next()) {
						lc.setEmployerName(rs2.getString(1));
					}
				}
				if (lc.getRefWorker() != null) {
					stmt3 = connection.prepareStatement(
							CSConstants.SQL_WORKER_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt3.setLong(1, lc.getRefWorker());
					ResultSet rs3 = stmt3.executeQuery();
					if (rs3.next()) {
						lc.setWorkerName(rs3.getString(1));
					}
				}
				if (lc.getRefPerson() != null) {
					stmt4 = connection.prepareStatement(
							CSConstants.SQL_PERSON_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt4.setLong(1, lc.getRefPerson());
					ResultSet rs4 = stmt4.executeQuery();
					if (rs4.next()) {
						lc.setPersonName(rs4.getString(1));
					}
				}
				if (lc.getRefKeyword1() != null) {
					stmt5 = connection.prepareStatement(
							CSConstants.SQL_KEYWORD_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt5.setLong(1, lc.getRefKeyword1());
					ResultSet rs5 = stmt5.executeQuery();
					if (rs5.next()) {
						lc.setKeyword1Name(rs5.getString(1));
					}
				}
				if (lc.getRefKeyword2() != null) {
					stmt6 = connection.prepareStatement(
							CSConstants.SQL_KEYWORD_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt6.setLong(1, lc.getRefKeyword2());
					ResultSet rs6 = stmt6.executeQuery();
					if (rs6.next()) {
						lc.setKeyword2Name(rs6.getString(1));
					}
				}
				contexts.add(lc);
				
			}
			rs.close();
			return contexts;
		} catch (SQLException e) {
			LOGGER.error("Error getting PB context for user : " + userName, e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
				if (stmt2 != null) {
					stmt2.close();
				}
				if (stmt3 != null) {
					stmt3.close();
				}
				if (stmt4 != null) {
					stmt4.close();
				}
				if (stmt5 != null) {
					stmt5.close();
				}
				if (stmt6 != null) {
					stmt6.close();
				}
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}
	}

	/**
	 * List authorized document types given a screen id.
	 * 
	 * @param refScreen
	 *            screen reference
	 * @return document types
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public List<String> getDocTypesByScreenId(Long refScreen)
			throws ImagingPBException {

		List<String> docTypes = new ArrayList<String>();

		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(
					CSConstants.SQL_DOCTYPES_BY_SCREEN_1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setLong(1, refScreen);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				docTypes.add(rs.getString(1).trim());
			}
		} catch (SQLException e) {
			LOGGER.error("Error retrieving doctypes", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}
		return docTypes;

	}

	/**
	 * Link an Image to a C/S Dossier.
	 * 
	 * @param imageName
	 *            the document name
	 * @param docTypeId
	 *            the id of the document type
	 * @param linkContext
	 *            the link details
	 * @return true if ok false if not
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public boolean linkImageToDossier(String imageName, Long docTypeId,
			LinkContext linkContext) throws ImagingPBException {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean updateOK = false;

		try {
			stmt = connection
					.prepareStatement(CSConstants.SQL_INSERT_IMAGE_LINK);
			stmt.setString(1, imageName);
			setSatementIntValue(stmt, 2, linkContext.getRefDossier());
			if (linkContext.getMap() == null) {
				stmt.setNull(3, Types.CHAR);
			} else {
				stmt.setString(3, linkContext.getMap());
			}
			setSatementIntValue(stmt, 4, docTypeId);
			setSatementIntValue(stmt, 5, linkContext.getRefEmployer());
			setSatementIntValue(stmt, 6, linkContext.getRefWorker());
			setSatementIntValue(stmt, 7, linkContext.getRefPerson());
			setSatementIntValue(stmt, 8, linkContext.getRefKeyword1());
			setSatementIntValue(stmt, 9, linkContext.getRefKeyword2());
			setSatementIntValue(stmt, 10, linkContext.getRefGajur());
			if (null != linkContext.getRefGajur()) {
				LOGGER.debug("ref_gajur value to be inserted "
						+ linkContext.getRefGajur().toString());
			} else {
				LOGGER.debug("ref_gajur value to be inserted is NULL");
			}

			int rowAffected = stmt.executeUpdate();
			updateOK = (rowAffected == 1);

		} catch (SQLException e) {
			LOGGER.error("Error inserting into image", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
				connection.commit();
				connection.close();

			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}

		return updateOK;
	}

	/**
	 * Set value or null.
	 * 
	 * @param stmt
	 *            the prepare statement
	 * @param index
	 *            position of field
	 * @param value
	 *            Long value to convert to integer
	 * @throws SQLException
	 *             if error occurs
	 */
	private void setSatementIntValue(PreparedStatement stmt, int index,
			Long value) throws SQLException {
		if (value == null) {
			stmt.setNull(index, Types.INTEGER);
		} else {
			stmt.setInt(index, value.intValue());
		}
	}

	/**
	 * Find a specific context based on the session id.
	 * 
	 * @param sessionId the PB session id
	 * @param userName the currently logged in user
	 * @return a unique context
	 * @throws ImagingPBException if error occurs
	 */
	public LinkContext getContextBySessionId(Long sessionId, String userName) throws ImagingPBException {

		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
		PreparedStatement stmt5 = null;
		PreparedStatement stmt6 = null;

		LinkContext lc = null;

		try {

			stmt = connection.prepareStatement(
					CSConstants.SQL_CONTEXT_BY_SESSION_ID_1_AND_USER_2,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setLong(1, sessionId);
			stmt.setString(2, userName); // For security purpose also filter on logged in user.
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				lc = LinkContextMapper.mapLinkContext(rs);
				if (lc.getRefEmployer() != null) {
					stmt2 = connection.prepareStatement(
							CSConstants.SQL_EMPLOYER_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt2.setLong(1, lc.getRefEmployer());
					ResultSet rs2 = stmt2.executeQuery();
					if (rs2.next()) {
						lc.setEmployerName(rs2.getString(1));
					}
				}
				if (lc.getRefWorker() != null) {
					stmt3 = connection.prepareStatement(
							CSConstants.SQL_WORKER_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt3.setLong(1, lc.getRefWorker());
					ResultSet rs3 = stmt3.executeQuery();
					if (rs3.next()) {
						lc.setWorkerName(rs3.getString(1));
					}
				}
				if (lc.getRefPerson() != null) {
					stmt4 = connection.prepareStatement(
							CSConstants.SQL_PERSON_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt4.setLong(1, lc.getRefPerson());
					ResultSet rs4 = stmt4.executeQuery();
					if (rs4.next()) {
						lc.setPersonName(rs4.getString(1));
					}
				}
				if (lc.getRefKeyword1() != null) {
					stmt5 = connection.prepareStatement(
							CSConstants.SQL_KEYWORD_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt5.setLong(1, lc.getRefKeyword1());
					ResultSet rs5 = stmt5.executeQuery();
					if (rs5.next()) {
						lc.setKeyword1Name(rs5.getString(1));
					}
				}
				if (lc.getRefKeyword2() != null) {
					stmt6 = connection.prepareStatement(
							CSConstants.SQL_KEYWORD_NAME_BY_ID_1,
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					stmt6.setLong(1, lc.getRefKeyword2());
					ResultSet rs6 = stmt6.executeQuery();
					if (rs6.next()) {
						lc.setKeyword2Name(rs6.getString(1));
					}
				}
			}

		} catch (SQLException e) {
			LOGGER.error("Error retrieving context by sessionid", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
				if (stmt2 != null) {
					stmt2.close();
				}
				if (stmt3 != null) {
					stmt3.close();
				}
				if (stmt4 != null) {
					stmt4.close();
				}
				if (stmt5 != null) {
					stmt5.close();
				}
				if (stmt6 != null) {
					stmt6.close();
				}
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}

		if (lc != null) {
			return lc;
		} else {
			LOGGER.error("Error retrieving context with id " + sessionId);
			throw new ImagingPBException();
		}
	}

	/**
	 * Return the doc type id given its name.
	 * 
	 * @param documentType
	 *            the document type
	 * @return the id
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public Long getDocTypeIdByName(String documentType)
			throws ImagingPBException {

		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		Long docTypeId = null;

		try {
			stmt = connection.prepareStatement(
					CSConstants.SQL_ID_DOCTYPE_BY_NAME_1,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, documentType);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				docTypeId = rs.getLong(1);
			}

		} catch (SQLException e) {
			LOGGER.error("Error retrieving doctype id", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}

		if (docTypeId == null) {
			LOGGER.warn("Cannot find doctype id for type '" + documentType
					+ "'");
		}
		return docTypeId;
	}

	/**
	 * 
	 * Check if document is already linked with the same context.
	 * 
	 * @param documentName
	 *            the document name
	 * @param docTypeId
	 *            the document type id
	 * @param context
	 *            the context to check
	 * @return true if already linked or false if not
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public boolean checkAlreadyLinked(String documentName, Long docTypeId,
			LinkContext context) throws ImagingPBException {

		String sqlExactMatch = CSConstants.SQL_COUNT_IMAGES_BY_IMAGENAME_1	+ " AND " 
		        + CSConstants.TBL_IMG_DOSSIER
				+ valueOrNull(context.getRefDossier()) + " AND "
				+ CSConstants.TBL_IMG_MAP 
				+ valueOrNull(context.getMap())	+ " AND " 
				+ CSConstants.TBL_IMG_DOCTYPE
				+ valueOrNull(docTypeId) + " AND "
				+ CSConstants.TBL_IMG_EMPLOYER
				+ valueOrNull(context.getRefEmployer()) + " AND "
				+ CSConstants.TBL_IMG_WORKER
				+ valueOrNull(context.getRefWorker()) + " AND "
				+ CSConstants.TBL_IMG_PERSON
				+ valueOrNull(context.getRefPerson()) + " AND "
				+ CSConstants.TBL_IMG_KEYW_ONE
				+ valueOrNull(context.getRefKeyword1()) + " AND "
				+ CSConstants.TBL_IMG_KEYW_TWO
				+ valueOrNull(context.getRefKeyword2()) + " AND "
				+ CSConstants.TBL_IMG_GAJUR
				+ valueOrNull(context.getRefGajur());
        /*
		String sqlPersonOrKeyword = CSConstants.SQL_COUNT_IMAGES_BY_IMAGENAME_1
				+ " AND (" + CSConstants.TBL_IMG_PERSON + " IS NOT NULL"
				+ " OR " + CSConstants.TBL_IMG_KEYW_ONE + " IS NOT NULL"
				+ " OR " + CSConstants.TBL_IMG_KEYW_TWO + " IS NOT NULL)";
          */
		LOGGER.info("SQL_EXACT_MATCH :" + sqlExactMatch);
		//LOGGER.info("SQL_PERSON_OR_KEYWORD :" + sqlPersonOrKeyword);
		
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt1 = null;
		//PreparedStatement stmt2 = null;

		int countExactMatch = 1; // Prevent insert by default
		//int countPersonOrKeyword = 1; // Prevent insert by default

		try {
			stmt1 = connection.prepareStatement(sqlExactMatch,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt1.setString(1, documentName);
			ResultSet rs = stmt1.executeQuery();
			rs.next();
			countExactMatch = rs.getInt("total");
			/*
			stmt2 = connection.prepareStatement(sqlPersonOrKeyword,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt2.setString(1, documentName);
			rs = stmt2.executeQuery();
			rs.next();
			countPersonOrKeyword = rs.getInt("total");
			*/
		} catch (SQLException e) {
			LOGGER.error("Error checking if already linked", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt1.close();
				//stmt2.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}
		LOGGER.info("COUNT_EXACT_MATCH :" + countExactMatch);
		//LOGGER.info("COUNT_PERSON_OR_KEYWORD :" + countPersonOrKeyword);
		//return (countExactMatch > 0 || countPersonOrKeyword > 0);
		return (countExactMatch > 0);
	}

	/**
	 * Return sql string for field value.
	 * 
	 * @param value
	 *            the long value
	 * @return string
	 */
	private String valueOrNull(Long value) {
		if (value == null) {
			return " IS NULL";
		} else {
			return " = " + value.toString();
		}
	}

	/**
	 * Return sql string for field value.
	 * 
	 * @param value
	 *            the string value
	 * @return string
	 */
	private String valueOrNull(String value) {
		if (value == null) {
			return " IS NULL";
		} else {
			return " = '" + value + "'";
		}
	}

	/**
	 * Return sql string for field value.
	 * 
	 * @param value
	 *            the long value
	 * @return string
	 */
	private String valueOrEmpty(String field, Long value) {
		if (value == null) {
			return " ";
		} else {
			return " AND " + field + " = " + value.toString();
		}
	}

	/**
	 * Return sql string for field value.
	 * 
	 * @param value
	 *            the string value
	 * @return string
	 */
	private String valueOrEmpty(String field, String value) {
		if (value == null || "".equals(value.trim())) {
			return " ";
		} else {
			return " AND " + field + " = '" + value + "'";
		}
	}

	/**
	 * Remove the last character of a string
	 * 
	 * @param value
	 *            the string value
	 * @return string
	 */
	private String deleteLastChar(String phrase) {

		String rephrase = null;
		if (phrase != null && phrase.length() > 1) {
			rephrase = phrase.substring(0, phrase.length() - 1);
		}

		return rephrase;
	}

	/**
	 * Remove "AND" at the beginning of the string and "," at the end of the
	 * string
	 * 
	 * @param context
	 *            the context to match
	 * @return sql string
	 */
	private String cleanSqlConditions(String qc) {
		String s = qc;

		if (qc.startsWith("AND")) {
			s = qc.replaceFirst("AND", " ");
		}
		if (qc.endsWith(",")) {
			s = deleteLastChar(qc);
		}
		return s;
	}

	/**
	 * Build sql for special JP based on the context data
	 * 
	 * @param context
	 *            the context to match
	 * @return sql string
	 */
	private String sqlForSpecialjp(LinkContext context) {
		// all the fields are optional
		String queryConditions = valueOrEmpty(CSConstants.TBL_IMG_DOSSIER,
				context.getRefDossier())
				+ valueOrEmpty(CSConstants.TBL_IMG_EMPLOYER,
						context.getRefEmployer())
				+ valueOrEmpty(CSConstants.TBL_IMG_WORKER,
						context.getRefWorker())
				+ valueOrEmpty(CSConstants.TBL_IMG_MAP, context.getMap())
				+ valueOrEmpty(CSConstants.TBL_IMG_PERSON,
						context.getRefPerson())
				+ valueOrEmpty(CSConstants.TBL_IMG_DOCTYPE,
						context.getRefDocType())
				+ valueOrEmpty(CSConstants.TBL_IMG_GAJUR, context.getRefGajur());

		if (context.getRefKeyword1() != null) {
			if (context.getRefKeyword2() == null) {
				queryConditions += " AND (" + CSConstants.TBL_IMG_KEYW_ONE
						+ " = " + context.getRefKeyword1() + " OR "
						+ CSConstants.TBL_IMG_KEYW_TWO + " = "
						+ context.getRefKeyword1() + ")";
				LOGGER.debug("queryConditions : " + queryConditions);
			} else {
				if (context.getLogicalOp() != null) {
					String logicalOp = context.getLogicalOp() == 1 ? " AND "
							: " OR ";
					queryConditions += " AND ((" + CSConstants.TBL_IMG_KEYW_ONE
							+ " IN (" + context.getRefKeyword1() + ","
							+ context.getRefKeyword2() + ")" + logicalOp
							+ CSConstants.TBL_IMG_KEYW_TWO + " IN ("
							+ context.getRefKeyword1() + ","
							+ context.getRefKeyword2() + "))" + " AND "
							+ CSConstants.TBL_IMG_KEYW_ONE + " != "
							+ CSConstants.TBL_IMG_KEYW_TWO + ")";
				}
			}
		}
		String sql = CSConstants.SQL_IMGNAME_FROM_IMAGE + " WHERE "
				+ cleanSqlConditions(queryConditions.trim());
		return sql;
	}

	/**
	 * Build a standard sql based the context data
	 * 
	 * @param context
	 *            the context to match
	 * @return sql string
	 */
	private String standardSql(LinkContext context) {
		String sql = CSConstants.SQL_IMGNAME_FROM_IMAGE + " WHERE "
				+ CSConstants.TBL_IMG_DOSSIER
				+ valueOrNull(context.getRefDossier()) + " AND "
				+ CSConstants.TBL_IMG_EMPLOYER
				+ valueOrNull(context.getRefEmployer()) + " AND "
				+ CSConstants.TBL_IMG_WORKER
				+ valueOrNull(context.getRefWorker()) + " AND "
				+ CSConstants.TBL_IMG_GAJUR
				+ valueOrNull(context.getRefGajur());
		// Optional fields
		if (context.getRefPerson() != null) {
			sql += " AND " + CSConstants.TBL_IMG_PERSON
					+ valueOrNull(context.getRefPerson());
		}
		if (context.getRefKeyword1() != null) {
			if (context.getRefKeyword2() == null) {
				sql += " AND ((" + CSConstants.TBL_IMG_KEYW_ONE
						+ valueOrNull(context.getRefKeyword1()) + ") OR ("
						+ CSConstants.TBL_IMG_KEYW_TWO
						+ valueOrNull(context.getRefKeyword1()) + "))";
				LOGGER.debug("Pre-sql : " + sql);
			} else {
				if (context.getLogicalOp() != null) {
					String logicalOp = context.getLogicalOp() == 1 ? " AND "
							: " OR ";
					sql += " AND ((" + CSConstants.TBL_IMG_KEYW_ONE + " IN ("
							+ context.getRefKeyword1().toString() + ","
							+ context.getRefKeyword2().toString() + ")"
							+ logicalOp + CSConstants.TBL_IMG_KEYW_TWO
							+ " IN (" + context.getRefKeyword1().toString()
							+ "," + context.getRefKeyword2().toString()
							+ "))" + " AND " + CSConstants.TBL_IMG_KEYW_ONE
							+ " != " + CSConstants.TBL_IMG_KEYW_TWO + ")";
				}
			}
		}

		if (context.getRefDocType() != null) {
			sql += " AND (" + CSConstants.TBL_IMG_DOCTYPE
					+ valueOrNull(context.getRefDocType());
		}

		return sql;
	}

	/**
	 * Find images based on a given context.
	 * 
	 * @param context
	 *            the context to match
	 * @param maxItems
	 *            the maximum items to return
	 * @return all references to images in string list
	 * @throws ImagingPBException
	 *             if error occurs
	 */
	public List<String> findImagesByContext(LinkContext context, int maxItems)
			throws ImagingPBException {
		List<String> refImages = new ArrayList<String>();
		int numItems = 0;

		// CHECK CONTEXT NOT EMPTY !!!!
		if (context.getRefDossier() == null && context.getRefEmployer() == null && context.getRefWorker() == null 
				&& context.getRefGajur() == null && context.getRefPerson() == null && context.getRefKeyword1() == null) {
			throw new ImagingPBException("Cannot find images, context is empty !");
		}
		
		String sql;
		if (context.getSpecialjp()) {
			sql = sqlForSpecialjp(context);
		} else {
			sql = standardSql(context);
		}

		LOGGER.debug("sql = " + sql);

		Connection connection;

		connection = DataSourceUtils.getConnection(dataSource);

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			while (rs.next() && numItems < maxItems) {
				refImages.add(rs.getString(1));
				numItems++;
			}
		} catch (SQLException e) {
			LOGGER.error("Error retrieving images", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}
		return refImages;
	}

	/**
	 * Update groupanswer table.
	 * 
	 * @param docName
	 * @param questionId
	 * @throws ImagingPBException
	 */
	public void updateGroupAnswer(String docName, Long questionId)
			throws ImagingPBException {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean updateOK = false;

		try {
			stmt = connection
					.prepareStatement(CSConstants.SQL_UPDATE_GROUPANSWER);
			stmt.setString(1, docName);
			stmt.setLong(2, questionId);

			int rowAffected = stmt.executeUpdate();
			updateOK = (rowAffected == 1);
			
			if (updateOK) {
				connection.commit();
			}
			
		} catch (SQLException e) {
			LOGGER.error("Error updating groupanswer table", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
				//connection.close();

			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}

		if (!updateOK) {
			LOGGER.error("Table groupanswer failed to be updated!");
			throw new ImagingPBException();
		}
	}

	/**
	 * Update vrgl table.
	 * 
	 * @param docName
	 * @param questionId
	 * @throws ImagingPBException
	 */
	public void updateVrgl(String docName, Long questionId)
			throws ImagingPBException {
		PreparedStatement stmt = null;
		Connection connection = DataSourceUtils.getConnection(dataSource);
		boolean updateOK = false;

		try {
			stmt = connection.prepareStatement(CSConstants.SQL_UPDATE_VRGL);
			stmt.setString(1, docName);
			stmt.setLong(2, questionId);

			int rowAffected = stmt.executeUpdate();
			updateOK = (rowAffected == 1);
			
			if (updateOK) {
				connection.commit();
			}
			
		} catch (SQLException e) {
			LOGGER.error("Error updating vrgl table", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
			//	connection.close();

			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}

		if (!updateOK) {
			LOGGER.error("Table vrgl failed to be updated!");
			throw new ImagingPBException();
		}
	}

	/**
	 * List keywords from keyw table.
	 * 
	 * @return
	 * @throws ImagingPBException
	 */
	public List<Keyword> findKeywords() throws ImagingPBException {
		List<Keyword> keywords = new ArrayList<Keyword>();

		String sql = CSConstants.SQL_ALL_KEYWORDS;
		
		LOGGER.debug("sql = " + sql);

		Connection connection = DataSourceUtils.getConnection(dataSource);

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				keywords.add(new Keyword(rs.getLong(1), rs.getString("keywnl"), rs.getString("keywfr")));
			}
		} catch (SQLException e) {
			LOGGER.error("Error retrieving keywords", e);
			throw new ImagingPBException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close imaging statement !", e);
				throw new ImagingPBException();
			}
		}
		return keywords;
	}
}
