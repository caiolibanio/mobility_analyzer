package mobility.core;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTimeOperations {
	
	public static boolean isHomeTime(Tweet tweet){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);

		if((hourLondon >= 20 && hourLondon <= 23 || 
				hourLondon >=0 && hourLondon <= 7) &&
				(dayWeekLondon >= 2 && dayWeekLondon <= 6)) {
			
			return true;
			
		}
		return false;
		
	}
	
	public static Calendar getLondonTime(Timestamp time){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int year = time.getYear() + 1900;
		int month = time.getMonth();
		int date = time.getDate();
		int hrs = time.getHours();
		int mins = time.getMinutes();
		
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, date);
		cal.set(Calendar.HOUR_OF_DAY, hrs);
		cal.set(Calendar.MINUTE, mins);
		Calendar londonTime = convertTimeZones(cal, "Europe/London");
		return londonTime;
		
	}
	
	public static Calendar convertTimeZones(Calendar calendar, String timeZone){
	    Calendar londonTime = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
	    londonTime.setTimeInMillis(calendar.getTimeInMillis());
        return londonTime;
	}

}
