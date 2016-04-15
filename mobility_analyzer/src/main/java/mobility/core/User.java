package mobility.core;

import java.util.ArrayList;
import java.util.List;

public class User {
	
	private List<Tweet> tweetList;
	private Double radiusOfGyration;
	private Long user_id;
	
	
	public User(List<Tweet> tweetList) {
		super();
		this.tweetList = tweetList;
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
	
	
	
	

}
