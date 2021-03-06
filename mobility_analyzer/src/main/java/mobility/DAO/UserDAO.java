package mobility.DAO;

import java.util.ArrayList;
import java.util.List;

import location.FoursquarePOI;

import java.sql.*;

import mobility.connection.Conexao;
import mobility.core.Displacement;
import mobility.core.Point;
import mobility.core.Tweet;
import mobility.core.User;
import mobility.service.DisplacementService;
import mobility.util.Util;

public class UserDAO implements IDAO<User> {

	private DisplacementService displacementService = new DisplacementService();

	// private PointDAO pointDAO = new PointDAO();

	private TweetDAO tweetDAO = new TweetDAO();

	@Override
	public void save(User entidade) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		String sql = "INSERT INTO geo_tweets_users_selected (user_id, home_point_id,"
				+ " num_messages, radius_of_gyration, total_displacement, centroid_point_id, displacement_id "
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?)";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, entidade.getUser_id());
			pstm.setDouble(2, entidade.getPointHome().getId());
			pstm.setLong(3, entidade.getNum_messages());
			pstm.setDouble(4, entidade.getRadiusOfGyration());
			pstm.setDouble(5, entidade.getUser_movement());
			pstm.setDouble(6, entidade.getPointCentroid().getId());
			pstm.setLong(7, entidade.getDisplacement().getId());
			pstm.executeUpdate();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back");
					e.printStackTrace();
					conn.rollback();
				} catch (SQLException excep) {
					System.err.print("Transaction could not be rolled back");
					excep.printStackTrace();
				}
			}
		} finally {
			Conexao.close(conn, pstm, null);
		}

	}

	public void saveUsers(List<User> users) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		String sql = "INSERT INTO geo_tweets_users_selected (user_id, home_point_id,"
				+ " num_messages, radius_of_gyration, total_displacement, centroid_point_id, displacement_id "
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?)";
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);

			for (User entidade : users) {
				pstm.setLong(1, entidade.getUser_id());
				pstm.setDouble(2, entidade.getPointHome().getId());
				pstm.setLong(3, entidade.getNum_messages());
				pstm.setDouble(4, entidade.getRadiusOfGyration());
				pstm.setDouble(5, entidade.getUser_movement());
				pstm.setDouble(6, entidade.getPointCentroid().getId());
				pstm.setLong(7, entidade.getDisplacement().getId());
				pstm.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back");
					e.printStackTrace();
					conn.rollback();
				} catch (SQLException excep) {
					System.err.print("Transaction could not be rolled back");
					excep.printStackTrace();
				}
			}
		} finally {
			Conexao.close(conn, pstm, null);
		}
	}

	public void saveUsers(List<User> users, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		String sql = "INSERT INTO geo_tweets_users_selected (user_id, geom_home_point,"
				+ " num_messages, radius_of_gyration, total_displacement, geom_centroid_point, displacement_id, home_social_data_code, centroid_social_data_code "
				+ ") VALUES (?, ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326), ?, ?, ?, ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326), ?, ?, ?)";

		pstm = conn.prepareStatement(sql);

		for (User entidade : users) {
			pstm.setLong(1, entidade.getUser_id());
			pstm.setDouble(2, entidade.getPointHome().getLongitude());
			pstm.setDouble(3, entidade.getPointHome().getLatitude());
			pstm.setLong(4, entidade.getNum_messages());
			pstm.setDouble(5, entidade.getRadiusOfGyration());
			pstm.setDouble(6, entidade.getUser_movement());
			pstm.setDouble(7, entidade.getPointCentroid().getLongitude());
			pstm.setDouble(8, entidade.getPointCentroid().getLatitude());
			pstm.setLong(9, entidade.getDisplacement().getId());
			
			pstm.setString(10, findPolygonOfPoint(entidade.getPointHome().getLongitude(), entidade.getPointHome().getLatitude(), conn));
			pstm.setString(11, findPolygonOfPoint(entidade.getPointCentroid().getLongitude(), entidade.getPointCentroid().getLatitude(), conn));
			pstm.executeUpdate();
		}
		Conexao.close(null, pstm, null);

	}

	private String findPolygonOfPoint(Double longitude, Double latitude, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT code FROM social_data WHERE st_contains(social_data.geom, ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326)) = TRUE";
		pstm = conn.prepareStatement(sql);
		pstm.setDouble(1, longitude);
		pstm.setDouble(2, latitude);
		rs = pstm.executeQuery();
		String code;
		if(rs.next()){
			code = rs.getString("code");
		}else{
			code = null;
		}
		return code;
	}

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<User> findAll() {
		// TODO
		return null;
	}

	public List<User> findAllUsersGeoTweet() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM geo_tweets_users ORDER BY user_id";
