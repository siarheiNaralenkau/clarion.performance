package com.wolterskluwer.dd.common.dao;

import org.bson.Document;

import com.wolterskluwer.dd.common.datasource.mongo.MongoDBDataSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CompanyDAO extends GenericDao {
    Map<String, Document> getCompaniesByIds(Set<String> companyIdsList);
    List<Document> getCompaniesBySector(String sectorId, String sortFieldName, String sortDirection, int top, int skip);
    Document getCompanyWithRelationships(String id, String idField);
    MongoDBDataSource getDataSource();
}