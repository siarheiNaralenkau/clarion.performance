package com.wolterskluwer.dd.common.dao;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public interface GenericDao {
    Document findById(String id, String idField);

    List<Document> findAll();

    Document getDocumentById(String id, String idField);

    List<Document> findByProperty(String key, Object value);

    List<Document> findByProperties(Map<String, Object> values);
}
