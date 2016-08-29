package mobility.statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import mobility.core.SocioData;
import mobility.core.User;
import mobility.service.SocioDataService;
import mobility.service.UserService;
import mobility.socioparser.ReadWriteExcelFile;
import mobility.util.Util;

public class LinearRegressionCalculator {
	
	private CorrelationMethods correlationMethods = new CorrelationMethods();
	
	private UserService userService = new UserService();
	
	private SocioDataService socioDataService = new SocioDataService();
	
	private List<User> listUsers;
	
	private ArrayList<ArrayList<String>> matrixSocialData = null;
	
	private List<String> columnsToIgnore = new ArrayList<String>();
	
	private ReadWriteExcelFile excelHandler = new ReadWriteExcelFile();
	
	private ArrayList<ArrayList<String>> resultCombinations = new ArrayList<ArrayList<String>>();
	
	private List<Double> allAdjRSquared = new ArrayList<Double>();
	
	public void initData(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		matrixSocialData = socioDataService.findAllMatrix();
		listUsers.addAll(userService.findAllSelectedUsersWithoutMessages(5500));
	}
	
	public void analyseMultipleRegressions(String locationBased, String outputFileName){
		String code = null;
		List<Double> listRadius = new ArrayList<Double>();
		List<Double> listTotalMovement = new ArrayList<Double>();
		List<Integer> listNumberOfMessages = new ArrayList<Integer>();
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
				
				listRadius.add(user.getRadiusOfGyration());
				listTotalMovement.add(user.getUser_movement());
				listNumberOfMessages.add(user.getNum_messages());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		columnMatrix = createColimnMatrix(matrixY);
//		generateCombinations();
		calculateMultipleRegression(listRadius, columnMatrix, outputFileName);
		Collections.sort(allAdjRSquared);
		System.out.println("Maior R-squared ajustado: " + allAdjRSquared.get(allAdjRSquared.size() - 1));
		
	}
	
	private void calculateMultipleRegression(List<Double> listRadius, ArrayList<ArrayList<String>> columnMatrix, String outputFileName) {
		List<Double> regressionList = new ArrayList<Double>();
		Set<String> combinations = new HashSet<String>();
		
		for(ArrayList<String> combList : resultCombinations){
//			regressionList = generateRegressionList(listRadius, columnMatrix, combinations);
			for(String c : combList){
				if(c.length() > 1){
					combinations.add(c);
				}
			}
		}
		
		String outPutLine = "";
		for(String comb : combinations){
			Double adjRSquared = calculateRegressionAdjRSquared(listRadius, columnMatrix, comb);
			allAdjRSquared.add(adjRSquared);
			outPutLine += comb + ": " + adjRSquared + System.lineSeparator();
		}
		
		Util.writeOnFile(outPutLine, outputFileName);
		

		
	}

