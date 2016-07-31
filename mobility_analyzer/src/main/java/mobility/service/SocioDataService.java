package mobility.service;

import java.util.ArrayList;
import java.util.List;

import mobility.DAO.SocialDataDAO;
import mobility.core.Point;
import mobility.core.SocioData;

public class SocioDataService {
	private SocialDataDAO socioDAO = new SocialDataDAO();
	
	
	public SocioData findByCode(String code){
		return socioDAO.findByCode(code);
	}
	
	public List<SocioData> findAll(){
		return socioDAO.findAll();
	}
	
	public String findPolygonCodeOfPoint(Point point){
		return socioDAO.findPolygonCodeOfPoint(point);
	}
	
	public ArrayList<ArrayList<String>> findAllMatrix(){
		return socioDAO.findAllMatrix();
	}
	
	public ArrayList<String> findValueFromCoords(List<String> columnsList, Point point){
		return socioDAO.findValueFromCoords(columnsList, point);
	}

}
