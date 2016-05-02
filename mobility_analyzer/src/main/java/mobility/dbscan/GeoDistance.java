package mobility.dbscan;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import mobility.core.GeoCalculator;

public class GeoDistance implements DistanceMeasure {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1544277339281429749L;
	private GeoCalculator calc = new GeoCalculator();

	public double compute(double[] a, double[] b) throws DimensionMismatchException {
		double result = calc.calculateDistance(a[0], a[1], b[0], b[1]);
		return result;
	}

}
