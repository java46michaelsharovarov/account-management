package telran.accountmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import telran.accountmanagement.model.Account;

public interface AccountsService {	
	
	Logger LOG = LoggerFactory.getLogger(AccountsService.class);
	boolean addAccount(Account account);
	boolean updateAccount(Account account);
	boolean isExist(String username);
	boolean deleteAccount(String username);
	
}	
