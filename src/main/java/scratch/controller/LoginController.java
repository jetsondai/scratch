package scratch.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import scratch.model.User;
import scratch.service.UserService;
import scratch.support.CookieSupport;
import scratch.support.GlobalSession;
import scratch.support.SessionSupport;

@Controller
@RequestMapping("/user")
public class LoginController {

	@Autowired
	private UserService service;
	
	/**
	 * �û���¼����
	 * @return
	 */
	@RequestMapping(path="/login", method=RequestMethod.GET)
	public String loginForm() {
		return "user_login";
	}
	
	/**
	 *�û���¼���� 
	 */
	@RequestMapping(path="/login", method=RequestMethod.POST)
	public String login(@ModelAttribute("user") User user, @RequestParam(required=false) boolean remember, 
			RedirectAttributes ra, HttpSession session, HttpServletResponse response) {
		//У���˺�����
		User curUser= service.verify(user);
		if(curUser == null) {
			ra.addFlashAttribute(user)
			  .addFlashAttribute("error", "�˺Ż��������");
			return "redirect:/user/login";
		}
		//����ס�˺ţ�����Cookie���ظ��ͻ���
		if(remember) {
			CookieSupport.addUser(curUser);
		}
		//���˺���Ϣ����session
		session.setAttribute(GlobalSession.USER, curUser);
		return "redirect:/";
	}
	
	/**
	 * �ǳ�
	 * @param session
	 * @return
	 */
	@RequestMapping(path="/logout", method=RequestMethod.GET)
	public String logout(HttpSession session, Model model) {
		//session�д����û������Ƴ���Ϣ˳���ǳ�
		//��������ڣ��������Ƴ���Ϣ
		SessionSupport.removeUser();
		model.addAttribute("success", "�ǳ��ɹ�");	
		return "common_message";
	}
	
	
}
