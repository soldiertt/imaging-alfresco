package be.fsoffe.imaging.audit.model;

import java.util.Date;

/**
 * Represents a audit event.
 * 
 * @author jbourlet
 *
 */
public class EventItem {
	
	private String userName;
	private Date auditDate;
	private String docId;
	private String docType;
	private String fromBox;
	private String destination;
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Date getAuditDate() {
		return auditDate;
	}
	
	public void setAuditDate(Date auditDate) {
		this.auditDate = auditDate;
	}
	
	public String getDocId() {
		return docId;
	}
	
	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public String getDocType() {
		return docType;
	}
	
	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	public String getFromBox() {
		return fromBox;
	}
	
	public void setFromBox(String fromBox) {
		this.fromBox = fromBox;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
}
