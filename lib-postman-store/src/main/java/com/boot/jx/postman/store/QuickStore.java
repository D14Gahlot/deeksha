package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.QuickMedia;
import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOptions.OutputMode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Component
public class QuickStore extends CommonMongoTemplateAbstract {

	public static interface QuickGalleryItem {
		String getId();

		String getCategory();

		String getCode();

		String getTitle();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(QuickStore.class);

	@SuppressWarnings("unchecked")
	public <T extends QuickGalleryItem> List<T> groupByCategory(Class<T> clazz) {
		List<DBObject> list = new ArrayList<DBObject>();
		// Equivalent to $project
		DBObject projectFields = new BasicDBObject();
		projectFields.put("_id", 0);
		projectFields.put("category", "$_id");
		DBObject project = new BasicDBObject("$project", projectFields);
		List<QuickMedia> other = new ArrayList<QuickMedia>();
		list.add(Aggregation.group("category").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));
		list.add(project);
		try {
			DBCollection col = mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz));
			col.aggregate(list, AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build())
					.forEachRemaining(doc -> other.add(new QuickMedia().from(doc)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (List<T>) other;
	}

	public <T extends QuickGalleryItem> List<T> findByCategory(String category, Class<T> clazz) {
		return find(CommonMongoQueryBuilder.collection(clazz).where(Criteria.where("category").regex(category, "i")));
	}

	public <T extends QuickGalleryItem> List<T> findGalleryItems(String codeIdOrTitle, Class<T> clazz) {

		List<Criteria> orList = new ArrayList<Criteria>();
		orList.add(Criteria.where("code").is(codeIdOrTitle));
		orList.add(Criteria.where("_id").is(codeIdOrTitle));
		if (codeIdOrTitle != null && ObjectId.isValid(codeIdOrTitle)) {
			orList.add(Criteria.where("id").is(new ObjectId(codeIdOrTitle)));
		}
		orList.add(Criteria.where("title").regex(codeIdOrTitle, "i"));

		return find(CommonMongoQueryBuilder.collection(clazz)
				.where(new Criteria().orOperator(orList.toArray(new Criteria[orList.size()]))));
	}

	public <T extends QuickGalleryItem> T findByCode(String code, Class<T> clazz) {

		List<Criteria> orList = new ArrayList<Criteria>();
		orList.add(Criteria.where("code").is(code));
		orList.add(Criteria.where("_id").is(code));
		if (code != null && ObjectId.isValid(code)) {
			orList.add(Criteria.where("id").is(new ObjectId(code)));
		}

		return findOne(
				CommonMongoQueryBuilder.collection(clazz)
						.where(new Criteria().orOperator(orList.toArray(new Criteria[orList.size()]))).getQuery(),
				clazz);
	}

}
