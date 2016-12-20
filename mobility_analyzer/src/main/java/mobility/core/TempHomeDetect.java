package mobility.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import mobility.DAO.UserDAO;
import mobility.dbscan.GeoDistance;
import mobility.service.ClusteredPointService;
import mobility.service.TweetService;
import mobility.service.UserService;
import mobility.statistic.ClusteredHome;
import mobility.statistic.ClusteredUser;

public class TempHomeDetect {
	
	private static GeoCalculator calc = new GeoCalculator();

	private static UserService userService = new UserService();

	private static TweetService tweetService = new TweetService();
	
	private static List<User> listUsers;
	
	private static List<ClusteredHome> listClusteredHomes = new ArrayList<ClusteredHome>();
	
	private static List<ClusteredPoint> listOfClusteredPoints = new ArrayList<ClusteredPoint>();
	
	private static ClusteredPointService clusteredPointService = new ClusteredPointService();
	
	

	public static void main(String[] args) {
		listUsers = new ArrayList<User>();
//		listUsers.add(userService.findUserById(new Long(14493067)));
//		listUsers.add(userService.findUserById(new Long(2659)));
		listUsers.addAll(userService.findAllSelectedUsers(1000));

		try {
			calculating();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erro fatal!");
		}
	}

	private static void calculating() throws SQLException {
		
		findHomePoint(listUsers);
		processClurteredPoints();
		insertClusteredPoints();
		
		
		
		calculateHomeCentroid();
		updateUsers();
		
	}
	
	private static void updateUsers() {
		for(User user : listUsers){
			userService.updateUserHomePoint(user.getUser_id(), user.getPointHome());
		}
		
	}

	private static void calculateHomeCentroid() {
		for(User user : listUsers){
			user.setPointHome(clusteredPointService.calculateHomeClusterCentroid(user.getUser_id()));
			
		}
		
	}

	private static void insertClusteredPoints() {
		clusteredPointService.insertClusteredPoinstHome(listOfClusteredPoints);
		
	}
	
	private static void processClurteredPoints() {
		int countCluster;
		for (ClusteredHome clusteredHome : listClusteredHomes) {
			for (DoublePoint pt : clusteredHome.getHomeCluster()) {
				double[] dPoint = pt.getPoint();
				Point point = new Point(dPoint[0], dPoint[1]);
				ClusteredPoint clusteredPoint = new ClusteredPoint(clusteredHome.getUser_id(), point, 1);
				listOfClusteredPoints.add(clusteredPoint);

			}
		}
	}

	private static void findHomePoint(List<User> users) {
		for (User u : users) {
			List<DoublePoint> points = formatPointsToCluster(u);
			List<Cluster<DoublePoint>> cluster = clusteringPoints(points);
			if (cluster.size() > 0) {
				findingHome(cluster, u.getUser_id());
				
			}

		}
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
	
	private static List<Cluster<DoublePoint>> clusteringPoints(List<DoublePoint> points) {
		DBSCANClusterer dbscan = new DBSCANClusterer(40.0, 4, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		return cluster;
	}
	
	private static void findingHome(List<Cluster<DoublePoint>> cluster, Long user_id) {
		List<List<DoublePoint>> listOfClusters = new ArrayList<List<DoublePoint>>();
		for (Cluster<DoublePoint> c : cluster) {
			List<DoublePoint> singleCluster = new ArrayList<DoublePoint>();
			for (DoublePoint p : c.getPoints()) {
				singleCluster.add(p);
			}
			listOfClusters.add(singleCluster);
		}
		List<DoublePoint> homeCluster = findBiggestCluster(listOfClusters);
		
		if(homeCluster.size() >= 4){
			ClusteredHome cHome = new ClusteredHome(user_id, homeCluster);
			listClusteredHomes.add(cHome);
		}
		
		
		
		
		
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


}


