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
import java.util.Collections;
import java.util.List;

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
			
			List<Tweet> t = new ArrayList<Tweet>();
			int count = 0;
			
			while(count <= 200){
				t.add(users.get(0).getTweetList().get(count));
				count++;
			}
			users.get(0).setTweetList(t);
			
			generateDisplacements(users);
			findHomePoint(users);
			findUserCentroid(users);
			calculateRadius(users);
			
			
			
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
			Point home = findingHome(cluster);
			u.setPointHome(home);
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
		DBSCANClusterer dbscan = new DBSCANClusterer(45.0, 3, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		return cluster;
	}

	private static List<DoublePoint> formatPointsToCluster(User user) {

	    List<DoublePoint> points = new ArrayList<DoublePoint>();
	    for (Tweet t : user.getTweetList()) {
	    	double[] d = new double[2];
	    	d[0] = t.getLatitude();
            d[1] = t.getLongitude();
            points.add(new DoublePoint(d));
	    }
	    return points;
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
				if(calc.calculateDistance(u.getPointCentroid().getLatitude(), u.getPointCentroid().getLongitude(), t.getLatitude(), t.getLongitude()) >= 45.0){
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
		
		
//		List<User> listUsers = new ArrayList<User>();
//		User u = new User(new ArrayList<Tweet>());
//		u.setUser_id(new Long(630953012));
//		listUsers.add(u);
		
		return listUsers;

	}

}

//CREATE TABLE geo_tweets_users_selected
//(
//  user_id numeric NOT NULL,
//  longitude_home numeric,
//  latitude_home numeric,
//  num_messages numeric,
//  radius_of_gyration numeric,
//  total_displacement numeric,
//  longitude_centroid numeric,
//  latitude_centroid numeric,
//  CONSTRAINT tid_geo_tweets_users_selected_pk PRIMARY KEY (user_id)
//)
//WITH (
//  OIDS=FALSE
//);
//ALTER TABLE geo_tweets_users_selected
//  OWNER TO postgres;

