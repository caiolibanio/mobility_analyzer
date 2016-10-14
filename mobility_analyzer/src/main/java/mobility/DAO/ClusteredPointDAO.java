package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.ClusteredCentroid;
import mobility.core.ClusteredPoint;
import mobility.core.Displacement;
import mobility.core.Point;
import mobility.core.Tweet;
import mobility.core.User;
import mobility.util.Util;

public class ClusteredPointDAO implements IDAO<ClusteredPoint> {

	
	public void saveClusteredPoints(List<ClusteredPoint> listPoints) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
        String sql = "INSERT INTO clustered_points (user_id, message_point, cluster_number) "
        		+ "VALUES (?, ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326), ?)";
        try {
        	conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);
			for (ClusteredPoint entidade : listPoints) {
				pstm.setLong(1, entidade.getUser_id());
				pstm.setDouble(2, entidade.getPointMessage().getLongitude());
				pstm.setDouble(3, entidade.getPointMessage().getLatitude());
				pstm.setLong(4, entidade.getClusterNumber());
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

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void saveClusteredCentroids(List<ClusteredCentroid> listPoints) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
        String sql = "INSERT INTO clustered_centroids (user_id, message_point, cluster_number, poi_name) "
        		+ "VALUES (?, ST_SetSRID(ST_MakePoint(?" + ", " + "?), 4326), ?, ?)";
        try {
        	conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);
			for (ClusteredCentroid entidade : listPoints) {
				pstm.setLong(1, entidade.getUser_id());
				pstm.setDouble(2, entidade.getPointMessage().getLongitude());
				pstm.setDouble(3, entidade.getPointMessage().getLatitude());
				pstm.setLong(4, entidade.getClusterNumber());
				pstm.setString(5, entidade.getPoiDescription());
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
	
	public List<Long> findAllUniqueUSersID(){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT DISTINCT user_id FROM clustered_points ORDER BY user_id";
//		 String sql = "select * from auxiliar order by user_id";
		List<Long> user_idList = new ArrayList<Long>();
		ClusteredPoint clusteredPoint = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				Long user_id = new Long(rs.getLong("user_id"));
				user_idList.add(user_id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return user_idList;
	}

	@Override
	public List<ClusteredPoint> findAll() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT user_id, ST_AsText(message_point) AS centroidPoint, cluster_number"
				+ " FROM clustered_points ORDER BY user_id";
//		 String sql = "select * from auxiliar order by user_id";
		List<ClusteredPoint> clusteredPointsList = new ArrayList<ClusteredPoint>();
		ClusteredPoint clusteredPoint = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				clusteredPoint = new ClusteredPoint(rs.getLong("user_id"),
						Util.textToPoint(rs.getString("centroidPoint")), rs.getInt("cluster_number"));
				clusteredPointsList.add(clusteredPoint);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return clusteredPointsList;
	}
	
	public List<ClusteredCentroid> findAllCentroids() {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT id, user_id, ST_AsText(message_point) AS centroidPoint, cluster_number, poi_description"
				+ " FROM clustered_centroids WHERE id > 975 ORDER BY id"; 

//		String sql = "SELECT id, user_id, ST_AsText(message_point) AS centroidPoint, cluster_number, poi_description"
//				+ " FROM clustered_centroids ORDER BY id"; //Este eh o correto!!!

		List<ClusteredCentroid> clusteredCentroidList = new ArrayList<ClusteredCentroid>();
		ClusteredCentroid clusteredCentroid = null;
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			while (rs.next()) {
				clusteredCentroid = new ClusteredCentroid(rs.getLong("user_id"),
						Util.textToPoint(rs.getString("centroidPoint")), rs.getInt("cluster_number"));
				clusteredCentroid.setPoiDescription(rs.getString("poi_description"));
				clusteredCentroid.setId(rs.getLong("id"));
				clusteredCentroidList.add(clusteredCentroid);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return clusteredCentroidList;
	}

	@Override
	public void save(ClusteredPoint entidade) {
		// TODO Auto-generated method stub
		
	}
	
	public int calculateNumberofClusters(Long user_id){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "select max(cluster_number) from clustered_points where user_id = ?";
//		 String sql = "select * from auxiliar order by user_id";
		int numberOfCLustersPerUser = 0;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, user_id);
			rs = pstm.executeQuery();
			
			if (rs.next()) {
				numberOfCLustersPerUser = rs.getInt("max");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return numberOfCLustersPerUser;
	}
	
	public Point calculateClusterCentroid(Long user_id, int clusterNumber){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		String sql = "SELECT calculate_centroid_clustered_points(?, ?) AS centroid";
//		 String sql = "select * from auxiliar order by user_id";
		Point centroidPoint = null;
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, user_id);
			pstm.setLong(2, clusterNumber);
			rs = pstm.executeQuery();
			
			if (rs.next()) {
				centroidPoint = Util.textToPoint(rs.getString("centroid"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Conexao.close(conn, pstm, rs);
		}
		return centroidPoint;
	}
	
	public void updateCentroidPOIName(Long id, String poiDescription){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
        String sql = "UPDATE clustered_centroids SET poi_description = ? WHERE id = ?";
        try {
        	conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, poiDescription);
			pstm.setLong(2, id);
			pstm.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					System.err.print("Transaction is being rolled back in ID: " + id);
					e.printStackTrace();
					conn.rollback();
				} catch (SQLException excep) {
					System.err.print("Transaction could not be rolled back in ID: " + id);
					excep.printStackTrace();
				}
			}
		}finally {
			Conexao.close(conn, pstm, null);
		}
	}

}