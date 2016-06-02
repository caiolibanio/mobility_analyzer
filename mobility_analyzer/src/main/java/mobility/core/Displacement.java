package mobility.core;

import java.util.List;

public class Displacement {
	
	private Double total_Displacement;
	
	private int displacementCounter;
	
	private int displacementPerDay;
	
	private List<Integer> listDisplacements;
	
	private List<Integer> lowDisplacement;
	
	private List<Integer> topDisplacement;
	
	public Displacement(){}

	public Double getTotal_Displacement() {
		return total_Displacement;
	}

	public void setTotal_Displacement(Double total_Displacement) {
		this.total_Displacement = total_Displacement;
	}

	public int getDisplacementCounter() {
		return displacementCounter;
	}

	public void setDisplacementCounter(int displacementCounter) {
		this.displacementCounter = displacementCounter;
	}

	public int getDisplacementPerDay() {
		return displacementPerDay;
	}

	public void setDisplacementPerDay(int displacementPerDay) {
		this.displacementPerDay = displacementPerDay;
	}

	public List<Integer> getLowDisplacement() {
		return lowDisplacement;
	}

	public void setLowDisplacement(List<Integer> lowDisplacement) {
		this.lowDisplacement = lowDisplacement;
	}

	public List<Integer> getTopDisplacement() {
		return topDisplacement;
	}

	public void setTopDisplacement(List<Integer> topDisplacement) {
		this.topDisplacement = topDisplacement;
	}

	public List<Integer> getListDisplacements() {
		return listDisplacements;
	}

	public void setListDisplacements(List<Integer> listDisplacements) {
		this.listDisplacements = listDisplacements;
	}
	
	

}
