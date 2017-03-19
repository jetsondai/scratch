package scratch.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import scratch.controller.RegisterController;
import scratch.dao.UserDao;
import scratch.exception.MailException;
import scratch.model.User;
import scratch.support.CipherSupport;

@Transactional
@Service
public class UserSerivce {

	private static Logger log = Logger.getLogger(UserSerivce.class);
	
	private static long TIME_OUT = 6000000;	//有效期100分钟
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private CipherSupport cipher;
	
	@Autowired
	private UserDao dao;
	
	/**
	 * 用户校验(登录)
	 * @param user
	 * @return
	 */
	public User verify(User user) {
		return dao.getByNameAndPass(user);
	}
	
	/**
	 * 用户注册
	 * 需要注意事务
	 * @param user
	 * @throws MailException 
	 */
	public void add(User user) throws MailException {
		if(isExist(user)) throw new RuntimeException("账号已经存在");
		dao.save(user);
		//抛出checked异常，不影响事务
		try{
			sendActiviMail(user);	
		} catch (Exception e) {
			throw new MailException();
		}
		return;
	}
	
	/**
	 * 判断用户名是否被人使用
	 * @param user
	 * @return
	 */
	public boolean isExist(User user) {
		return (dao.getByName(user) != null);
	}
	
	/**
	 * 通过用户id获取用户
	 * @param userId
	 * @return
	 */
	public User getById(long userId) {
		return dao.getById(userId);
	}
	
	public boolean activi(String actiCode) {
		//根据解密激活码，获取信息
		String[] infos = decodeActi(actiCode);
		if(infos == null) {
			return false;
		}
		String username = infos[0];
		long registerTime = Long.valueOf(infos[1]);
		long curTime = System.currentTimeMillis();
		//判断激活码是否过期
		if(registerTime + TIME_OUT < curTime) {
			return false;
		}
		//激活用户状态
		return dao.updateStatus(username, "1") == 1;
	}
	
	public boolean sendActiviMail(User user) {
		return mailService.sendMail(user.getEmail(), getActiUrl(user.getUsername()));
	}
	
	/**
	 * 根据激活码获取用户名和注册时间
	 * array[0]:用户名
	 * array[1]:注册时间
	 * @param actiCode
	 * @return
	 */
	private String[] decodeActi(String actiCode) {
		log.debug("激活码：" + actiCode);
		String info = cipher.decode(actiCode);
		if(info == null) {
			return null;
		}
		log.debug("解密后的激活码信息：" + info);
		String[] infos = info.split("&");
		if(infos.length != 2) {
			return null;
		}
		return infos;
	}
	
	/**
	 * 
	 * 根据用户名生成激活用的URL
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
