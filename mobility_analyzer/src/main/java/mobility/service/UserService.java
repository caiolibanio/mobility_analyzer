package mobility.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import mobility.DAO.DisplacementDAO;
import mobility.DAO.DisplacementPerDayDAO;
import mobility.DAO.DistanceDisplacementDAO;
import mobility.DAO.PointDAO;
import mobility.DAO.UserDAO;
import mobility.connection.Conexao;
import mobility.core.DisplacementPerDay;
import mobility.core.DistanceDisplacement;
import mobility.core.Point;
import mobility.core.User;

public class UserService {
	
	private UserDAO userDAO = new UserDAO();
	
	private DisplacementDAO displacementDAO = new DisplacementDAO();
	
	private DisplacementPerDayDAO displacementPerDayDAO = new DisplacementPerDayDAO();
	
	private DistanceDisplacementDAO distanceDisplacementDAO = new DistanceDisplacementDAO();
	
	private PointDAO pointDAO = new PointDAO();

	public UserService() {
		super();
	}
	
	public void saveUsers(List<User> users){
		Connection conn = Conexao.open();
		try {
			conn.setAutoCommit(false);
			for(User user : users){
				Long idDisplacement = displacementDAO.save(user.getDisplacement(), conn);
				setDisplacementIdOnEntities(idDisplacement, user);
				displacementPerDayDAO.save(user.getDisplacement().getListDisplacementsPerDay(), conn);
				insertPoints(user, conn);
				distanceDisplacementDAO.save(user.getDisplacement().getListDistanceDisplacements(), conn);
			}
			userDAO.saveUsers(users, conn);
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
        } finally {
            Conexao.close(conn, null, null);
        }
	}
	
	private void insertPoints(User user, Connection conn) throws SQLException {
		for(DistanceDisplacement d :user.getDisplacement().getListDistanceDisplacements()){
			Long idPA = pointDAO.save(d.getPointA(), conn);
			d.getPointA().setId(idPA);
			Long idPB = pointDAO.save(d.getPointB(), conn);
			d.getPointB().setId(idPB);
		}
		Long idHome = pointDAO.save(user.getPointHome(), conn);
		Long idCentroid = pointDAO.save(user.getPointCentroid(), conn);
		user.getPointHome().setId(idHome);
		user.getPointCentroid().setId(idCentroid);
		
	}

	private void setDisplacementIdOnEntities(Long idDisplacement, User user) {
		user.getDisplacement().setId(idDisplacement);
		for(DisplacementPerDay d : user.getDisplacement().getListDisplacementsPerDay()){
			d.setDisplacement(user.getDisplacement());
		}
		for(DistanceDisplacement d : user.getDisplacement().getListDistanceDisplacements()){
			d.setDisplacement(user.getDisplacement());
		}
		
	}
	
	public List<User> findAllUsersGeoTweet(){
		return userDAO.findAllUsersGeoTweet();
	}
	
	public Point findUserCentroid(Long userId){
		return userDAO.findUserCentroid(userId);
	}
	
	public User findUserById(Long userId){
		return userDAO.findUserById(userId);
	}
	
	public List<User> findAllSelectedUsers(){
		return userDAO.findAllSelectedUsers();
	}
	

}