//		 String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				userList.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}
	
	public List<User> findAllUsersGeoTweetComp() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "select * from geo_tweets_users order by user_id";
//		 String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				userList.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}

	public List<User> findAllSelectedUsers() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT user_id, num_messages, radius_of_gyration, total_displacement, displacement_id,"
				+ " ST_AsText(geom_home_point) AS home, ST_AsText(geom_centroid_point) AS centroid, home_social_data_code,"
				+ " centroid_social_data_code FROM geo_tweets_users_selected" + " ORDER BY user_id";
		// String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			int count = 0;
			List<Displacement> listDisplacement = displacementService.findAll();
			while (rs.next()) {
				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				user.setDisplacement(findDisplacementById(rs.getLong("displacement_id"), listDisplacement));
				user.setNum_messages(rs.getInt("num_messages"));
				user.setPointCentroid(Util.textToPoint(rs.getString("centroid")));
				user.setPointHome(Util.textToPoint(rs.getString("home")));
				user.setRadiusOfGyration(rs.getDouble("radius_of_gyration"));
				user.setTweetList(tweetDAO.findTweetsByUser(user.getUser_id()));
				user.setUser_movement(rs.getDouble("total_displacement"));
				user.setHomePolygonCode(rs.getString("home_social_data_code"));
				user.setCentroidPolygonCode(rs.getString("centroid_social_data_code"));
				userList.add(user);
				System.out.println(++count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}

	public List<User> findAllSelectedUsersWithoutMessages() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT user_id, num_messages, radius_of_gyration, total_displacement, displacement_id,"
				+ " ST_AsText(geom_home_point) AS home, ST_AsText(geom_centroid_point) AS centroid, home_social_data_code,"
				+ " centroid_social_data_code FROM geo_tweets_users_selected" + " ORDER BY user_id";
		// String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			int count = 0;
			List<Displacement> listDisplacement = displacementService.findAll();
			while (rs.next()) {

				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				user.setDisplacement(findDisplacementById(rs.getLong("displacement_id"), listDisplacement));
				user.setNum_messages(rs.getInt("num_messages"));
				user.setPointCentroid(Util.textToPoint(rs.getString("centroid")));
				user.setPointHome(Util.textToPoint(rs.getString("home")));
				user.setRadiusOfGyration(rs.getDouble("radius_of_gyration"));
				// user.setTweetList(tweetDAO.findTweetsByUser(user.getUser_id()));
				user.setUser_movement(rs.getDouble("total_displacement"));
				user.setHomePolygonCode(rs.getString("home_social_data_code"));
				user.setCentroidPolygonCode(rs.getString("centroid_social_data_code"));
				userList.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}

	public List<User> findAllSelectedUsersWithoutMessages(int numMessages) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT user_id, num_messages, radius_of_gyration, total_displacement, displacement_id,"
				+ " ST_AsText(geom_home_point) AS home, ST_AsText(geom_centroid_point) AS centroid, home_social_data_code,"
				+ " centroid_social_data_code FROM geo_tweets_users_selected" + " ORDER BY user_id";
		// String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			int count = 0;
			List<Displacement> listDisplacement = displacementService.findAll();
			while (rs.next()) {

				if (rs.getInt("num_messages") >= numMessages) {
					user = new User(new ArrayList<Tweet>());
					user.setUser_id(rs.getLong("user_id"));
					user.setDisplacement(findDisplacementById(rs.getLong("displacement_id"), listDisplacement));
					user.setNum_messages(rs.getInt("num_messages"));
					user.setPointCentroid(Util.textToPoint(rs.getString("centroid")));
					user.setPointHome(Util.textToPoint(rs.getString("home")));
					user.setRadiusOfGyration(rs.getDouble("radius_of_gyration"));
					// user.setTweetList(tweetDAO.findTweetsByUser(user.getUser_id()));
					user.setUser_movement(rs.getDouble("total_displacement"));
					user.setHomePolygonCode(rs.getString("home_social_data_code"));
					user.setCentroidPolygonCode(rs.getString("centroid_social_data_code"));
					userList.add(user);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}

	public List<User> findAllSelectedUsers(int numMessages) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT user_id, num_messages, radius_of_gyration, total_displacement, displacement_id,"
				+ " ST_AsText(geom_home_point) AS home, ST_AsText(geom_centroid_point) AS centroid, home_social_data_code,"
				+ " centroid_social_data_code FROM geo_tweets_users_selected WHERE num_messages >= ? ORDER BY user_id";
		// String sql = "select * from auxiliar order by user_id";
		List<User> userList = new ArrayList<User>();
		User user = null;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setInt(1, numMessages);
			rs = pstm.executeQuery();
			int count = 0;
			List<Displacement> listDisplacement = displacementService.findAll();
			while (rs.next()) {

				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				user.setDisplacement(findDisplacementById(rs.getLong("displacement_id"), listDisplacement));
				user.setNum_messages(rs.getInt("num_messages"));
				user.setPointCentroid(Util.textToPoint(rs.getString("centroid")));
				user.setPointHome(Util.textToPoint(rs.getString("home")));  //eh p ficar descomentado!
				user.setRadiusOfGyration(rs.getDouble("radius_of_gyration"));
				user.setTweetList(tweetDAO.findTweetsByUser(user.getUser_id()));
				user.setUser_movement(rs.getDouble("total_displacement"));
				user.setHomePolygonCode(rs.getString("home_social_data_code")); //eh p ficar descomentado!!
				user.setCentroidPolygonCode(rs.getString("centroid_social_data_code"));
				userList.add(user);
				System.out.println(++count);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return userList;
	}

	private Displacement findDisplacementById(Long id, List<Displacement> listDisplacement) {
		for (Displacement disp : listDisplacement) {
			if (disp.getId().equals(id)) {
				return disp;
			}
		}
		return null;
	}

	public Point findUserCentroid(Long userId) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Point centroid = null;
		String sql = "select * from calculate_centroid(?)";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, userId);
			rs = pstm.executeQuery();

			if (rs.next()) {
				String point = rs.getString("st_astext");
				centroid = Util.textToPoint(point);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return centroid;

	}

	public User findUserById(Long userId) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		User user = null;
		String sql = "SELECT user_id, num_messages, radius_of_gyration, total_displacement, displacement_id,"
				+ " ST_AsText(geom_home_point) AS home, ST_AsText(geom_centroid_point) AS centroid, home_social_data_code,"
				+ " centroid_social_data_code FROM geo_tweets_users_selected" + " WHERE user_id = ?"
				+ " ORDER BY user_id";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, userId);
			rs = pstm.executeQuery();

			if (rs.next()) {
				user = new User(new ArrayList<Tweet>());
				user.setUser_id(rs.getLong("user_id"));
				user.setDisplacement(displacementService.findById(rs.getLong("displacement_id")));
				user.setNum_messages(rs.getInt("num_messages"));
				user.setPointCentroid(Util.textToPoint(rs.getString("centroid")));
				user.setPointHome(Util.textToPoint(rs.getString("home")));  
				user.setRadiusOfGyration(rs.getDouble("radius_of_gyration"));
				user.setTweetList(tweetDAO.findTweetsByUser(user.getUser_id()));
				user.setUser_movement(rs.getDouble("total_displacement"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return user;

	}

	public void updateUserHomePoint(Long user_id, Point point) {
		if(user_id.equals(new Long(149667567))){
			System.out.println();
		}
		
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
        String sql = "UPDATE geo_tweets_users_selected SET geom_home_point = ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326) WHERE user_id = ?";
        try {
        	
        	conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);
			pstm.setDouble(1, point.getLongitude());
			pstm.setDouble(2, point.getLatitude());
			pstm.setLong(3, user_id);
			pstm.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back in ID: " + user_id);
					e.printStackTrace();
					conn.rollback();
				} catch (SQLException excep) {
					System.err.print("Transaction could not be rolled back in ID: " + user_id);
					excep.printStackTrace();
				}
			}
		}finally {
			Conexao.close(conn, pstm, null);
		}
		
	}

}
