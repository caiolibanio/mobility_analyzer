package mobility.statistic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import mobility.DAO.SocialDataDAO;
import mobility.core.ClusteredCentroid;
import mobility.core.ClusteredPoint;
import mobility.core.DateTimeOperations;
import mobility.core.DisplacementPerDay;
import mobility.core.DistanceDisplacement;
import mobility.core.GeoCalculator;
import mobility.core.Point;
import mobility.core.SocioData;
import mobility.core.Tweet;
import mobility.core.User;
import mobility.dbscan.GeoDistance;
import mobility.service.ClusteredPointService;
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
	
	private ClusteredPointService poiService = new ClusteredPointService();
	
	
	
	
	public void initData(){
		columnsToIgnore.add("code");
		columnsToIgnore.add("name");
		columnsToIgnore.add("geom");
		listUsers = new ArrayList<User>();
		listSocioData = new ArrayList<SocioData>();
		matrixSocialData = socioDataService.findAllMatrix();
		listUsers.addAll(userService.findAllSelectedUsers(5500));
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
		
		List<Integer> listDisplacement = new ArrayList<Integer>();
		List<Double> listDisplacementPerDayMedian = new ArrayList<Double>();
		List<Double> listDistanceDisplacementMedian = new ArrayList<Double>();
		
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
			listDisplacement.add(user.getDisplacement().getDisplacementCounter());
			listDisplacementPerDayMedian.add(user.getDisplacement().getDisplacementPerDayMedian());
			listDistanceDisplacementMedian.add(user.getDisplacement().getDistanceDisplacementMedian());
			fillSocialDataMatrixByCode(code, matrixY);

		}

		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormatedTest(listRadius, listTotalMovement,
				listNumberOfMessages, listDisplacement, listDisplacementPerDayMedian,
				listDistanceDisplacementMedian,columnMatrix, method, outputFileName);
		saveMultiCorrelationsToXLS(realMatrix, "MuiltiCorrelationAll");
		return realMatrix;
		
	}
	
	public void findMuiltiCorrelationAll(String method, String locationBased, String outputFileName, boolean recalculate){
		String code = null;
		List<Double> listRadius = new ArrayList<Double>();
		List<Double> listTotalMovement = new ArrayList<Double>();
		List<Integer> listNumberOfMessages = new ArrayList<Integer>();
		
		List<Integer> listDisplacement = new ArrayList<Integer>();
		List<Double> listDisplacementPerDayMedian = new ArrayList<Double>();
		List<Double> listDistanceDisplacementMedian = new ArrayList<Double>();
		List<Double> listMedianPrices = new ArrayList<Double>();
		
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
				if(!recalculate){
					listRadius.add(user.getRadiusOfGyration());
					listTotalMovement.add(user.getUser_movement());
					listNumberOfMessages.add(user.getNum_messages());
					listDisplacement.add(user.getDisplacement().getDisplacementCounter());
					listDisplacementPerDayMedian.add(user.getDisplacement().getDisplacementPerDayMedian());
					listDistanceDisplacementMedian.add(user.getDisplacement().getDistanceDisplacementMedian());
					listMedianPrices.add(poiService.findMedianPriceByUserID(user.getUser_id()));
					fillSocialDataMatrixByCode(code, matrixY);
				}else{
					//filtro
//					List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidays(user); //53
//					List<Tweet> listSelectedTweets = selectTweetsOnWeekends(user); 
//					List<Tweet> listSelectedTweets = selectTweetsOnWeekdays(user);
					List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidaysAndSundays(user);
					//fim filtro
					Double total_movement_dist = geoCalculator.generateDisplacementValue(listSelectedTweets);
					
					if(total_movement_dist > 0.0){
						listRadius.add(geoCalculator.calculateRadiusOfGyration(geoCalculator.tweetsAsPoints(listSelectedTweets)));
						listTotalMovement.add(total_movement_dist);
						listNumberOfMessages.add(listSelectedTweets.size());
						
						user.setTweetList(listSelectedTweets);
						calculateTotalDisplacement(user);
						calculateDisplacementPerDay(user);
						calculateDistancePerDisplacement(user);
						user.getDisplacement().calculateDisplacementPerDayMedian();
						user.getDisplacement().calculateDistanceDisplacementMedian();
						List<ClusteredPoint> listclusteredPoints = poiService.findAll();
						List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
						
						listDisplacement.add(user.getDisplacement().getDisplacementCounter());
						listDisplacementPerDayMedian.add(user.getDisplacement().getDisplacementPerDayMedian());
						listDistanceDisplacementMedian.add(user.getDisplacement().getDistanceDisplacementMedian());
						listMedianPrices.add(calculateAvaragePrice(listSelectedTweets, listclusteredPoints, listPOIs));
						fillSocialDataMatrixByCode(code, matrixY);
					}
					
					
				}

			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsWithPrice(listRadius, listTotalMovement,
				listNumberOfMessages, listDisplacement, listDisplacementPerDayMedian,
				listDistanceDisplacementMedian, listMedianPrices, columnMatrix, method, outputFileName);
		saveMultiCorrelationsToXLS(realMatrix, outputFileName);
		
	}
	
	public void findMuiltiCorrelationByActivitiesCenters(String method, String locationBased, String outPutFileName, boolean recalculating){
		String code = null;
		List<Double> listRadius = new ArrayList<Double>();
		List<Double> listTotalMovement = new ArrayList<Double>();
		List<Integer> listNumberOfMessages = new ArrayList<Integer>();
		
		List<Integer> listDisplacement = new ArrayList<Integer>();
		List<Double> listDisplacementPerDayMedian = new ArrayList<Double>();
		List<Double> listDistanceDisplacementMedian = new ArrayList<Double>();
		List<Double> listMedianPrices = new ArrayList<Double>();
		
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
				//filtro
//				List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidays(user); //31 users 51
//				List<Tweet> listSelectedTweets = selectTweetsOnWeekends(user); //33 55
//				List<Tweet> listSelectedTweets = selectTweetsOnWeekdays(user); //33 55
				List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidaysAndSundays(user); //33
				//fim filtro
				
				List<DoublePoint> clusteredPoints = findClusteredPoints(listSelectedTweets); 
				System.out.println("Clusterizou: " + count);
				++count;
				if(clusteredPoints.size() > 0){
					if(!recalculating){
						listRadius.add(user.getRadiusOfGyration());
						listTotalMovement.add(user.getUser_movement());
						listNumberOfMessages.add(user.getNum_messages());
						listDisplacement.add(user.getDisplacement().getDisplacementCounter());
						listDisplacementPerDayMedian.add(user.getDisplacement().getDisplacementPerDayMedian());
						listDistanceDisplacementMedian.add(user.getDisplacement().getDistanceDisplacementMedian());
						listMedianPrices.add(poiService.findMedianPriceByUserID(user.getUser_id()));
						fillSocialDataMatrixByActivityCenter(matrixY, clusteredPoints);
					}else{
						Double total_movement_dist = geoCalculator.generateDisplacementValue(listSelectedTweets);
						
						if(total_movement_dist > 0.0){
							listRadius.add(geoCalculator.calculateRadiusOfGyration(geoCalculator.tweetsAsPoints(listSelectedTweets)));
							listTotalMovement.add(geoCalculator.generateDisplacementValue(listSelectedTweets));
							listNumberOfMessages.add(listSelectedTweets.size());
							
							user.setTweetList(listSelectedTweets);
							calculateTotalDisplacement(user);
							calculateDisplacementPerDay(user);
							calculateDistancePerDisplacement(user);
							user.getDisplacement().calculateDisplacementPerDayMedian();
							user.getDisplacement().calculateDistanceDisplacementMedian();
							List<ClusteredPoint> listclusteredPoints = poiService.findAll();
							List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
							
							listDisplacement.add(user.getDisplacement().getDisplacementCounter());
							listDisplacementPerDayMedian.add(user.getDisplacement().getDisplacementPerDayMedian());
							listDistanceDisplacementMedian.add(user.getDisplacement().getDistanceDisplacementMedian());
							listMedianPrices.add(calculateAvaragePrice(listSelectedTweets, listclusteredPoints, listPOIs));
							fillSocialDataMatrixByActivityCenter(matrixY, clusteredPoints);
						}
						
					}
					
				}
				
				
			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsWithPrice(listRadius, listTotalMovement,
				listNumberOfMessages, listDisplacement, listDisplacementPerDayMedian,
				listDistanceDisplacementMedian, listMedianPrices, columnMatrix, method, outPutFileName);
		saveMultiCorrelationsToXLS(realMatrix, outPutFileName);
		System.out.println(new Date());
		
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

	private List<DoublePoint> findClusteredPoints(List<Tweet> listTweets) {
		List<DoublePoint> listOfPoints = new ArrayList<DoublePoint>();
		List<DoublePoint> points = formatPointsToClusterGeneral(listTweets);

		List<Cluster<DoublePoint>> cluster = clusteringPoints(points);
//		ClusteredUser clusteredUser = new ClusteredUser(cluster, user.getUser_id());
//		listClusteredUsers.add(clusteredUser);

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
	
	private static List<DoublePoint> formatPointsToClusterGeneral(List<Tweet> listTweets) {

		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Tweet t : listTweets) {

			double[] d = new double[2];
			d[0] = t.getLatitude();
			d[1] = t.getLongitude();
			points.add(new DoublePoint(d));

		}
		return points;
	}
	
	private void fillColumnLabelsTest(String string, ArrayList<ArrayList<String>> columnMatrix) {
		columnsLabels.clear(); //Ajustar isso!!!
		columnsLabels.add("Radius");
		columnsLabels.add("Total_movement");
		columnsLabels.add("Number_of_messages");
		columnsLabels.add("Number_of_displacements");
		columnsLabels.add("Desplacement_per_day_median");
		columnsLabels.add("Distance_displacement_median");
		columnsLabels.add("Median_Prices");
		for(ArrayList<String> list : columnMatrix){
			columnsLabels.add(list.get(0));
		}
		
	}

	private RealMatrix calculateMultiCorrelationsFormatedTest(List<Double> listRadius, List<Double> listTotalMovement,
			List<Integer> listNumberOfMessages, List<Integer> listDisplacement, List<Double> listDisplacementPerDayMedian,
			List<Double> listDistanceDisplacementMedian, ArrayList<ArrayList<String>> columnMatrix, String method,
			String outPutFileName) {
		double[][] matrix = new double[listRadius.size()][columnMatrix.size() + 6];
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
		values += "NUMBER OF DISPLACEMENTS" + System.lineSeparator();
		for (int i = 0; i < listDisplacement.size(); i++) {
			matrix[i][3] = listDisplacement.get(i);
			values += matrix[i][3] + System.lineSeparator();

		}
		values += "DISPLACEMENTS PER DAY MEDIAN" + System.lineSeparator();
		for (int i = 0; i < listDisplacementPerDayMedian.size(); i++) {
			matrix[i][4] = listDisplacementPerDayMedian.get(i);
			values += matrix[i][4] + System.lineSeparator();

		}
		values += "DISTANCE DISPLACEMENT MEDIAN" + System.lineSeparator();
		for (int i = 0; i < listDistanceDisplacementMedian.size(); i++) {
			matrix[i][5] = listDistanceDisplacementMedian.get(i);
			values += matrix[i][5] + System.lineSeparator();

		}
		
		values += "---" + System.lineSeparator();

		values += "--------------HERE WE START THE SOCIAL VARIABLES--------------" + System.lineSeparator();
		String valZ = null;
		for(int i = 0; i < columnMatrix.size(); i++){
			valZ = columnMatrix.get(i).remove(0);
			values += valZ + System.lineSeparator();
			
			for(int j = 0; j < columnMatrix.get(i).size(); j++){
				matrix[j][i+6] = Double.valueOf(columnMatrix.get(i).get(j));
				values += matrix[j][i+6] + System.lineSeparator();
			}
			values += "---" + System.lineSeparator();
			
		}
		writeOnFile(values, outPutFileName);
		RealMatrix realMatrix = calclateMultiCorrelations(matrix, method);
		return realMatrix;
	}
	
	private RealMatrix calculateMultiCorrelationsWithPrice(List<Double> listRadius, List<Double> listTotalMovement,
			List<Integer> listNumberOfMessages, List<Integer> listDisplacement, List<Double> listDisplacementPerDayMedian,
			List<Double> listDistanceDisplacementMedian, List<Double> listMedianPrices, ArrayList<ArrayList<String>> columnMatrix, String method,
			String outPutFileName) {
		double[][] matrix = new double[listRadius.size()][columnMatrix.size() + 7];
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
		values += "NUMBER OF DISPLACEMENTS" + System.lineSeparator();
		for (int i = 0; i < listDisplacement.size(); i++) {
			matrix[i][3] = listDisplacement.get(i);
			values += matrix[i][3] + System.lineSeparator();

		}
		values += "DISPLACEMENTS PER DAY MEDIAN" + System.lineSeparator();
		for (int i = 0; i < listDisplacementPerDayMedian.size(); i++) {
			matrix[i][4] = listDisplacementPerDayMedian.get(i);
			values += matrix[i][4] + System.lineSeparator();

		}
		values += "DISTANCE DISPLACEMENT MEDIAN" + System.lineSeparator();
		for (int i = 0; i < listDistanceDisplacementMedian.size(); i++) {
			matrix[i][5] = listDistanceDisplacementMedian.get(i);
			values += matrix[i][5] + System.lineSeparator();

		}
		
		values += "MEDIAN PRICES" + System.lineSeparator();
		for (int i = 0; i < listMedianPrices.size(); i++) {
			matrix[i][6] = listMedianPrices.get(i);
			values += matrix[i][6] + System.lineSeparator();

		}
		
		values += "---" + System.lineSeparator();

		values += "--------------HERE WE START THE SOCIAL VARIABLES--------------" + System.lineSeparator();
		String valZ = null;
		for(int i = 0; i < columnMatrix.size(); i++){
			valZ = columnMatrix.get(i).remove(0);
			values += valZ + System.lineSeparator();
			
			for(int j = 0; j < columnMatrix.get(i).size(); j++){
				matrix[j][i+7] = Double.valueOf(columnMatrix.get(i).get(j));
				values += matrix[j][i+7] + System.lineSeparator();
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
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistanceByWeekend_5500");
		
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
		fillColumnLabels("Radius_of_gyration_weekend", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "RadiusByWeekend_5500");
		
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
		fillColumnLabels("Num_messages_weekend", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "NumMessagesByWeekend_5500");
		
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
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistanceByWeekdays_5500");
		
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
		fillColumnLabels("Radius_of_gyration", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "RadiusByWeekdays_5500");
		
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
		fillColumnLabels("Num_messages", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "NumMessagesByWeekdays_5500");
		
	}
	
	public void findMuiltiCorrelationNumMessagesByHoliday(String method, String locationBased){
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
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				listX.add((double) selectedTweets.size());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Num_messages", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "NumMessagesByAllHoliday-5500");
		
	}
	
	public void findMuiltiCorrelationRadiusByHoliday(String method, String locationBased){
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
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				listX.add(geoCalculator.calculateRadiusOfGyration(geoCalculator.tweetsAsPoints(selectedTweets)));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Radius_of_gyration", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "RadiusByAllHoliday-5500");
		
	}
	
	public void findMuiltiCorrelationPOIPricesByHoliday(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		List<ClusteredPoint> listclusteredPoints = poiService.findAll();
		List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				
				
				Double avaregePrice = calculateAvaragePrice(selectedTweets, listclusteredPoints, listPOIs);
				
				listX.add(avaregePrice);
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Avarage_prices", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "PricesByAllHoliday-5500");
		
	}
	
	private Double calculateAvaragePrice(List<Tweet> selectedTweets, List<ClusteredPoint> listclusteredPoints,
			List<ClusteredCentroid> listPOIs) {
		List<ClusteredPoint> clusteredPoints = new ArrayList<ClusteredPoint>();
		List<Integer> prices = new ArrayList<Integer>();
		List<Integer> listClusters = new ArrayList<Integer>();
		for(Tweet t : selectedTweets){
			for(ClusteredPoint clusteredPoint : listclusteredPoints){
				if(t.getLatitude().equals(clusteredPoint.getPointMessage().getLatitude()) && 
						t.getLongitude().equals(clusteredPoint.getPointMessage().getLongitude()) && 
						!listClusters.contains(clusteredPoint.getClusterNumber())){
					clusteredPoints.add(clusteredPoint);
					listClusters.add(clusteredPoint.getClusterNumber());
				}
			}
		}
		
		if(clusteredPoints.size() > 0){
			for(ClusteredPoint cluster : clusteredPoints){
				for(ClusteredCentroid poi : listPOIs){
					if(poi.getUser_id().equals(cluster.getUser_id()) && 
							poi.getClusterNumber() == cluster.getClusterNumber() && 
							poi.getPrice() > 0){
						prices.add(poi.getPrice());
					}
				}
			}
		}
		
		Double medianPrice = ((double) prices.stream().mapToInt(Integer::intValue).sum()) / prices.size(); //avarege
		medianPrice = (double) Math.round(medianPrice);
		return medianPrice;
		
	}

	public void findMuiltiCorrelationTotalDistanceByHoliday(String method, String locationBased){
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
				List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidays(user);
				listX.add(geoCalculator.generateDisplacementValue(listSelectedTweets));
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "TotalDistanceByAllHoliday-5500");
		
	}
	
	private List<Tweet> selectTweetsByHoliday(User user, int day, int month, int year) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isBankHoliday(t, day, month, year)){
				list.add(t);
			}
		}
		
		return list;
	}
	
	private List<Tweet> selectTweetsByAllBankHolidays(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isBankHoliday2015(t)){
				list.add(t);
			}
		}
		
		return list;
	}
	
	private List<Tweet> selectTweetsByAllBankHolidaysAndSundays(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isBankHolidayAndSunday2015(t)){
				list.add(t);
			}
		}
		
		return list;
	}

	private List<Tweet> selectTweetsOnWeekends(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(!DateTimeOperations.isWeekdays(t)){
				list.add(t);
			}
		}
		
		return list;
		
	}
	
	private List<Tweet> selectTweetsOnWeekdays(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isWeekdays(t)){
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
	
	public void findMuiltiCorrelationDisplacementByHoliday(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		List<ClusteredPoint> listclusteredPoints = poiService.findAll();
		List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				user.setTweetList(selectedTweets);
				calculateTotalDisplacement(user);
				
				listX.add((double)user.getDisplacement().getDisplacementCounter());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Total_Displacement", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "Total_Displacement_holiday-5500");
		
	}
	
	public void findMuiltiCorrelationDisplacementPerDayMedianByHoliday(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		List<ClusteredPoint> listclusteredPoints = poiService.findAll();
		List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				user.setTweetList(selectedTweets);
				calculateTotalDisplacement(user);
				calculateDisplacementPerDay(user);
				user.getDisplacement().calculateDisplacementPerDayMedian();
				
				listX.add((double)user.getDisplacement().getDisplacementPerDayMedian());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Displacement_perDay_median", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "Displacement_perDay_median_holiday-5500");
		
	}
	
	public void findMuiltiCorrelationDistanceDisplacementMedianByHoliday(String method, String locationBased){
		String code = null;
		List<Double> listX = new ArrayList<Double>();
		ArrayList<ArrayList<String>> matrixY = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> columnMatrix = new ArrayList<ArrayList<String>>();
		Set<String> listDifferentRegions = new HashSet<String>();
		matrixY.add(matrixSocialData.get(0));
		List<ClusteredPoint> listclusteredPoints = poiService.findAll();
		List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			if(code != null){ //verificar ponto fora de londres
				listDifferentRegions.add(code);
				List<Tweet> selectedTweets = selectTweetsByAllBankHolidays(user);
				user.setTweetList(selectedTweets);
				calculateTotalDisplacement(user);
				calculateDistancePerDisplacement(user);
				user.getDisplacement().calculateDistanceDisplacementMedian();
				
				listX.add((double)user.getDisplacement().getDisplacementPerDayMedian());
				fillSocialDataMatrixByCode(code, matrixY);
			}
		}
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Distance_Displacement_median", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method);
		saveMultiCorrelationsToXLS(realMatrix, "Distance_Displacement_median_holiday-5500");
		
	}
	
	private void calculateDistancePerDisplacement(User u) {

		List<Tweet> listTweets = u.getTweetList();
		Collections.sort(listTweets);
		u.getDisplacement().getListDistanceDisplacements().clear();
		for (int i = 1; i < listTweets.size(); i++) {
			Tweet tweet = listTweets.get(i);
			Tweet predTweet = listTweets.get(i - 1);
			if (isDisplacement(tweet.getLatitude(), tweet.getLongitude(), predTweet.getLatitude(),
					predTweet.getLongitude())) {
				Double distanceDisplacement = geoCalculator.calculateDistance(tweet.getLatitude(), tweet.getLongitude(),
						predTweet.getLatitude(), predTweet.getLongitude());
				Point pointA = new Point(tweet.getLatitude(), tweet.getLongitude());
				Point pointB = new Point(predTweet.getLatitude(), predTweet.getLongitude());
				DistanceDisplacement distDisplacement = new DistanceDisplacement();
				distDisplacement.setPointA(pointA);
				distDisplacement.setPointB(pointB);
				distDisplacement.setDistanceDisplacement(distanceDisplacement);
				u.getDisplacement().getListDistanceDisplacements().add(distDisplacement);
			}
		}

	}
	
	private void calculateTotalDisplacement(User user) {
		int displCount = 0;
		List<Tweet> listTweets = user.getTweetList();
		displCount = calculateTotalDisplacementPerTweets(listTweets);
		user.getDisplacement().setDisplacementCounter(displCount);
		displCount = 0;

	}
	
	private void calculateDisplacementPerDay(User u) {
		int displPerDay = 0;
		List<Tweet> analyzedTweets = new ArrayList<Tweet>();

		List<Tweet> listTweets = u.getTweetList();
		Collections.sort(listTweets);
		u.getDisplacement().getListDisplacementsPerDay().clear();
		for (Tweet t : listTweets) {
			if (!analyzedTweets.contains(t)) {
				Calendar calendar = DateTimeOperations.getLondonTime(t.getDate());
				List<Tweet> tweetsPerDate = getTweetsByDate(listTweets, calendar);
				analyzedTweets.addAll(tweetsPerDate);
				displPerDay = calculateTotalDisplacementPerTweets(tweetsPerDate);
				if (displPerDay > 0) {
					DisplacementPerDay displacement = new DisplacementPerDay();
					displacement.setDisplacementPerDay(displPerDay);
					displacement.setDate(t.getDate());
					u.getDisplacement().getListDisplacementsPerDay().add(displacement);
				}
			}
		}

	}
	
	private static List<Tweet> getTweetsByDate(List<Tweet> tweetList, Calendar calendar) {
		List<Tweet> list = new ArrayList<Tweet>();
		for (Tweet t : tweetList) {
			Timestamp time = t.getDate();
			Calendar londonTime = DateTimeOperations.getLondonTime(time);
			if (londonTime.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
					&& londonTime.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
					&& londonTime.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
				list.add(t);
			}
		}
		return list;
	}
	
	private int calculateTotalDisplacementPerTweets(List<Tweet> listTweets) {
		int displCount = 0;
		Collections.sort(listTweets);
		for (int i = 1; i < listTweets.size(); i++) {
			Tweet tweet = listTweets.get(i);
			Tweet predTweet = listTweets.get(i - 1);
			if (isDisplacement(tweet.getLatitude(), tweet.getLongitude(), predTweet.getLatitude(),
					predTweet.getLongitude())) {
				displCount++;
			}
		}
		return displCount;
	}
	
	private boolean isDisplacement(Double lat1, Double lon1, Double lat2, Double lon2) {
		if (geoCalculator.calculateDistance(lat1, lon1, lat2, lon2) > 45) {
			return true;
		}
		return false;
	}
	
	public static class Teste{
		public static void main (String args[]){
			CorrelationCalculator corr = new CorrelationCalculator();
			corr.initData();
//			corr.findMuiltiCorrelationTotalDistanceByWeekend("kendall", "home");
//			corr.findMuiltiCorrelationRadiusByWeekend("kendall", "home");
//			corr.findMuiltiCorrelationNumMessagesByWeekend("kendall", "home");
			
//			corr.findMuiltiCorrelationTotalDistanceByWeekdays("kendall", "home");
//			corr.findMuiltiCorrelationRadiusByWeekdays("kendall", "home");
//			corr.findMuiltiCorrelationNumMessagesByWeekdays("kendall", "home");
			
			//Spring Bank Holiday 2015
//			corr.findMuiltiCorrelationNumMessagesByHoliday("kendall", "home");
//			corr.findMuiltiCorrelationTotalDistanceByHoliday("kendall", "home");
//			corr.findMuiltiCorrelationRadiusByHoliday("kendall", "home");
//			
//			//May Day 2015
//			corr.findMuiltiCorrelationNumMessagesByHoliday("kendall", "home", 4, 5, 2015);
//			corr.findMuiltiCorrelationTotalDistanceByHoliday("kendall", "home", 4, 5, 2015);
//			corr.findMuiltiCorrelationRadiusByHoliday("kendall", "home", 4, 5, 2015);
//			
//			//Good Friday 2015
//			corr.findMuiltiCorrelationNumMessagesByHoliday("kendall", "home", 3, 4, 2015);
//			corr.findMuiltiCorrelationTotalDistanceByHoliday("kendall", "home", 3, 4, 2015);
//			corr.findMuiltiCorrelationRadiusByHoliday("kendall", "home", 3, 4, 2015);
			

			
//			A partir daqui ja foi executado!!!------------------------------
//			corr.findMuiltiCorrelationAll("kendall", "home", "MultiCorrelationAll_5500_holidANDSund_recalc", true);
			
//			System.out.println("Esta em 1000...");
//			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_1000");
//			System.out.println("Esta em 2500...");
//			removeUsersByNumOfMessages(2500);
//			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_2500");
			
//			System.out.println("Esta em 5500...");
//			removeUsersByNumOfMessages(5500);
			corr.findMuiltiCorrelationByActivitiesCenters("kendall", "home", "ActivitiesCentersMedians_5500_holiANDSunday_recalc", true);
			
			
//			corr.findMuiltiCorrelationPOIPricesByHoliday("kendall", "home");
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
