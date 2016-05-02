package mobility.dbscan;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

public class TesteDBSCAN {

public static void main(String[] args) throws FileNotFoundException, IOException {
    File[] files = getFiles("./files2/");

    DBSCANClusterer dbscan = new DBSCANClusterer(45.0, 3, new GeoDistance());
    List<Cluster<DoublePoint>> cluster = dbscan.cluster(getGPS(files));

    for(Cluster<DoublePoint> c: cluster){
    	
    	for(DoublePoint p : c.getPoints()){
    		 System.out.println(p);
    	}
    	
       
    }                       
}

private static File[] getFiles(String args) {
    return new File(args).listFiles();
}

private static List<DoublePoint> getGPS(File[] files) throws FileNotFoundException, IOException {

    List<DoublePoint> points = new ArrayList<DoublePoint>();
    for (File f : files) {
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line;

        while ((line = in.readLine()) != null) {
            try {
                double[] d = new double[2];
                d[0] = Double.parseDouble(line.split(",")[0]);
                d[1] = Double.parseDouble(line.split(",")[1]);
                points.add(new DoublePoint(d));
            } catch (ArrayIndexOutOfBoundsException e) {
            } catch(NumberFormatException e){
            }
        }
    }
    return points;
}
}
