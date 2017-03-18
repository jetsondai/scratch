package scratch.service;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	@Autowired
	private JavaMailSender mailSender;
	
	//��Ȩ��1��klrhqhlltnihcahg
	//��Ȩ��2��cxkrakqhlnuwcaga
	public boolean sendMail(String toMail, String content) {
		content = registerTemplate(content);
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper messHelper = new MimeMessageHelper(message);
		
		try {
			messHelper.setFrom("398299262@qq.com");
			messHelper.setTo(toMail);
			messHelper.setSubject("ע����֤");
			messHelper.setText(content, true);
		} catch (MessagingException e) {
			return false;
		}
		mailSender.send(message);
		return true;
	}
	
	public String registerTemplate(String url) {
		StringBuffer html = new StringBuffer();
		html.append("<html>");
		html.append("<body>");
		html.append("<p>�װ����û������ã�</p>");
		html.append("  ��л����ע�ᣬ�����������˺���֤���ӣ�");
		html.append("<a href='"+ url + "'>" + url + "</a>");
		html.append("</body>");
		html.append("</html>");
		
		return html.toString();
	}
	
}
