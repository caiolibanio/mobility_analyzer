package mobility.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import mobility.DAO.UserDAO;
import mobility.dbscan.GeoDistance;
import mobility.service.TweetService;
import mobility.service.UserService;

public class ExtractSelectedUsers {

	
	private static GeoCalculator calc = new GeoCalculator();
	
	private static UserService userService = new UserService();
	
	private static TweetService tweetService = new TweetService();

	public static void main(String[] args) {

		System.out.println("Final user selection is in process...");

		try {
			calculating();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erro fatal!");
		}
	}
	
	private static void calculating() throws SQLException {

		Statement st = null;
		ResultSet rs = null;
		int countOK = 0;
		boolean lastLine = false;
		int step1 = 1;
		int step2 = 1000;
		List<Tweet> tweets = new ArrayList<Tweet>();
		List<User> users = new ArrayList<User>();
		List<User> usersToInsert = new ArrayList<User>();
		int countStepToInsert = 0;
		int countUserProcessed = 0;

		try {
			users = getUserList();

			for (User u : users) {
				u.getTweetList().addAll(tweetService.findTweetsByUser(u.getUser_id()));
				usersToInsert.add(u);
				
				if(usersToInsert.size() == 1000){
					countUserProcessed += 1000;
					System.out.println("Users to analyse: " + countUserProcessed);
					
					analyseUsers(usersToInsert);
					insert(usersToInsert);
					usersToInsert.clear();
					
				}
				
			}
			if(usersToInsert.size() > 0){
				analyseUsers(usersToInsert);
				insert(usersToInsert);
				usersToInsert.clear();
			}
			

		} catch (SQLException se) {
			System.err.println("Erro Fatal no processo!");
			System.err.println(se.getMessage());
		}

		Date date = new Date();
		System.out.println(date);
		

	}
	
	private static void analyseUsers(List<User> usersToInsert) throws SQLException {
		filteringMessagesByQuantity(usersToInsert);
		findUserCentroid(usersToInsert);
		filteringMessagesByDisplacement(usersToInsert);
		generateDisplacements(usersToInsert);
		findHomePoint(usersToInsert);
		calculateRadius(usersToInsert);
		calculateNumMessages(usersToInsert);
		
	}

	private static void calculateNumMessages(List<User> users) {
		for(User u : users){
			u.setNum_messages(u.getTweetList().size());
		}
	}

	private static void calculateRadius(List<User> users) {
		for(User u: users){
			u.setRadiusOfGyration(calc.calculateRadiusOfGyration(u.tweetsAsPoints(), u.getPointCentroid()));
		}
		
	}

	private static void findUserCentroid(List<User> users) throws SQLException {
		
		for(User u : users){
			Point centroid = userService.findUserCentroid(u.getUser_id());
			u.setPointCentroid(centroid);
		}
	}

	private static void findHomePoint(List<User> users) {
		for(User u: users){
			List<DoublePoint> points = formatPointsToCluster(u);
			List<Cluster<DoublePoint>> cluster = clusteringPoints(points);
			if(cluster.size() > 0){
				Point home = findingHome(cluster);
				u.setPointHome(home);
			}else{
				u.setPointHome(new Point(0.0, 0.0));
			}
			
			
		}
	}
	
	private static Point findingHome(List<Cluster<DoublePoint>> cluster) {
		List<List<DoublePoint>> listOfClusters = new ArrayList<List<DoublePoint>>();  
		for(Cluster<DoublePoint> c: cluster){
			List<DoublePoint> singleCluster = new ArrayList<DoublePoint>();
			for(DoublePoint p : c.getPoints()){
				singleCluster.add(p);
	    	}
			listOfClusters.add(singleCluster);
		}
		List<DoublePoint> homeCluster = findBiggestCluster(listOfClusters);
		Point homePoint = calculateHomePoint(homeCluster);
		return homePoint;
	}

