package telran.accountmanagement.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Account {

	@NotEmpty
	@Email(message = "Username should be in format of Email")
	public String username;
	
	@NotNull
	@Size(min = 6, message = "Password should not be less than 6 characters")
	public String password;
	
	@NotEmpty(message = "Should be at least one role")
	public String[] roles;
	
}
