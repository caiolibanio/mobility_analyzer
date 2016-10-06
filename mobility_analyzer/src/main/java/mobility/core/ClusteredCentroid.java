package mobility.core;

public class ClusteredCentroid extends ClusteredPoint{
	
	private String poiDescription;

	public ClusteredCentroid(Long user_id, Point pointMessage, int clusterNumber) {
		super(user_id, pointMessage, clusterNumber);
		// TODO Auto-generated constructor stub
	}

	public String getPoiDescription() {
		return poiDescription;
	}

	public void setPoiDescription(String poiDescription) {
		this.poiDescription = poiDescription;
	}

	
	
	

}
