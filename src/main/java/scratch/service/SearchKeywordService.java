package scratch.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import scratch.dao.SearchKeywordDao;
import scratch.model.SearchKeyword;

@Transactional
@Service
public class SearchKeywordService {

	@Autowired
	private SearchKeywordDao searchKeywordDao;
	
	//��������
	public void updateList(List<SearchKeyword> keywords){
		for(SearchKeyword word:keywords){
			searchKeywordDao.save(word);
		}
	}
	
	public void update(SearchKeyword word) {
		searchKeywordDao.saveOrUpdate(word, word.getSearchId());
	}
	
	public void delete(SearchKeyword word) {
		searchKeywordDao.remove(word);
	}
	
	public void modifySearchTime(long id, Timestamp searchTime){
		searchKeywordDao.modifySearchTime(id, searchTime);
	}
	
	public void modifySearchTime(long id){
		searchKeywordDao.modifySearchTime(id, new Timestamp(System.currentTimeMillis()));
	}
	
	public List<SearchKeyword> listByTagId(long tagId) {
		if(tagId == 0){
			//�������йؼ���
			return searchKeywordDao.list(SearchKeyword.class);
		}else{
			//����ָ��tag�Ĺؼ���
			return searchKeywordDao.listByTagId(tagId);
		}
	}
}
