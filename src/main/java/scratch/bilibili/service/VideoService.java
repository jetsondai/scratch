package scratch.bilibili.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scratch.bilibili.dao.VideoDao;
import scratch.bilibili.model.Video;
import scratch.dao.SearchTagDao;
import scratch.model.SearchKeyword;
import scratch.model.SearchTag;
import scratch.service.Page;

@Service
public class VideoService {

	@Autowired
	private VideoDao videoDao;
	
	@Autowired
	private SearchTagDao tagDao;
	
	@Autowired
	private Page page;
	
	public List<Video> list(Long type, String order, Integer pageNo) {
		return list(null, type, order, pageNo);
	}
	
	public List<Video> list(String keyword, Long type, String order, Integer pageNo) {
		page.setCurPage(pageNo);
		return videoDao.list(keyword, type, order, page);
	}
	
	public List<Video> list(Long tagId, Integer pageNo) {
		page.setCurPage(pageNo);
		SearchTag tag = tagDao.get(SearchTag.class, tagId);
		Long type = tag.getType().getCode();						//��������
		List<SearchKeyword> searchWords = tag.getSearchKeyWords();	//�������ǩ��Ӧ�����йؼ���
		String keyword = "";
		for(SearchKeyword w : searchWords) {
			keyword += keyword.isEmpty() ? w.getKeyword() : " " + w.getKeyword(); 
		}
		//���ݹؼ��֡���Ƶ���͡��������ڼ���
		return videoDao.list(keyword, type, VideoDao.ORDER_DATE, page);
	}
	
}
