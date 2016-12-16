package mobility.statistic;

import java.util.List;

import org.apache.commons.math3.ml.clustering.DoublePoint;

public class ClusteredHome {
	
	private Long user_id;
	
	private List<DoublePoint> homeCluster;

	public ClusteredHome(Long user_id, List<DoublePoint> homeCluster) {
		super();
		this.user_id = user_id;
		this.homeCluster = homeCluster;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public List<DoublePoint> getHomeCluster() {
		return homeCluster;
	}

	public void setHomeCluster(List<DoublePoint> homeCluster) {
		this.homeCluster = homeCluster;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((homeCluster == null) ? 0 : homeCluster.hashCode());
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
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
		ClusteredHome other = (ClusteredHome) obj;
		if (homeCluster == null) {
			if (other.homeCluster != null)
				return false;
		} else if (!homeCluster.equals(other.homeCluster))
			return false;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		return true;
	}
	
	
	
	
}
