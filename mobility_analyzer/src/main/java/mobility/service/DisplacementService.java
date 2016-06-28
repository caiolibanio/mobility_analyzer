package mobility.service;

import java.util.List;

import mobility.DAO.DisplacementDAO;
import mobility.core.Displacement;

public class DisplacementService {
	
	private DisplacementDAO displacementDAO = new DisplacementDAO();
	
	
	public List<Displacement> findAll(){
		return displacementDAO.findAll();
	}
	
	public Displacement findById(Long id){
		return displacementDAO.findById(id);
	}

}
