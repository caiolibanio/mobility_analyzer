package mobility.statistic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import mobility.DAO.SocialDataDAO;
import mobility.core.DateTimeOperations;
import mobility.core.GeoCalculator;
import mobility.core.Point;
import mobility.core.SocioData;
import mobility.core.Tweet;
import mobility.core.User;
import mobility.dbscan.GeoDistance;
import mobility.service.SocioDataService;
import mobility.service.UserService;
import mobility.socioparser.ReadWriteExcelFile;

public class CorrelationCalculator {
	
	private CorrelationMethods correlationMethods = new CorrelationMethods();
	
	private UserService userService = new UserService();
	
	private SocioDataService socioDataService = new SocioDataService();
	
	private static List<User> listUsers;
	
	private List<SocioData> listSocioData;
	
	private ArrayList<ArrayList<String>> matrixSocialData = null;
	
	private List<String> columnsToIgnore = new ArrayList<String>();
	
	private List<String> columnsLabels = new ArrayList<String>();
	
	private ReadWriteExcelFile excelHandler = new ReadWriteExcelFile();
	
	private GeoCalculator geoCalculator = new GeoCalculator();
	
	private List<ClusteredUser> listClusteredUsers = new ArrayList<ClusteredUser>();
	
	
	
	
	public void initData(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		listSocioData = new ArrayList<SocioData>();
		matrixSocialData = socioDataService.findAllMatrix();
		listUsers.addAll(userService.findAllSelectedUsers(2500));
	}
	
	public void initDataToTest(ArrayList<ArrayList<String>> matrixSocialDataFromTest,
			List<User> listUsersFromTest){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		listSocioData = new ArrayList<SocioData>();
		matrixSocialData = matrixSocialDataFromTest;
		listUsers.addAll(listUsersFromTest);
	}
	
	public RealMatrix findMuiltiCorrelationAllToTest(String method, String locationBased, String outputFileName){
		String code = null;
		List<Double> listRadius = new ArrayList<Double>();
		List<Double> listTotalMovement = new ArrayList<Double>();
		List<Integer> listNumberOfMessages = new ArrayList<Integer>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		matrixY.add(matrixSocialData.get(0));
		int count = 1;
		for (User user : listUsers) {
			
			if(count == 1){
				code = "100";
			}else if(count == 2){
				code = "101";
			}else if(count == 3){
				code = "102";
			}else if(count == 4){
				code = "103";
			}
			++count;
			
			listRadius.add(user.getRadiusOfGyration());
			listTotalMovement.add(user.getUser_movement());
			listNumberOfMessages.add(user.getNum_messages());
			fillSocialDataMatrixByCode(code, matrixY);

		}

		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormatedTest(listRadius, listTotalMovement, listNumberOfMessages,columnMatrix, method, outputFileName);
		saveMultiCorrelationsToXLS(realMatrix, "MuiltiCorrelationAll");
		return realMatrix;
		
	}
	
	public void findMuiltiCorrelationAll(String method, String locationBased, String outputFileName){
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
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormatedTest(listRadius, listTotalMovement, listNumberOfMessages,columnMatrix, method, outputFileName);
		saveMultiCorrelationsToXLS(realMatrix, outputFileName);
		
	}
	
	public void findMuiltiCorrelationByActivitiesCenters(String method, String locationBased, String outPutFileName){
		String code = null;
		List<Double> listRadius = new ArrayList<Double>();
		List<Double> listTotalMovement = new ArrayList<Double>();
		List<Integer> listNumberOfMessages = new ArrayList<Integer>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		matrixY.add(matrixSocialData.get(0));
		
		for(String label : columnsToIgnore){
			matrixY.get(0).remove(label);   //remover labels ignorados
		}
		int count = 1;
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				
				List<DoublePoint> clusteredPoints = findClusteredPoints(user);
				System.out.println("Clusterizou: " + count);
				++count;
				if(clusteredPoints.size() > 0){
					listRadius.add(user.getRadiusOfGyration());
					listTotalMovement.add(user.getUser_movement());
					listNumberOfMessages.add(user.getNum_messages());
					fillSocialDataMatrixByActivityCenter(matrixY, clusteredPoints);
				}
				
				
			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormatedTest(listRadius, listTotalMovement, listNumberOfMessages,columnMatrix, method, outPutFileName);
		saveMultiCorrelationsToXLS(realMatrix, outPutFileName);
		
	}
	
