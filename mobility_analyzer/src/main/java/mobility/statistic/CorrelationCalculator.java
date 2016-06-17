package mobility.statistic;

import java.util.ArrayList;
import java.util.List;

import mobility.core.Point;
import mobility.core.SocioData;
import mobility.core.User;
import mobility.service.SocioDataService;
import mobility.service.UserService;

public class CorrelationCalculator {
	
	private CorrelationMethods correlationMethods = new CorrelationMethods();
	
	private UserService userService = new UserService();
	
	private SocioDataService socioDataService = new SocioDataService();
	
	private List<User> listUsers;
	
	private List<SocioData> listSocioData;
	
	
	
	public void initData(){
		listUsers = new ArrayList<User>();
		listSocioData = new ArrayList<SocioData>();
		listUsers.addAll(userService.findAllSelectedUsers());
		listSocioData.addAll(socioDataService.findAll());
	}
	
	public double findCorrelationRadiusXMedianPrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		
		
		
		for(User user : listUsers){
			home = user.getPointHome();
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar ponto fora de londres
				continue;
			}
			
			listX.add(user.getRadiusOfGyration());
			listY.add(findMedianPrice(code));
			
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		
		
		return corrCoef;
		
	}
	
	public double findCorrelationNumMessagesXMedianPrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			listX.add((double)user.getNum_messages());
			listY.add(findMedianPrice(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationTotalDistanceXMedianPrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			listX.add((double)user.getUser_movement());
			listY.add(findMedianPrice(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationTotalDistanceXPersonPerHectare(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			
			listX.add((double)user.getUser_movement());
			listY.add(findPersonPerHectareValue(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationTotalDistanceXEmploymentRate(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			
			listX.add((double)user.getUser_movement());
			listY.add(findEmploymentRate(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	
	public double findCorrelationTotalDistanceXNoQualifications(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			listX.add((double)user.getUser_movement());
			listY.add(findNoQualificationValue(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationTotalDistanceXUnemploymentRate(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			
			listX.add((double)user.getUser_movement());
			listY.add(findUnemploymentValue(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationTotalDistanceXEconomicActive(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			
			listX.add((double)user.getUser_movement());
			listY.add(findEconomicActive(code));
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
//		System.out.println("Size array: " + arrayDistance.length);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	

	public double findCorrelationUnemploymentXHousePrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			listX.add(findUnemploymentValue(code));
			listY.add(findMedianPrice(code));
			
		}
		double[] arrayX = listToArray(listX); 
		double[] arrayY = listToArray(listY);
		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	public double findCorrelationPersonsPerHectareXUnemploymentRate(String method){
		Point home = null;
		String code = null;
		List<Double> listY = new ArrayList<Double>();
		List<Double> listX = new ArrayList<Double>();
		for(User user : listUsers){
			home = user.getPointHome();
			
			
			code = socioDataService.findPolygonCodeOfPoint(home);
			
			if(code == null){ //verificar centroid fora de londres
				continue;
			}
			
			listX.add(findPersonPerHectareValue(code));
			listY.add(findUnemploymentValue(code));
			
		}
		double[] arrayX = listToArray(listX);
		double[] arrayY = listToArray(listY); 

		
		Double corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsCorrelationCoef(arrayX, arrayY);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsCorrelationCoef(arrayX, arrayY);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansCorrelationCoef(arrayX, arrayY);
		}
		return corrCoef;
	}
	
	private Double findEconomicActive(String code) {
		Double value = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				value = (double) data.getEconomically_active_total();
				break;
			}
		}
		return value;
	}

	private Double findUnemploymentValue(String code) {
		Double value = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				value = (double) data.getNo_qualifications();
				break;
			}
		}
		return value;
	}

	private Double findNoQualificationValue(String code) {
		Double value = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				value = (double) data.getNo_qualifications();
				break;
			}
		}
		return value;
	}

	private Double findEmploymentRate(String code) {
		Double employmentRate = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				employmentRate = data.getEmployment_rate();
				break;
			}
		}
		return employmentRate;
	}

	private Double findPersonPerHectareValue(String code) {
		Double personPerHectare = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				personPerHectare = data.getPersons_per_hectare();
				break;
			}
		}
		return personPerHectare;
	}

	private double[] listToArray(List<Double> listRadius) {
		double[] array = new double[listRadius.size()];
		for(int i = 0; i < listRadius.size(); i++){
			array[i] = listRadius.get(i);
		}
		return array;
	}

	private Double findMedianPrice(String code) {
		Double medianPrice = null;
		for(SocioData data : listSocioData){
			if(data.getCode().equals(code)){
				medianPrice = data.getMedian_price();
				break;
			}
		}
		return medianPrice;
	}
	
	public static class Teste{
		public static void main (String args[]){
			CorrelationCalculator corr = new CorrelationCalculator();
			corr.initData();
			
//			System.out.println("--------------Distancia Percorrida-----------------------");
//			System.out.println("Distancia Percorrida X Media de precos de imoveis: " + corr.findCorrelationTotalDistanceXMedianPrices("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas por hectare: " + corr.findCorrelationTotalDistanceXPersonPerHectare("kendall"));
//			System.out.println("Distancia Percorrida X Taxa de empregabilidade: " + corr.findCorrelationTotalDistanceXEmploymentRate("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas sem qualificacao: " + corr.findCorrelationTotalDistanceXNoQualifications("kendall"));
//			System.out.println("Distancia Percorrida X Taxa de desemprego: " + corr.findCorrelationTotalDistanceXUnemploymentRate("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas econ. ativas: " + corr.findCorrelationTotalDistanceXEconomicActive("kendall"));
			
//			System.out.println("---------------------Raio de giro----------------------------");
//			System.out.println("Raio de giro X Media de precos de imoveis: " + corr.findCorrelationRadiusXMedianPrices("kendall"));
//			
//			System.out.println("---------------------Numero de mensagens----------------------");
//			System.out.println("Num. de mensagens X Media de precos de imoveis: " + corr.findCorrelationNumMessagesXMedianPrices("kendall"));			
			
			System.out.println("Social X Social");
			System.out.println("Desemprego X Preco de imoveis: " + corr.findCorrelationUnemploymentXHousePrices("kendall"));
			System.out.println("Pess. por hectare X desemprego: " + corr.findCorrelationPersonsPerHectareXUnemploymentRate("kendall"));
			
		}
	}

}
