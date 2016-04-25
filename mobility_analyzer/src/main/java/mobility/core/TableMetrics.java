package mobility.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableMetrics {

	static Connection connection = null;

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
		int countFail = 0;
		boolean lastLine = false;
		int step1 = 1;
		int step2 = 1000;
		List<Tweet> tweets = new ArrayList<Tweet>();
		List<User> users = new ArrayList<User>();
		int countUser = 0;

		try {
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres", "admin");
			connection.setAutoCommit(false);

			users = getUserList(300);

			for (User u : users) {
				st = connection.createStatement();
				rs = st.executeQuery("select tid, longitude, latitude, date, user_id from geo_tweets where user_id = " + u.getUser_id());
				countUser++;
//				System.out.println("Esta no usuario: " + countUser);

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

				rs.close();
				st.close();

			}

		} catch (SQLException se) {
			System.err.println("Erro em uma linha... Continuando com a proxima.");
			System.err.println(se.getMessage());
			countFail++;
		} finally {
			rs.close();
			st.close();
			// connection.commit();
			connection.close();

		}

		List<Integer> numMessages = new ArrayList<Integer>();

		for (User u : users) {
			numMessages.add(u.getTweetList().size());
		}
		Collections.sort(numMessages);
		
		
		
		System.out.println("--------------------TODOS OS USUARIOS-------------------------");
		System.out.println("Usuario com maior numero de mensagens: " + numMessages.get(numMessages.size() - 1));
		System.out.println("Usuario com menor numero de mensagens: " + numMessages.get(0));
		System.out.println("Usuario com mais de 20 mensagens: " + messageFilterSize(numMessages));
		System.out.println("Mediana de mensagens por usuario: " + calcMedian(numMessages));
		System.out.println("Media de mensagens por usuario: " + calcMedia(numMessages));
		System.out.println("Media de mensagens por dia: " + calcMessagesPerDayMedia(users));
		System.out.println("Mediana de mensagens por dia: " + calcMessagesPerDayMedian(users));
		System.out.println("Media de deslocamento: " + calcMediaOfDisplacement(users));
		System.out.println("Mediana de deslocamento: " + calcMedianOfDisplacement(users));
		System.out.println("Media do raio de giro: " + calcRadiusMedia(users));
		System.out.println("Mediana do raio de giro: " + calcRadiusMedian(users));
		
		List<Integer> selectedValues = new ArrayList<Integer>();
		List<User> selectedUsers = new ArrayList<User>();
		
		for(User u : users){
			if(u.getTweetList().size() >= 20){
				selectedUsers.add(u);
				selectedValues.add(u.getTweetList().size());
			}
		}
		
		Collections.sort(selectedValues);
		
		
		System.out.println("Lista reduzida a usuarios com mais de 20 msgns: " + selectedUsers.size());
		
		
		System.out.println("--------------USUARIOS COM MAIS DE 20 MSGNS----------------");
		System.out.println("Usuario com maior numero de mensagens: " + selectedValues.get(selectedValues.size() - 1));
		System.out.println("Usuario com menor numero de mensagens: " + selectedValues.get(0));
		System.out.println("Usuario com mais de 20 mensagens: " + messageFilterSize(selectedValues));
		System.out.println("Mediana de mensagens por usuario: " + calcMedian(selectedValues));
		System.out.println("Media de mensagens por usuario: " + calcMedia(selectedValues));
		System.out.println("Media de mensagens por dia: " + calcMessagesPerDayMedia(selectedUsers));
		System.out.println("Mediana de mensagens por dia: " + calcMessagesPerDayMedian(selectedUsers));
		System.out.println("Media de deslocamento: " + calcMediaOfDisplacement(selectedUsers));
		System.out.println("Mediana de deslocamento: " + calcMedianOfDisplacement(selectedUsers));
		System.out.println("Media do raio de giro: " + calcRadiusMedia(selectedUsers));
		System.out.println("Mediana do raio de giro: " + calcRadiusMedian(selectedUsers));
		
		

		// GeoCalculator calc = new GeoCalculator();
		// User user = new User(tweets);
		// user.setUser_id(tweets.get(0).getUser_id());
		//
		// Double[] midPoint = calc.calculateMidPoint(user.tweetsAsPoints());
		//
		// Point midP = new Point(midPoint[0], midPoint[1]);
		//
		// user.setRadiusOfGyration(calc.calculateRadiusOfGyration(user.tweetsAsPoints(),
		// midP));
		// calc.generateDisplacement(user.getTweetList());
		// calc.generateReturnProbs(user.getTweetList());
		//
		// printVals(user, midP);

	}

	private static int messageFilterSize(List<Integer> numMessages) {
		int index = 0;
		
		for(int i = 0; i < numMessages.size(); i++){
			if(numMessages.get(i) >= 20){
				index = i;
				break;
			}
		}
		return numMessages.size() - index;
		
	}

	private static Double calcMediaOfDisplacement(List<User> users) {
		List<Point> points = new ArrayList<Point>();
		Double totalDisplacement = 0.0;
		GeoCalculator calc = new GeoCalculator();
		List<Double> listDisplacement = new ArrayList<Double>();

		for (User u : users) {
			calc.generateDisplacement(u.getTweetList());
		}

		for (User u : users) {
			for (Tweet t : u.getTweetList()) {
				totalDisplacement += t.getUserDisplacement();
			}
			listDisplacement.add(totalDisplacement);
			totalDisplacement = 0.0;
		}

		return calcMediaDouble(listDisplacement);

	}
	
	private static Double calcRadiusMedia(List<User> users){
		GeoCalculator calc = new GeoCalculator();
		Double generalRadius = 0.0;
		
		for(User u : users){
			Double[] midPoint = calc.calculateMidPoint(u.tweetsAsPoints());
			Point midP = new Point(midPoint[0], midPoint[1]);
			generalRadius += calc.calculateRadiusOfGyration(u.tweetsAsPoints(), midP);
		}
		
		return generalRadius / users.size();
	}
	
	private static Double calcRadiusMedian(List<User> users){
		GeoCalculator calc = new GeoCalculator();
		List<Double> radList = new ArrayList<Double>();
		
		for(User u : users){
			Double[] midPoint = calc.calculateMidPoint(u.tweetsAsPoints());
			Point midP = new Point(midPoint[0], midPoint[1]);
			radList.add(calc.calculateRadiusOfGyration(u.tweetsAsPoints(), midP));
		}
		
		Collections.sort(radList);
		return radList.get(radList.size() / 2);
		
	}

	private static Double calcMedianOfDisplacement(List<User> users) {
		List<Point> points = new ArrayList<Point>();
		Double totalDisplacement = 0.0;
		GeoCalculator calc = new GeoCalculator();
		List<Double> listDisplacement = new ArrayList<Double>();

		for (User u : users) {
			calc.generateDisplacement(u.getTweetList());
		}

		for (User u : users) {
			for (Tweet t : u.getTweetList()) {
				totalDisplacement += t.getUserDisplacement();
			}
			listDisplacement.add(totalDisplacement);
			totalDisplacement = 0.0;
		}

		return calcMedianDouble(listDisplacement);

	}

	private static int calcMessagesPerDayMedia(List<User> users) {

		List<Tweet> tList = new ArrayList<Tweet>();
		Map<String, Integer> map = new HashMap<String, Integer>();
		int total = 0;

		for (User u : users) {
			for (Tweet t : u.getTweetList()) {
				tList.add(t);
			}
		}

		for (Tweet t : tList) {
			String dateString = getDateToString(t);
			if (map.containsKey(dateString)) {
				map.put(dateString, map.get(dateString) + 1);
			} else {
				map.put(dateString, 1);
			}
		}

		Collection<Integer> values = map.values();

		for (Integer t : values) {
			total += t;
		}
		int media = total / values.size();
		return media;

	}
	
	private static int calcMessagesPerDayMedian(List<User> users) {

		List<Tweet> tList = new ArrayList<Tweet>();
		Map<String, Integer> map = new HashMap<String, Integer>();
		int total = 0;

		for (User u : users) {
			for (Tweet t : u.getTweetList()) {
				tList.add(t);
			}
		}

		for (Tweet t : tList) {
			String dateString = getDateToString(t);
			if (map.containsKey(dateString)) {
				map.put(dateString, map.get(dateString) + 1);
			} else {
				map.put(dateString, 1);
			}
		}

		Collection<Integer> values = map.values();
		List<Integer> list = new ArrayList<Integer>();
		for(Integer i : values){
			list.add(i);
		}
		Collections.sort(list);
		
		return list.get(list.size() / 2);

	}

	private static String getDateToString(Tweet t) {
		int day = t.getDate().getDate();
		int month = t.getDate().getMonth();
		int year = t.getDate().getYear();
		String dateString = day + "-" + month + "-" + year;
		return dateString;

	}

	private static int calcMedian(List<Integer> vals) {
		Collections.sort(vals);
		int mid = (vals.size() - 1) / 2;
		return vals.get(mid);

	}

	private static Double calcMedianDouble(List<Double> vals) {
		Collections.sort(vals);
		int mid = (vals.size() - 1) / 2;
		return vals.get(mid);

	}

	private static int calcMedia(List<Integer> vals) {
		int total = 0;
		for (Integer i : vals) {
			total += i;
		}
		return total / vals.size();
	}

	private static Double calcMediaDouble(List<Double> vals) {
		int total = 0;
		for (Double i : vals) {
			total += i;
		}
		return (double) total / vals.size();
	}

	private static void printVals(User user, Point midP) {
		String messages = "";
		for (Tweet t : user.getTweetList()) {
			messages += "Text: " + t.getMessage() + "| coords: lat: " + t.getLatitude() + " long: " + t.getLongitude()
					+ "| Displacement: " + t.getUserDisplacement() + "| Return prob: " + t.getReturnProb()
					+ "| Timestamp: " + t.getDate() + System.lineSeparator();
		}

		System.out.println("USER ID: " + user.getUser_id() + System.lineSeparator() + "MESSAGES: " + messages
				+ System.lineSeparator() + " Radius of Gyration :" + user.getRadiusOfGyration() + " Mid Point: "
				+ " lat: " + midP.getLatitude() + " long: " + midP.getLongitude());

	}

	private static List<User> getUserList(int n) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		List<User> listUsers = new ArrayList<User>();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres",
				"admin");
		connection.setAutoCommit(false);
		st = connection.createStatement();
		rs = st.executeQuery("select * from geo_tweets_users order by user_id limit " + n + " offset 300000");

		while (rs.next()) {
			User u = new User(new ArrayList<Tweet>());
			u.setUser_id(rs.getLong("user_id"));
			listUsers.add(u);
		}
		st.close();
		rs.close();
		connection.close();
		return listUsers;

	}

}