	private void fillSocialDataMatrixByActivityCenter(ArrayList<ArrayList<String>> matrixY, List<DoublePoint> listOfPoints) {
//		int codeIndex = findCodeIndex(code);
//		for(ArrayList<String> list : matrixSocialData){
//			if(list.get(codeIndex).equals(code)){
//				matrixY.add(list);
//				break;
//			}
//		}
		matrixY.add(generateUserSocialValuesMedians(listOfPoints, matrixY.get(0)));
		
		
		
	}
	//Calc of medias
	private ArrayList<String> generateUserSocialValuesMedia(List<DoublePoint> listOfPoints, ArrayList<String> listOfLabels){
		ArrayList<String> listOfMedias = new ArrayList<String>();
		
		
			ArrayList<ArrayList<String>> listOfValues = new ArrayList<ArrayList<String>>();
			for(DoublePoint point : listOfPoints){
				double[] dPoint = point.getPoint();
				Point p = new Point(dPoint[0], dPoint[1]);
				listOfValues.add(socioDataService.findValueFromCoords(listOfLabels, p));
			}
			
			listOfMedias = calculateListOfMedias(listOfValues);
		
		return listOfMedias;
		
	}
	//Calc of medians
	private ArrayList<String> generateUserSocialValuesMedians(List<DoublePoint> listOfPoints, ArrayList<String> listOfLabels){
		ArrayList<String> listOfMedians = new ArrayList<String>();
		
		
			ArrayList<ArrayList<String>> listOfValues = new ArrayList<ArrayList<String>>();
			for(DoublePoint point : listOfPoints){
				double[] dPoint = point.getPoint();
				Point p = new Point(dPoint[0], dPoint[1]);
				listOfValues.add(socioDataService.findValueFromCoords(listOfLabels, p));
			}
			
			listOfMedians = calculateListOfMedians(listOfValues);
		
		return listOfMedians;
		
	}
	
	private ArrayList<String> calculateListOfMedias(ArrayList<ArrayList<String>> listOfValues) {
		ArrayList<String> listOfMedias = new ArrayList<String>();
		int numberOfColumns = listOfValues.get(0).size();
		int numberOfLines = listOfValues.size();
		Double value = 0.0;
		
		for(int col = 0; col < numberOfColumns; col++){
			for(int row = 0; row < numberOfLines; row++){
				value += Double.parseDouble(listOfValues.get(row).get(col));
			}
			Double media = value / numberOfLines;
			listOfMedias.add(String.valueOf(media));
		}
		return listOfMedias;
		
	}
	
	private ArrayList<String> calculateListOfMedians(ArrayList<ArrayList<String>> listOfValues) {
		ArrayList<String> listOfMedians = new ArrayList<String>();
		int numberOfColumns = listOfValues.get(0).size();
		int numberOfLines = listOfValues.size();
		
		for(int col = 0; col < numberOfColumns; col++){
			List<Double> values = new ArrayList<Double>();
			for(int row = 0; row < numberOfLines; row++){
				values.add(Double.parseDouble(listOfValues.get(row).get(col)));
			}
			Double median = calcMedian(values);
			listOfMedians.add(String.valueOf(median));
		}
		return listOfMedians;
		
	}
	
	private static Double calcMedian(List<Double> vals) {
		Collections.sort(vals);
		int mid = (vals.size() - 1) / 2;
		return vals.get(mid);

	}

	private List<DoublePoint> findClusteredPoints(User user) {
		List<DoublePoint> listOfPoints = new ArrayList<DoublePoint>();
		List<DoublePoint> points = formatPointsToClusterGeneral(user);
		List<Cluster<DoublePoint>> cluster = checkClusteredUser(user);
		
		if(cluster == null){
			cluster = clusteringPoints(points);
			ClusteredUser clusteredUser = new ClusteredUser(cluster, user.getUser_id());
			listClusteredUsers.add(clusteredUser);
		}
		ArrayList<ArrayList<DoublePoint>> listOfClusters = returnClustersList(cluster);
//		List<List<DoublePoint>> listOfClustersWithoutHome = removeHomeCluster(listOfClusters);
		
		for (ArrayList<DoublePoint> c : listOfClusters) {
			for (DoublePoint p : c) {
				if(!listOfPoints.contains(p)){
					listOfPoints.add(p);
				}
			}
		}
		return listOfPoints;
		
	}
	