	private static Point calculateHomePoint(List<DoublePoint> homeCluster) {
		List<Point> points = new ArrayList<Point>();
		for(DoublePoint p : homeCluster){
			double[] dPoint = p.getPoint();
			Point point = new Point(dPoint[0], dPoint[1]);
			points.add(point);
		}
		Double[] homeMidPoint = calc.calculateMidPoint(points);
		Point homePoint = new Point(homeMidPoint[0], homeMidPoint[1]);
		return homePoint;
	}

	private static List<DoublePoint> findBiggestCluster(List<List<DoublePoint>> listOfClusters) {
		int index = 0;
		
		for(int i = 0; i < listOfClusters.size(); i++){
			
			if(listOfClusters.get(i).size() > index){
				index = i;
			}
		}
		return listOfClusters.get(index);
	}

	private static List<Cluster<DoublePoint>> clusteringPoints(List<DoublePoint> points) {
		DBSCANClusterer dbscan = new DBSCANClusterer(45.0, 4, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		return cluster;
	}

	private static List<DoublePoint> formatPointsToCluster(User user) {

	    List<DoublePoint> points = new ArrayList<DoublePoint>();
	    for (Tweet t : user.getTweetList()) {
	    	if(isHomeTime(t)){
	    		double[] d = new double[2];
		    	d[0] = t.getLatitude();
	            d[1] = t.getLongitude();
	            points.add(new DoublePoint(d));
	    	}
	    }
	    return points;
	}
	
	private static boolean isHomeTime(Tweet tweet){
		Timestamp time = tweet.getDate();
		Calendar londonTime = getLondonTime(time);
		
		int yearLondon = londonTime.get(Calendar.YEAR);
		int monthLondon = londonTime.get(Calendar.MONTH);
		int dayLondon = londonTime.get(Calendar.DAY_OF_MONTH);
		int dayWeekLondon = londonTime.get(Calendar.DAY_OF_WEEK);
		int hourLondon = londonTime.get(Calendar.HOUR_OF_DAY);
		int minuteLondon = londonTime.get(Calendar.MINUTE);
		
		if((hourLondon >= 20 && hourLondon <= 23 || 
				hourLondon >=0 && hourLondon <= 7) &&
				(dayWeekLondon >= 2 && dayWeekLondon <= 6)) {
			
			return true;
			
		}
		return false;
		
	}
	
	private static Calendar getLondonTime(Timestamp time){
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
	
	private static Calendar convertTimeZones(Calendar calendar, String timeZone){
	    Calendar londonTime = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
	    londonTime.setTimeInMillis(calendar.getTimeInMillis());
        return londonTime;
	}

	private static void generateDistanceMovement(List<User> users) {
		for(User u : users){
			calc.generateDisplacement(u.getTweetList());
			Double totalDispl = 0.0;
			for(Tweet t : u.getTweetList()){
				totalDispl += t.getUserDisplacement();
			}
			u.setUser_movement(totalDispl);
		}
		
	}
	
	private static void generateDisplacements(List<User> users) {
		generateDistanceMovement(users);
		calculateTotalDisplacement(users);
		calculateDisplacementPerDay(users);
		calculateDistancePerDisplacement(users);
		calculateDisplacementAttrb(users);
	}
	
	private static void calculateDisplacementAttrb(List<User> users) {
		for(User u : users){
			u.getDisplacement().generateLowDisplacementPerDay(5);
			u.getDisplacement().generateTopDisplacementPerDay(5);
			u.getDisplacement().generateLowDistanceDisplacement(5);
			u.getDisplacement().generateTopDistanceDisplacement(5);
			u.getDisplacement().calculateDisplacementPerDayMedian();
			u.getDisplacement().calculateDistanceDisplacementMedian();
		}
		
	}

	private static void calculateDistancePerDisplacement(List<User> users) {
		Double displCount = 0.0;
		for (User u : users) {
			List<Tweet> listTweets = u.getTweetList();
			Collections.sort(listTweets);
			for (int i = 1; i < listTweets.size(); i++) {
				Tweet tweet = listTweets.get(i);
				Tweet predTweet = listTweets.get(i - 1);
				if (isDisplacement(tweet.getLatitude(), tweet.getLongitude(), predTweet.getLatitude(),
						predTweet.getLongitude())) {
					Double distanceDisplacement = calc.calculateDistance(tweet.getLatitude(), tweet.getLongitude(),
							predTweet.getLatitude(), predTweet.getLongitude());
					Point pointA = new Point(tweet.getLatitude(), tweet.getLongitude());
					Point pointB = new Point(predTweet.getLatitude(), predTweet.getLongitude());
					DistanceDisplacement distDisplacement = new DistanceDisplacement();
					distDisplacement.setPointA(pointA);
					distDisplacement.setPointB(pointB);
					distDisplacement.setDistanceDisplacement(distanceDisplacement);
					u.getDisplacement().getListDistanceDisplacements().add(distDisplacement);
				}
			}
		}
	}

	private static void calculateTotalDisplacement(List<User> users) {
		int displCount = 0;
		for(User u : users){
			List<Tweet> listTweets = u.getTweetList();
			displCount = calculateTotalDisplacementPerTweets(listTweets);
			u.getDisplacement().setDisplacementCounter(displCount);
			displCount = 0;
		}
		
	}
	
	private static int calculateTotalDisplacementPerTweets(List<Tweet> listTweets){
		int displCount = 0;
		Collections.sort(listTweets);
		for(int i = 1; i < listTweets.size(); i++){
			Tweet tweet = listTweets.get(i);
			Tweet predTweet = listTweets.get(i-1);
			if(isDisplacement(tweet.getLatitude(), tweet.getLongitude(),
					predTweet.getLatitude(), predTweet.getLongitude())){
				displCount++;
			}
		}
		return displCount;
	}
	
	private static List<Tweet> getTweetsByDate(List<Tweet> tweetList, Calendar calendar){
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : tweetList){
			Timestamp time = t.getDate();
			Calendar londonTime = getLondonTime(time);
			if(londonTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && 
					londonTime.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && 
					londonTime.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)){
				list.add(t);
			}
		}
		return list;
	}
	
