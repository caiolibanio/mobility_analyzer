package mobility.statistic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;

import mobility.DAO.SocialDataDAO;
import mobility.core.Point;
import mobility.core.SocioData;
import mobility.core.User;
import mobility.service.SocioDataService;
import mobility.service.UserService;
import mobility.socioparser.ReadWriteExcelFile;

public class CorrelationCalculator {
	
	private CorrelationMethods correlationMethods = new CorrelationMethods();
	
	private UserService userService = new UserService();
	
	private SocioDataService socioDataService = new SocioDataService();
	
	private List<User> listUsers;
	
	private List<SocioData> listSocioData;
	
	private ArrayList<ArrayList<String>> matrixSocialData = null;
	
	private List<String> columnsToIgnore = new ArrayList<String>();
	
	private List<String> columnsLabels = new ArrayList<String>();
	
	private ReadWriteExcelFile excelHandler = new ReadWriteExcelFile();
	
	
	
	public void initData(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		listSocioData = new ArrayList<SocioData>();
		matrixSocialData = socioDataService.findAllMatrix();
		listUsers.addAll(userService.findAllSelectedUsers());
//		listSocioData.addAll(socioDataService.findAll());
	}
	
	public RealMatrix findMuiltiCorrelationTotalDistance(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add(user.getUser_movement());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
//		List<String> listY = columnMatrix.remove(2);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		return realMatrix;
	}
	
	public RealMatrix findMuiltiCorrelationRadius(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add(user.getRadiusOfGyration());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Radius_of_gyration", columnMatrix);
//		List<String> listY = columnMatrix.remove(2);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		return realMatrix;
	}
	
	public RealMatrix findMuiltiCorrelationNumMessages(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add((double)user.getNum_messages());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Number_of_messages", columnMatrix);
//		List<String> listY = columnMatrix.remove(2);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		return realMatrix;
	}
	
	public RealMatrix findMuiltiCorrelationDisplacementsPerDay(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add((double)user.getDisplacement().getDisplacementPerDayMedian());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Displacement_per_day", columnMatrix);
//		List<String> listY = columnMatrix.remove(2);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		return realMatrix;
	}
	
	public RealMatrix findMuiltiCorrelationNumberOfDisplacements(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add((double)user.getDisplacement().getDisplacementCounter());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Number_of_Displacements", columnMatrix);
//		List<String> listY = columnMatrix.remove(2);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		return realMatrix;
	}
	
	private void fillColumnLabels(String mobilityLabel, ArrayList<ArrayList<String>> columnMatrix) {
		columnsLabels.add(mobilityLabel);
		for(ArrayList<String> list : columnMatrix){
			columnsLabels.add(list.get(0));
		}
		
	}

	private RealMatrix calculateMultiCorrelationsFormated(List<Double> listX,
			ArrayList<ArrayList<String>> columnMatrix, String method) {
		double[][] matrix = new double[listX.size()][columnMatrix.size() + 1];
		List<RealMatrix> listRealMatrix = new ArrayList<RealMatrix>();
		int numColumns = 2;
//		String valY = listY.remove(0);
		String valZ = null;
		String values = "total displacement" + System.lineSeparator();
		
		System.out.println("Valores X");
		for (int i = 0; i < listX.size(); i++) {
			matrix[i][0] = listX.get(i);
			System.out.println(matrix[i][0]);
			values += matrix[i][0] + System.lineSeparator();

		}
		
		values += "---" + System.lineSeparator();

		System.out.println("Valores Z");
		for(int i = 0; i < columnMatrix.size(); i++){
			valZ = columnMatrix.get(i).remove(0);
			values += valZ + System.lineSeparator();
			
			for(int j = 0; j < columnMatrix.get(i).size(); j++){
				matrix[j][i+1] = Double.valueOf(columnMatrix.get(i).get(j));
				System.out.println(matrix[j][i+1]);
				values += matrix[j][i+1] + System.lineSeparator();
			}
			values += "---" + System.lineSeparator();
			
		}
		writeOnFile(values);
		RealMatrix realMatrix = calclateMultiCorrelations(matrix, method);
		return realMatrix;
		
	}
	
