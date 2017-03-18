package scratch.controller;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import scratch.model.User;
import scratch.service.UserSerivce;
import scratch.support.RedirectAttrSupport;

@Controller
@SessionAttributes("user")
@RequestMapping("/user")
public class RegisterController {
	
	@Autowired
	private UserSerivce service;
	
	@ModelAttribute
	public void addUser(Model model) {
		if(!model.containsAttribute("user")) {
			model.addAttribute(new User());
		}
	}
	
	/**
	 * �û�ע���
	 * @return
	 */
	@RequestMapping(path="/register", method=RequestMethod.GET)
	public String registerForm(){
		return "user_register";
	}
	
	/**
	 * �û�ע�ᴦ��
	 * @param user
	 * @param result
	 * @param rePassword
	 * @param ra
	 * @return
	 */
	@RequestMapping(path="/register", method=RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result, 
			@RequestParam String rePassword, RedirectAttributes ra) {
		//У����Ϣ
		if(result.hasErrors()) {
			RedirectAttrSupport.setError(ra, result);
			return "redirect:/user/register";
		}
		if(!user.getPassword().equals(rePassword)) {
			ra.addFlashAttribute("error", "�����������벻һ��");
			return "redirect:/user/register";
		}
		//�����û�
		try{
			service.add(user);
		} catch(Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/user/register";
		}
		ra.addFlashAttribute("success", "ע��ɹ������ᷢ��һ���ʼ�����������ʼ������������˺���֤�������˺��޷�ʹ��");
		return "redirect:/common/message";
	}
	
	@RequestMapping(path="/register/sendMail")
	public String sendMail(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if(user == null) {
			model.addAttribute("error", "��Ч����");
			return "common_message";
		}
		if(!"1".equals(user.getStatus())) {
			service.sendActiviMail(user);
			model.addAttribute("success", "�ʼ��Ѿ�����");
			return "common_message";
		} 
		model.addAttribute("error", "���û��˺��Ѿ�������跢���ʼ�");
		return "common_message";
	}
	
	
	/**
	 * �û�ע�ἤ��
	 * @param actiCode
	 * @param request
	 * @return
	 */
	@RequestMapping(path="/register/activiti/{actiCode}", method=RequestMethod.GET)
	public String activiti(@PathVariable("actiCode") String actiCode, Model model) {
		if(!service.activi(actiCode)) {
			model.addAttribute("error", "�������ʵ�ҳ�治����");
			return "common_message";
		} else {
			model.addAttribute("success", "�˺ż���ɹ�");
			return "common_message";
		}
	}
	
	@RequestMapping(path="/register/user", method=RequestMethod.POST)
	public @ResponseBody String existUser(@RequestParam("username") String username) {
		User user = new User();
		user.setUsername(username);
		return service.isExist(user) ? "�˺Ŵ���" : null;
	}

}
