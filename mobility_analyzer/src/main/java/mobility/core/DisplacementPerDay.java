package mobility.core;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DisplacementPerDay implements Comparable<DisplacementPerDay>{
	
	private Long id;
	
	private int displacementPerDay = 0;
	
	private Timestamp date;
	
	private Displacement displacement;

	public DisplacementPerDay() {

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

	public Displacement getDisplacement() {
		return displacement;
	}

	public void setDisplacement(Displacement displacement) {
		this.displacement = displacement;
	}

	@Override
	public int compareTo(DisplacementPerDay arg0) {
		return this.getDisplacementPerDay() - arg0.getDisplacementPerDay();
	}
	
	
	
	
	
}
