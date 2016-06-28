package mobility.statistic;

import mobility.service.SocioDataService;
import mobility.service.UserService;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class CorrelationMethods {
	
	private UserService userService;
	
	private SocioDataService socioDataService;
	
	private KendallsCorrelation kendallsCorrelation;
	
	private PearsonsCorrelation pearsonsCorrelation;
	
	private SpearmansCorrelation spearmansCorrelation;
	
	
	
	public CorrelationMethods() {
		this.userService = new UserService();
		this.socioDataService = new SocioDataService();
		this.kendallsCorrelation = new KendallsCorrelation();
		this.pearsonsCorrelation = new PearsonsCorrelation();
		this.spearmansCorrelation = new SpearmansCorrelation();
	}

	public double findKendallsCorrelationCoef(double[] xArray, double[] yArray){
		return kendallsCorrelation.correlation(xArray, yArray);
	}
	
	public double findPearsonsCorrelationCoef(double[] xArray, double[] yArray){
		return pearsonsCorrelation.correlation(xArray, yArray);
	}
	
	public double findSpearmansCorrelationCoef(double[] xArray, double[] yArray){
		return spearmansCorrelation.correlation(xArray, yArray);
	}
	
	public RealMatrix findKendallsMultiCorrelationCoef(double[][] matrix){
		return kendallsCorrelation.computeCorrelationMatrix(matrix);
	}
	
	public RealMatrix findPearsonsMultiCorrelationCoef(double[][] matrix){
		return pearsonsCorrelation.computeCorrelationMatrix(matrix);
	}
	
	public RealMatrix findSpearmansMultiCorrelationCoef(double[][] matrix){
		return spearmansCorrelation.computeCorrelationMatrix(matrix);
	}
	
	
	
	
	

}
