package com.boot.jx;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.test.context.TestPropertySource;

import com.boot.jx.mongo.CommonMongoSource;
import com.boot.jx.mongo.MongoTemplateCommonImpl;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.utils.JsonUtil;
import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.AggregationOptions.OutputMode;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChatStoreTest { // Noncompliant

	@Value("${spring.data.mongodb.uri}")
	private String secondProperty;

	private MongoTemplateCommonImpl mongoTemplate;

	private void initMongo() {
		AppContextUtil.setTenant("demo");
		String connectionString = System.getProperty("spring.data.mongodb.uri");
		CommonMongoSource commonMongoSource = new CommonMongoSource();
		commonMongoSource.setDataSourceUrl(connectionString);
		commonMongoSource.setGlobalDataSourceUrl(connectionString);
		commonMongoSource.setGlobalDBProfix("tnt");

		mongoTemplate = new MongoTemplateCommonImpl(commonMongoSource.getMongoDbFactory());
		mongoTemplate.setMongoDBCredentials(commonMongoSource);
	}

	public static void main(String[] args) throws ParseException {
		String connectionString = System.getProperty("mongodb.uri");
		// System.out.println(connectionString);
//		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//			MongoDatabase db = mongoClient.getDatabase("sample_training");
//			MongoCollection<Document> zips = db.getCollection("zips");
//			MongoCollection<Document> posts = db.getCollection("posts");
//			threeMostPopulatedCitiesInTexas(zips);
//			threeMostPopularTags(posts);
//		}
	}

	@Test
	public void testLanguageEnumFromNumber() {
		initMongo();

		List<DBObject> list = new ArrayList<DBObject>();
//		list.add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))) // Match
//				.toDBObject(Aggregation.DEFAULT_CONTEXT));

		// Equivalent to $project
		DBObject projectFields = new BasicDBObject();
		projectFields.put("_id", 0);
		projectFields.put("category", "$_id");
		DBObject project = new BasicDBObject("$project", projectFields);

		List<QuickMedia> other = new ArrayList<QuickMedia>();
		list.add(Aggregation.group("category").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));
		list.add(project);

		try {
			DBCollection col = mongoTemplate.getCollection(mongoTemplate.getCollectionName(QuickMedia.class));
			
			col.aggregate(list, AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build())
			.forEachRemaining(doc -> other.add(new QuickMedia().from(doc)));
			
			
//			AggregationOutput output = col.aggregate(list);
//			// .forEachRemaining(doc -> other.add(new QuickMedia().from(doc)));
//
//			for (DBObject result : output.results()) {
//				System.out.println("====" + JsonUtil.toJson(result));
////	            BasicDBList employeeList = (BasicDBList) result.get("docs");
////	            BasicDBObject employeeDoc = (BasicDBObject) employeeList.get(0);
////	            String name = employeeDoc.get("name").toString();
////	            System.out.println(name);
//			}

			System.out.println("====" + JsonUtil.toJson(other));
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue("Lang is not TL", true);
	}

}
