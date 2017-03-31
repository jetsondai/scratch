package scratch.controller;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import scratch.exception.MailException;
import scratch.model.User;
import scratch.service.UserService;

@Controller
@SessionAttributes({"key", "userId"})
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/user/update/pwd", method=RequestMethod.POST)
	public void updatePassword() {
		
	}
	
	/**
	 * �������ã���д������Ϣ��
	 * @return
	 */
	@RequestMapping(value="/user/reset", method=RequestMethod.GET)
	public String resetPasswordForm() {
		//��ʾ��д��
		return "user_reset";
	}
	
	/**
	 * �������ã��ʼ�����������������
	 * @param username
	 * @param email
	 * @param ra
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/user/resetmail", method=RequestMethod.POST)
	public String resetPasswordEmail(@RequestParam("username") String username, 
			@RequestParam("email") String email, RedirectAttributes ra, Model model) {
		//�ж��ʼ��Ƿ����ڸ��˺�
		User user = userService.get(username, email);
		if(user == null){
			//��ʾ����
			ra.addAttribute("error", "������������");
			return "redirect:/user/reset";
		}
		//���������ʼ�
		try {
			userService.sendRestMail(user);
		} catch (MailException | MessagingException e) {
			//��ʾ����
			e.printStackTrace();
			model.addAttribute("error", "�ʼ�����ʧ��");
			return "common_message";
		}
		model.addAttribute("error", "�ʼ����ͳɹ�");
		return "common_message";
	}
	
	/**
	 * �������ã��˶�����URL��Ч�ԣ�����������д��
	 * @param key
	 */
	@RequestMapping(value="/user/resetpwd", method=RequestMethod.GET)
	public String resetPasswordForm(@RequestParam("key") String key, 
			@RequestParam("user") Long userId, Model model) {
		//У��KEY
		if(!userService.decodeReset(key, userId)) {
			model.addAttribute("error", "��Ч����");
			return "common_message";
		}
		//������������ҳ��
		model.addAttribute("userId", userId);
		return "user_reset_pwd";
	}
	
	/**
	 * �������ã������������
	 * @param password
	 * @param rePassword
	 * @param key			KEYӦ���û������KEYʹ�����֮�󣬾�Ӧ����������
	 * @param userId
	 * @param model
	 * @param ra
	 * @return
	 */
	@RequestMapping(value="/user/resetpwd", method=RequestMethod.POST)
	public String resetPassword(@RequestParam("password") String password,
			@RequestParam("repassword") String rePassword,
			@ModelAttribute("userId") Long userId, 
			Model model, RedirectAttributes ra, SessionStatus status) {
		
		if(!password.equals(rePassword)) {
			ra.addAttribute("error", "�������벻һ��");
			return "redirect:/user/resetpwd";
		}
		
		User u = new User();
		u.setPassword(password);
		userService.update(u, userId);
		
		status.setComplete();
		model.addAttribute("success", "������³ɹ�");
		return "common_message";
	}
	
}
