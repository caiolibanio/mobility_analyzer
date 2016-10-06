package mobility.DAO;

import java.util.ArrayList;
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
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Displacement displacement = null;
		List<Displacement> listDisplacement = new ArrayList<Displacement>();
		String sql = "select * from displacement";
		try {
			pstm = conn.prepareStatement(sql);
			rs = pstm.executeQuery();
			
			while(rs.next()){
				displacement = new Displacement();
				displacement.setId(rs.getLong("id"));
				displacement.setDisplacementCounter(rs.getInt("displacement_counter"));
				displacement.setDisplacementPerDayMedian(rs.getDouble("displacement_per_day_median"));
				displacement.setDistanceDisplacementMedian(rs.getDouble("distance_displacement_median"));
				listDisplacement.add(displacement);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            Conexao.close(conn, pstm, rs);
        }
		return listDisplacement;
	}
	
	public Displacement findById(Long id){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Displacement displacement = null;
		String sql = "select * from displacement where id = ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, id);
			rs = pstm.executeQuery();
			
			if(rs.next()){
				displacement = new Displacement();
				displacement.setId(rs.getLong("id"));
				displacement.setDisplacementCounter(rs.getInt("displacement_counter"));
				displacement.setDisplacementPerDayMedian(rs.getDouble("displacement_per_day_median"));
				displacement.setDistanceDisplacementMedian(rs.getDouble("distance_displacement_median"));
				displacement.setDistanceDisplacementMedian(rs.getDouble("distance_displacement_median"));
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
