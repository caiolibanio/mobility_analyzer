package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.ClusteredPoint;
import mobility.core.Displacement;
import mobility.core.User;

public class ClusteredPointDAO implements IDAO<ClusteredPoint> {

	
	public void saveClusteredPoints(List<ClusteredPoint> listPoints) {
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
        String sql = "INSERT INTO clusteres_points (user_id, message_point, cluster_number) "
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

	@Override
	public List<ClusteredPoint> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(ClusteredPoint entidade) {
		// TODO Auto-generated method stub
		
	}

}
