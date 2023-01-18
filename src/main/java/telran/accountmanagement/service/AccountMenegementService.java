package telran.accountmanagement.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import telran.accountmanagement.model.Account;

@Service
public class AccountMenegementService implements AccountsService {

	HashMap<String, Account> accounts;
	PasswordEncoder encoder;
	UserDetailsManager manager;
	@Value("${app.file.data.name}")
	String fileName;

	public AccountMenegementService(PasswordEncoder encoder, UserDetailsManager manager) {
		this.encoder = encoder;
		this.manager = manager;
	}
	
	@Override
	public boolean addAccount(Account account) {
		if(accounts.putIfAbsent(account.username, account) != null || manager.userExists(account.username)) {
			LOG.debug("user {} already exist", account.username);
			return false;
		}
		manager.createUser(User.withUsername(account.username)
				.password(encoder.encode(account.password))
				.roles(account.role)
				.build());
		LOG.debug("user {} has been added", account.username);
		return true;
	}

	@Override
	public boolean updateAccount(Account account) {
		if(accounts.computeIfPresent(account.username, (k, v) -> account) == null) {
			LOG.debug("user {} doesn't exist", account.username);
			return false;
		}
		manager.updateUser(User.withUsername(account.username)
				.password(encoder.encode(account.password))
				.roles(account.role)
				.build());
		LOG.debug("user {} has been updated", account.username);
		return true;
	}

	@Override
	public boolean isExist(String username) {
		boolean res = accounts.containsKey(username) || manager.userExists(username);
		LOG.debug("user {} exist: {}", username, res);	
		return res;
	}

	@Override
	public boolean deleteAccount(String username) {
		if(accounts.remove(username) == null) {
			LOG.debug("user {} doesn't exist", username);
			return false;
		}
		manager.deleteUser(username);
		LOG.debug("user {} has been deleted", username);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	void fillingManager() throws Exception {
		accounts = (HashMap<String, Account>) getDataObject(fileName);
		if (!accounts.isEmpty()) {
			accounts.forEach((u, a) -> {
				manager.createUser(
					User.withUsername(a.username)
					.password(encoder.encode(a.password))
					.roles(a.role)
					.build());
				LOG.info("Added user: {}", manager.loadUserByUsername(u));
			});
		}
	}

	private Object getDataObject(String fileName) throws Exception {
		File source = new File(fileName);
		if(source.exists()){
			try (ObjectInputStream input =
					new ObjectInputStream(new FileInputStream(new File(fileName)))) {
				return input.readObject();
			}
		} else {
			return new HashMap<>();
		}
	}
	
	@PreDestroy
	void save() throws Exception {
		try (ObjectOutputStream output =
				new ObjectOutputStream(new FileOutputStream(new File(fileName)))) {
			output.writeObject(accounts);			
		}
		LOG.info("Data is saved to file - {}", fileName);
	}

}
