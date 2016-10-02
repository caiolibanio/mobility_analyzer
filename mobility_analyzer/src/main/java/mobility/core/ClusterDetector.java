package mobility.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mobility.service.TweetService;
import mobility.service.UserService;

public class ClusterDetector {
	
	private static GeoCalculator calc = new GeoCalculator();

	private static UserService userService = new UserService();

	private static TweetService tweetService = new TweetService();
	
	private static List<User> listUsers;

	public static void main(String[] args) {
		
		listUsers.addAll(userService.findAllSelectedUsers(2500));
		System.out.println("Final user selection is in process...");

		try {
			calculating();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Erro fatal!");
		}
	}

	private static void calculating() throws SQLException {
		List<User> users = new ArrayList<User>();
		List<User> usersToInsert = new ArrayList<User>();
		int countUserProcessed = 0;

		for (User u : users) {

			u.getTweetList().addAll(tweetService.findTweetsByUser(u.getUser_id()));
			usersToInsert.add(u);

			if (usersToInsert.size() == 1000) {
				countUserProcessed += 1000;
				
				analyseUsers(usersToInsert);
				insert(usersToInsert);
				System.out.println("Analysed Users: " + countUserProcessed);
				usersToInsert.clear();

			}

		}
		if (usersToInsert.size() > 0) {
			analyseUsers(usersToInsert);
			insert(usersToInsert);
			usersToInsert.clear();
		}

		Date date = new Date();
		System.out.println(date);

	}

	private static void insert(List<User> usersToInsert) {
		// TODO Auto-generated method stub
		
	}

	private static void analyseUsers(List<User> usersToInsert) {
		// TODO Auto-generated method stub
		
	}
	
	

}
