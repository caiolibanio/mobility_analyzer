package mobility.DAO;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import mobility.connection.Conexao;
import mobility.core.Point;
import mobility.core.Tweet;
import mobility.core.User;

public class UserDAO implements IDAO<User> {

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
			pstm.setLong(7, entidade.getDisplacementId());
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
	
	public void saveUsers(List<User> users){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		String sql = "INSERT INTO geo_tweets_users_selected (user_id, home_point_id,"
				+ " num_messages, radius_of_gyration, total_displacement, centroid_point_id, displacement_id "
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?)";
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);
			
			for(User entidade : users){
				pstm.setLong(1, entidade.getUser_id());
				pstm.setDouble(2, entidade.getPointHome().getId());
				pstm.setLong(3, entidade.getNum_messages());
				pstm.setDouble(4, entidade.getRadiusOfGyration());
				pstm.setDouble(5, entidade.getUser_movement());
				pstm.setDouble(6, entidade.getPointCentroid().getId());
				pstm.setLong(7, entidade.getDisplacementId());
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
		}finally {
			Conexao.close(conn, pstm, null);
		}
	}
	
	public void saveUsers(List<User> users, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		String sql = "INSERT INTO geo_tweets_users_selected (user_id, home_point_id,"
				+ " num_messages, radius_of_gyration, total_displacement, centroid_point_id, displacement_id "
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?)";

		pstm = conn.prepareStatement(sql);

		for (User entidade : users) {
			pstm.setLong(1, entidade.getUser_id());
			pstm.setDouble(2, entidade.getPointHome().getId());
			pstm.setLong(3, entidade.getNum_messages());
			pstm.setDouble(4, entidade.getRadiusOfGyration());
			pstm.setDouble(5, entidade.getUser_movement());
			pstm.setDouble(6, entidade.getPointCentroid().getId());
			pstm.setLong(7, entidade.getDisplacementId());
			pstm.executeUpdate();
		}
		Conexao.close(null, pstm, null);

	}

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<User> findAll() {
		//TODO
		return null;
	}
	
	public List<User> findAllUsersGeoTweet() {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        ResultSet rs = null;
//        String sql = "select * from geo_tweets_users order by user_id";
        String sql = "select * from auxiliar order by user_id";
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

	public Point findUserCentroid(Long userId){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Point centroid = null;
		String sql = "select * from calculate_centroid(?)";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, userId);
			rs = pstm.executeQuery();
			
			if(rs.next()){
				String point = rs.getString("st_astext");
				point = point.replace("POINT(", "");
				point = point.replace(")", "");
				String[] coords = point.split(" ");
				centroid = new Point(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            Conexao.close(conn, pstm, rs);
        }
		return centroid;
		
	}

	
	
	

}
