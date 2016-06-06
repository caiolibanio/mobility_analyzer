package mobility.DAO;

import java.util.List;

/**
 * http://www.mballem.com/
 */
public interface IDAO<T> {
	
    void save(T entidade);
    
    int findMaxId();
    
    List<T> findAll();
    
}
