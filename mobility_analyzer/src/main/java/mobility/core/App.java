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
		// List<Point> list = new ArrayList<Point>();
		// Point point1 = new Point(-74.0059731, 40.7143528, 1095.75);
		// Point point2 = new Point(-87.6297982, 41.8781136, 730.5);
		// Point point3 = new Point(-84.3879824, 33.7489954, 365.25);
		//
		// list.add(point1);
		// list.add(point2);
		// list.add(point3);
		//
		// GeoCalculator calc = new GeoCalculator();
		// Double[] array = calc.calculateMidPoint(list);

		// GeoCalculator calc = new GeoCalculator();
		// System.out.println(calc.calculateDistance(47.6788206, -122.3271205,
		// 47.6788206, -122.5271205)); // in meters

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
				rs = st.executeQuery("select * from geo_tweets where user_id = 161059155 limit 5");
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
		List<Point> points = new ArrayList<Point>();
		User user = new User(tweets);
		
		for(Tweet t : user.getTweetList()){
			Point p = new Point(t.getLatitude(), t.getLongitude());
			points.add(p);
		}
		
		Double[] midPoint = calc.calculateMidPoint(points);
		
		Point midP = new Point(midPoint[0], midPoint[1]);
		user.setRadiusOfGyration(calc.calculateRadiusOfGyration(user.getTweetList(), midP));
		calc.generateDisplacement(user.getTweetList());
		calc.generateReturnProbs(user.getTweetList());
		
		

	}

}
