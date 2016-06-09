package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.Displacement;
import mobility.core.DisplacementPerDay;

public class DisplacementPerDayDAO implements IDAO<DisplacementPerDay> {

	@Override
	public void save(DisplacementPerDay entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO displacement_per_day (displacement_id, displacement_per_day, date) "
        		+ "VALUES (?, ?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setLong(1, entidade.getDisplacement_id());
            pstm.setDouble(2, entidade.getDisplacementPerDay());
            pstm.setTimestamp(3, entidade.getDate());
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
	
	
	public void save(List<DisplacementPerDay> displList, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		String sql = "INSERT INTO displacement_per_day (displacement_id, displacement_per_day, date) "
				+ "VALUES (?, ?, ?)";
		pstm = conn.prepareStatement(sql);
		for(DisplacementPerDay entidade : displList){
			pstm.setLong(1, entidade.getDisplacement_id());
			pstm.setDouble(2, entidade.getDisplacementPerDay());
			pstm.setTimestamp(3, entidade.getDate());
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
	public List<DisplacementPerDay> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