	private List<Cluster<DoublePoint>> checkClusteredUser(User user) {
		
		for(ClusteredUser u : listClusteredUsers){
			if(u.getUser_id().equals(user.getUser_id())){
				return u.getCluster();
			}
		}
		return null;
	}

	private List<List<DoublePoint>> removeHomeCluster(List<List<DoublePoint>> listOfClusters) {
		List<DoublePoint> homeCluster = findBiggestCluster(listOfClusters);
		listOfClusters.remove(homeCluster);
		return listOfClusters;
	}
	
	private static List<DoublePoint> findBiggestCluster(List<List<DoublePoint>> listOfClusters) {
		int index = 0;
		for (int i = 0; i < listOfClusters.size(); i++) {
			if (listOfClusters.get(i).size() > index) {
				index = i;
			}
		}
		return listOfClusters.get(index);
	}

	private ArrayList<ArrayList<DoublePoint>> returnClustersList(List<Cluster<DoublePoint>> cluster) {
		ArrayList<ArrayList<DoublePoint>> listOfClusters = new ArrayList<ArrayList<DoublePoint>>();
		for (Cluster<DoublePoint> c : cluster) {
			ArrayList<DoublePoint> singleCluster = new ArrayList<DoublePoint>();
			for (DoublePoint p : c.getPoints()) {
				singleCluster.add(p);
			}
			listOfClusters.add(singleCluster);
		}
		return listOfClusters;
	}

