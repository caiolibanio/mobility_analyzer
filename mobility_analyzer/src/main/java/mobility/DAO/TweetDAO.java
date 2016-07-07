package mobility.DAO;

import java.util.ArrayList;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.DistanceDisplacement;
import mobility.core.Tweet;
import java.sql.*;

public class TweetDAO implements IDAO<Tweet> {

	@Override
	public void save(Tweet entidade) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Tweet> findAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Tweet> findTweetsByUser(Long userId){
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        String sql = "SELECT tid, longitude, latitude, date, user_id FROM geo_tweets WHERE user_id = ?";

        List<Tweet> tweetList = new ArrayList<Tweet>();

        try {
            pstm = conn.prepareStatement(sql);
            pstm.setLong(1, userId);
            rs = pstm.executeQuery();
            while (rs.next()) {
            	Long tid = rs.getLong("tid");
//				String json = rs.getString("json");
				Double lon = rs.getDouble("longitude");
				Double lat = rs.getDouble("latitude");
				Timestamp time = rs.getTimestamp("date");
				Tweet tweet = new Tweet(tid, "");
				tweet.setLongitude(lon);
				tweet.setLatitude(lat);
				tweet.setDate(time);
				tweet.setUser_id(rs.getLong("user_id"));
				tweet.setMessage("");

				tweetList.add(tweet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conexao.close(conn, pstm, rs);
        }
        return tweetList;
	}

}
