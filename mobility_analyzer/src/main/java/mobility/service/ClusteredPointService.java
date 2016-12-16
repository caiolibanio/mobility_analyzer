package mobility.service;

import java.util.ArrayList;
import java.util.List; 

import location.PointOfInterest;
import mobility.DAO.ClusteredPointDAO;
import mobility.core.ClusteredCentroid;
import mobility.core.ClusteredPoint;
import mobility.core.Point;
import mobility.statistic.ClusteredHome;

public class ClusteredPointService {
	
	private ClusteredPointDAO clusteredPointDAO = new ClusteredPointDAO();
	
	public void insertClusteredPoinstHome(List<ClusteredPoint> listClusteredHomes){
		clusteredPointDAO.saveClusteredPointHomes(listClusteredHomes);
	}
	
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
	
	public Point calculateHomeClusterCentroid(Long user_id){
		return clusteredPointDAO.calculateHomeClusterCentroid(user_id);
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
	
	public void adjustAveragePrices(List<Double> listMedianPrices){
		List<Integer> emptyPricesIndex = new ArrayList<Integer>();
		
		for(int i = 0; i < listMedianPrices.size(); i++){
			if(listMedianPrices.get(i).equals(0.0)){
				emptyPricesIndex.add(i);
			}
		}
		Double averagePrice = ((double) listMedianPrices.stream().mapToInt(Double::intValue).sum()) / (listMedianPrices.size() - emptyPricesIndex.size()); //avarege
		averagePrice = (double) Math.round(averagePrice);
		
		for(Integer index : emptyPricesIndex){
			listMedianPrices.set(index, averagePrice);
		}
	}
	
}
