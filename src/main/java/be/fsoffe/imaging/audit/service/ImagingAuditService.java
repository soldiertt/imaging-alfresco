package be.fsoffe.imaging.audit.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.fsoffe.imaging.audit.dao.AuditEventDaoJdbc;
import be.fsoffe.imaging.audit.exception.ImagingAuditException;
import be.fsoffe.imaging.audit.model.EventBoxItem;
import be.fsoffe.imaging.audit.model.EventItem;

/**
 * Service made available to manage audit events.
 * 
 * @author jbourlet
 *
 */
public class ImagingAuditService {
	
	private AuditEventDaoJdbc auditDao;

	public void setAuditDao(AuditEventDaoJdbc auditDao) {
		this.auditDao = auditDao;
	}
	
	/**
	 * Register a new audit event.
	 * 
	 * @param eventItem the event content
	 * @throws ImagingAuditException if error occurs
	 */
	public void saveEvent(EventItem eventItem) throws ImagingAuditException {
		auditDao.saveEvent(eventItem);
	}
	
	/**
	 * Return a list of arrays with count of activities by document type
	 * given the username and date range.
	 * 
	 * @param uname the user name
	 * @param dateStart Date range start date
	 * @param dateEnd Date range end date
	 * @param byLetterType if need extra column with letter type
	 * @return count by doc type arrays
	 * @throws ImagingAuditException if error occurs
	 */
	public List<String[]> findActivitiesByUserAndDate(String uname, Date dateStart, Date dateEnd, boolean byLetterType) throws ImagingAuditException {
		//End date shoud be +1 day
		Calendar dateEndUpdated = Calendar.getInstance();
		dateEndUpdated.setTime(dateEnd);
		dateEndUpdated.add(Calendar.DAY_OF_MONTH, 1);
		return auditDao.findActivitiesByUserAndDate(uname, dateStart, dateEndUpdated.getTime(), byLetterType);
	}

	public Long findInAuditBoxEvent(String docId, String boxName) throws ImagingAuditException {
		return auditDao.findInAuditBoxEventId(docId, boxName);
	}

	public void saveBoxEvent(EventBoxItem eventItem) throws ImagingAuditException {
		auditDao.saveEvent(eventItem);
	}

	public void updateBoxEvent(EventBoxItem eventToUpdate) throws ImagingAuditException {
		auditDao.updateEvent(eventToUpdate);
	}
	
	public void cleanAudit() throws ImagingAuditException {
		auditDao.cleanAudit();
	}
}
