package telran.accountmanagement.service;

import java.time.LocalDateTime;
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
public class AccountMenegementService implements AccountsService {

	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private UserDetailsManager manager;
	@Autowired
	private AccountRepository accounts;
	@Value("${app.password.period:100}")
	private int passwordPeriod;
	
	@Override
	public boolean addAccount(Account account) {
		if(accounts.existsById(account.username)) {
			LOG.debug("user {} already exist", account.username);
			return false;
		}
		account.password = encoder.encode(account.password);
		AccountEntity accountDocument = AccountEntity.of(account);
		accountDocument.setExpiration(LocalDateTime.now().plusHours(passwordPeriod));
		accounts.save(accountDocument);
		manager.createUser(User.withUsername(account.username)
				.password(account.password)
				.roles(account.roles)
				.build());
		LOG.debug("user {} has been added", account.username);
		return true; 
	}

	@Override
	public boolean updateAccount(Account account) {
		AccountEntity accountDocument = accounts.findById(account.username).orElse(null); 
		if(accountDocument == null) {
			LOG.debug("user {} doesn't exist", account.username);
			return false;
		}
		if(accountDocument.getExpiration().isBefore(LocalDateTime.now()) && encoder.matches(account.password, accountDocument.getPassword())) {
			LOG.debug("password is outdated");
			return false;
		}
		account.password = encoder.encode(account.password);
		accountDocument.setPassword(account.password);
		accountDocument.setExpiration(LocalDateTime.now().plusHours(passwordPeriod));
		accountDocument.setRevoked(false);
		accountDocument.setRoles(account.roles);
		accounts.save(accountDocument);
		manager.updateUser(User.withUsername(account.username)
				.password(account.password)
				.roles(account.roles)
				.build());
		LOG.debug("user {} has been updated", account.username);
		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isExist(String username) {
		boolean res = accounts.existsById(username) || manager.userExists(username);
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
	void detailsManagerFilling() {
		List<AccountEntity> list = accounts.findByExpirationAfterAndRevoked(LocalDateTime.now().toString(), false);
		list.forEach(a -> {
			manager.createUser(
					User.withUsername(a.getEmail())
					.password(encoder.encode(a.getPassword()))
					.roles(a.getRoles())
					.build());
				LOG.info("added user: {}", manager.loadUserByUsername(a.getEmail()));
		});
	}


}
