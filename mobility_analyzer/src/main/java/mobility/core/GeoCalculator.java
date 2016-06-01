package mobility.core;

import java.util.Collections;
import java.util.List;

import mobility.geodesy.Ellipsoid;
import mobility.geodesy.GeodeticCalculator;
import mobility.geodesy.GlobalPosition;



public class GeoCalculator {
	
	public GeoCalculator(){}
	
	public Double[] calculateMidPoint(List<Point> listPoints){
		
		Double totalWeight = 0.;
		Double combinedX = 0.;
		Double combinedY = 0.;
		Double combinedZ = 0.;
		Double lon = 0.;
		Double lat = 0.;
		Double hyp = 0.;
		Double[] coordsOut = new Double[2];
		
		//convert decimal degrees to radians
		for(Point p : listPoints){
			p.setLatitude(p.getLatitude() * (Math.PI / 180));
			p.setLongitude(p.getLongitude() * (Math.PI / 180));
			totalWeight += p.getWeight();
			
		}
		
		//Convert lat/long to cartesian (x,y,z) coordinates
		for(Point p : listPoints){
			p.setX(Math.cos(p.getLatitude()) * Math.cos(p.getLongitude()));
			p.setY(Math.cos(p.getLatitude()) * Math.sin(p.getLongitude()));
			p.setZ(Math.sin(p.getLatitude()));
		}
		
		//Compute combined weighted cartesian coordinate
		for(Point p : listPoints){
			combinedX += p.getX() * p.getWeight();
			combinedY += p.getY() * p.getWeight();
			combinedZ += p.getZ() * p.getWeight();
		}
		combinedX = combinedX / totalWeight;
		combinedY = combinedY / totalWeight;
		combinedZ = combinedZ / totalWeight;
		
		//Convert cartesian coordinate to latitude and longitude for the midpoint
		lon = Math.atan2(combinedY, combinedX);
		hyp = Math.sqrt(combinedX * combinedX + combinedY * combinedY);
		lat = Math.atan2(combinedZ, hyp);
		
		//Convert midpoint lat and lon from radians to degrees
		coordsOut[0] = lat * (180/Math.PI);
		coordsOut[1] = lon * (180/Math.PI);
		
		return coordsOut;
		
		
	}
	
	public Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) { //in meters
		GeodeticCalculator geoCalc = new GeodeticCalculator();
		Ellipsoid reference = Ellipsoid.WGS84;
		GlobalPosition pointA = new GlobalPosition(lat1, lon1, 0.0); // Point A
		GlobalPosition userPos = new GlobalPosition(lat2, lon2, 0.0); // Point B
		double distance = geoCalc.calculateGeodeticCurve(reference, userPos, pointA).getEllipsoidalDistance(); // Distance between Point A and Point B
		return distance;
		
	}
	
	public void generateReturnProbs(List<Tweet> listTweets){
		int countProx = 0;
		for(Tweet ti : listTweets){
			for(Tweet ty : listTweets){
				if(calculateDistance(ti.getLatitude(), ti.getLongitude(),
						ty.getLatitude(), ty.getLongitude()) <= 45.0){
					countProx++;
				}
			}
			ti.setReturnProb(countProx / (double) listTweets.size());
			countProx = 0;
		}
	}
	
	public void generateDisplacement(List<Tweet> listTweets){
		Collections.sort(listTweets);
		for(int i = 1; i < listTweets.size(); i++){
			Tweet tweet = listTweets.get(i);
			Tweet predTweet = listTweets.get(i-1);
			tweet.setUserDisplacement(calculateDistance(tweet.getLatitude(), 
					tweet.getLongitude(), predTweet.getLatitude(), predTweet.getLongitude()));
		}
	}
	
	public Double calculateRadiusOfGyration(List<Point> points, Point point){ //in meters
		Double sum = 0.0;
		
		for(Point t : points){
			Double base = calculateDistance(t.getLatitude(), t.getLongitude(), point.getLatitude(), point.getLongitude());
			sum += Math.pow(base, 2);
		}
		sum = Math.sqrt((sum / points.size()));
		return sum;
	}
	
	
	
}
