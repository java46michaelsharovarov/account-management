package telran.accountmanagement.repo;

import java.util.List;

import telran.accountmanagement.entities.AccountEntity;

public interface AccountAggregationRepository {

	int getMaxRoles();
	List<AccountEntity> getAllAccountsWithMaxRoles();
	int getMaxRolesOccurrenceCount();
	List<AccountEntity> getAllRolesWithMaxOccurrence();
	int getActivMinRolesOccurrenceCount();
	
}
