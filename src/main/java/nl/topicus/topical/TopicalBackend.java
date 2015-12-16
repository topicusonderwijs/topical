package nl.topicus.topical;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.availability.AvailabilityData;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.LegacyFreeBusyStatus;
import microsoft.exchange.webservices.data.core.response.AttendeeAvailability;
import microsoft.exchange.webservices.data.core.response.ServiceResponseCollection;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.availability.AttendeeInfo;
import microsoft.exchange.webservices.data.misc.availability.GetUserAvailabilityResults;
import microsoft.exchange.webservices.data.misc.availability.TimeWindow;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import spark.Spark;

import com.google.gson.Gson;

public class TopicalBackend {

	private static final String domain = "YOUR-EXCHANGE-DOMAIN";
	private static final String username = "YOUR-USERNAME";
	private static final String password = "YOUR-PASSWORD";
	private static final String url = "https://YOUR-EXCHANGE-URL/EWS/Exchange.asmx";
	private static String[] rooms = new String[] {
			"MEETING-ROOM-1-EMAIL-ADDRESS", "MEETING-ROOM-2-EMAIL-ADDRESS" };

	public static void main(String[] args) {
		new TopicalBackend();
	}

	public TopicalBackend() {

		Gson gson = new Gson();

		Spark.get("/rooms", (req, res) -> {
			res.type("application/json");
			return rooms;
		}, gson::toJson);

		Spark.get("/events/:room", (req, res) -> {
			res.type("application/json");
			return listEventsNextSevenDays(req.params(":room"));
		}, gson::toJson);

		Spark.get("claim/:room", (req, res) -> {
			try {
				createAppointment(req.params(":room"));
				return "claim succesvol";
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		});
	}

	private List<SimpleEvent> listEventsNextSevenDays(String adres) {
		AttendeeInfo attendee = AttendeeInfo.getAttendeeInfoFromString(adres);
		List<AttendeeInfo> attendees = Arrays.asList(attendee);

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		Calendar nextWeek = (Calendar) today.clone();
		nextWeek.add(Calendar.DATE, 7);

		TimeWindow timeWindow = new TimeWindow(today.getTime(),
				nextWeek.getTime());
		try {
			ExchangeService service = createService();
			GetUserAvailabilityResults userAvailability = service
					.getUserAvailability(attendees, timeWindow,
							AvailabilityData.FreeBusy);
			ServiceResponseCollection<AttendeeAvailability> attendeesAvailability = userAvailability
					.getAttendeesAvailability();
			AttendeeAvailability availability = attendeesAvailability
					.iterator().next();

			return availability
					.getCalendarEvents()
					.stream()
					.filter(e -> e.getFreeBusyStatus().equals(
							LegacyFreeBusyStatus.Busy))
					.map(e -> new SimpleEvent(e.getStartTime(), e.getEndTime(),
							e.getDetails().getSubject()))
					.collect(Collectors.toList());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	/**
	 * Creates an appointment starting now and ending within an hour or when the
	 * next appointment starts, whichever comes first.
	 * 
	 * @throws Exception
	 *             if the room is currently occupied or the exchange server
	 *             throws an exception
	 */
	private void createAppointment(String adres) throws Exception {
		List<SimpleEvent> events = listEventsNextSevenDays(adres);
		SimpleEvent currentEvent = getCurrentEvent(events);
		if (currentEvent != null) {
			throw new Exception(
					"Room is currently not available. Subject of current event: "
							+ currentEvent.getSubject());
		}

		Appointment appointment = new Appointment(createService());
		appointment.setSubject("Vergaderruimte geclaimed via Topical");
		appointment
				.setBody(MessageBody
						.getMessageBodyFromText("Deze afspraak is ingeschoten vanuit Topical"));

		Calendar nu = Calendar.getInstance();
		appointment.setStart(nu.getTime());

		Calendar inOneHour = (Calendar) nu.clone();
		inOneHour.add(Calendar.HOUR, 1);

		SimpleEvent nextEvent = getNextEvent(events);
		if (nextEvent.getStartTime().before(inOneHour.getTime())) {
			appointment.setEnd(nextEvent.getStartTime());
		} else {
			appointment.setEnd(inOneHour.getTime());
		}

		appointment.getRequiredAttendees().add(adres);
		appointment.save();
	}

	private SimpleEvent getCurrentEvent(List<SimpleEvent> events) {
		Date nu = Calendar.getInstance().getTime();
		List<SimpleEvent> currentEvents = events
				.stream()
				.filter(e -> e.getStartTime().before(nu)
						&& e.getEndTime().after(nu))
				.collect(Collectors.toList());
		return currentEvents.size() > 0 ? currentEvents.get(0) : null;
	}

	private SimpleEvent getNextEvent(List<SimpleEvent> events) {
		Date nu = Calendar.getInstance().getTime();
		List<SimpleEvent> upcomingEvents = events.stream()
				.filter(e -> e.getStartTime().after(nu))
				.collect(Collectors.toList());
		return upcomingEvents.size() > 0 ? upcomingEvents.get(0) : null;
	}

	private ExchangeService createService() {
		ExchangeService service = new ExchangeService(
				ExchangeVersion.Exchange2010_SP2);

		ExchangeCredentials credentials = new WebCredentials(username,
				password, domain);
		service.setCredentials(credentials);
		try {
			service.setUrl(new URI(url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return service;
	}
}