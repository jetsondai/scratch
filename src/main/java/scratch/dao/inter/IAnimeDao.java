package scratch.dao.inter;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import scratch.model.Anime;

public interface IAnimeDao {

	List<Anime> findAll();
	
	Anime findById(@Param("id") Long id);
	
	Anime findByAlias(@Param("alias") String alias, Boolean finished);
	
	void save(@Param("anime") Anime anime);
	
	void update(@Param("anime") Anime anime);
	
	void delete(@Param("id") Long id);

	
}
