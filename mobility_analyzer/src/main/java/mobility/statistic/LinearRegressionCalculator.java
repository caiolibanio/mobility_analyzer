package mobility.statistic;

import java.util.ArrayList;
import java.util.List;

import mobility.core.SocioData;
import mobility.core.User;
import mobility.service.SocioDataService;
import mobility.service.UserService;
import mobility.socioparser.ReadWriteExcelFile;

public class LinearRegressionCalculator {
	
	private CorrelationMethods correlationMethods = new CorrelationMethods();
	
	private UserService userService = new UserService();
	
	private SocioDataService socioDataService = new SocioDataService();
	
	private List<User> listUsers;
	
	private ArrayList<ArrayList<String>> matrixSocialData = null;
	
	private List<String> columnsToIgnore = new ArrayList<String>();
	
	private ReadWriteExcelFile excelHandler = new ReadWriteExcelFile();
	
	private ArrayList<ArrayList<String>> resultCombinations = new ArrayList<ArrayList<String>>();
	
	public void initData(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		matrixSocialData = socioDataService.findAllMatrix();
		listUsers.addAll(userService.findAllSelectedUsers(2500));
	}
	
	public String analyseMultipleRegressions(String locationBased, String outputFileName){
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
		generateCombinations();
		calculateMultipleRegression(listRadius, columnMatrix);
		
		
		return null;
		
	}
	
	private void calculateMultipleRegression(List<Double> listRadius, ArrayList<ArrayList<String>> columnMatrix) {
		List<Double> regressionList = new ArrayList<Double>();
		
		for(ArrayList<String> combinations : resultCombinations){
			regressionList = generateRegressionList(listRadius, columnMatrix, combinations);
		}
		
//		regressionList = generateRegressionList(listRadius, columnMatrix, combinations);
		
	}

	private List<Double> generateRegressionList(List<Double> listRadius, ArrayList<ArrayList<String>> columnMatrix,
			List<String> combinations) {
		List<Double> values = findValuesFromColumnMatrix("", columnMatrix);
		
		generateList(combinations, listRadius, columnMatrix);
		
		return null;
		
	}

	private void generateList(List<String> listCombinations, List<Double> listRadius,
			ArrayList<ArrayList<String>> columnMatrix) {
		
		
		
	}

	private List<Double> findValuesFromColumnMatrix(String columnName, ArrayList<ArrayList<String>> columnMatrix) {
		List<Double> vals = new ArrayList<Double>();
		for(ArrayList<String> list : columnMatrix){
			if(list.get(0).equals(columnName)){
				 for(String valString : list){
					 vals.add(Double.parseDouble(valString));
				 }
			}
			return vals;
		}
		return vals;
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
//	            str += card + ", ";
	            listComb.add(card);
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
	
	private void generateCombinations(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		
		List<String> listColumns = socioDataService.findColumnNames();
		listColumns.removeAll(columnsToIgnore);
		String[] arr = new String[listColumns.size()];
		arr = listColumns.toArray(arr);
	    combinations(arr, 10, 0, new String[10]);
	    System.out.println(resultCombinations.size());
	}
	
	public static class Teste{
		public static void main(String args[]){
			LinearRegressionCalculator linear = new LinearRegressionCalculator();
			linear.generateCombinations();
			
			
		}
	}

}
