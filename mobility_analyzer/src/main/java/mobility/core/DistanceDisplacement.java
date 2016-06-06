package mobility.core;

public class DistanceDisplacement implements Comparable<DistanceDisplacement>{
	
	private Long id;
	
	private Point pointA;
	
	private Point pointB;
	
	private Double distanceDisplacement;
	
	private Long displacement_id;

	public DistanceDisplacement(Point pointA, Point pointB, Double distanceDisplacement) {
		super();
		this.pointA = pointA;
		this.pointB = pointB;
		this.distanceDisplacement = distanceDisplacement;
	}

	public Point getPointA() {
		return pointA;
	}

	public void setPointA(Point pointA) {
		this.pointA = pointA;
	}

	public Point getPointB() {
		return pointB;
	}

	public void setPointB(Point pointB) {
		this.pointB = pointB;
	}

	public Double getDistanceDisplacement() {
		return distanceDisplacement;
	}

	public void setDistanceDisplacement(Double distanceDisplacement) {
		this.distanceDisplacement = distanceDisplacement;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	

	public Long getDisplacement_id() {
		return displacement_id;
	}

	public void setDisplacement_id(Long displacement_id) {
		this.displacement_id = displacement_id;
	}

	@Override
	public int compareTo(DistanceDisplacement o) {
		if(this.getDistanceDisplacement() < o.getDistanceDisplacement()){
			return -1;
		}else{
			if(this.getDistanceDisplacement() == o.getDistanceDisplacement()){
				return 0;
			}
		}
		return 1;
	}
	
	
}
