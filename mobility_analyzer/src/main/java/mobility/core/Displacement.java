package mobility.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Displacement {
	
	private Long id;
	
	private int displacementCounter = 0;
	
	private List<DisplacementPerDay> listDisplacementsPerDay = new ArrayList<DisplacementPerDay>();
	
	private List<Integer> lowDisplacementPerDay = new ArrayList<Integer>();
	
	private List<Integer> topDisplacementPerDay = new ArrayList<Integer>();
	
	private List<DistanceDisplacement> listDistanceDisplacements = new ArrayList<DistanceDisplacement>();
	
	private List<Double> lowDistanceDisplacement = new ArrayList<Double>();
	
	private List<Double> topDistanceDisplacement = new ArrayList<Double>();
	
	private Double displacementPerDayMedian = 0.0;
	
	private Double distanceDisplacementMedian = 0.0;
	
	public Displacement(){
		
	}

	public int getDisplacementCounter() {
		return displacementCounter;
	}

	public void setDisplacementCounter(int displacementCounter) {
		this.displacementCounter = displacementCounter;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<DistanceDisplacement> getListDistanceDisplacements() {
		return listDistanceDisplacements;
	}

	public void setListDistanceDisplacements(List<DistanceDisplacement> listDistanceDisplacements) {
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

	public Double getDisplacementPerDayMedian() {
		return displacementPerDayMedian;
	}

	public void setDisplacementPerDayMedian(Double displacementPerDayMedian) {
		this.displacementPerDayMedian = displacementPerDayMedian;
	}

	public Double getDistanceDisplacementMedian() {
		return distanceDisplacementMedian;
	}

	public void setDistanceDisplacementMedian(Double distanceDisplacementMedian) {
		this.distanceDisplacementMedian = distanceDisplacementMedian;
	}

	public List<DisplacementPerDay> getListDisplacementsPerDay() {
		return listDisplacementsPerDay;
	}

	public void setListDisplacementsPerDay(List<DisplacementPerDay> listDisplacementsPerDay) {
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

	public void generateLowDisplacementPerDay(int percentage){
		int value = Math.round((float)(getListDisplacementsPerDay().size() * (percentage / 100.0))) ;
		Collections.sort(getListDisplacementsPerDay());
		if(getListDisplacementsPerDay().size() >= value){
			for(int i = 0; i < value; i++){
				getLowDisplacementPerDay().add(
						getListDisplacementsPerDay().get(i).getDisplacementPerDay());
			}
		}
	}
	
	public void generateTopDisplacementPerDay(int percentage){
		int value = Math.round((float)(getListDisplacementsPerDay().size() * (percentage / 100.0))) ;
		Collections.sort(getListDisplacementsPerDay());
		if(getListDisplacementsPerDay().size() >= value){
			for(int i = value; i > 0; i--){
				getTopDisplacementPerDay().add(
						getListDisplacementsPerDay().get(
								getListDisplacementsPerDay().size() - i).getDisplacementPerDay());
			}
		}
	}
	
	public void generateLowDistanceDisplacement(int percentage){
		int value = Math.round((float)(getListDistanceDisplacements().size() * (percentage / 100.0))) ;
		Collections.sort(getListDistanceDisplacements());
		if(getListDistanceDisplacements().size() >= value){
			for(int i = 0; i < value; i++){
				getLowDistanceDisplacement().add(getListDistanceDisplacements().get(i).getDistanceDisplacement());
			}
		}
	}
	
	public void generateTopDistanceDisplacement(int percentage){
		int value = Math.round((float)(getListDistanceDisplacements().size() * (percentage / 100.0))) ;
		Collections.sort(getListDistanceDisplacements());
		if(getListDistanceDisplacements().size() >= value){
			for(int i = value; i > 0; i--){
				getTopDistanceDisplacement().add(getListDistanceDisplacements().
						get(getListDistanceDisplacements().size() - i).getDistanceDisplacement());
			}
		}
	}
	
	public void calculateDisplacementPerDayMedian(){
		Double sum = 0.0;
		if(!getListDisplacementsPerDay().isEmpty()){
			for(DisplacementPerDay val : getListDisplacementsPerDay()){
				sum += val.getDisplacementPerDay();
			}
			Double median = (sum / getListDisplacementsPerDay().size());
			setDisplacementPerDayMedian(median);
		}
	}
	
	public void calculateDistanceDisplacementMedian(){
		Double sum = 0.0;
		if(!getListDistanceDisplacements().isEmpty()){
			for(DistanceDisplacement val : getListDistanceDisplacements()){
				sum += val.getDistanceDisplacement();
			}
			Double median = (sum / getListDistanceDisplacements().size());
			setDistanceDisplacementMedian(median);
		}
	}
	
	
	
	

}
