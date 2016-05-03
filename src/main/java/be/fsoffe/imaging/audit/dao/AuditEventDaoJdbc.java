package be.fsoffe.imaging.audit.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import be.fsoffe.imaging.audit.exception.ImagingAuditException;
import be.fsoffe.imaging.audit.model.EventBoxItem;
import be.fsoffe.imaging.audit.model.EventItem;

/**
 * DAO to manage operations on the IMG_AUDIT table.
 * 
 * @author jbourlet
 *
 */
public class AuditEventDaoJdbc implements Serializable {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = -5876425835999614953L;
	
	private static final Log LOGGER = LogFactory.getLog(AuditEventDaoJdbc.class);
	
	private transient DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Insert a new audit event in img_audit table.
	 * 
	 * @param eventItem the event content
	 * @throws ImagingAuditException if error occurs
	 */
	public void saveEvent(EventItem eventItem) throws ImagingAuditException {
		
		String sql = "insert into img_audit (uname, cdate, docuuid, doctype, frombox, destination) values (?, ?, ?, ?, ?, ?)";
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		
		try {
			//connection.setReadOnly(false);
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, eventItem.getUserName());
			Date now = Calendar.getInstance().getTime();
			Timestamp sqlNow = new Timestamp(now.getTime());
			stmt.setTimestamp(2, sqlNow);
			stmt.setString(3, eventItem.getDocId());
			stmt.setString(4, eventItem.getDocType());
			stmt.setString(5, eventItem.getFromBox());
			stmt.setString(6, eventItem.getDestination());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Cannot save audit event !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
	}
	
	/**
	 * Return all activities of a user since a specific date, count actions by document type.
	 * 
	 * @param userName the user name
	 * @param dateStart Date range start date
	 * @param dateEnd Date range end date
	 * @param byLetterType if need extra column with letter type
	 * @return Count of activities group by document type
	 * @throws ImagingAuditException if error occurs
	 */
	public List<String[]> findActivitiesByUserAndDate(String userName, Date dateStart, Date dateEnd, boolean byLetterType) throws ImagingAuditException {
		
		String sql = "";
		if (byLetterType) {
			sql = "select imga.doctype, anp.string_value, count(*) from img_audit imga, alf_node an LEFT JOIN alf_node_properties anp"
					+ " ON an.id = anp.node_id AND anp.qname_id=(select id from alf_qname where local_name='docLetterType')"
					+ " WHERE imga.uname = ? AND cdate >= ? AND cdate <= ? AND an.store_id=6 and an.uuid=imga.docuuid"
					+ " GROUP BY imga.doctype, anp.string_value ORDER BY imga.doctype";
		} else {
			sql = "select doctype, count(*) from img_audit WHERE uname = ? AND cdate >= ? AND cdate <= ? GROUP BY doctype";
		}
		
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		List<String[]> results = new ArrayList<String[]>();
		try {
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, userName);
			Timestamp sqlDateStart = new Timestamp(dateStart.getTime());
			stmt.setTimestamp(2, sqlDateStart);
			Timestamp sqlDateEnd = new Timestamp(dateEnd.getTime());
			stmt.setTimestamp(3, sqlDateEnd);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				if (byLetterType) {
					results.add(new String[] {rs.getString(1), rs.getString(2), rs.getString(3)});
				} else {
					results.add(new String[] {rs.getString(1), rs.getString(2)});
				}
			}
			return results;
		} catch (SQLException e) {
			LOGGER.error("Cannot select audit events !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
	}

	public Long findInAuditBoxEventId(String docId, String boxName) throws ImagingAuditException {
		
		String sql =  "select adtbox.audit_id from img_audit_box adtbox "
				+ "where adtbox.outdate is null and adtbox.docuuid=? and adtbox.boxname=?";
		
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, docId);
			stmt.setString(2, boxName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getLong(1);
			} else {
				return null;
			}
		} catch (SQLException e) {
			LOGGER.error("Cannot select audit events !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
		
	}

	public void saveEvent(EventBoxItem eventItem) throws ImagingAuditException {
		
		String sql = "insert into img_audit_box (docuuid, docsource, indate, boxname) values (?, ?, ?, ?)";
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, eventItem.getDocId());
			stmt.setString(2, eventItem.getDocSource());
			stmt.setTimestamp(3, new Timestamp(eventItem.getInDate().getTime()));
			stmt.setString(4, eventItem.getBoxName());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Cannot save audit event !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
		
	}

	public void updateEvent(EventBoxItem eventToUpdate) throws ImagingAuditException {
		String sql = "update img_audit_box set uname=?, outdate=?, doctype=?, doclettertype=? WHERE audit_id=?";
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, eventToUpdate.getUserName());
			stmt.setTimestamp(2, new Timestamp(eventToUpdate.getOutDate().getTime()));
			stmt.setString(3, eventToUpdate.getDocType());
			stmt.setString(4, eventToUpdate.getDocLetterType());
			stmt.setLong(5, eventToUpdate.getAuditId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Cannot save audit event !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
	}
	
	public void cleanAudit() throws ImagingAuditException {
		String sql = "delete from img_audit adt1 where exists"
				+ " (select * from img_audit adt2 where adt1.uname=adt2.uname"
				+ " and adt1.docuuid=adt2.docuuid and adt1.frombox=adt2.frombox"
				+ " and adt1.destination=adt2.destination and to_char(adt1.cdate,'YYYY-MM-DD') = to_char(adt2.cdate, 'YYYY-MM-DD')"
				+ " and adt1.frombox='Repository' and adt1.destination='Repository' and adt2.cdate < adt1.cdate)";
		Connection connection = DataSourceUtils.getConnection(dataSource);
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			int delCount = stmt.executeUpdate();
			LOGGER.info("Total deleted events : " + delCount);
		} catch (SQLException e) {
			LOGGER.error("Cannot clean audit table !", e);
			throw new ImagingAuditException();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
				LOGGER.error("Cannot close audit statement !", e);
				throw new ImagingAuditException();
			}
		}
		
	}
}
