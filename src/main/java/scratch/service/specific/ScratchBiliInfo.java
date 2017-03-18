package scratch.service.specific;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rory.regex.RegexMatch;

import scratch.model.SearchInfo;

/**
 * ִ�о������ҳ����ץȡ
 * @author Admin
 *
 */
@Service
public class ScratchBiliInfo {
	
	/**
	 * http://search.bilibili.com/all?keywrod=?
	 * @param html
	 * @return
	 * @throws Exception
	 */
	public  List<SearchInfo> readInfos(URL url) throws Exception{
			
			List<SearchInfo> searchInfos = new ArrayList<SearchInfo>();
			RegexMatch regexMatch = new RegexMatch(url);
			
			//������Ϣ
			List<String> infoList = regexMatch.findTag("li")
					.filter("video matrix")
					.getResultList();
			
			for(String info:infoList){
				RegexMatch infoMatch = new RegexMatch(info);
				SearchInfo searchInfo = new SearchInfo();
				//��ȡtitle
				String title = infoMatch.findTag("a")
						.filter("class=\"title\"")
						.getAttribute("title")
						.getReuslt();
				
				//��ȡurl������
				String href = infoMatch.findTag("a")
						.filter("class=\"title\"")
						.getAttribute("href")
						.getReuslt();
				if(href != null) {
					href = href.split("\\?")[0];
				}
				
				//��ȡ�ϴ�����
				String pubDate = infoMatch.findTag("span")
						.filter("class=\"so-icon time\"")
						.intercept("</i>", "</span>", ".*")
						.getReuslt();
				
				//��ȡͼƬ����
				String img = infoMatch.findTag("div")
						.filter("class=\"img\"")
						.findTag("img")
						.getAttribute("src")
						.getReuslt();
				
				searchInfo.setTitle(title);
				searchInfo.setUrl(href);
				searchInfo.setPic(img);
				
				if(!pubDate.isEmpty()){
					try{
						searchInfo.setPubDate(java.sql.Date.valueOf(pubDate.trim()));
					}catch(java.lang.IllegalArgumentException e){
						continue;
					}
				}
				
				searchInfos.add(searchInfo);
			}
			 return searchInfos;
		}
}