	private Double calculateRegressionAdjRSquared(List<Double> listMobility, ArrayList<ArrayList<String>> columnMatrix,
			String combination) {
		List<List<Double>> regresionMatrix = new ArrayList<List<Double>>();
		regresionMatrix.add(listMobility);
		String[] columnsFromCombination = combination.split(" ");
		
		for(String col : columnsFromCombination){
			List<Double> values = findValuesFromColumnMatrix(col, columnMatrix);
			regresionMatrix.add(values);
		}
		int numberOfColumns = regresionMatrix.get(0).size();
		int numberOfLines = regresionMatrix.size();
		List<Double> regressionList = new ArrayList<Double>();
		
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 0; row < numberOfLines; row++){
				regressionList.add(regresionMatrix.get(row).get(col));
			}
		}
		
		int observations = listMobility.size();
		int variables = regresionMatrix.size() - 1;
		Double adjRSquared = calculateAdjRSquared(regressionList, observations, variables);
		
		return adjRSquared;
		
	}

	

	private Double calculateAdjRSquared(List<Double> regressionList, int observations, int variables) {
		OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
		double[] data = new double[regressionList.size()];
		for(int i = 0; i < regressionList.size(); i++){
			data[i] = regressionList.get(i);
		}
		ols.newSampleData(data, observations, variables);
		Double adjRSquared = ols.calculateAdjustedRSquared();
		
		return adjRSquared;
		
	}

	private List<Double> findValuesFromColumnMatrix(String columnName, ArrayList<ArrayList<String>> columnMatrix) {
		List<Double> vals = new ArrayList<Double>();
		for(ArrayList<String> list : columnMatrix){
			if(list.get(0).equals(columnName)){
				 for(int i = 1; i < list.size(); i++){
					 vals.add(Double.parseDouble(list.get(i)));
				 }
				 return vals;
			}
			
		}
		return null;
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
	
	private void fillSocialDataMatrixByCode(String code, ArrayList<ArrayList<String>> matrixY) {
		int codeIndex = findCodeIndex();
		for(ArrayList<String> list : matrixSocialData){
			if(list.get(codeIndex).equals(code)){
				matrixY.add(list);
				break;
			}
		}
		
		
	}
	
	private int findCodeIndex() {
		for(int i = 0; i < matrixSocialData.get(0).size(); i++){
			if(matrixSocialData.get(0).get(i).equals("code")){
				return i;
				
			}
		}
		return -1;
	}
	
	private void combinations(String[] arr, int len, int startPosition, String[] result){
	    if (len == 0){
	        String str = "";
	        ArrayList<String> listComb = new ArrayList<String>();
	        for(String card : result)
	        {
	            str += card + " ";
	            listComb.add(str.trim());
	        }
//	        System.out.println(str);
	        resultCombinations.add(listComb);
	        return;
	    }       
	    for (int i = startPosition; i <= arr.length-len; i++){
	        result[result.length - len] = arr[i];
	        combinations(arr, len-1, i+1, result);
	    }
	}
	
	private void combinationsIt(String[] arr, int len, int startPosition, String[] result){
//	    if (len == 0){
//	        String str = "";
//	        ArrayList<String> listComb = new ArrayList<String>();
//	        for(String card : result)
//	        {
//	            str += card + " ";
//	            listComb.add(str.trim());
//	        }
//	        System.out.println(str);
////	        resultCombinations.add(listComb);
//	        return;
//	    }       
	    for (int i = startPosition; i <= arr.length-len; i++){
	        result[result.length - len] = arr[i];
//	        combinationsIt(arr, len-1, i+1, result);
	        int lenIn = len - 1;
	        len -= 1;
	        int iIn = i + 1;
	        i += 1;
	        
	        if (len == 0){
    	        String str = "";
    	        ArrayList<String> listComb = new ArrayList<String>();
    	        for(String card : result)
    	        {
    	            str += card + " ";
    	            listComb.add(str.trim());
    	        }
    	        System.out.println(str);
//    	        resultCombinations.add(listComb);
    	        continue;
    	    }else{
    	        for (int j = iIn ; j <= arr.length-lenIn; j++){
    	        	result[result.length - lenIn] = arr[j];
    	        	lenIn -= 1;
    	        	
    	        	
    	        	if (lenIn == 0){
    	    	        String str = "";
    	    	        ArrayList<String> listComb = new ArrayList<String>();
    	    	        for(String card : result)
    	    	        {
    	    	            str += card + " ";
    	    	            listComb.add(str.trim());
    	    	        }
    	    	        System.out.println(str);
//    	    	        resultCombinations.add(listComb);
    	    	        break;
    	    	    }       
    	        	
    	        }
    	    }
	        

	    }
	}
	
	public void readCombinationsFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		ArrayList<String> combList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			line = line.replace("(", "");
			line = line.replace(",)", "");
			line = line.replace(")", "");
			line = line.replace(",", "");
			line = line.replace("'", "");
			
			combList.add(line);

			
		}
		resultCombinations.add(combList);

		br.close();
	}
	
	public static class Teste{
		public static void main(String args[]){
			LinearRegressionCalculator linear = new LinearRegressionCalculator();
			try {
				linear.readCombinationsFromFile(new File("combinations.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			linear.initData();
			linear.analyseMultipleRegressions("home", "multRegressionAdjRSquared");
			
		}
	}

}
