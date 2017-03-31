package scratch.service.specific;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rory.regex.ParameterURL;

import scratch.dao.SearchInfoDao;
import scratch.dao.SearchTagDao;
import scratch.model.SearchInfo;
import scratch.model.SearchKeyword;
import scratch.model.SearchTag;
import scratch.model.dictionary.SearchType;
import scratch.service.SearchKeywordService;

@Transactional
@Service
public class ScratchBili {
	
	//Bվ ����page���50ҳ���������ݲ��ṩ
	private static String SEARCHURL = "http://search.bilibili.com/all";
	
	private static Logger log = Logger.getLogger(ScratchBili.class);
	
	@Autowired
	private SearchInfoDao searchInfoDao;
	
	@Autowired
	private SearchTagDao tagDao;
	
	@Autowired
	private SearchKeywordService keywordService;
	
	@Autowired
	private ScratchBiliInfo scratcchBiliInfo;
	
	/**
	 * ���ݱ�ǩ��Ϣ�������ؼ���
	 * @param tagId
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void run(long tagId) throws MalformedURLException, IOException{
		
		List<SearchInfo> searchInfoList = new ArrayList<SearchInfo>();
		List<SearchKeyword> keywordList = new ArrayList<SearchKeyword>();
		Map<String, String> param = new HashMap<String, String>();
		
		//��ȡ��ǩ��Ϣ
		SearchTag tag = tagDao.get(SearchTag.class, tagId);
		//�����ؼ����б�
		keywordList = tag.getSearchKeyWords();
		if(keywordList == null) return;
		//��ȡ������Ϣ
		SearchType type = tag.getType();
		//��ʼ����url������Ϣ
		if(type != null && !type.getCode().equals(0)) {
			param.put("tids_1", type.getCode().toString());
		}
		param.put("order", "totalrank");
		//ƴ�����ӣ�����ÿ�����ӵ�����
		for(SearchKeyword k:keywordList){
			boolean jump = false;
			Date searchDate = k.getLastSearchTime();
			//���ݹؼ��֣�ƴ������URL
			param.put("keyword", k.getKeyword());
			List<URL> urlList = listURL(param);
			for(URL u : urlList) {
				List<SearchInfo> list = new ArrayList<SearchInfo>();
				//��ӡ��������
				log.debug("���ڶ�ȡURL��" + u.toString());
				//��ȡurl�е�Ԫ����Ϣ
				try {
					list = scratcchBiliInfo.readInfos(u);
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.debug("��ץȡ�����ݣ�" + list);
				//����Ĭ��ֵ
				for(SearchInfo info : list) {
					/*log.debug("�ϴ�ץȡ�����ڣ�" + searchDate + "; ���ݷ���������" + info.getPubDate());
					//ʱ�䲻�ԣ�ʱ��Ӧ���������ڱȽ�
					if(searchDate != null && searchDate.after(info.getPubDate())) {
						log.debug("ץȡ�����ݹ��ڣ����ݼ�������");
						jump = true;
						break;
					}*/
					log.debug("��ʼ�������ݻ�����Ϣ");
					info.setKeyword(k);
					info.setStatus(0);
					searchInfoList.add(info);
				}
				if(jump) {
					break;
				}
			}
		}
		
		//��ӡ�����Ϣ
		log.debug("������������");
		updateInfo(searchInfoList, keywordList);
	}

	private List<URL> listURL(Map<String, String> param) {
		List<URL> list = new ArrayList<URL>();
		ParameterURL pURL = new ParameterURL(SEARCHURL);
		//����URL������Ϣ
		for(Entry<String, String> entry : param.entrySet()) {
			pURL.setParameter(entry.getKey(), entry.getValue());
		}
		//���ü���ҳ��
		for(int p=0; p<10; p++){
			pURL.setParameter("page", String.valueOf(p));
			list.add(pURL.getURL());
		}
		return list;
	}
	
	public void updateInfo(List<SearchInfo> searchInfos, List<SearchKeyword> searchKeywords) {
		//�������������ݱ��浽���ݿ�
		for(SearchInfo searchInfo:searchInfos){
			if(searchInfoDao.isExistByUrl(searchInfo.getUrl())){
				continue;
			}
			searchInfoDao.save(searchInfo);
		}
		
		//���¼���ʱ��
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		for(SearchKeyword searchKeyword:searchKeywords){
			keywordService.modifySearchTime(searchKeyword.getSearchId(), curTime);
		}
	}
}
