package mobility.statistic;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;

public class ClusteredUser {
	
	private List<Cluster<DoublePoint>> cluster;
	
	private Long user_id;

	public ClusteredUser(List<Cluster<DoublePoint>> cluster, Long user_id) {
		this.cluster = cluster;
		this.user_id = user_id;
	}

	public List<Cluster<DoublePoint>> getCluster() {
		return cluster;
	}

	public void setCluster(List<Cluster<DoublePoint>> cluster) {
		this.cluster = cluster;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ClusteredUser other = (ClusteredUser) obj;
		if (user_id == null) {
			if (other.user_id != null)
				return false;
		} else if (!user_id.equals(other.user_id))
			return false;
		return true;
	}
	
	
	
	

}
