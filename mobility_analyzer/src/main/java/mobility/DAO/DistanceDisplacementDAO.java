package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.DisplacementPerDay;
import mobility.core.DistanceDisplacement;

public class DistanceDisplacementDAO implements IDAO<DistanceDisplacement> {

	@Override
	public void save(DistanceDisplacement entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO distance_displacement (distance_displacement, displacement_id, pointa_id, pointb_id) "
        		+ "VALUES (?, ?, ?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, entidade.getDistanceDisplacement());
            pstm.setLong(2, entidade.getDisplacement_id());
            pstm.setLong(3, entidade.getPointA().getId());
            pstm.setLong(4, entidade.getPointB().getId());
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
	
	public void save(List<DistanceDisplacement> displList, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		String sql = "INSERT INTO distance_displacement (distance_displacement, displacement_id, pointa_id, pointb_id) "
				+ "VALUES (?, ?, ?, ?)";

		pstm = conn.prepareStatement(sql);
		for(DistanceDisplacement entidade : displList){
			pstm.setDouble(1, entidade.getDistanceDisplacement());
			pstm.setLong(2, entidade.getDisplacement_id());
			pstm.setLong(3, entidade.getPointA().getId());
			pstm.setLong(4, entidade.getPointB().getId());
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
	public List<DistanceDisplacement> findAll() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
