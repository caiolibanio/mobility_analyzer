package mobility.util;

import mobility.core.Point;

public class Util {
	
	public static Point textToPoint(String text){
		Point point = new Point();
		text = text.replace("POINT(", "");
		text = text.replace(")", "");
		String[] coords = text.split(" ");
		point = new Point(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
		return point;
	}
	
	public static String pointToText(Point point){
		String text = "'POINT(" + point.getLongitude() + " " + point.getLatitude() + ")'";
		return text;
	}

}
