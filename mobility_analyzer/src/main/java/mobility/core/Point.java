package mobility.core;

public class Point {
	
	private Double longitude;
	private Double latitude;
	private Double weight = 1.0;
	private Double X;
	private Double Y;
	private Double Z;
	private Long id;
	
	public Point(Double latitude, Double longitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.weight  = weight;
	}
	
	public Point(){}
	
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Double getX() {
		return X;
	}
	public void setX(Double x) {
		X = x;
	}
	public Double getY() {
		return Y;
	}
	public void setY(Double y) {
		Y = y;
	}
	public Double getZ() {
		return Z;
	}
	public void setZ(Double z) {
		Z = z;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	
	
	
	

}
