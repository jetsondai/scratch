package scratch.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;

import scratch.service.Page;

public class BasicDao<E> {

	@Autowired
	protected SessionFactory sessionFactory;
	
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	protected Criteria createCriteria(Class<?> clazz) {
		return getCurrentSession().createCriteria(clazz);
	}
	
	/**
	 * ��������
	 * @param o
	 */
	public void save(Object o) {
		Session session = getCurrentSession();
		session.save(o);
	}
	
	/**
	 * ���¶���
	 * @param o
	 */
	public void update(Object o){
		Session session = getCurrentSession();
		session.update(o);
	}
	
	public void saveOrUpdate(Object o) {
		getCurrentSession().saveOrUpdate(o);
	}
	
	public void saveOrUpdate(Object o, Serializable id) {
		Session session = getCurrentSession();
		//Id��������ڣ�����Ϊ���µĶ���
		if(id == null) {
			session.save(o);
			return;
		}
		//Id���ڣ���ȡ���ݿ��еĶ���
		Object dbObject = session.get(o.getClass(), id);
		if(dbObject != null) {
			try {
				updateObject(o, dbObject, o.getClass());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			dbObject = o;
		}
		session.saveOrUpdate(dbObject);
	}
	
	private void updateObject(Object newObject, Object oldObject, Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			f.setAccessible(true);
			Object newValue = f.get(newObject);
			if(newValue == null) {
				continue;
			}
			f.set(oldObject, newValue);
		}
	}
	
	public void remove(Object o) {
		getCurrentSession().delete(o);
		return;
	}
	
	public void remove(Object o, Long id) {
		o = getCurrentSession().get(o.getClass(), id);
		remove(o);
		return;
	}
	
	
	/**
	 * ��ȡ��������
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E get(Class<E> clazz) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(clazz);
		return (E) criteria.uniqueResult();
	}
	
	public E get(Class<E> clzz, Serializable id) {
		Session session = getCurrentSession();
		return (E) session.get(clzz ,id);
	}
	
	

	/**
	 * ��ȡ�������
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> list(Class<T> clazz) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(clazz);
		return (List<T>) criteria.list();
	}
	
	/**
	 * ͨ��hql��ȡ��������
	 * @param hql
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getByHql(String hql, Object...args) {
		return (T) createQuery(hql, args).uniqueResult();
	}
	
	/**
	 * ͨ��hql��ȡ�������
	 * @param hql
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> listByHql(String hql, Object...args) {
		return (List<T>) createQuery(hql, args).list();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> List<T> listByCriteria(Criteria c, Page page) {
		if(c == null) {
			return null;
		}
		
		//���Ҫ���з�ҳ��ѯ
		if(page != null) {
			//�Ȼ�ȡ�ܼ�¼��
			c.setProjection(Projections.rowCount());
			page.setTotalItem(Long.valueOf(c.uniqueResult().toString()));
			c.setProjection(null);
			//���˼�¼��
			c.setFirstResult(page.getFirstIndex())
				.setMaxResults(page.getPerPageItem());
		}
		return c.list();
	}
	
	
	/**
	 * ͨ��hql���¶���
	 * @param hql
	 * @param args
	 * @return Ӱ������
	 */
	public int updateByHql(String hql, Object...args) {
		return createQuery(hql, args).executeUpdate();
	}
	
	/**
	 * ͨ��hql�Ͳ���������Query����
	 * @param hql
	 * @param args
	 * @return
	 */
	private Query createQuery(String hql, Object...args) {
		int index = 0;
		Session session = getCurrentSession();
		Query query = session.createQuery(hql);
		for(Object o : args) {
			query.setParameter(index++, o);
		}
		return query;
	}
	
}
