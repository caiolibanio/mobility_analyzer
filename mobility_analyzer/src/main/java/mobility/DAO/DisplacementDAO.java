package mobility.DAO;

import java.util.List;
import java.sql.*;

import mobility.connection.Conexao;
import mobility.core.Displacement;
import mobility.core.User;

public class DisplacementDAO implements IDAO<Displacement> {

	@Override
	public void save(Displacement entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO displacement (displacement_counter, displacement_per_day_median, distance_displacement_median) "
        		+ "VALUES (?, ?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setInt(1, entidade.getDisplacementCounter());
            pstm.setDouble(2, entidade.getDisplacementPerDayMedian());
            pstm.setDouble(3, entidade.getDistanceDisplacementMedian());
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
	
	
	public Long save(Displacement entidade, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		Long generatedId = null;
		String sql = "INSERT INTO displacement (displacement_counter, displacement_per_day_median, distance_displacement_median) "
				+ "VALUES (?, ?, ?)";

		pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		pstm.setInt(1, entidade.getDisplacementCounter());
		pstm.setDouble(2, entidade.getDisplacementPerDayMedian());
		pstm.setDouble(3, entidade.getDistanceDisplacementMedian());
		pstm.executeUpdate();

		ResultSet generatedKeys = pstm.getGeneratedKeys();
		if (generatedKeys.next()) {
			generatedId = generatedKeys.getLong(1);
		}

		Conexao.close(null, pstm, null);
		return generatedId;
	}
	

	@Override
	public int findMaxId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Displacement> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
