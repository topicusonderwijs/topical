package nl.topicus.topical;

import java.time.Instant;
import java.util.Date;

public class SimpleEvent {
	
	private Instant startTime;

	private Instant endTime;

	private String subject;

	public SimpleEvent(Date startTime, Date endTime, String subject) {
		this.setStartTime(startTime.toInstant());
		this.setEndTime(endTime.toInstant());
		this.setSubject(subject);
		
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
