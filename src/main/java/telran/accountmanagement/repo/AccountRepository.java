package telran.accountmanagement.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.accountmanagement.entities.AccountEntity;

public interface AccountRepository extends MongoRepository<AccountEntity, String> {

//	@Query("{ $and: [{'expiration' : {$gt : ?0}}, {'revoked' : ?1}] }")
	List<AccountEntity> findByExpirationAfterAndRevoked(String date, boolean revoked);
}
