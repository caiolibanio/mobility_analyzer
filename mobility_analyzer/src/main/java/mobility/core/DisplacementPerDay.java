package mobility.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DisplacementPerDay implements Comparable<DisplacementPerDay>{
	
	private Long id;
	
	private int displacementPerDay = 0;
	
	private Timestamp date;
	
	private Long displacement_id;

	public DisplacementPerDay(int displacementPerDay, Timestamp date, Long displacement_id) {
		super();
		this.displacementPerDay = displacementPerDay;
		this.date = date;
		this.displacement_id = displacement_id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getDisplacementPerDay() {
		return displacementPerDay;
	}

	public void setDisplacementPerDay(int displacementPerDay) {
		this.displacementPerDay = displacementPerDay;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	

	public Long getDisplacement_id() {
		return displacement_id;
	}

	public void setDisplacement_id(Long displacement_id) {
		this.displacement_id = displacement_id;
	}

	@Override
	public int compareTo(DisplacementPerDay arg0) {
		if(this.getDisplacementPerDay() < arg0.getDisplacementPerDay()){
			return -1;
		}else{
			if(this.getDisplacementPerDay() == arg0.getDisplacementPerDay()){
				return 0;
			}
		}
		return 1;
	}
	
	
	
	
	
}
