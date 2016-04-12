package mobility.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



/**
 * Hello world!
 *
 */
public class App {

	static Connection connection = null;

	public static void main(String[] args) {
//		 List<Point> list = new ArrayList<Point>();
//		 Point point1 = new Point(40.7143528, -74.0059731);
//		 point1.setWeight(1.0);
//		 Point point2 = new Point(41.8781136, -87.6297982);
//		 point2.setWeight(1.0);
//		 Point point3 = new Point(33.7489954, -84.3879824);
//		 point3.setWeight(1.0);
//		
//		 list.add(point1);
//		 list.add(point2);
//		 list.add(point3);
//		
//		 GeoCalculator calc = new GeoCalculator();
//		 Double[] array = calc.calculateMidPoint(list);

		 
		 //"POINT(-82.0079179 38.7804872666667)" center postgis
		 
//		 Point midP = new Point(38.7804872666667, -82.0079179);
//		 
//		 System.out.println(calc.calculateRadiusOfGyration(list, midP));


		
		//----------------------------------------------------------------------

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

		while (!lastLine) {
			try {
				connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/tweets", "postgres",
						"admin");
				connection.setAutoCommit(false);
				st = connection.createStatement();
				rs = st.executeQuery("select * from geo_tweets where tid = 81937 OR tid = 81982 OR tid = 81896 OR"
						+ " tid = 84036 OR tid = 84053");
				step1 = step1 + 1000;
				step2 = step2 + 1000;
				
				while (rs.next() && countOK < 5) {

					Long tid = rs.getLong("tid");
					String json = rs.getString("json");
					Double lon = rs.getDouble("longitude");
					Double lat = rs.getDouble("latitude");
					Timestamp time = rs.getTimestamp("date");
					Tweet tweet = new Tweet(tid, json);
					tweet.setLongitude(lon);
					tweet.setLatitude(lat);
					tweet.setDate(time);
					tweet.setUser_id(rs.getLong("user_id"));
					tweet.setMessage(rs.getString("message"));
					tweets.add(tweet);
					countOK++;

				}
				lastLine = true;

			} catch (SQLException se) {
				System.err.println("Erro em uma linha... Continuando com a proxima.");
				System.err.println(se.getMessage());
				countFail++;
			} finally {
				rs.close();
				st.close();
//				connection.commit();
				connection.close();

			}

		}
		
		GeoCalculator calc = new GeoCalculator();
		User user = new User(tweets);
		user.setUser_id(tweets.get(0).getUser_id());
		
		Double[] midPoint = calc.calculateMidPoint(user.tweetsAsPoints());
		
		Point midP = new Point(midPoint[0], midPoint[1]);
		
		user.setRadiusOfGyration(calc.calculateRadiusOfGyration(user.tweetsAsPoints(), midP));
		calc.generateDisplacement(user.getTweetList());
		calc.generateReturnProbs(user.getTweetList());
		
		printVals(user, midP);
		
		
		
		

	}

	private static void printVals(User user, Point midP) {
		String messages = "";
		for(Tweet t : user.getTweetList()){
			messages += "Text: " + t.getMessage() + "| coords: lat: "
		+ t.getLatitude() + " long: " + t.getLongitude() + "| Displacement: "
					+ t.getUserDisplacement() + "| Return prob: " + t.getReturnProb() + "| Timestamp: " + t.getDate() + System.lineSeparator();
		}
		
		System.out.println("USER ID: " + user.getUser_id() + System.lineSeparator() + 
				"MESSAGES: " + messages + System.lineSeparator() + " Radius of Gyration :"
				+ user.getRadiusOfGyration() + " Mid Point: " + " lat: " + midP.getLatitude()
				+ " long: " + midP.getLongitude());
		
	}

}
