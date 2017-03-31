package scratch.aspect;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import scratch.exception.PrivilegeException;
import scratch.model.User;
import scratch.service.UserService;
import scratch.support.CipherSupport;
import scratch.support.GlobalSession;

/**
 * ����1������������AOP�޷����У�û������bean
 * @author Admin
 *
 */
@Component
@Aspect()
public class UserChecker {

	@Autowired
	private CipherSupport cipher;
	
	@Autowired
	private UserService userService;
	
	@Pointcut("execution(* scratch.controller.HomeController.mainPage(..)) || "
			+ "execution(* scratch.controller.SearchInfoController.*(..)) || "
			+ "execution(* scratch.controller.SearchWordController.*(..)) || "
			+ "execution(* scratch.controller.ScratchController.*(..))")
	public void checker(){}
	
	//�����в���ʹ��checked�쳣
	@Before("checker()")
	public void checkUser(){
		HttpServletRequest request =((ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes())
					.getRequest(); 
        HttpSession session = request.getSession();
        
		//���session���Ƿ�����û���Ϣ
        User user = getUserBySession(session);
        if(user == null) {
            //���cookie�Ƿ�����û���Ϣ
        	user = getUserByCookie(request.getCookies());	
        	session.setAttribute(GlobalSession.USER, user);
        }
        
        if(user == null) {
        	throw new PrivilegeException("�볢�Ե�¼���ٴη���ҳ��");
        }
        if(!"1".equals(user.getStatus())) {
        	throw new PrivilegeException("�˺���δ��֤���޷�����ҳ��");
        }
        
        return;
	}
	
	//Ϊʲô���ز�������ΪService����applicationContext�������У������ڴ���ķ�Χ
	//�����������applicationContext������
	//	<context:component-scan base-package="scratch.aspect" />
	//	<aop:aspectj-autoproxy/>
	@Around("@annotation(scratch.aspect.PasswordEncode)")
	public Object encodePswd(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println(pjp);
		Object[] args = pjp.getArgs();
		for(Object a : args) {
			if(User.class.equals(a.getClass())) {
				User u = (User)a;
				String psdw = u.getPassword();
				if(u.getPassword() != null) {
					u.setPassword(cipher.encode(psdw));
				}
			}
		}
		return pjp.proceed(args);
	}
	
	
	private User getUserBySession(HttpSession session) {
		User user = (User) session.getAttribute(GlobalSession.USER);
		if(user == null) return null;
		String username = user.getUsername();
		if(username == null || username.trim().equals("")) return null;
		return user;
	}
	
	/**
	 * ��Cookie�л�ȡuser��userEncode��Ϣ
	 * @param cookies
	 * @return
	 */
	private User getUserByCookie(Cookie[] cookies) {
		if(cookies == null) return null;
		String userId = null;
		String userEncode = null;
        for(Cookie c : cookies){
        	if("user".equals(c.getName())) {
        		userId = c.getValue();
        	}
        	if("userEncode".equals(c.getName())) {
        		userEncode = c.getValue();
        	}
        }
        if(userId == null || userEncode == null) return null;
        
        if(!cipher.decode(userEncode).equals(userId)) return null;
        
        return userService.getById(Long.valueOf(userId));
	}
	
}
