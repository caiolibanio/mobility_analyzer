package mobility.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import mobility.dbscan.GeoDistance;
import mobility.service.ClusteredPointService;
import mobility.service.TweetService;
import mobility.service.UserService;
import mobility.statistic.ClusteredUser;

public class ClusterDetector {
	
	private static GeoCalculator calc = new GeoCalculator();

	private static UserService userService = new UserService();

	private static TweetService tweetService = new TweetService();
	
	private static List<User> listUsers;
	
	private static List<ClusteredUser> listClusteredUsers = new ArrayList<ClusteredUser>();
	
	private static List<ClusteredPoint> listOfClusteredPoints = new ArrayList<ClusteredPoint>();
	
	private static ClusteredPointService clusteredPointService = new ClusteredPointService();

	public static void main(String[] args) {
		listUsers = new ArrayList<User>();
//		listUsers.add(userService.findUserById(new Long(14493067)));
//		listUsers.add(userService.findUserById(new Long(20094532)));
		listUsers.addAll(userService.findAllSelectedUsers(1000));

		try {
			calculating();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erro fatal!");
		}
	}

	private static void calculating() throws SQLException {
		List<User> usersToInsert = new ArrayList<User>();
		int countUserProcessed = 0;

		clusteringMessagesOfUsers(listUsers);
		processClurteredPoints();
		insertClusteredPoints();
		

		Date date = new Date();
		System.out.println(date);

	}

	private static void insertClusteredPoints() {
		clusteredPointService.saveClusteredPoints(listOfClusteredPoints);
		
	}

	private static void clusteringMessagesOfUsers(List<User> usersToInsert) {
		int count = 0;
		for(User user : usersToInsert){
			findClusteredPoints(user);
			++count;
			System.out.println("clusterizou: " + count);
		}
	}
	
	//Refatorar isso!
	private static void processClurteredPoints(){
		int countCluster;
		for(ClusteredUser clusteredUser : listClusteredUsers){
			countCluster = 0;
			
			for(Cluster<DoublePoint> cluster : clusteredUser.getCluster()){
				
				if(cluster.getPoints().size() >= 3){
					++countCluster;
					for (DoublePoint p : cluster.getPoints()) {
						double[] dPoint = p.getPoint();
						Point point = new Point(dPoint[0], dPoint[1]);
						ClusteredPoint clusteredPoint = new ClusteredPoint(clusteredUser.getUser_id(), point, countCluster);
						listOfClusteredPoints.add(clusteredPoint);
						
					}
				}
			}
		}
	}
	
	private static void findClusteredPoints(User user) {
		List<DoublePoint> listOfPoints = new ArrayList<DoublePoint>();
		List<DoublePoint> points = formatPointsToClusterGeneral(user);
		List<Cluster<DoublePoint>> cluster = checkClusteredUser(user);
		
		if(cluster == null){
			cluster = clusteringPoints(points);
			if(cluster.size() > 0){
				ClusteredUser clusteredUser = new ClusteredUser(cluster, user.getUser_id());
				listClusteredUsers.add(clusteredUser);
			}
		}
//		ArrayList<ArrayList<DoublePoint>> listOfClusters = returnClustersList(cluster);
//
//		return listOfClusters;
		
	}
	
	private static List<DoublePoint> formatPointsToClusterGeneral(User user) {

		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Tweet t : user.getTweetList()) {

			double[] d = new double[2];
			d[0] = t.getLatitude();
			d[1] = t.getLongitude();
			points.add(new DoublePoint(d));

		}
		return points;
	}
	
	private static List<Cluster<DoublePoint>> checkClusteredUser(User user) {
		
		for(ClusteredUser u : listClusteredUsers){
			if(u.getUser_id().equals(user.getUser_id())){
				return u.getCluster();
			}
		}
		return null;
	}

	private static List<Cluster<DoublePoint>> clusteringPoints(List<DoublePoint> points) {
		DBSCANClusterer dbscan = new DBSCANClusterer(40.0, 3, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		return cluster;
	}
	
	private static ArrayList<ArrayList<DoublePoint>> returnClustersList(List<Cluster<DoublePoint>> cluster) {
		ArrayList<ArrayList<DoublePoint>> listOfClusters = new ArrayList<ArrayList<DoublePoint>>();
		for (Cluster<DoublePoint> c : cluster) {
			ArrayList<DoublePoint> singleCluster = new ArrayList<DoublePoint>();
			for (DoublePoint p : c.getPoints()) {
				singleCluster.add(p);
			}
			listOfClusters.add(singleCluster);
		}
		return listOfClusters;
	}
	
	

}
