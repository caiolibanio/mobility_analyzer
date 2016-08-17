package mobility.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

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
	
	public static String pointToTextWithSRID(Point point){
		String text = "ST_SetSRID(ST_MakePoint(" + point.getLongitude() + " " + point.getLatitude() + "), 4326)";
		return text;
	}
	
	public static void writeOnFile(String text, String outPutFileName){
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

}
