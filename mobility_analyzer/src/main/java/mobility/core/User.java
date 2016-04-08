package mobility.core;

import java.util.List;

public class User {
	
	private List<Tweet> tweetList;
	private Double radiusOfGyration;
	
	
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
	
	

}
