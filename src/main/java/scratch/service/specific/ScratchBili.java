package scratch.service.specific;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rory.regex.ParameterURL;

import scratch.dao.SearchInfoDao;
import scratch.model.SearchInfo;
import scratch.model.SearchKeyword;
import scratch.service.SearchKeywordService;

@Transactional
@Service
public class ScratchBili {
	
	//Bվ ����page���50ҳ���������ݲ��ṩ
	private static String SEARCHURL = "http://search.bilibili.com/all?keyword=:keyword&order=:order&page=:page";
	
	@Autowired
	private SearchInfoDao searchInfoDao;
	
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
		
		keywordList = keywordService.listByTagId(tagId);
		if(keywordList == null) return;
		
		//ƴ�����ӣ�����ÿ�����ӵ�����
		for(SearchKeyword k:keywordList){
			boolean jump = false;
			Date searchDate = k.getLastSearchTime();
			//���ݹؼ��֣�ƴ������URL
			List<URL> urlList = listURL(k);
			for(URL u : urlList) {
				List<SearchInfo> list = new ArrayList<SearchInfo>();
				//��ӡ��������
				System.out.println("URL��" + u.toString());
				//��ȡurl�е�Ԫ����Ϣ
				try {
					list = scratcchBiliInfo.readInfos(u);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//����Ĭ��ֵ
				for(SearchInfo info : list) {
					//ʱ�䲻�ԣ�ʱ��Ӧ���������ڱȽ�
					if(searchDate != null && searchDate.after(info.getPubDate())) {
						jump = true;
						break;
					}
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
		System.out.println(searchInfoList);
		
		updateInfo(searchInfoList, keywordList);
	}

	private List<URL> listURL(SearchKeyword w) {
		List<URL> list = new ArrayList<URL>();
		ParameterURL pURL = new ParameterURL(SEARCHURL);
		pURL.setParameter("keyword", w.getKeyword());
		pURL.setParameter("order", "pubdate");
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
