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

import mobility.dbscan.GeoDistance;

public class ExtractSelectedUsers {
	
	private static Connection connection = null;
	
	private static GeoCalculator calc = new GeoCalculator();

	public static void main(String[] args) {

		System.out.println("-------- PostgreSQL " + "JDBC Connection Testing ------------");

		try {

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
			e.printStackTrace();
			return;

		}

		System.out.println("PostgreSQL JDBC Driver Registered!");

		try {
			calculating();
			connection.close();
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
			
//			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres", "admin");
//			connection.setAutoCommit(false);

			for (User u : users) {
				
				if(connection == null || connection.isClosed()){
					connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres", "admin");
					connection.setAutoCommit(false);
				}
				st = connection.createStatement();
				rs = st.executeQuery("select tid, longitude, latitude, date, user_id from geo_tweets where user_id = " + u.getUser_id());
				

				while (rs.next()) {
					Long tid = rs.getLong("tid");
//					String json = rs.getString("json");
					Double lon = rs.getDouble("longitude");
					Double lat = rs.getDouble("latitude");
					Timestamp time = rs.getTimestamp("date");
					Tweet tweet = new Tweet(tid, "");
					tweet.setLongitude(lon);
					tweet.setLatitude(lat);
					tweet.setDate(time);
					tweet.setUser_id(rs.getLong("user_id"));
					tweet.setMessage("");
					u.addToTweetList(tweet);
				}
				
				usersToInsert.add(u);
				rs.close();
				st.close();
				
				
				if(usersToInsert.size() == 1000){
					countUserProcessed += 1000;
					System.out.println("Users to analyse: " + countUserProcessed);
					
					analyseUsers(usersToInsert);
					insert(usersToInsert);
					usersToInsert.clear();
					connection.commit();
					connection.close();
					
				}
				
			}
			if(usersToInsert.size() > 0){
				analyseUsers(usersToInsert);
				insert(usersToInsert);
				usersToInsert.clear();
				connection.commit();
				connection.close();
			}
			

		} catch (SQLException se) {
			System.err.println("Erro Fatal no processo!");
			System.err.println(se.getMessage());
		} finally {
			rs.close();
			st.close();
//			connection.commit();
			connection.close();

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
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from calculate_centroid" + "(" + u.getUser_id() + ")");
			if(rs.next()){
				String point = rs.getString("st_astext");
				point = point.replace("POINT(", "");
				point = point.replace(")", "");
				String[] coords = point.split(" ");
				Point centroid = new Point(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
				u.setPointCentroid(centroid);
			}
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
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Timestamp time = tweet.getDate();
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
		
//		cal.set(Calendar.YEAR, 2014);
//		cal.set(Calendar.MONTH, 4);
//		cal.set(Calendar.DAY_OF_MONTH, 26);
//		cal.set(Calendar.HOUR_OF_DAY, 23);
//		cal.set(Calendar.MINUTE, 59);
		
		

		Calendar londonTime = convertTimeZones(cal, "Europe/London");
		
		
		
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
	
	private static Calendar convertTimeZones(Calendar calendar, String timeZone){
	    Calendar londonTime = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
	    londonTime.setTimeInMillis(calendar.getTimeInMillis());
        return londonTime;
	}

	private static void generateDisplacements(List<User> users) {
		for(User u : users){
			calc.generateDisplacement(u.getTweetList());
			Double totalDispl = 0.0;
			for(Tweet t : u.getTweetList()){
				totalDispl += t.getUserDisplacement();
			}
			u.setTotal_Displacement(totalDispl);
		}
		
	}

	private static void insert(List<User> users) throws SQLException {
		
		for(User u : users){
			Statement stmt = connection.createStatement();
			String sql = "INSERT INTO geo_tweets_users_selected" + "(" +  "user_id" + ", " + "longitude_home" + ", " + "latitude_home" + 
			", " +"num_messages" + ", " + "radius_of_gyration" + ", " + "total_displacement" + ", " + "longitude_centroid" + " ," +
					"latitude_centroid" + ") " + "VALUES (" + "'" + u.getUser_id() + "'" + ", " + "'" + u.getPointHome().getLongitude() + "'" + ", " + 
			"'" + u.getPointHome().getLatitude() + "'" + " ," + "'" + u.getNum_messages() + "'" + " ," + "'" + u.getRadiusOfGyration() + "'" + ", " + 
			"'" + u.getTotal_Displacement() + "'" + " ," + "'" + u.getPointCentroid().getLongitude() + "'" + " ," + 
			"'" + u.getPointCentroid().getLatitude() + "'" + ");";

			stmt.executeUpdate(sql);
			stmt.close();
		}
		
		
		
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
		Statement st = null;
		ResultSet rs = null;
		List<User> listUsers = new ArrayList<User>();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres",
				"admin");
		connection.setAutoCommit(false);
		st = connection.createStatement();
		rs = st.executeQuery("select * from geo_tweets_users order by user_id");

		while (rs.next()) {
			User u = new User(new ArrayList<Tweet>());
			u.setUser_id(rs.getLong("user_id"));
			listUsers.add(u);
		}
		st.close();
		rs.close();
		connection.close();
		
		
//		List<User> listUsers = new ArrayList<User>();
//		User u = new User(new ArrayList<Tweet>());
//		u.setUser_id(new Long(161059155));
//		listUsers.add(u);
		
		return listUsers;

	}

}


