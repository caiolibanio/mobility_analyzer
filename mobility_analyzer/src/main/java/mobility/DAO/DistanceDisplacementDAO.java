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
import mobility.util.Util;

public class DistanceDisplacementDAO implements IDAO<DistanceDisplacement> {
	
	private DisplacementDAO displacementDAO = new DisplacementDAO();
	
//	private PointDAO pointDAO = new PointDAO();

	@Override
	public void save(DistanceDisplacement entidade) {
		Connection conn = Conexao.open();
        PreparedStatement pstm = null;
        String sql = "INSERT INTO distance_displacement (distance_displacement, displacement_id, pointa_id, pointb_id) "
        		+ "VALUES (?, ?, ?, ?)";
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, entidade.getDistanceDisplacement());
            pstm.setLong(2, entidade.getDisplacement().getId());
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
			pstm.setLong(2, entidade.getDisplacement().getId());
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
	
	public DistanceDisplacement findById(Long id){
		Connection conn = Conexao.open();
		PreparedStatement pstm = null;
		ResultSet rs = null;
		DistanceDisplacement displacement = null;
		String sql = "SELECT id, distance_displacement, displacement_id, geom_a_point AS point_a,"
				+ " geom_b_point AS point_b FROM distance_displacement WHERE id = ?";
		try {
			pstm = conn.prepareStatement(sql);
			pstm.setLong(1, id);
			rs = pstm.executeQuery();
			
			if(rs.next()){
				displacement = new DistanceDisplacement();
				displacement.setDisplacement(displacementDAO.findById(rs.getLong("displacement_id")));
				displacement.setDistanceDisplacement(rs.getDouble("distance_displacement"));
				displacement.setId(rs.getLong("id"));
				displacement.setPointA(Util.textToPoint(rs.getString("point_a")));
				displacement.setPointB(Util.textToPoint(rs.getString("point_b")));
				
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
