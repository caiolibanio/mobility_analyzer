package mobility.service;

import java.util.List;

import mobility.DAO.ClusteredPointDAO;
import mobility.core.ClusteredPoint;

public class ClusteredPointService {
	
	private ClusteredPointDAO clusteredPointDAO = new ClusteredPointDAO();
	
	public void saveClusteredPoints(List<ClusteredPoint> listPoints){
		clusteredPointDAO.saveClusteredPoints(listPoints);
	}
	
}
