package mobility.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
			
			generateDisplacements(users);
			
			
			
			
			filteringMessagesByQuantity(users);
			filteringMessagesByDisplacement(users);
			
			insert(users);

		} catch (SQLException se) {
			System.err.println("Erro em uma linha... Continuando com a proxima.");
			System.err.println(se.getMessage());
			countFail++;
		} finally {
			rs.close();
			st.close();
			connection.commit();
			connection.close();

		}

		
		

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
					"latitude_centroid" + ") " + "VALUES (" + "'" + u.getUser_id() + "'" + ", " + "'" + u.getLongitude_home() + "'" + ", " + 
			"'" + u.getLatitude_home() + "'" + " ," + "'" + u.getNum_messages() + "'" + " ," + "'" + u.getRadiusOfGyration() + "'" + ", " + 
			"'" + u.getTotal_Displacement() + "'" + " ," + "'" + u.getLongitude_centroid() + "'" + " ," + 
			"'" + u.getLatitude_centroid() + "'" + ");";

			stmt.executeUpdate(sql);
			stmt.close();
		}
		
		
		
	}

	private static void filteringMessagesByDisplacement(List<User> users) {
		List<User> toRemove = new ArrayList<User>();
		for(User u : users){
			Double medianDisp = u.getTotal_Displacement() / u.getTweetList().size();
			if(medianDisp < 45.0){
				toRemove.add(u);
			}
		}
		
		users.removeAll(toRemove);
		
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

	private static List<User> getUserList(int n) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		List<User> listUsers = new ArrayList<User>();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres",
				"admin");
		connection.setAutoCommit(false);
		st = connection.createStatement();
		rs = st.executeQuery("select * from geo_tweets_users order by user_id limit " + n);

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
