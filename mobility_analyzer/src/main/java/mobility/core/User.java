package mobility.core;

import java.util.ArrayList;
import java.util.List;

public class User {
	
	private List<Tweet> tweetList;
	private Double radiusOfGyration;
	private Long user_id;
	private Double longitude_home;
	private Double latitude_home;
	private int num_messages;
	private Double total_Displacement;
	private Double longitude_centroid;
	private Double latitude_centroid;
	
	
	
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


	public Double getLongitude_home() {
		return longitude_home;
	}


	public void setLongitude_home(Double longitude_home) {
		this.longitude_home = longitude_home;
	}


	public Double getLatitude_home() {
		return latitude_home;
	}


	public void setLatitude_home(Double latitude_home) {
		this.latitude_home = latitude_home;
	}


	public int getNum_messages() {
		return num_messages;
	}


	public void setNum_messages(int num_messages) {
		this.num_messages = num_messages;
	}


	public Double getTotal_Displacement() {
		return total_Displacement;
	}


	public void setTotal_Displacement(Double total_Displacement) {
		this.total_Displacement = total_Displacement;
	}


	public Double getLongitude_centroid() {
		return longitude_centroid;
	}


	public void setLongitude_centroid(Double longitude_centroid) {
		this.longitude_centroid = longitude_centroid;
	}


	public Double getLatitude_centroid() {
		return latitude_centroid;
	}


	public void setLatitude_centroid(Double latitude_centroid) {
		this.latitude_centroid = latitude_centroid;
	}
	
	
	
	
	
	

}
