package scratch.service.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import scratch.exception.MailException;
import scratch.model.User;
import scratch.service.UserSerivce;
import scratch.test.ContextClass;

public class UserServiceTest extends ContextClass{

	@Autowired
	private UserSerivce userSerivce;
	
	/**
	 * �û�ע�����
	 * 1.���������Ƿ�ᵼ���û�ע��ع���
	 * 2.���û�ע��������checked�쳣��ʹ�ʼ�����ʧ�ܣ�Ҳ���ᵼ��ע���û�����ع�
	 * @throws MailException 
	 */
	@Rollback
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Test
	public void registerTest() throws MailException {
		User user = new User("hejianok", "hejianok");
		user.setEmail("398299262@qq.com");
		userSerivce.add(user);
	}
	
}