	private static List<Cluster<DoublePoint>> clusteringPoints(List<DoublePoint> points) {
		DBSCANClusterer dbscan = new DBSCANClusterer(45.0, 4, new GeoDistance());
		List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);
		return cluster;
	}
	
	private static List<DoublePoint> formatPointsToCluster(User user) {

		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Tweet t : user.getTweetList()) {
			if (DateTimeOperations.isHomeTime(t)) {
				double[] d = new double[2];
				d[0] = t.getLatitude();
				d[1] = t.getLongitude();
				points.add(new DoublePoint(d));
			}
		}
		return points;
	}
	
	private static List<DoublePoint> formatPointsToClusterGeneral(User user) {

		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Tweet t : user.getTweetList()) {

			double[] d = new double[2];
			d[0] = t.getLatitude();
			d[1] = t.getLongitude();
			points.add(new DoublePoint(d));

		}
		return points;
	}
	
	private void fillColumnLabelsTest(String string, ArrayList<ArrayList<String>> columnMatrix) {
		columnsLabels.add("Radius");
		columnsLabels.add("Total_movement");
		columnsLabels.add("Number_of_messages");
		for(ArrayList<String> list : columnMatrix){
			columnsLabels.add(list.get(0));
		}
		
	}

	private RealMatrix calculateMultiCorrelationsFormatedTest(List<Double> listRadius, List<Double> listTotalMovement,
			List<Integer> listNumberOfMessages, ArrayList<ArrayList<String>> columnMatrix, String method, String outPutFileName) {
		double[][] matrix = new double[listRadius.size()][columnMatrix.size() + 3];
		String values = "RADIUS" + System.lineSeparator();
		
		for (int i = 0; i < listRadius.size(); i++) {
			matrix[i][0] = listRadius.get(i);
			values += matrix[i][0] + System.lineSeparator();

		}
		values += "LIST TOTAL MOVEMENT" + System.lineSeparator();
		for (int i = 0; i < listTotalMovement.size(); i++) {
			matrix[i][1] = listTotalMovement.get(i);
			values += matrix[i][1] + System.lineSeparator();

		}
		values += "NUMBER OF MESSAGES" + System.lineSeparator();
		for (int i = 0; i < listNumberOfMessages.size(); i++) {
			matrix[i][2] = listNumberOfMessages.get(i);
			values += matrix[i][2] + System.lineSeparator();

		}
		
		values += "---" + System.lineSeparator();

		values += "--------------HERE WE START THE SOCIAL VARIABLES--------------" + System.lineSeparator();
		String valZ = null;
		for(int i = 0; i < columnMatrix.size(); i++){
			valZ = columnMatrix.get(i).remove(0);
			values += valZ + System.lineSeparator();
			
			for(int j = 0; j < columnMatrix.get(i).size(); j++){
				matrix[j][i+3] = Double.valueOf(columnMatrix.get(i).get(j));
				values += matrix[j][i+3] + System.lineSeparator();
			}
			values += "---" + System.lineSeparator();
			
		}
		writeOnFile(values, outPutFileName);
		RealMatrix realMatrix = calclateMultiCorrelations(matrix, method);
		return realMatrix;
	}

	public void findMuiltiCorrelationTotalDistanceByWeekend(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsOnWeekends(user);
				listX.add(geoCalculator.generateDisplacementValue(selectedTweets));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistanceByWeekend");
		
	}
	
	public void findMuiltiCorrelationRadiusByWeekend(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsOnWeekends(user);
				listX.add(geoCalculator.calculateRadiusOfGyration(geoCalculator.tweetsAsPoints(selectedTweets)));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "RadiusByWeekend");
		
	}
	
	public void findMuiltiCorrelationNumMessagesByWeekend(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsOnWeekends(user);
				listX.add((double) selectedTweets.size());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "NumMessagesByWeekend");
		
	}
	
	public void findMuiltiCorrelationTotalDistanceByWeekdays(String method, String locationBased){
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
				List<Tweet> listSelectedTweets = selectTweetsOnWeekdays(user);
				listX.add(geoCalculator.generateDisplacementValue(listSelectedTweets));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistanceByWeekdays");
		
	}
	
	public void findMuiltiCorrelationRadiusByWeekdays(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsOnWeekdays(user);
				listX.add(geoCalculator.calculateRadiusOfGyration(geoCalculator.tweetsAsPoints(selectedTweets)));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "RadiusByWeekdays");
		
	}
	
	public void findMuiltiCorrelationNumMessagesByWeekdays(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsOnWeekdays(user);
				listX.add((double) selectedTweets.size());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "NumMessagesByWeekdays");
		
	}
	
	private List<Tweet> selectTweetsOnWeekends(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(!DateTimeOperations.isHomeTime(t)){
				list.add(t);
			}
		}
		
		return list;
		
	}
	
	private List<Tweet> selectTweetsOnWeekdays(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isHomeTime(t)){
				list.add(t);
			}
		}
		return list;
		
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
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistance");
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
		saveMultiCorrelationsToXLS(realMatrix, "Radius");
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
		saveMultiCorrelationsToXLS(realMatrix, "NumMessages");
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
		saveMultiCorrelationsToXLS(realMatrix, "DisplacementsPerDay");
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
		saveMultiCorrelationsToXLS(realMatrix, "NumberOfDisplacements");
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
		String values = "mobility_variable" + System.lineSeparator();
		
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
	
	public void writeOnFile(String text, String outPutFileName){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outPutFileName + "_values.txt", "UTF-8");
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
	
	private void exportCorrelationResultMatrixToXLS(ArrayList<ArrayList<String>> matrixResult, String fileName){
		try {
			excelHandler.writeXLSFileTableCorrelations(matrixResult, fileName);
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
	
	private void saveMultiCorrelationsToXLS(RealMatrix matrixResults, String fileName){
		ArrayList<ArrayList<String>> matrixFormated = formatMatrixToXLSFile(matrixResults);
		exportCorrelationResultMatrixToXLS(matrixFormated, fileName);
	}
	
	public static class Teste{
		public static void main (String args[]){
			CorrelationCalculator corr = new CorrelationCalculator();
			corr.initData();
//			corr.findMuiltiCorrelationTotalDistanceByWeekend("kendall", "home");
//			corr.findMuiltiCorrelationRadiusByWeekend("kendall", "home");
//			corr.findMuiltiCorrelationNumMessagesByWeekend("kendall", "home");
//			
//			corr.findMuiltiCorrelationTotalDistanceByWeekdays("kendall", "home");
//			corr.findMuiltiCorrelationRadiusByWeekdays("kendall", "home");
//			corr.findMuiltiCorrelationNumMessagesByWeekdays("kendall", "home");
//			corr.findMuiltiCorrelationAll("spearman", "home", "MultiCorrelationAll_5500");
			
			
//			System.out.println("Esta em 1000...");
//			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_1000");
			System.out.println("Esta em 2500...");
//			removeUsersByNumOfMessages(2500);
			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_2500");
			System.out.println("Esta em 5500...");
			removeUsersByNumOfMessages(5500);
			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_5500");
			
		}

		private static void removeUsersByNumOfMessages(int val) {
			List<User> toRemove = new ArrayList<User>();
			for(User u: listUsers){
				if(u.getNum_messages() < val){
					toRemove.add(u);
				}
			}
			listUsers.removeAll(toRemove);
		}

		
	}

}
