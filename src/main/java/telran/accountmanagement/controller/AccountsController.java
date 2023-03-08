package telran.accountmanagement.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import telran.accountmanagement.model.Account;
import telran.accountmanagement.service.AccountsService;

@Validated
@RestController
@RequestMapping("accounts")
public class AccountsController {

	private static final String REQUEST_TO_GET = "request to get user : {}";
	private static final String FORMAT_OF_EMAIL = "Username should be in format of Email";
	private static final String USER_DELETED = "user %s has been deleted";
	private static final String CANNOT_DELETE = "you cannot delete an account with name %s";
	private static final String REQUEST_TO_DELETE = "request to delete user : {}";
	private static final String USER_NOT_EXIST = "user %s doesn't exist";
	private static final String USER_UPDATED = "user %s has been updated";
	private static final String CANNOT_CHANGE = "you cannot change an account with name %s";
	private static final String REQUEST_TO_UPDATE = "request to update user : {}";
	private static final String USER_EXIST = "user %s already exist";
	private static final String USER_ADDED = "user %s has been added";
	private static final String REQUEST_TO_ADD = "request to add user : {}";
	private static final String ATTEMPT_TO_DELETE = "attempt to delete the initial administrator account";
	private static final String ATTEMPT_TO_CHANGE = "attempt to change the initial administrator account";
	@Autowired
	AccountsService service;
	@Autowired
	ObjectMapper mapper;
	@Value("${app.admin.username:admin@gmail.com}")
	String admin;
	Logger LOG = LoggerFactory.getLogger(AccountsController.class);

	@PostMapping
	String post(@RequestBody @Valid Account account) throws JsonProcessingException {
		LOG.debug(REQUEST_TO_ADD, mapper.writeValueAsString(account));
		return service.addAccount(account) ? String.format(USER_ADDED, account.username)
				: String.format(USER_EXIST, account.username);
	}

	@PutMapping
	String put(@RequestBody @Valid Account account) throws JsonProcessingException {
		LOG.debug(REQUEST_TO_UPDATE, mapper.writeValueAsString(account));
		if(account.username.equals(admin)) {
			LOG.debug(ATTEMPT_TO_CHANGE);
			return String.format(CANNOT_CHANGE, account.username);
		}
		return service.updateAccount(account) ? String.format(USER_UPDATED, account.username)
				: String.format(USER_NOT_EXIST, account.username);
	}
	
	@DeleteMapping("/{username}")
	String delete(@Email(message = FORMAT_OF_EMAIL)
				  @PathVariable("username") String username) {
		LOG.debug(REQUEST_TO_DELETE, username);
		if(username.equals(admin)) {
			LOG.debug(ATTEMPT_TO_DELETE);
			return String.format(CANNOT_DELETE, username);
		}
		return service.deleteAccount(username) ? String.format(USER_DELETED, username)
				: String.format(USER_NOT_EXIST, username);
	}
	
	@GetMapping("/{username}")
	boolean get(@Email(message = FORMAT_OF_EMAIL)
				@PathVariable("username") String username) {
		LOG.debug(REQUEST_TO_GET, username);
		return service.isExist(username); 
	}
	
	@GetMapping
	List<String> getAccounts(@RequestParam(name = "role", defaultValue = "") String role) {
		return role.isEmpty() ? service.getActiveAccounts() : service.getAccountsByRole(role);
	}
	
	@GetMapping("/roles/max")
	int getMaxRoles() {
		return service.getMaxRoles();
	}
	
	@GetMapping("/roles/max/all")
	List<String> getAllAccountsWithMaxRoles() {
		return service.getAllAccountsWithMaxRoles();
	}
	
	@GetMapping("/roles/max/occurrence")
	int getMaxRolesOccurrenceCount() {
		return service.getMaxRolesOccurrenceCount();
	}
	
	@GetMapping("/roles/max/occurrence/all")
	List<String> getAllRolesWithMaxOccurrence() {
		return service.getAllRolesWithMaxOccurrence();
	}
	
	@GetMapping("/roles/min/occurrence/active")
	int getActivMinRolesOccurrenceCount() {
		return service.getActivMinRolesOccurrenceCount();
	}

	@PreDestroy
	void shutdown() {
		LOG.info("bye, performed graceful shutdown");
	}

}