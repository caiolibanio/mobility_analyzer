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
		listUsers.addAll(userService.findAllSelectedUsers(500));
	}
	
	public void findMuiltiCorrelationTotalDistanceByDate(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			if(code != null){ //verificar ponto fora de londres
				listX.add(user.getUser_movement());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
		
	}
	
	public void findMuiltiCorrelationTotalDistance(String method, String locationBased){
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
			if(code != null){ //verificar ponto fora de londres
				listX.add(user.getUser_movement());
				fillSocialDataMatrixByCode(code, matrixY);
			}
			
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
	}
	
	public void findMuiltiCorrelationRadius(String method, String locationBased){
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
			if(code != null){ //verificar ponto fora de londres
				listX.add(user.getRadiusOfGyration());
				fillSocialDataMatrixByCode(code, matrixY);
			}
			
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Radius_of_gyration", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
	}
	
	public void findMuiltiCorrelationNumMessages(String method, String locationBased){
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
			if(code != null){ //verificar ponto fora de londres
				listX.add((double)user.getNum_messages());
				fillSocialDataMatrixByCode(code, matrixY);
			}
			
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Number_of_messages", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
	}
	
	public void findMuiltiCorrelationDisplacementsPerDay(String method, String locationBased){
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
			if(code != null){ //verificar ponto fora de londres
				listX.add((double)user.getDisplacement().getDisplacementPerDayMedian());
				fillSocialDataMatrixByCode(code, matrixY);
			}
			
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Displacement_per_day", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
	}
	
	public void findMuiltiCorrelationNumberOfDisplacements(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			String c1 = user.getHomePolygonCode();
			String c2 = user.getCentroidPolygonCode();
			
			if(c1 != null && c2 != null){ //verificar ponto fora de londres
				listX.add((double)user.getDisplacement().getDisplacementCounter());
				fillSocialDataMatrixByCode(c1, matrixY);
			}
			
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Number_of_Displacements", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix);
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
		String values = "total displacement" + System.lineSeparator();
		
		for (int i = 0; i < listX.size(); i++) {
			matrix[i][0] = listX.get(i);
			values += matrix[i][0] + System.lineSeparator();

		}
		
		values += "---" + System.lineSeparator();


		String valZ = null;
		for(int i = 0; i < columnMatrix.size(); i++){
			valZ = columnMatrix.get(i).remove(0);
			values += valZ + System.lineSeparator();
			
			for(int j = 0; j < columnMatrix.get(i).size(); j++){
				matrix[j][i+1] = Double.valueOf(columnMatrix.get(i).get(j));
				values += matrix[j][i+1] + System.lineSeparator();
			}
			values += "---" + System.lineSeparator();
			
		}
//		writeOnFile(values);
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
			corr.initData();
			corr.findMuiltiCorrelationTotalDistanceByDate("kendall", "home");
		}

		
	}

}
