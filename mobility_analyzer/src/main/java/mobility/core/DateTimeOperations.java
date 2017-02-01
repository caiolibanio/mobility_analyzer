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
	
	public static boolean isWorkdays(Tweet tweet){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);
		
		int minutes = londonTime.get(Calendar.MINUTE);
		int dayOfMonth = londonTime.get(Calendar.DAY_OF_MONTH);
		int year = londonTime.get(Calendar.YEAR);

		if((dayWeekLondon >= 2 && dayWeekLondon <= 6) && !isBankHoliday2015(tweet)) {
			
			return true;
			
		}
		return false;
	}
	
	public static boolean isWeekend(Tweet tweet){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);
		
		int minutes = londonTime.get(Calendar.MINUTE);
		int dayOfMonth = londonTime.get(Calendar.DAY_OF_MONTH);
		int year = londonTime.get(Calendar.YEAR);

		if((dayWeekLondon == 7 || dayWeekLondon == 1) && !isBankHoliday2015(tweet)) {
			
			return true;
			
		}
		return false;
	}
	
	public static boolean isSunday(Tweet tweet){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);
		
		int minutes = londonTime.get(Calendar.MINUTE);
		int dayOfMonth = londonTime.get(Calendar.DAY_OF_MONTH);
		int year = londonTime.get(Calendar.YEAR);

		if(dayWeekLondon == 1) {
			
			return true;
			
		}
		return false;
	}
	
	public static boolean isBankHoliday(Tweet tweet, int dayHoliday, int monthHoliday, int yearHoliday){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);
		
		int minutes = londonTime.get(Calendar.MINUTE);
		int dayOfMonth = londonTime.get(Calendar.DAY_OF_MONTH);
		int monthTweet = londonTime.get(Calendar.MONTH);
		int yearTweet = londonTime.get(Calendar.YEAR);

		if(dayOfMonth == dayHoliday && (monthTweet + 1) == monthHoliday && yearTweet == yearHoliday) {
			
			return true;
			
		}
		return false;
	}
	
	public static boolean isBankHoliday2015(Tweet tweet){
		if(isBankHoliday(tweet, 1, 1, 2015)){ //	New Year’s Day
			return true;
		}else if(isBankHoliday(tweet, 3, 4, 2015)){ //	Good Friday
			return true;
		}else if(isBankHoliday(tweet, 6, 4, 2015)){ // 	Easter Monday
			return true;
		}else if(isBankHoliday(tweet, 4, 5, 2015)){ // Early May bank holiday
			return true;
		}else if(isBankHoliday(tweet, 25, 5, 2015)){// 	Spring bank holiday
			return true;
		}else if(isBankHoliday(tweet, 31, 8, 2015)){// Summer bank holiday
			return true;
		}else if(isBankHoliday(tweet, 25, 12, 2014)){//Christmas Day
			return true;
		}else if(isBankHoliday(tweet, 26, 12, 2014)){ // Boxing Day (substitute day)
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isBankHolidayAndSunday2015(Tweet tweet){
		if(isBankHoliday(tweet, 1, 1, 2015)){ //	New Year’s Day
			return true;
		}else if(isBankHoliday(tweet, 3, 4, 2015)){ //	Good Friday
			return true;
		}else if(isBankHoliday(tweet, 6, 4, 2015)){ // 	Easter Monday
			return true;
		}else if(isBankHoliday(tweet, 4, 5, 2015)){ // Early May bank holiday
			return true;
		}else if(isBankHoliday(tweet, 25, 5, 2015)){// 	Spring bank holiday
			return true;
		}else if(isBankHoliday(tweet, 31, 8, 2015)){// Summer bank holiday
			return true;
		}else if(isBankHoliday(tweet, 25, 12, 2014)){//Christmas Day
			return true;
		}else if(isBankHoliday(tweet, 28, 12, 2014)){ // Boxing Day (substitute day)
			return true;
		}else if(isSunday(tweet)){
			return true;
		}else{
			return false;
		}
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
