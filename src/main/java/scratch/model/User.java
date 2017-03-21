package scratch.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name="user")
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="username", length=30)
	@NotNull(message="�û�������Ϊ��")
	@Pattern(regexp="^[a-zA-Z0-9_]{6,18}$", message="�û���ֻ�������ֻ���ĸ��ϣ��ҳ��Ȳ�������6λ����18λ")
	private String username;
	
	@Column(name="email", length=100)
	@Pattern(regexp="^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message="��������ȷ������")
	private String email;
	
	@Column(name="password", length=30)
	@NotNull(message="���벻��Ϊ��")
	@Size(min=6, message="���볤�Ȳ�������6λ")
	private String password;
	
	@Column(name="status", length=1)
	private String status = "0";
	
	public User() {}

	public User(Long userId) {
		this.userId = userId;
	}

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", email=" + email + ", password=" + password + ", status=" + status
				+ "]" + super.toString();
	}
}
