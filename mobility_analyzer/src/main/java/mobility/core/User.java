package mobility.core;

import java.util.ArrayList;
import java.util.List;

public class User {
	
	private List<Tweet> tweetList;
	private Double radiusOfGyration;
	private Long user_id;
	private Point pointHome;
	private Point pointCentroid;
	private int num_messages;
	private Double user_movement;
	private Displacement displacement;
	
	
	
	public User(List<Tweet> tweetList) {
		super();
		this.tweetList = tweetList;
		this.displacement = new Displacement();
	}

	public List<Tweet> getTweetList() {
		return tweetList;
	}


	public void setTweetList(List<Tweet> tweetList) {
		this.tweetList = tweetList;
	}


	public Double getRadiusOfGyration() {
		return radiusOfGyration;
	}


	public void setRadiusOfGyration(Double radiusOfGyration) {
		this.radiusOfGyration = radiusOfGyration;
	}
	
	public List<Point> tweetsAsPoints(){
		List<Point> points = new ArrayList<Point>();
		for(Tweet t : tweetList){
			Point p = new Point(t.getLatitude(), t.getLongitude());
			points.add(p);
		}
		return points;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}
	
	public void addToTweetList(Tweet t){
		tweetList.add(t);
	}


	public int getNum_messages() {
		return num_messages;
	}


	public void setNum_messages(int num_messages) {
		this.num_messages = num_messages;
	}

	public Double getUser_movement() {
		return user_movement;
	}


	public void setUser_movement(Double user_movement) {
		this.user_movement = user_movement;
	}


	public Point getPointHome() {
		return pointHome;
	}


	public void setPointHome(Point pointHome) {
		this.pointHome = pointHome;
	}


	public Point getPointCentroid() {
		return pointCentroid;
	}


	public void setPointCentroid(Point pointCentroid) {
		this.pointCentroid = pointCentroid;
	}


	public Displacement getDisplacement() {
		return displacement;
	}


	public void setDisplacement(Displacement displacement) {
		this.displacement = displacement;
	}
	
	
	
	
}
