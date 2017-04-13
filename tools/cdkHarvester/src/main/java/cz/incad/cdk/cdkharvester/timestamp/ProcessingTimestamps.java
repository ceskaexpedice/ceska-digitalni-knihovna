package cz.incad.cdk.cdkharvester.timestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation is dedicated for handling timestamps
 * @author pstastny
 *
 */
public interface ProcessingTimestamps {
	
	/**
	 * Returns latest timestamp for given source
	 * @param pid Pid of source
	 * @return
	 * @throws IOException
	 */
	public LocalDateTime getTimestamp(String pid) throws IOException;

	/**
	 * Sets the timestamp for given pid
	 * @param pid
	 * @param date
	 * @throws IOException
	 */
	public void setTimestamp(String pid, LocalDateTime date) throws IOException;
	
	/**
	 * REturns fromatted string
	 * @param time
	 * @return
	 * @throws IOException
	 */
	public String format(LocalDateTime time) throws IOException;

	/**
	 * Parse given string 
	 * @param dateString
	 * @return
	 * @throws IOException
	 */
	public LocalDateTime parse(String dateString) throws IOException;
	
}
