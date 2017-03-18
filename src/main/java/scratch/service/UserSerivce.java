package scratch.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scratch.controller.RegisterController;
import scratch.dao.UserDao;
import scratch.model.User;
import scratch.support.CipherSupport;

@Transactional
@Service
public class UserSerivce {

	private static Logger log = Logger.getLogger(UserSerivce.class);
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private CipherSupport cipher;
	
	@Autowired
	private UserDao dao;
	
	private static long TIME_OUT = 6000000;	//��Ч��100����
	
	public boolean isExist(User user) {
		return (dao.getByName(user) != null);
	}
	
	public User getById(long userId) {
		return dao.getById(userId);
	}
	
	public User check(User user) {
		System.out.println(user.getPassword());
		return dao.getByNameAndPass(user);
	}
	
	public void add(User user) {
		if(isExist(user)) throw new RuntimeException("�˺��Ѿ�����");
		dao.save(user);
		try{
			sendActiviMail(user);	
		} catch (Exception e) {
			throw e;
		}
		return;
	}
	
	public boolean activi(String actiCode) {
		//���ݽ��ܼ����룬��ȡ��Ϣ
		String[] infos = decodeActi(actiCode);
		if(infos == null) {
			return false;
		}
		String username = infos[0];
		long registerTime = Long.valueOf(infos[1]);
		long curTime = System.currentTimeMillis();
		//�жϼ������Ƿ����
		if(registerTime + TIME_OUT < curTime) {
			return false;
		}
		//�����û�״̬
		return dao.updateStatus(username, "1") == 1;
	}
	
	public boolean sendActiviMail(User user) {
		return mailService.sendMail(user.getEmail(), getActiUrl(user.getUsername()));
	}
	
	/**
	 * ���ݼ������ȡ�û�����ע��ʱ��
	 * array[0]:�û���
	 * array[1]:ע��ʱ��
	 * @param actiCode
	 * @return
	 */
	private String[] decodeActi(String actiCode) {
		log.debug("�����룺" + actiCode);
		String info = cipher.decode(actiCode);
		if(info == null) {
			return null;
		}
		log.debug("���ܺ�ļ�������Ϣ��" + info);
		String[] infos = info.split("&");
		if(infos.length != 2) {
			return null;
		}
		return infos;
	}
	
	/**
	 * 
	 * �����û������ɼ����õ�URL
	 * @param username
	 * @return
	 */
	private String getActiUrl(String username) {
		String activitCode = cipher.encode(username + "&" + System.currentTimeMillis());
		String url = MvcUriComponentsBuilder.fromMethodName(
				RegisterController.class, "activiti", activitCode, null)
				.build()
				.encode()
				.toUriString();
		return url;
	}
	
}
