package mobility.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Displacement {
	
	private Double total_Displacement = 0.0;
	
	private int displacementCounter = 0;
	
	private int displacementPerDay = 0;
	
	private List<Integer> listDisplacementsPerDay = new ArrayList<Integer>();
	
	private List<Integer> lowDisplacementPerDay = new ArrayList<Integer>();
	
	private List<Integer> topDisplacementPerDay = new ArrayList<Integer>();
	
	private List<Double> listDistanceDisplacements = new ArrayList<Double>();
	
	private List<Double> lowDistanceDisplacement = new ArrayList<Double>();
	
	private List<Double> topDistanceDisplacement = new ArrayList<Double>();
	
	public Displacement(){
		
	}

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

	public List<Integer> getListDisplacementsPerDay() {
		return listDisplacementsPerDay;
	}

	public void setListDisplacementsPerDay(List<Integer> listDisplacementsPerDay) {
		this.listDisplacementsPerDay = listDisplacementsPerDay;
	}

	public List<Integer> getLowDisplacementPerDay() {
		return lowDisplacementPerDay;
	}

	public void setLowDisplacementPerDay(List<Integer> lowDisplacementPerDay) {
		this.lowDisplacementPerDay = lowDisplacementPerDay;
	}

	public List<Integer> getTopDisplacementPerDay() {
		return topDisplacementPerDay;
	}

	public void setTopDisplacementPerDay(List<Integer> topDisplacementPerDay) {
		this.topDisplacementPerDay = topDisplacementPerDay;
	}

	public List<Double> getListDistanceDisplacements() {
		return listDistanceDisplacements;
	}

	public void setListDistanceDisplacements(List<Double> listDistanceDisplacements) {
		this.listDistanceDisplacements = listDistanceDisplacements;
	}

	public List<Double> getLowDistanceDisplacement() {
		return lowDistanceDisplacement;
	}

	public void setLowDistanceDisplacement(List<Double> lowDistanceDisplacement) {
		this.lowDistanceDisplacement = lowDistanceDisplacement;
	}

	public List<Double> getTopDistanceDisplacement() {
		return topDistanceDisplacement;
	}

	public void setTopDistanceDisplacement(List<Double> topDistanceDisplacement) {
		this.topDistanceDisplacement = topDistanceDisplacement;
	}

	public void generateLowDisplacementPerDay(int percentage){
		int value = Math.round((float)(getListDisplacementsPerDay().size() * (percentage / 100.0))) ;
		Collections.sort(getListDisplacementsPerDay());
		if(getListDisplacementsPerDay().size() >= value){
			for(int i = 0; i < value; i++){
				getLowDisplacementPerDay().add(getListDisplacementsPerDay().get(i));
			}
		}
	}
	
	public void generateTopDisplacementPerDay(int percentage){
		int value = Math.round((float)(getListDisplacementsPerDay().size() * (percentage / 100.0))) ;
		Collections.sort(getListDisplacementsPerDay());
		if(getListDisplacementsPerDay().size() >= value){
			for(int i = value; i >= 0; i--){
				getTopDisplacementPerDay().add(getListDisplacementsPerDay().
						get(getListDisplacementsPerDay().size() - i));
			}
		}
	}
	
	public void generateLowDistanceDisplacement(int percentage){
		int value = Math.round((float)(getListDistanceDisplacements().size() * (percentage / 100.0))) ;
		Collections.sort(getListDistanceDisplacements());
		if(getListDistanceDisplacements().size() >= value){
			for(int i = 0; i < value; i++){
				getLowDistanceDisplacement().add(getListDistanceDisplacements().get(i));
			}
		}
	}
	
	public void generateTopDistanceDisplacement(int percentage){
		int value = Math.round((float)(getListDistanceDisplacements().size() * (percentage / 100.0))) ;
		Collections.sort(getListDistanceDisplacements());
		if(getListDistanceDisplacements().size() >= value){
			for(int i = value; i >= 0; i--){
				getTopDistanceDisplacement().add(getListDistanceDisplacements().
						get(getListDistanceDisplacements().size() - i));
			}
		}
	}
	
	
	
	

}
