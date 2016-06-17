package mobility.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mobility.connection.Conexao;
import mobility.core.Displacement;
import mobility.core.DisplacementPerDay;
import mobility.core.DistanceDisplacement;

public class DisplacementPerDayDAO implements IDAO<DisplacementPerDay> {
	
	private DisplacementDAO displacementDAO = new DisplacementDAO();

	@Override
	public void save(DisplacementPerDay entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO displacement_per_day (displacement_id, displacement_per_day, date) "
        		+ "VALUES (?, ?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setLong(1, entidade.getDisplacement().getId());
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
			pstm.setLong(1, entidade.getDisplacement().getId());
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
	
	public DisplacementPerDay findById(Long id){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		DisplacementPerDay displacement = null;
		String sql = "select * from displacement_per_day where id = ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, id);
			rs = pstm.executeQuery();
			
			if(rs.next()){
				displacement = new DisplacementPerDay();
				displacement.setDate(rs.getTimestamp("date"));
				displacement.setDisplacement(displacementDAO.findById(rs.getLong("displacement_id")));
				displacement.setDisplacementPerDay(rs.getInt("displacement_per_day"));
				displacement.setId(rs.getLong("id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            Conexao.close(conn, pstm, rs);
        }
		return displacement;
	}

}
