package com.wolterskluwer.dd.common.dao.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.wolterskluwer.dd.common.dao.GenericDao;
import com.wolterskluwer.dd.common.datasource.mongo.MongoDBDataSource;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

abstract class GenericDaoImpl implements GenericDao {    
    MongoCollection<Document> mongoCollection;  
    private MongoDBDataSource dataSource;

    public MongoDBDataSource getDataSource() {
    	return this.dataSource;
    }
    
    GenericDaoImpl(MongoDBDataSource mongoDataSource, String mongoCollectionName) {
    	this.dataSource = mongoDataSource;
        mongoCollection = mongoDataSource.getDBConnection().getCollection(mongoCollectionName);
    }
    
    GenericDaoImpl() {}

    @Override
    public Document getDocumentById(String id, String idField) {
        return mongoCollection.find(new Document(idField, id)).first();
    }
    
    @Override
    public List<Document> findAll() {
        return mongoCollection.find().into(new ArrayList<Document>());
    }

    @Override
    public Document findById(String id, String idField) {
        return getDocumentById(id, idField);
    }

    @Override
    public List<Document> findByProperty(String key, Object value) {
        return mongoCollection.find(eq(key, value)).into(new ArrayList<Document>());
    }

    @Override
    public List<Document> findByProperties(Map<String, Object> values) {
        List<Bson> filter = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if ("null".equals(entry.getValue()))
                filter.add(eq(entry.getKey(), null));
            else
                filter.add(eq(entry.getKey(), entry.getValue()));
        }
        return mongoCollection.find(and(filter)).into(new ArrayList<Document>());
    }

}
