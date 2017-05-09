package scratch.dao.inter;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import scratch.model.Anime;
import scratch.model.AnimeEpisode;

public interface IAnimeEpisodeDao {

	/**
	 * 根据URL查找对象
	 * @param url
	 * @return
	 */
	AnimeEpisode findByUrl(@Param("url") String url);
	
	/**
	 * 以Anime id 和抓取时间>time为条件，检索对象
	 * @param anime
	 * @param time
	 * @return
	 */
	List<AnimeEpisode> findByAnimeAndTime(@Param("anime") Anime anime, @Param("time") Date time);
	
	void save(@Param("episode") AnimeEpisode episode);
	
}