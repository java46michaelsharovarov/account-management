package telran.accountmanagement.entities;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import telran.accountmanagement.model.Account;

@Document(collection = "myAccounts")
public class AccountEntity {

	@Id
	private String email;	
	private String password;
	private LocalDateTime expiration;
	private boolean revoked;
	private String[] roles;
	
	public static AccountEntity of(Account accountDto) {
		AccountEntity account = new AccountEntity();
		account.email = accountDto.username;
		account.password = accountDto.password;
		account.revoked = false;
		account.setRoles(accountDto.roles);
		return account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(LocalDateTime expiration) {
		this.expiration = expiration;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public String getEmail() {
		return email;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	
	@Override
	public String toString() {
		return "AccountEntity [email=" + email + ", password=" + password + ", expiration=" + expiration + ", revoked="
				+ revoked + ", roles=" + Arrays.toString(roles) + "]";
	}
	
}
