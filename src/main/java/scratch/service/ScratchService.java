package scratch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scratch.dao.SearchKeywordDao;

@Service
public class ScratchService {
	
	@Autowired
	private SearchKeywordDao keywordDao;
	
	//�ж��û��ǲ��ǵ�һ��ץȡ����
	public boolean alertSearch(Long userId) {
		//�����ùؼ����ҵ��ǹؼ���һ�ζ�û���й�����
		int keyCount = keywordDao.countByUser(userId);
		int keyNoSearchCount = keywordDao.countNoSearchByUser(userId);
		if(keyCount > 0 && keyCount == keyNoSearchCount) {
			return true;
		}
		return false;
	}
	
}