	public void writeOnFile(String text){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("values.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println(text);
		
		writer.close();
	
	}

	public List<String> findCorrelationTotalDistance(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add(user.getUser_movement());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		listAnsw = calculateCorrelationsFormated(listX, columnMatrix, method);
		return listAnsw;
	}
	
	public List<String> findCorrelationNumMessages(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add((double)user.getNum_messages());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		columnMatrix = createColimnMatrix(matrixY);
		listAnsw = calculateCorrelationsFormated(listX, columnMatrix, method);
		return listAnsw;
	}
	
	public List<String> findCorrelationRadius(String method, String locationBased, int numMessages){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		List<String> listAnsw = new ArrayList<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code == null || user.getNum_messages() < numMessages){ //verificar ponto fora de londres
				continue;
			}
			listX.add(user.getRadiusOfGyration());
			fillSocialDataMatrixByCode(code, matrixY);
		}
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		listAnsw = calculateCorrelationsFormated(listX, columnMatrix, method);
		return listAnsw;
	}
	
	private ArrayList<ArrayList<String>> createColimnMatrix(ArrayList<ArrayList<String>> matrixY) {
		int columnCount = matrixY.get(0).size();
		ArrayList<String> colValues = null;
		ArrayList<ArrayList<String>> matrixColumns = new ArrayList<ArrayList<String>>();
		for(int col = 0; col < columnCount; col++){
			
			colValues = new ArrayList<String>();
			for(int row = 0; row < matrixY.size(); row++){
				if(columnsToIgnore.contains(matrixY.get(0).get(col))){
					break;
				}else{
					colValues.add(matrixY.get(row).get(col));
				}
				
			}
			if(!colValues.isEmpty()){
				matrixColumns.add(colValues);
			}
			
		}
		return matrixColumns;
		
	}

	private List<String> calculateCorrelationsFormated(List<Double> listX, ArrayList<ArrayList<String>> columnMatrix, String method) {
		String formatedOut = null;
		double[] arrayX = listToArray(listX); 
		Double corrCoef = null;
		List<String> formatedOutList = new ArrayList<String>();
		for(ArrayList<String> line : columnMatrix){
			formatedOut = line.remove(0);
			if(columnsToIgnore.contains(formatedOut)){
				continue;
			}
			double[] arrayY = listStringToArray(line);
			corrCoef = calclateCorrelations(arrayX, arrayY, method);
			formatedOut += ": " + corrCoef;
			formatedOutList.add(formatedOut);
			
		}
		return formatedOutList;
		
	}

	private Double calclateCorrelations(double[] arrayX, double[] arrayY, String method) {
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
	
	private RealMatrix calclateMultiCorrelations(double[][] matrix, String method) {
		RealMatrix corrCoef = null;
		if(method.equals("kendall")){
			corrCoef = correlationMethods.findKendallsMultiCorrelationCoef(matrix);
		}else if(method.equals("pearson")){
			corrCoef = correlationMethods.findPearsonsMultiCorrelationCoef(matrix);
		}else if (method.equals("spearman")){
			corrCoef = correlationMethods.findSpearmansMultiCorrelationCoef(matrix);
		}
		return corrCoef;
	}

	private double[] listStringToArray(ArrayList<String> line) {
		double[] array = new double[line.size()];
		for(int i = 0; i < line.size(); i++){
			array[i] = Double.parseDouble(line.get(i));
		}
		return array;
	}

	private void fillSocialDataMatrixByCode(String code, ArrayList<ArrayList<String>> matrixY) {
		int codeIndex = findCodeIndex(code);
		for(ArrayList<String> list : matrixSocialData){
			if(list.get(codeIndex).equals(code)){
				matrixY.add(list);
				break;
			}
		}
		
		
	}

	private int findCodeIndex(String code) {
		for(int i = 0; i < matrixSocialData.get(0).size(); i++){
			if(matrixSocialData.get(0).get(i).equals("code")){
				return i;
				
			}
		}
		return -1;
	}

	public double findCorrelationRadiusXMedianPrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		
		
		
		for(User user : listUsers){
//			home = user.getPointHome();
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
	
	
	
	

	public double findCorrelationUnemploymentXHousePrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
	
	//---------------------DISTANCIA PERCORRIDA----------------------
	
	public double findCorrelationTotalDistanceXMedianPrices(String method){
		Point home = null;
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		for(User user : listUsers){
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
//			home = user.getPointHome();
			
			
			code = user.getHomePolygonCode();
			
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
				value = (double) data.getUnemployment_rate();
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
	
	private void printResultMatrix(RealMatrix matrixResults) {
		double[][] resultMatrix = matrixResults.getData();
		String horizontalLabels = "";
		for(String label : columnsLabels){
			horizontalLabels += label + " - ";
		}
		System.out.println(horizontalLabels);
		String verticalLabels = "";
		for(int row = 0; row < resultMatrix.length; row++){
			verticalLabels = columnsLabels.get(row) + " : ";
			for(int col = 0; col < resultMatrix[0].length; col++){
				verticalLabels += resultMatrix[row][col] + " - ";
			}
			System.out.println(verticalLabels);
		}
		
		
	}
	
	private void exportCorrelationResultMatrixToXLS(ArrayList<ArrayList<String>> matrixResult){
		try {
			excelHandler.writeXLSFileTableCorrelations(matrixResult);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<ArrayList<String>> formatMatrixToXLSFile(RealMatrix matrixResults){
		double[][] resultMatrix = matrixResults.getData();
		ArrayList<ArrayList<String>> matrixResultsString = new ArrayList<ArrayList<String>>();
		ArrayList<String> lineList = null;
		for(int row = 0; row < resultMatrix.length; row++){
			lineList = new ArrayList<String>();
			for(int col = 0; col < resultMatrix.length; col++){
				lineList.add(String.valueOf(resultMatrix[row][col]));
			}
			matrixResultsString.add(lineList);
		}
		
		for(int i = 0; i < matrixResultsString.size(); i++){
			matrixResultsString.get(i).add(0, columnsLabels.get(i));
		}
		
		ArrayList<String> labelsFormatted = new ArrayList<String>();
		labelsFormatted.add(" - ");
		labelsFormatted.addAll(columnsLabels);
		matrixResultsString.add(0, labelsFormatted);
		return matrixResultsString;
		
		
	}
	
	private void saveMultiCorrelationsToXLS(RealMatrix matrixResults){
		ArrayList<ArrayList<String>> matrixFormated = formatMatrixToXLSFile(matrixResults);
		exportCorrelationResultMatrixToXLS(matrixFormated);
		
		
	}
	
	public static class Teste{
		public static void main (String args[]){
			CorrelationCalculator corr = new CorrelationCalculator();
			SocialDataDAO d = new SocialDataDAO();
			
			
			corr.initData();
//			System.out.println(corr.findCorrelationTotalDistance("kendall", "home", 500));
//			System.out.println(corr.findCorrelationNumMessages("kendall", "home", 500));
//			System.out.println(corr.findCorrelationRadius("kendall", "home", 500));
			RealMatrix matrixResults = corr.findMuiltiCorrelationNumberOfDisplacements("kendall", "home", 500);
			corr.saveMultiCorrelationsToXLS(matrixResults);
//			corr.printResultMatrix(matrixResults);
			
			
//			System.out.println("--------------Distancia Percorrida-----------------------");
//			System.out.println("Distancia Percorrida X Media de precos de imoveis: " + corr.findCorrelationTotalDistanceXMedianPrices("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas por hectare: " + corr.findCorrelationTotalDistanceXPersonPerHectare("kendall"));
//			System.out.println("Distancia Percorrida X Taxa de empregabilidade: " + corr.findCorrelationTotalDistanceXEmploymentRate("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas sem qualificacao: " + corr.findCorrelationTotalDistanceXNoQualifications("kendall"));
//			System.out.println("Distancia Percorrida X Taxa de desemprego: " + corr.findCorrelationTotalDistanceXUnemploymentRate("kendall"));
//			System.out.println("Distancia Percorrida X Pessoas econ. ativas: " + corr.findCorrelationTotalDistanceXEconomicActive("kendall"));
//			
//			System.out.println("---------------------Raio de giro----------------------------");
//			System.out.println("Raio de giro X Media de precos de imoveis: " + corr.findCorrelationRadiusXMedianPrices("kendall"));
//			
//			System.out.println("---------------------Numero de mensagens----------------------");
//			System.out.println("Num. de mensagens X Media de precos de imoveis: " + corr.findCorrelationNumMessagesXMedianPrices("kendall"));			
//			
//			System.out.println("Social X Social");
//			System.out.println("Desemprego X Preco de imoveis: " + corr.findCorrelationUnemploymentXHousePrices("kendall"));
//			System.out.println("Pess. por hectare X desemprego: " + corr.findCorrelationPersonsPerHectareXUnemploymentRate("kendall"));
			
		}

		
	}

}
