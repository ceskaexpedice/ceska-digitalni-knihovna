package cz.incad.cdk.cdkharvester.timestamp;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class AbstractProcessingTimestamps implements ProcessingTimestamps {

	public String now() {
		ZonedDateTime departure = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
		return departure.format(DateTimeFormatter.ISO_INSTANT);
	}

	public String format(LocalDateTime time) {
		ZonedDateTime departure = ZonedDateTime.of(time, ZoneId.of(ZoneOffset.UTC.getId()));
		return departure.format(DateTimeFormatter.ISO_INSTANT);
	}

	public LocalDateTime parse(String dateString) {
		DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
		Instant dateInstant = Instant.from(isoFormatter.parse(dateString));
		LocalDateTime date = LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
		return date;
	}
	
	


	protected LocalDateTime nullvalue() {
		return this.parse("1900-01-01T00:00:00.002Z");
	}

}
