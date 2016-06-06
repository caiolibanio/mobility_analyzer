package mobility.service;

import java.util.List;

import mobility.DAO.TweetDAO;
import mobility.core.Tweet;

public class TweetService {
	
	private TweetDAO tweetDAO = new TweetDAO();
	
	public List<Tweet> findTweetsByUser(Long userId){
		return tweetDAO.findTweetsByUser(userId);
	}

}
