package mobility.core;

import java.sql.Date;
import java.sql.Timestamp;

public class Tweet implements Comparable<Tweet>{
	private  Long tid;
	private String json;
	private Long user_id;
	private Double longitude;
	private Double latitude;
	private String message;
	private Timestamp date;
	private Double returnProb;
	private Double userDisplacement = 0.0;
	private boolean toAnalyse = false;
	
	public Tweet(Long tid, String jsoon) {
		super();
		this.tid = tid;
		this.json = jsoon;
	}

	public Long getTid() {
		return tid;
	}

	public void setTid(Long tid) {
		this.tid = tid;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String jsoon) {
		this.json = jsoon;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public Double getReturnProb() {
		return returnProb;
	}

	public void setReturnProb(Double returnProb) {
		this.returnProb = returnProb;
	}
	

	public Double getUserDisplacement() {
		return userDisplacement;
	}

	public void setUserDisplacement(Double userDisplacement) {
		this.userDisplacement = userDisplacement;
	}

	public boolean isToAnalyse() {
		return toAnalyse;
	}

	public void setToAnalyse(boolean toAnalyse) {
		this.toAnalyse = toAnalyse;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tid == null) ? 0 : tid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tweet other = (Tweet) obj;
		if (tid == null) {
			if (other.tid != null)
				return false;
		} else if (!tid.equals(other.tid))
			return false;
		return true;
	}

	public int compareTo(Tweet arg0) {
		if(this.date.before(arg0.getDate())){
			return -1;
		}else if(this.date.after(arg0.getDate())){
			return 1;
		}else{
			return 0;
		}
	}
	
	
	
	

}
