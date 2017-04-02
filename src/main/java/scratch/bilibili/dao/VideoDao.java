package scratch.bilibili.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import scratch.bilibili.model.Video;
import scratch.dao.BasicDao;
import scratch.service.Page;

@Repository
public class VideoDao extends BasicDao<Video>{

	private static final String QUERY_COUNT_TYPE = "select type.code,count(*) from Video group by type.code";
	
	public static final String ORDER_DATE = "createDate";
	
	public static final String ORDER_PLAY = "play";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Long, Long> listCountByType() {
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<Map> list = getCurrentSession().createQuery(QUERY_COUNT_TYPE)
				.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
				.list();
		for(Map m:list) {
			map.put((Long)m.get("0"), (Long)m.get("1"));
		}
		return map;
	}
	
	/**
	 * ���ܣ�������Ƶ
	 * 
	 * ˵�������type��null�����ѯ�������ͣ�
	 * ���order��empty����null���������ݿ�Ĭ������
	 * ���page��null��������ҳ��ѯ
	 * 
	 * @param keyword	�����Ĺؼ���
	 * @param type		��Ƶ���� 
	 * @param order		����
	 * @param page		ҳ��
	 * @return
	 */
	public List<Video> list(String keyword, Long type, String order, Page page) {
		Criteria c = createCriteria(Video.class);
		if(keyword != null && !keyword.isEmpty()) {
			String[] words = keyword.split(" ");
			Criterion lhs = null;
			for(int i=0; i<words.length; i++) {
				if(words[i].trim().isEmpty()) continue;
				Criterion criterion = Restrictions.like("title", words[i], MatchMode.ANYWHERE);
				if(i == 0) {
					lhs = criterion;
				} else {
					lhs = Restrictions.or(lhs, criterion);
				}
			}
			if(lhs != null) {
				c.add(lhs);
			}
		}
		
		if(type != null) {
			Criteria crt = c.createCriteria("type");
			crt.add(Restrictions.or(
					Restrictions.eq("code", type), 
					Restrictions.eq("parentType.code", type)));
		}
		if(order != null) {
			switch (order) {
				case ORDER_DATE:
					c.addOrder(Order.desc(ORDER_DATE));
					break;
				case ORDER_PLAY:
					c.addOrder(Order.desc(ORDER_PLAY));
				default:
					break;
			}	
		}
		return listByCriteria(c, page);
	}
	
}
