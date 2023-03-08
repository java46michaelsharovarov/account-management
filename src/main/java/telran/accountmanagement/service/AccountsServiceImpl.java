package telran.accountmanagement.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import telran.accountmanagement.entities.AccountEntity;
import telran.accountmanagement.model.Account;
import telran.accountmanagement.repo.AccountRepository;

@Service
@Transactional
public class AccountsServiceImpl implements AccountsService {

	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private UserDetailsManager manager;
	@Autowired
	private AccountRepository accounts;
	@Value("${app.password.period:24}")
	private int passwordPeriod;
	
	@Override
	public boolean addAccount(Account account) {
		if(accounts.existsById(account.username)) {
			LOG.debug("user {} already exist", account.username);
			return false;
		}
		account.password = encoder.encode(account.password);
		AccountEntity accountDocument = AccountEntity.of(account);
		accountDocument.setExpiration(LocalDateTime.now(ZoneId.of("UTC")).plusHours(passwordPeriod));
		accounts.save(accountDocument);
		createUserInUserDetailsManager(account.username, account.password, account.roles);
		LOG.debug("user {} has been added", accountDocument.toString());
		return true; 
	}

	@Override
	public boolean updateAccount(Account account) {
		AccountEntity accountDocument = accounts.findById(account.username).orElse(null); 
		if(accountDocument == null) {
			LOG.debug("user {} doesn't exist", account.username);
			return false;
		}
		if(!encoder.matches(account.password, accountDocument.getPassword())) {
			account.password = encoder.encode(account.password);
			accountDocument.setPassword(account.password);
		}		
		accountDocument.setExpiration(LocalDateTime.now(ZoneId.of("UTC")).plusHours(passwordPeriod));
		accountDocument.setRevoked(false);
		accountDocument.setRoles(account.roles);
		accounts.save(accountDocument);
		if(manager.userExists(account.username)) {
			updateUserInUserDetailsManager(account);
		} else {
			createUserInUserDetailsManager(account.username, account.password, account.roles);
		}		
		LOG.debug("user {} has been updated", account.username);
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isExist(String username) {
		boolean res = manager.userExists(username);
		LOG.debug("user {} exist: {}", username, res);	
		return res;
	}

	@Override
	public boolean deleteAccount(String username) {
		if(!accounts.existsById(username)) {
			LOG.debug("user {} doesn't exist", username);
			return false;
		}
		accounts.deleteById(username);
		manager.deleteUser(username);
		LOG.debug("user {} has been deleted", username);
		return true;
	}
	
	@PostConstruct
	@Transactional(readOnly = true)
	void detailsManagerFilling() {
		List<AccountEntity> list = accounts.findByExpirationAfterAndRevokedIsFalse(LocalDateTime.now(ZoneId.of("UTC")));
		LOG.debug("accounts retrieved from DB are: {}, current GMT date time is {}", fromAccountEntitesToNames(list), LocalDateTime.now(ZoneId.of("UTC")));
		list.forEach(a -> {
			createUserInUserDetailsManager(a.getEmail(), a.getPassword(), a.getRoles());
				LOG.debug("added user: {}", manager.loadUserByUsername(a.getEmail()));
		});
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getAccountsByRole(String role) {
		List<AccountEntity> accountsDB = accounts.findByRole(role);
		List<String> listOfNames = fromAccountEntitesToNames(accountsDB);
		LOG.debug("accounts by role - {}: {}", role, listOfNames);
		return listOfNames;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getActiveAccounts() {
		List<AccountEntity> accountsDB = accounts.findByExpirationAfterAndRevokedIsFalse(LocalDateTime.now(ZoneId.of("UTC")));
		List<String> listOfNames = fromAccountEntitesToNames(accountsDB);
		LOG.debug("active accounts: {}", listOfNames);
		return listOfNames;
	}

	@Override
	@Transactional(readOnly = true)
	public int getMaxRoles() {
		int res = accounts.getMaxRoles();
		LOG.debug("maximum number of roles: {}", res);
		return res; 
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getAllAccountsWithMaxRoles() {
		List<AccountEntity> accountsDB = accounts.getAllAccountsWithMaxRoles();
		List<String> listOfNames = fromAccountEntitesToNames(accountsDB);
		LOG.debug("accounts with max roles: {}", listOfNames);
		return listOfNames;
	}

	@Override
	@Transactional(readOnly = true)
	public int getMaxRolesOccurrenceCount() {
		int res = accounts.getMaxRolesOccurrenceCount();
		LOG.debug("maximum number of recurring roles for all accounts: {}", res);
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getAllRolesWithMaxOccurrence() {
		List<AccountEntity> accountsDB = accounts.getAllRolesWithMaxOccurrence();
		List<String> listOfRoles = fromAccountEntitesToNames(accountsDB);
		LOG.debug("accounts with max roles: {}", listOfRoles);
		return listOfRoles;
	}

	@Override
	@Transactional(readOnly = true)
	public int getActivMinRolesOccurrenceCount() {
		int res = accounts.getActivMinRolesOccurrenceCount();
		LOG.debug("minimum number of recurring roles in active accounts: {}", res);
		return res;
	}

	private List<String> fromAccountEntitesToNames(List<AccountEntity> accountsDB) {
		return accountsDB.stream().map(AccountEntity::getEmail).toList();
	}

	private void updateUserInUserDetailsManager(Account account) {
		manager.updateUser(User.withUsername(account.username)
				.password(account.password)
				.roles(account.roles)
				.build());
	}

	private void createUserInUserDetailsManager(String username, String password, String[] roles) {
		manager.createUser(User.withUsername(username)
				.password(password)
				.roles(roles)
				.build());
	}

}
