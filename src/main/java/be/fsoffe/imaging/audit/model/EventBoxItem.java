package be.fsoffe.imaging.audit.model;

import java.util.Date;

/**
 * Represents a audit event for a box.
 * 
 * @author jbourlet
 *
 */
public class EventBoxItem {
	
	private Long auditId;
	private String userName;
	private Date inDate;
	private Date outDate;
	private String docId;
	private String docType;
	private String docSource;
	private String docLetterType;
	private String boxName;
	
	
	public Long getAuditId() {
		return auditId;
	}

	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	public Date getInDate() {
		return inDate;
	}

	public void setInDate(Date inDate) {
		this.inDate = inDate;
	}

	public Date getOutDate() {
		return outDate;
	}

	public void setOutDate(Date outDate) {
		this.outDate = outDate;
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

	public String getDocSource() {
		return docSource;
	}

	public void setDocSource(String docSource) {
		this.docSource = docSource;
	}

	public String getDocLetterType() {
		return docLetterType;
	}

	public void setDocLetterType(String docLetterType) {
		this.docLetterType = docLetterType;
	}

	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
	
}
