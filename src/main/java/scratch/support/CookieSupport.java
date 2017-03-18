package scratch.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import scratch.model.User;

public class CookieSupport {

	@Autowired
	private static CipherSupport cipher;
	
	private static int MAXAGE = 300;
	
	private static String PATH = "/";
	
	private static HttpServletResponse getResponse() {
		return ((ServletWebRequest)RequestContextHolder.getRequestAttributes()).getResponse();
	}
	
	/**
	 * ���map��Cookie��
	 * @param map
	 */
	private static void addCookies(Map<String, String> map) {
		HttpServletResponse response = getResponse();
		for(Entry<String, String> e : map.entrySet()) {
			Cookie cookie = new Cookie(e.getKey(), e.getValue());
			cookie.setMaxAge(MAXAGE);
			cookie.setPath(PATH);
			response.addCookie(cookie);
		}
	}
	
	/**
	 * ���û���Ϣ�����Cookie�У����userId�ͼ��ܺ��userId
	 * ���⣺cookie ָ����path·������ȷ��ָ��������localhost
	 * @param user
	 */
	public static void addUser(User user) {
		Map<String, String> map = new HashMap<String, String>();
		String userId = user.getUserId().toString();
		map.put("userId", userId);
		map.put("user", cipher.encode(userId.toString()));
		addCookies(map);
	}
	
}
