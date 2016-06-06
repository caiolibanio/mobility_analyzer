package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.DistanceDisplacement;
import mobility.core.Point;

public class PointDAO implements IDAO<Point> {

	@Override
	public void save(Point entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO point (latitude, longitude) "
        		+ "VALUES (?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, entidade.getLatitude());
            pstm.setDouble(2, entidade.getLongitude());
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
	
	public Long save(Point entidade, Connection conn) throws SQLException {
		PreparedStatement pstm = null;
		Long generatedId = null;
		String sql = "INSERT INTO point (latitude, longitude) " + "VALUES (?, ?)";

		pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		pstm.setDouble(1, entidade.getLatitude());
		pstm.setDouble(2, entidade.getLongitude());
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
	public List<Point> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
