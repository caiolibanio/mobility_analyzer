package mobility.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

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
		List<User> users = new ArrayList<User>();
		List<User> usersToInsert = new ArrayList<User>();
		int countUserProcessed = 0;

		try {
			users = getUserList();

			for (User u : users) {

				u.getTweetList().addAll(tweetService.findTweetsByUser(u.getUser_id()));
				usersToInsert.add(u);

				if (usersToInsert.size() == 1000) {
					countUserProcessed += 1000;
					
					analyseUsers(usersToInsert);
					insert(usersToInsert);
					System.out.println("Analysed Users: " + countUserProcessed);
					usersToInsert.clear();

				}

			}
			if (usersToInsert.size() > 0) {
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

	private static List<User> getUserListComp() {
		return userService.findAllUsersGeoTweetComp();
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
		for (User u : users) {
			u.setNum_messages(u.getTweetList().size());
		}
	}

	private static void calculateRadius(List<User> users) {
		for (User u : users) {
			u.setRadiusOfGyration(calc.calculateRadiusOfGyration(u.tweetsAsPoints(), u.getPointCentroid()));
		}

	}

	private static void findUserCentroid(List<User> users) throws SQLException {

		for (User u : users) {
			Point centroid = userService.findUserCentroid(u.getUser_id());
			u.setPointCentroid(centroid);
		}
	}

	private static void findHomePoint(List<User> users) {
		for (User u : users) {
			List<DoublePoint> points = formatPointsToCluster(u);
			List<Cluster<DoublePoint>> cluster = clusteringPoints(40.0, 4, points);
			if (cluster.size() > 0) {
				Point home = findingHome(cluster);
				u.setPointHome(home);
			} else {
				u.setPointHome(new Point(0.0, 0.0));
			}

		}
	}

	private static Point findingHome(List<Cluster<DoublePoint>> cluster) {
		List<List<DoublePoint>> listOfClusters = new ArrayList<List<DoublePoint>>();
		for (Cluster<DoublePoint> c : cluster) {
			List<DoublePoint> singleCluster = new ArrayList<DoublePoint>();
			for (DoublePoint p : c.getPoints()) {
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
		for (DoublePoint p : homeCluster) {
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

		for (int i = 0; i < listOfClusters.size(); i++) {

			if (listOfClusters.get(i).size() > index) {
				index = i;
			}
		}
		return listOfClusters.get(index);
	}

	private static List<Cluster<DoublePoint>> clusteringPoints(double radius, int minPts, List<DoublePoint> points) {
		DBSCANClusterer dbscan = new DBSCANClusterer(radius, minPts, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		List<Cluster<DoublePoint>> realClusters = new ArrayList<Cluster<DoublePoint>>();
		for(Cluster<DoublePoint> c : cluster){
			if(c.getPoints().size() >= minPts){
				realClusters.add(c);
			}
		}
		return realClusters;
	}

	private static List<DoublePoint> formatPointsToCluster(User user) {

		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Tweet t : user.getTweetList()) {
			if (DateTimeOperations.isHomeTime(t)) {
				double[] d = new double[2];
				d[0] = t.getLatitude();
				d[1] = t.getLongitude();
				points.add(new DoublePoint(d));
			}
		}
		return points;
	}

	private static void generateDistanceMovement(List<User> users) {
		for (User u : users) {
			calc.generateDisplacement(u.getTweetList());
			Double totalDispl = 0.0;
			for (Tweet t : u.getTweetList()) {
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
		for (User u : users) {
			u.getDisplacement().generateLowDisplacementPerDay(5);
			u.getDisplacement().generateTopDisplacementPerDay(5);
			u.getDisplacement().generateLowDistanceDisplacement(5);
			u.getDisplacement().generateTopDistanceDisplacement(5);
			u.getDisplacement().calculateDisplacementPerDayMedian();
			u.getDisplacement().calculateDistanceDisplacementMedian();
		}

	}

	private static void calculateDistancePerDisplacement(List<User> users) {
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
		for (User u : users) {
			List<Tweet> listTweets = u.getTweetList();
			displCount = calculateTotalDisplacementPerTweets(listTweets);
			u.getDisplacement().setDisplacementCounter(displCount);
			displCount = 0;
		}

	}

	private static int calculateTotalDisplacementPerTweets(List<Tweet> listTweets) {
		int displCount = 0;
		Collections.sort(listTweets);
		for (int i = 1; i < listTweets.size(); i++) {
			Tweet tweet = listTweets.get(i);
			Tweet predTweet = listTweets.get(i - 1);
			if (isDisplacement(tweet.getLatitude(), tweet.getLongitude(), predTweet.getLatitude(),
					predTweet.getLongitude())) {
				displCount++;
			}
		}
		return displCount;
	}

	private static List<Tweet> getTweetsByDate(List<Tweet> tweetList, Calendar calendar) {
		List<Tweet> list = new ArrayList<Tweet>();
		for (Tweet t : tweetList) {
			Timestamp time = t.getDate();
			Calendar londonTime = DateTimeOperations.getLondonTime(time);
			if (londonTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
					&& londonTime.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
					&& londonTime.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
				list.add(t);
			}
		}
		return list;
	}

	private static boolean isDisplacement(Double lat1, Double lon1, Double lat2, Double lon2) {
		if (calc.calculateDistance(lat1, lon1, lat2, lon2) >= 40) {
			return true;
		}
		return false;
	}

	private static void calculateDisplacementPerDay(List<User> users) {
		int displPerDay = 0;
		List<Tweet> analyzedTweets = new ArrayList<Tweet>();
		for (User u : users) {
			List<Tweet> listTweets = u.getTweetList();
			Collections.sort(listTweets);
			analyzedTweets.clear();
			for (Tweet t : listTweets) {
				if (!analyzedTweets.contains(t)) {
					Calendar calendar = DateTimeOperations.getLondonTime(t.getDate());
					List<Tweet> tweetsPerDate = getTweetsByDate(listTweets, calendar);
					analyzedTweets.addAll(tweetsPerDate);
					displPerDay = calculateTotalDisplacementPerTweets(tweetsPerDate);
					if (displPerDay > 0) {
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
		for (User u : users) {
			for (Tweet t : u.getTweetList()) {
				if (calc.calculateDistance(u.getPointCentroid().getLatitude(), u.getPointCentroid().getLongitude(),
						t.getLatitude(), t.getLongitude()) >= 40.0) {
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
		for (User u : users) {
			if (u.getTweetList().size() < 20) {
				toRemove.add(u);
			}
		}
		users.removeAll(toRemove);

	}

	private static List<User> getUserList() throws SQLException {
		return userService.findAllUsersGeoTweet();

	}

}
