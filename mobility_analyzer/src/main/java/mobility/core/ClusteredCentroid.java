package mobility.core;

public class ClusteredCentroid extends ClusteredPoint{
	
	private String poiDescription;
	
	private Integer price;

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

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	
	
	

}
