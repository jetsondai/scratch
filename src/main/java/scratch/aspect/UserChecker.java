package scratch.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import scratch.context.CookieSupport;
import scratch.context.SessionContext;
import scratch.exception.PrivilegeException;
import scratch.model.entity.User;

import static scratch.exception.PrivilegeException.*;

import scratch.service.UserService;
import scratch.support.cipher.CipherSupport;
import scratch.support.web.spring.SessionUtils;


/**
 * 1.核对用户身份
 * 2.用户密码加密
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
	
//	@Before("@annotation(scratch.aspect.UserRole)")
	public void checkUser(JoinPoint pjp) throws Throwable{
		//读取注解，通过注解判断相应的用户权限
		Annotation[] annotations = getAnnotations(pjp);
		Role role = Role.User;
		boolean activi = true;
		for(Annotation a : annotations) {
			if (a instanceof UserRole) {
				role = ((UserRole) a).value();
				activi = ((UserRole) a).activi(); 
				break;
			}
		}
		//从Session中读取用户信息
		//读不取不到，则从Cookie中读取用户信息
        User user = SessionUtils.getAttribute(SessionContext.USER, User.class);
        if(user == null) {
        	user = CookieSupport.getUser();
        	if(user != null) {
        		user = userService.getById(Long.valueOf(user.getUserId()));
        		//Cookie中获取信息后，保存到Session中
        		SessionUtils.setAttribute(SessionContext.USER, user);
        	}
        }
        //核对用户是否存在
        if(user == null) {
        	throw new PrivilegeException(NOLOGIN);
        }
        //核对用户状态
        if(activi) {
        	/*if(!"1".equals(user.getStatus())) {
        		throw new PrivilegeException(NOACTIVI);
        	}*/
        }
        //管理员才能使用的页面，核对用户账号
        if(role == Role.Admin && !user.getRole().equals(Role.Admin.ordinal())) {
        	throw new PrivilegeException("无效页面"); 
        } 
        return;
	}

	private Annotation[] getAnnotations(JoinPoint pjp) {
		MethodSignature sign = (MethodSignature) pjp.getSignature();
		Method method = sign.getMethod();
		return method.getAnnotations();
	}
	
	/**
	 * 该通知主要用于Service层的密码加密
	 * Service的Bean必须与Aop代理处于同Bean容器中，否则Aop无法代理
	 * 解决：在相应上下文中加入Aop代理
	 * <context:component-scan base-package="scratch.aspect" />
	 * <aop:aspectj-autoproxy/>
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(scratch.aspect.PasswordEncode)")
	public Object encodePswd(ProceedingJoinPoint pjp) throws Throwable {
		
		Object[] args = pjp.getArgs();
		Parameter[] params = ((MethodSignature)pjp.getSignature())
				.getMethod().getParameters();
		
		// 对需要加密的两种情况进行判断(前提需要加密的字段不能为空)
		
		// 1.判断是否标记了@PasswordEncode的String参数
		for(int i=0; i<params.length; i++) {
			Parameter p = params[i];
			if(p.getAnnotation(PasswordEncode.class) != null && 
					String.class.equals(p.getType()) && 
					String.class.equals(args[i].getClass())) {
				String pswd = (String)args[i];
				if(!StringUtils.isEmpty(pswd)) {
					args[i] = cipher.encode(pswd);	
				}
			}
		}
		
		// 2.判断是否含有User类型的参数且存在password字段
		Arrays.asList(args).stream()
		.filter(arg -> {
			return User.class.equals(arg.getClass());
		})
		.forEach(arg -> {
			User u = (User)arg;
			String psdw = u.getPassword();
			if(!StringUtils.isEmpty(psdw)) {
				u.setPassword(cipher.encode(psdw));
			}
			
		});
		
		return pjp.proceed(args);
	}
	
}
