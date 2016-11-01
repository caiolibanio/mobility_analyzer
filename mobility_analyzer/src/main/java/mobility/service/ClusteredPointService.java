package mobility.service;

import java.util.List; 

import location.PointOfInterest;
import mobility.DAO.ClusteredPointDAO;
import mobility.core.ClusteredCentroid;
import mobility.core.ClusteredPoint;
import mobility.core.Point;

public class ClusteredPointService {
	
	private ClusteredPointDAO clusteredPointDAO = new ClusteredPointDAO();
	
	public void saveClusteredPoints(List<ClusteredPoint> listPoints){
		clusteredPointDAO.saveClusteredPoints(listPoints);
	}
	
	public List<ClusteredPoint> findAll(){
		return clusteredPointDAO.findAll();
	}
	
	public List<Long> findAllUniqueUSersID(){
		return clusteredPointDAO.findAllUniqueUSersID();
	}
	
	public int calculateNumberOfClusters(Long user_id){
		return clusteredPointDAO.calculateNumberofClusters(user_id);
	}
	
	public Point calculateClusterCentroid(Long user_id, int clusterNumber){
		return clusteredPointDAO.calculateClusterCentroid(user_id, clusterNumber);
	}
	
	public void saveClusteredCentroids(List<ClusteredCentroid> listCentroids){
		clusteredPointDAO.saveClusteredCentroids(listCentroids);
	}
	
	public List<ClusteredCentroid> findAllCentroids(){
		return clusteredPointDAO.findAllCentroids();
	}
	
	public void updateCentroidPOIName(Long id, PointOfInterest poi){
		clusteredPointDAO.updateCentroidPOIName(id, poi);
	}
	
	public Double findMedianPriceByUserID(Long user_id){
		return clusteredPointDAO.findMedianPriceByUserID(user_id);
	}
	
}
