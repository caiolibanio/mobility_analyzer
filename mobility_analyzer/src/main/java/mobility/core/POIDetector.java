package mobility.core;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import location.FoursquarePOI;
import location.POIGrabber;
import location.PointOfInterest;
import mobility.service.ClusteredPointService;

public class POIDetector {
	
	private static List<Long> listUserID = new ArrayList<Long>();
	
	private static ClusteredPointService clusteredPointService = new ClusteredPointService();
	
	private static List<ClusteredCentroid> listClusteredCentroids = new ArrayList<ClusteredCentroid>(); 
	
	private static GeoCalculator calc = new GeoCalculator();

	public static void main(String[] args) {
//		listUsers.add(userService.findUserById(new Long(14493067)));
//		listUsers.add(userService.findUserById(new Long(20094532)));
		listUserID.addAll(clusteredPointService.findAllUniqueUSersID());

		calculating();
	}

	private static void calculating() {
		
//		createClusteredCentroids();
//		insertClusteredCentroids();
		//-------
		List<ClusteredCentroid> listCentroidsFromDB = clusteredPointService.findAllCentroids();
		if(listCentroidsFromDB.size() > 0){
			try {
				retrivePOIFromFoursquare(listCentroidsFromDB);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private static void retrivePOIFromFoursquare(List<ClusteredCentroid> listCentroidsFromDB) throws Exception {
		
		for(ClusteredCentroid centroid : listCentroidsFromDB){
			PointOfInterest closestPOI = null;
			Double minDistance = null;
			List<PointOfInterest> POIList = generatePOIListByRadius(centroid.getPointMessage(), 45);
			for(PointOfInterest poi : POIList){
				Point centroidPoint = centroid.getPointMessage();
				Double distance = calc.calculateDistance(centroidPoint.getLatitude(),
						centroidPoint.getLongitude(), poi.getLat(), poi.getLng());
				if(minDistance == null){
					minDistance = distance;
					closestPOI = poi;
				}else if(distance < minDistance){
					minDistance = distance;
					closestPOI = poi;
				}
				
			}
			if(closestPOI != null){
				updateClosestPOIName(centroid.getId(), closestPOI);
			}
			
		}
		
	}

	private static void updateClosestPOIName(Long id, PointOfInterest poi) {
		clusteredPointService.updateCentroidPOIName(id, poi);
		
	}

	private static void insertClusteredCentroids() {
		clusteredPointService.saveClusteredCentroids(listClusteredCentroids);
		
	}
	
	private static List<PointOfInterest> generatePOIListByRadius(Point centroid, int radius) throws Exception{
		location.Point point = new location.Point(centroid.getLatitude(), centroid.getLongitude()); // UFCG
		POIGrabber p = POIGrabber.getInstance();
		p.setGooglePlacesAPIKey("googleKey");
		p.setFoursquareOAuthToken("VTCCBUXQD5SICWGDVKCMPOQXNLWJP5CCFX1KKXCPFMXOO2Q4&v=20160920"); //caio token
		p.setFactualAPIKey("factualKey");
		short[] source = { POIGrabber.SOURCE_FOURSQUARE };
		List<PointOfInterest> POIList = p.grabByRadius(point, radius, source);
		return POIList;
		
	}

	private static void createClusteredCentroids() {
		for(Long user_id : listUserID){
			int numberOfClusters = clusteredPointService.calculateNumberOfClusters(user_id);
			for(int i = 1; i <= numberOfClusters; i++){
				Point pointCentroid = clusteredPointService.calculateClusterCentroid(user_id, i);
				ClusteredCentroid clusteredCentroid = new ClusteredCentroid(user_id, pointCentroid, i);
				listClusteredCentroids.add(clusteredCentroid);
			}
		}
		
	}

}
