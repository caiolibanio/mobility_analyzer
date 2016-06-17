package mobility.core;

public class DistanceDisplacement implements Comparable<DistanceDisplacement>{
	
	private Long id;
	
	private Point pointA;
	
	private Point pointB;
	
	private Double distanceDisplacement;
	
	private Displacement displacement;

	public DistanceDisplacement() {
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

	public Displacement getDisplacement() {
		return displacement;
	}

	public void setDisplacement(Displacement displacement) {
		this.displacement = displacement;
	}

	@Override
	public int compareTo(DistanceDisplacement o) {
		return this.getDistanceDisplacement().compareTo(o.getDistanceDisplacement());
	}
	
	
}
