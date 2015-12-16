package nl.topicus.topical;

import java.util.Date;

public class SimpleEvent {
	
	private Date startTime;

	private Date endTime;

	private String subject;

	public SimpleEvent(Date startTime, Date endTime, String subject) {
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		this.setSubject(subject);
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
