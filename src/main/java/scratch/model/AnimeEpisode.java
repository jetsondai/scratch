package scratch.model;

import java.util.Date;

public class AnimeEpisode {

	// 自增ID
	private Long id;
	
	// 站点ID
	private Long hostId;
	
	//关联对象
	private Anime anime;
	
	//集号
	private Integer number;
	
	//链接
	private String url;
	
	// 抓取时间
	private Date scratchTime;
	
	// 保存事件
	private Date saveTime; 
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Anime getAnime() {
		return anime;
	}

	public void setAnime(Anime anime) {
		this.anime = anime;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Date getScratchTime() {
		return scratchTime;
	}

	public void setScratchTime(Date scratchTime) {
		this.scratchTime = scratchTime;
	}

	public Long getHostId() {
		return hostId;
	}

	public void setHostId(Long hostId) {
		this.hostId = hostId;
	}
	
	public Date getSaveTime() {
		return saveTime;
	}

	public void setSaveTime(Date saveTime) {
		this.saveTime = saveTime;
	}

	@Override
	public String toString() {
		return "AnimeEpisode [id=" + id + ", hostId=" + hostId + ", anime=" + anime + ", number=" + number + ", url="
				+ url + ", scratchTime=" + scratchTime + ", saveTime=" + saveTime + "]";
	}

}
