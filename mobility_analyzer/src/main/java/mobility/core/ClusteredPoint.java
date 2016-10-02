package mobility.core;

public class ClusteredPoint {
	
	private Long user_id;
	
	private Point pointMessage;
	
	private int clusterNumber;

	public ClusteredPoint(Long user_id, Point pointMessage, int clusterNumber) {
		super();
		this.user_id = user_id;
		this.pointMessage = pointMessage;
		this.clusterNumber = clusterNumber;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public Point getPointMessage() {
		return pointMessage;
	}

	public void setPointMessage(Point pointMessage) {
		this.pointMessage = pointMessage;
	}

	public int getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(int clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
	
	
}
