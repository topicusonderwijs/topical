package nl.topicus.topical;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TopicalBackend {

	private String domain;
	private String username;
	private String password;
	private String url;
	public List<String> rooms;

	public static void main(String[] args) {
		if (args.length < 5) {
			System.out
					.println("Usage: [domain] [username] [password] [url] [room1] [room2]");
		}
		List<String> argsList = Arrays.asList(args);
		new TopicalBackend(argsList.get(0), argsList.get(1), argsList.get(2),
				argsList.get(3), argsList.subList(4, argsList.size() - 1));
	}

	public TopicalBackend(String domain, String username, String password,
			String url, List<String> rooms) {
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.url = url;
		this.rooms = rooms;

		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class,
				new JsonSerializer<Instant>() {

					@Override
					public JsonElement serialize(Instant instant, Type type,
							JsonSerializationContext jsonSerializationContext) {
						return new JsonPrimitive(instant.toString());
					}
				}).create();

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
		Spark.after((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Methods", "GET, OPTIONS");
		});
	}

	private List<SimpleEvent> listEventsNextSevenDays(String adres) {
		AttendeeInfo attendee = AttendeeInfo.getAttendeeInfoFromString(adres);
		List<AttendeeInfo> attendees = Arrays.asList(attendee);

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		Calendar tomorrow = (Calendar) today.clone();
		tomorrow.add(Calendar.DATE, 1);

		TimeWindow timeWindow = new TimeWindow(today.getTime(),
				tomorrow.getTime());
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
		
		Instant nu = Instant.now();
		appointment.setStart(Date.from(nu.atZone(ZoneId.systemDefault())
				.toInstant()));

		Instant inOneHour = nu.plus(Duration.ofHours(1));

		SimpleEvent nextEvent = getNextEvent(events);
		if (nextEvent.getStartTime().isBefore(inOneHour)) {
			appointment.setEnd(Date.from(nextEvent.getStartTime()
					.atZone(ZoneId.systemDefault()).toInstant()));
		} else {
			appointment.setEnd(Date.from(inOneHour.atZone(
					ZoneId.systemDefault()).toInstant()));
		}

		appointment.getRequiredAttendees().add(adres);
		appointment.save();
	}

	private SimpleEvent getCurrentEvent(List<SimpleEvent> events) {
		Instant nu = Instant.now();
		List<SimpleEvent> currentEvents = events
				.stream()
				.filter(e -> e.getStartTime().isBefore(nu)
						&& e.getEndTime().isAfter(nu))
				.collect(Collectors.toList());
		return currentEvents.size() > 0 ? currentEvents.get(0) : null;
	}

	private SimpleEvent getNextEvent(List<SimpleEvent> events) {
		Instant nu = Instant.now();
		List<SimpleEvent> upcomingEvents = events.stream()
				.filter(e -> e.getStartTime().isAfter(nu))
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