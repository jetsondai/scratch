package scratch.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import scratch.model.SearchInfo;
import scratch.model.SearchTag;
import scratch.service.Page;

@Repository
public class SearchInfoDao extends BasicDao{
	
	public static String QUERY_WITH_URL = "from SearchInfo where url = ? ";
	public static String QUERY_WITH_STATUS = "from SearchInfo where status in :status order by pubDate desc";
	public static String QUERY_WITH_TAG_AND_STSATUS = "select s from SearchInfo as s, SearchKeyword k, SearchTag t "
				+ "where s.keyword = k.searchId and k.searchTag = t.tagId and "
				+ "t.tagId = :tagId and s.status in :status "
				+ "order by s.pubDate desc ";
	public static String QUERY_WITH_TAG = "select s from SearchInfo as s, SearchKeyword k, SearchTag t "
			+ "where s.keyword = k.searchId and k.searchTag = t.tagId and "
			+ "t.tagId = :tagId "
			+ "order by s.pubDate desc ";
	
	@SuppressWarnings("unchecked")
	public List<SearchInfo> list(Integer[] status){
		Session session = getCurrentSession();
		Query query = session.createQuery(QUERY_WITH_STATUS);
		query.setParameterList("status", status);
		return (List<SearchInfo>)query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<SearchInfo> listByTagId(long tagId, Integer[] status, int currentPage){
		Session session = getCurrentSession();
		Query query = session.createQuery(QUERY_WITH_TAG_AND_STSATUS);
		query.setParameter("tagId", tagId)
			 .setParameterList("status", status);
		query.setFirstResult(currentPage*20)
			 .setMaxResults(20);
		return (List<SearchInfo>)query.list();
	}
	
	public List<SearchInfo> listByTagId(long tagId, int currentPage){
		return listByTagId(tagId, new Integer[] {0}, currentPage);
	}
	
	public List<SearchInfo> listByTag(Long tagId, Page page) {
		Criteria crt = createCriteria(SearchInfo.class);
		crt.createCriteria("keyword")
			.add(Restrictions.eq("searchTag", new SearchTag(tagId)));
		return listByCriteria(crt, page);
	}
	
	/**
	 * ����״̬
	 * @param searchId
	 * @param status
	 */
	public void updateStatus(long searchId, int status){
		Session session = sessionFactory.getCurrentSession();
		SearchInfo info = (SearchInfo) session.load(SearchInfo.class, searchId);
		info.setStatus(status);
		session.update(info);
	}
	
	@SuppressWarnings("rawtypes")
	public boolean isExistByUrl(String url) {
		List list = listByHql(QUERY_WITH_URL, url);
		return (list == null || list.size() < 1 ? false : true);
	}
	
}
