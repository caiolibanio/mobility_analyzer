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
	
	public void findMuiltiCorrelationAll(String method, String locationBased, boolean recalculate, int filtroCode){
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
		List<Tweet> listSelectedTweets = new ArrayList<Tweet>();
		String outPutFileName = "";
		for(User user : listUsers){
			if(locationBased.equals("home")){
				code = user.getHomePolygonCode();
			}else{
				code = user.getCentroidPolygonCode();
			}
			
			
			if(code != null){ //verificar ponto fora de londres
				
				if(filtroCode == 1){
					listSelectedTweets = selectTweetsByAllBankHolidays(user); 
					outPutFileName = "MultiCorrelationAll_5000_holidays_OK";
				}else if(filtroCode == 2){
					listSelectedTweets = selectTweetsOnWeekends(user); 
					outPutFileName = "MultiCorrelationAll_5000_Weekends_OK";
				}else if(filtroCode == 3){
					listSelectedTweets = selectTweetsOnWorkdays(user); 
					outPutFileName = "MultiCorrelationAll_5000_Workdays_OK";
				}else if (filtroCode == 4){
					listSelectedTweets = selectTweetsByAllBankHolidaysAndSundays(user); 
					outPutFileName = "MultiCorrelationAll_5000_holidays_sundays_OK";
				}else{
					outPutFileName = "MultiCorrelationAll_5000_OK";
				}
				
				
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
		
		poiService.adjustAveragePrices(listMedianPrices);
		System.out.println(matrixY.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabelsTest("Total Distance", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsWithPrice(listRadius, listTotalMovement,
				listNumberOfMessages, listDisplacement, listDisplacementPerDayMedian,
				listDistanceDisplacementMedian, listMedianPrices, columnMatrix, method, outPutFileName);
		saveMultiCorrelationsToXLS(realMatrix, outPutFileName);
		
	}
	
	public void findMuiltiCorrelationByActivitiesCenters(String method, String locationBased, boolean recalculating, int filtroCode){
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
		List<Tweet> listSelectedTweets = new ArrayList<Tweet>();
		String outPutFileName = "";
		
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
				
				if(filtroCode == 1){
					listSelectedTweets = selectTweetsByAllBankHolidays(user); 
					outPutFileName = "ActivitiesCentersMedians_5000_holidays_OK";
				}else if(filtroCode == 2){
					listSelectedTweets = selectTweetsOnWeekends(user); 
					outPutFileName = "ActivitiesCentersMedians_5000_Weekends_OK";
				}else if(filtroCode == 3){
					listSelectedTweets = selectTweetsOnWorkdays(user); 
					outPutFileName = "ActivitiesCentersMedians_5000_Workdays_OK";
				}else if (filtroCode == 4){
					listSelectedTweets = selectTweetsByAllBankHolidaysAndSundays(user); 
					outPutFileName = "ActivitiesCentersMedians_5000_holidays_sundays_OK";
				}else{
					outPutFileName = "ActivitiesCentersMedians_5000_OK";
				}
				
				//filtro

//				
				
				//fim filtro
				
				List<DoublePoint> clusteredPoints = null;
				
				if(recalculating){
					clusteredPoints = findClusteredPoints(listSelectedTweets); 
				}else{
					clusteredPoints = findClusteredPoints(user.getTweetList()); 
				}
				
				
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
		
		poiService.adjustAveragePrices(listMedianPrices);
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


		ArrayList<ArrayList<DoublePoint>> listOfClusters = returnClustersList(cluster);
//		List<List<DoublePoint>> listOfClustersWithoutHome = removeHomeCluster(listOfClusters);
		
		for (ArrayList<DoublePoint> c : listOfClusters) {
			if(c.size() >= 3){
				for (DoublePoint p : c) {
					if(!listOfPoints.contains(p)){
						listOfPoints.add(p);
					}
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
		DBSCANClusterer dbscan = new DBSCANClusterer(40.0, 3, new GeoDistance());
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

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void findMuiltiCorrelationPOIPrices(String method, String locationBased, String fileName, boolean recalculate){
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
				if(!recalculate){
					
					listX.add(poiService.findMedianPriceByUserID(user.getUser_id()));
					fillSocialDataMatrixByCode(code, matrixY);
				}else{
					//filtro
//					List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidays(user); 
//					List<Tweet> listSelectedTweets = selectTweetsOnWeekends(user); 
//					List<Tweet> listSelectedTweets = selectTweetsOnWeekdays(user);
					List<Tweet> listSelectedTweets = selectTweetsByAllBankHolidaysAndSundays(user); 
					//fim filtro
					Double total_movement_dist = geoCalculator.generateDisplacementValue(listSelectedTweets);
					
					if(total_movement_dist > 0.0){
						
						List<ClusteredPoint> listclusteredPoints = poiService.findAll();
						List<ClusteredCentroid> listPOIs = poiService.findAllCentroids();
						
						listX.add(calculateAvaragePrice(listSelectedTweets, listclusteredPoints, listPOIs));
						fillSocialDataMatrixByCode(code, matrixY);
					}
					
					
				}

			}
		}
		
		poiService.adjustAveragePrices(listX);
		
		System.out.println(matrixY.size());
		System.out.println("Num of regions: " + listDifferentRegions.size());
		columnMatrix = createColimnMatrix(matrixY);
		fillColumnLabels("Avarage_prices", columnMatrix);
		RealMatrix realMatrix = calculateMultiCorrelationsFormated(listX, columnMatrix, method, fileName);
		saveMultiCorrelationsToXLS(realMatrix, fileName);
		
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
			if(DateTimeOperations.isWeekend(t)){
				list.add(t);
			}
		}
		
		return list;
		
	}
	
	private List<Tweet> selectTweetsOnWorkdays(User user) {
		List<Tweet> list = new ArrayList<Tweet>();
		for(Tweet t : user.getTweetList()){
			if(DateTimeOperations.isWorkdays(t)){
				list.add(t);
			}
		}
		return list;
		
	}
	
	private void fillColumnLabels(String mobilityLabel, ArrayList<ArrayList<String>> columnMatrix) {
		columnsLabels.add(mobilityLabel);
		for(ArrayList<String> list : columnMatrix){
			columnsLabels.add(list.get(0));
		}
		
	}

	private RealMatrix calculateMultiCorrelationsFormated(List<Double> listX,
			ArrayList<ArrayList<String>> columnMatrix, String method, String fileName) {
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
		writeOnFile(values, fileName);
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
//			CorrelationCalculator corr = new CorrelationCalculator();
//			corr.initData();
			
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
			

			
//			HOME-------
			CorrelationCalculator corr = new CorrelationCalculator();
			corr.initData();
			corr.findMuiltiCorrelationAll("kendall", "home", false, 0);
			corr = null;
//			
			CorrelationCalculator corr1 = new CorrelationCalculator();
			corr1.initData();
			corr1.findMuiltiCorrelationAll("kendall", "home", true, 1);
			corr1 = null;
//			
			CorrelationCalculator corr2 = new CorrelationCalculator();
			corr2.initData();
			corr2.findMuiltiCorrelationAll("kendall", "home", true, 2);
			corr2 = null;
			
			CorrelationCalculator corr3 = new CorrelationCalculator();
			corr3.initData();
			corr3.findMuiltiCorrelationAll("kendall", "home", true, 3);
			corr3 = null;
			
			CorrelationCalculator corr4 = new CorrelationCalculator();
			corr4.initData();
			corr4.findMuiltiCorrelationAll("kendall", "home", true, 4);
			corr4 = null;
			
			
//			FIM HOME ------------
			
			
			//AC-------
			CorrelationCalculator corrAC = new CorrelationCalculator();
			corrAC.initData();
			corrAC.findMuiltiCorrelationByActivitiesCenters("kendall", "home", false, 0);
			corrAC = null;
//			
			CorrelationCalculator corr1AC = new CorrelationCalculator();
			corr1AC.initData();
			corr1AC.findMuiltiCorrelationByActivitiesCenters("kendall", "home", true, 1);
			corr1AC = null;
//			
			CorrelationCalculator corr2AC = new CorrelationCalculator();
			corr2AC.initData();
			corr2AC.findMuiltiCorrelationByActivitiesCenters("kendall", "home", true, 2);
			corr2AC = null;
//			
			CorrelationCalculator corr3AC = new CorrelationCalculator();
			corr3AC.initData();
			corr3AC.findMuiltiCorrelationByActivitiesCenters("kendall", "home", true, 3);
			corr3AC = null;
//			
			CorrelationCalculator corr4AC = new CorrelationCalculator();
			corr4AC.initData();
			corr4AC.findMuiltiCorrelationByActivitiesCenters("kendall", "home", true, 4);
			
			
			
			//AC------fim-----
			
			
			
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
