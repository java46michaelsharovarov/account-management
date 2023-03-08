package telran.accountmanagement.repo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.stereotype.Repository;

import telran.accountmanagement.entities.AccountEntity;

@Repository
public class AccountAggregationRepositoryImpl implements AccountAggregationRepository {

	private static GroupOperation groupByAllAndMaxCount = group().max("count").as("maxCount");

	private static UnwindOperation unwindRoles = unwind("roles");

	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	@Lazy
	private AccountRepository accounts;
	
	@Override
	public int getMaxRoles() {
		Aggregation pipeline = 
				newAggregation(unwindRoles, groupByParamAndCount("email"), groupByAllAndMaxCount);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("maxCount");
	}

	@Override
	public List<AccountEntity> getAllAccountsWithMaxRoles() {
		int maxRoles = getMaxRoles();
		Aggregation pipeline = 
				newAggregation(unwindRoles, groupByParamAndCount("email"), matchFieldValue("count", maxRoles));
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, AccountEntity.class);
		return document.getMappedResults();
	}

	@Override
	public int getMaxRolesOccurrenceCount() {
		Aggregation pipeline = 
				newAggregation(unwindRoles, groupByParamAndCount("roles"), groupByAllAndMaxCount);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("maxCount");
	}

	@Override
	public List<AccountEntity> getAllRolesWithMaxOccurrence() {
		int maxRoles = getMaxRolesOccurrenceCount();
		Aggregation pipeline = 
				newAggregation(unwindRoles, groupByParamAndCount("roles"), matchFieldValue("count", maxRoles));
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, AccountEntity.class);
		return document.getMappedResults();
	}

	@Override
	public int getActivMinRolesOccurrenceCount() {
		ArrayList<AggregationOperation> operations = new ArrayList<>();
		
//*******'match' for single query*******//
//		operations.add(match(new Criteria("revoked").is(false)
//				.andOperator(new Criteria("expiration").gt(LocalDateTime.now(ZoneId.of("UTC"))))));
		
//*******'match' for two queries using an existing query*******//
		List<String> lisеOfActiveAccountIDs = accounts.findByExpirationAfterAndRevokedIsFalse(LocalDateTime.now(ZoneId.of("UTC")))
				.stream().map(AccountEntity::getEmail).toList();
		operations.add(match(new Criteria("_id").in(lisеOfActiveAccountIDs)));

		operations.add(unwindRoles);
		operations.add(groupByParamAndCount("roles"));
		operations.add(group().min("count").as("minCount"));
		Aggregation pipeline = newAggregation(operations);
		var document = mongoTemplate.aggregate(pipeline, AccountEntity.class, Document.class);
		return document.getUniqueMappedResult().getInteger("minCount");
	}

	private MatchOperation matchFieldValue(String field, int maxRoles) {
		return match(new Criteria(field).is(maxRoles));
	}

	private GroupOperation groupByParamAndCount(String param) {
		return group(param).count().as("count");
	}
	
}
