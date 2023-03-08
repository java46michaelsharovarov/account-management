package telran.accountmanagement.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.accountmanagement.entities.AccountEntity;

public interface AccountRepository extends MongoRepository<AccountEntity, String>, AccountAggregationRepository {

	List<AccountEntity> findByExpirationAfterAndRevokedIsFalse(LocalDateTime date);

	@Query(value = "{roles: {$elemMatch: {$eq: ?0}}}", fields = "{email: 1}")
	List<AccountEntity> findByRole(String role);
	
}