	private static boolean isDisplacement(Double lat1, Double lon1, Double lat2, Double lon2){
		if(calc.calculateDistance(lat1, lon1, lat2, lon2) > 45){
			return true;
		}
		return false;
	}

	private static void calculateDisplacementPerDay(List<User> users) {
		int displPerDay = 0;
		List<Tweet> analyzedTweets = new ArrayList<Tweet>();
		for(User u : users){
			List<Tweet> listTweets = u.getTweetList();
			Collections.sort(listTweets);
			analyzedTweets.clear();
			for(Tweet t : listTweets){
				if(! analyzedTweets.contains(t)){
					Calendar calendar = getLondonTime(t.getDate());
					List<Tweet> tweetsPerDate = getTweetsByDate(listTweets, calendar);
					analyzedTweets.addAll(tweetsPerDate);
					displPerDay = calculateTotalDisplacementPerTweets(tweetsPerDate);
					if(displPerDay > 0){
						DisplacementPerDay displacement = new DisplacementPerDay();
						displacement.setDisplacementPerDay(displPerDay);
						displacement.setDate(t.getDate());
						u.getDisplacement().getListDisplacementsPerDay().add(displacement);
					}
				}
			}
		}
		
	}
	
	private static void insert(List<User> users) throws SQLException {
		userService.saveUsers(users);
	}

	private static void filteringMessagesByDisplacement(List<User> users) {
		List<User> toAdd = new ArrayList<User>();
		for(User u : users){
			for(Tweet t : u.getTweetList()){
				if(calc.calculateDistance(u.getPointCentroid().getLatitude(), u.getPointCentroid().
						getLongitude(), t.getLatitude(), t.getLongitude()) >= 45.0){
					toAdd.add(u);
					break;
				}
			}
		}
		
		users.clear();
		users.addAll(toAdd);
		
	}

	private static void filteringMessagesByQuantity(List<User> users) {
		List<User> toRemove = new ArrayList<User>();
		for(User u : users){
			if(u.getTweetList().size() < 20){
				toRemove.add(u);
			}
		}
		users.removeAll(toRemove);
		
	}

	private static List<User> getUserList() throws SQLException {
		return userService.findAllUsersGeoTweet();

	}

}


