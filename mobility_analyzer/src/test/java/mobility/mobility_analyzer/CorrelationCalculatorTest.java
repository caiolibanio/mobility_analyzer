package mobility.mobility_analyzer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;

import mobility.core.User;
import mobility.statistic.CorrelationCalculator;

public class CorrelationCalculatorTest {
	
	private ArrayList<ArrayList<String>> matrixSocialData = null;
	
	private static List<User> listUsers;
	
	private List<String> columnsToIgnore = new ArrayList<String>();
	
	@Before
	public void setUp() {
		ArrayList<String> l1 = new ArrayList<String>(Arrays.asList("code",
				"age_structure_all_ages", "age_structure_0_15"));
		ArrayList<String> l2 = new ArrayList<String>(Arrays.asList("100",
				"6", "9"));
		ArrayList<String> l3 = new ArrayList<String>(Arrays.asList("101",
				"94", "89"));
		ArrayList<String> l4 = new ArrayList<String>(Arrays.asList("102",
				"554", "842"));
		ArrayList<String> l5 = new ArrayList<String>(Arrays.asList("103",
				"600", "900"));
		
		
		
		matrixSocialData = new ArrayList<ArrayList<String>>();
		matrixSocialData.add(l1);
		matrixSocialData.add(l2);
		matrixSocialData.add(l3);
		matrixSocialData.add(l4);
		matrixSocialData.add(l5);
		
		listUsers = new ArrayList<User>();
		User u1 = new User(null);
		u1.setRadiusOfGyration(5.2);
		u1.setUser_movement(10.0);
		u1.setNum_messages(10);
		listUsers.add(u1);
		
		User u2 = new User(null);
		u2.setRadiusOfGyration(12.2);
		u2.setUser_movement(15.0);
		u2.setNum_messages(9);
		listUsers.add(u2);
		
		User u3 = new User(null);
		u3.setRadiusOfGyration(20.2);
		u3.setUser_movement(11.0);
		u3.setNum_messages(20);
		listUsers.add(u3);
		
		User u4 = new User(null);
		u4.setRadiusOfGyration(22.2);
		u4.setUser_movement(19.0);
		u4.setNum_messages(20);
		listUsers.add(u4);
		
	}
	
	@Test
	public void Testing(){
		CorrelationCalculator corr = new CorrelationCalculator();
		corr.initDataToTest(matrixSocialData, listUsers);
		RealMatrix matrixResult = corr.findMuiltiCorrelationAllToTest("kendall", "home");
		double[] columnsVals = matrixResult.getColumn(3);
		
		assertEquals(1, columnsVals[0], 0.001);
		assertEquals(0.6666667, columnsVals[1], 0.001);
		assertEquals(0.5477226, columnsVals[2], 0.001);
	}
	
	

}
