package scratch.service;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import scratch.aspect.PasswordEncode;
import scratch.controller.RegisterController;
import scratch.controller.UserController;
import scratch.dao.UserDao;
import scratch.exception.MailException;
import scratch.model.User;
import scratch.support.CipherSupport;

@Transactional
@Service
public class UserService {

	private static Logger log = Logger.getLogger(UserService.class);
	
	private static long TIME_OUT = 6000000;	//��Ч��100����
	
	private static String[][] KEY = new String[][] {{"51748915", "87741296"}, 
		{"12744597", "26741096"}};
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private CipherSupport cipher;
	
	@Autowired
	private UserDao dao;
	
	/**
	 * �û�У��(��¼)
	 * @param user
	 * @return
	 */
	@PasswordEncode
	public User verify(User user) {
		return dao.getByNameAndPass(user);
	}
	
	/**
	 * �û�ע��
	 * ��Ҫע������
	 * @param user
	 * @throws MailException 
	 */
	@PasswordEncode
	public void add(User user) throws MailException {
		if(isExist(user)) throw new RuntimeException("�˺��Ѿ�����");
		dao.save(user);
		//�׳�checked�쳣����Ӱ������
		try{
			sendActiviMail(user);	
		} catch (Exception e) {
			throw new MailException();
		}
		return;
	}
	
	public User get(String username, String email) {
		return dao.getByNameAndEmail(username, email);
	}

	@PasswordEncode
	public void update(User u, Long id) {
		dao.update(u, id);
	}
	
	
	/**
	 * �ж��û����Ƿ���ʹ��
	 * @param user
	 * @return
	 */
	public boolean isExist(User user) {
		return (dao.getByName(user) != null);
	}
	
	/**
	 * ͨ���û�id��ȡ�û�
	 * @param userId
	 * @return
	 */
	public User getById(long userId) {
		return dao.getById(userId);
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
	
	public void sendActiviMail(User user) throws MailException, MessagingException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("url", getActiUrl(user.getUserId()));
		mailService.sendMail(MailTemplate.REGISTER, user.getEmail(), map);
		return;
	}
	
	public void sendRestMail(User user) throws MailException, MessagingException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("url", getRestUrl(user.getUserId()));
		mailService.sendMail(MailTemplate.RESETPWD, user.getEmail(), map);
		return;
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
	
	public boolean decodeReset(String encode, Long userId) {
		Long decodeUserId = decrypt(encode, KEY[1]);
		return decodeUserId != null ? decodeUserId.equals(userId) : false; 
	}
	
	/**
	 * 
	 * �����û������ɼ����õ�URL
	 * @param username
	 * @return
	 */
	private String getActiUrl(Long userId) {
		String encodeStr = encrypt(userId, KEY[0]);
		String url = MvcUriComponentsBuilder.fromMethodName(
				RegisterController.class, "activiti", encodeStr, null)
				.build()
				.encode()
				.toUriString();
		return url;
	}
	
	private String getRestUrl(Long userId) {
		String encodeStr = encrypt(userId, KEY[1]);
		String url = MvcUriComponentsBuilder.fromMethodName(
				UserController.class, "resetPasswordForm", encodeStr, userId, null)
				.build()
				.encode()
				.toUriString();
		return url;		
	}
	
	/**
	 * ���ܷ�ʽ��md5(userId & currentTimeMills & key2, key1)
	 * @param userId
	 * @param key
	 * @return
	 */
	private String encrypt(Long userId, String[] key) {
		String encode = userId + "&" + System.currentTimeMillis() + "&" + key[1];
		return cipher.encode(key[0], encode);
	}
	
	private Long decrypt(String input, String[] key) {
		String decode = cipher.decode(key[0], input);
		//�������ݸ����˶�
		String[] infos = (decode == null ? null : decode.split("&"));
		if(infos == null || infos.length != 3) {
			return null;
		}
		//ʱ����Ч�ں˶�
		Long timeStamp = Long.valueOf(infos[1]);
		Long curTime = System.currentTimeMillis();
		if(timeStamp > curTime || (timeStamp + TIME_OUT < curTime)) {
			return null;
		}
		//KEY2�˶�
		if(!key[1].equals(infos[2])) {
			return null;
		}
		//�û���Ч�Ժ˶�
		Long userId = Long.valueOf(infos[0]);
		if(userId == null || userId == 0) {
			return null;
		}
		return userId;
	}
	
}
