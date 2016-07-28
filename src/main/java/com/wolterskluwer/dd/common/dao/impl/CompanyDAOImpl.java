package com.wolterskluwer.dd.common.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import java.util.*;

import com.wolterskluwer.dd.common.comparators.CompanySectorRevenueComparator;
import com.wolterskluwer.dd.common.consts.CompanyFields;
import com.wolterskluwer.dd.common.consts.CompanySectorFields;
import com.wolterskluwer.dd.common.consts.MongoKeywords;
import com.wolterskluwer.dd.common.consts.RelationshipsFields;
import com.wolterskluwer.dd.common.dao.CompanyDAO;
import com.wolterskluwer.dd.common.datasource.mongo.MongoDBDataSource;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.wolterskluwer.dd.common.consts.CompanyFields.COLLECTION;

public class CompanyDAOImpl extends GenericDaoImpl implements CompanyDAO {
	
	public CompanyDAOImpl() {
		super();
	}
	
    public CompanyDAOImpl(MongoDBDataSource mongoDataSource) {
        super(mongoDataSource, COLLECTION);
    }
    
    public MongoDBDataSource getDataSource() {
		return super.getDataSource();    	
    }

    @Override
    public List<Document> getCompaniesBySector(String sectorId, String sortFieldName, String sortDirection, int top, int skip) {
        Bson sortOption = buildSortOption(sortFieldName, sortDirection);
        FindIterable<Document> searchResults = mongoCollection.find(
                Filters.elemMatch(CompanyFields.SECTORS, Filters.eq(CompanySectorFields.SECTOR_ID, sectorId)));
        searchResults = searchResults.sort(sortOption);
        if(top != 0) {
            searchResults = searchResults.limit(top).skip(skip);
        }
        List<Document> companyDocs = searchResults.into(new ArrayList<Document>());

        if(sortFieldName.equals(CompanyFields.SORT_SECTOR_REVENUE)) {
            Collections.sort(companyDocs, new CompanySectorRevenueComparator(sectorId, sortDirection));
        }
        return companyDocs;
    }

    @Override
    public Map<String, Document> getCompaniesByIds(Set<String> companyIdsList) {
    	// TODO Override this method to receive each company separately...
        List<Document> companyDocs = mongoCollection.find(Filters.in(CompanyFields.COMPANY_ID, companyIdsList))
                .into(new ArrayList<Document>());
        Map<String, Document> companiesMap = new LinkedHashMap<>();
        for(Document compDoc : companyDocs) {
            companiesMap.put(compDoc.getString(CompanyFields.COMPANY_ID), compDoc);
        }
        return companiesMap;
    }

    @Override
    public Document getCompanyWithRelationships(String id, String idField) {
        List<Bson> aggregationQuery = new ArrayList<>();
        aggregationQuery.add(new Document(MongoKeywords.MATCH, new Document(idField, id)));
        Map<String, Object> joinRelationshipsQuery = new HashMap<>();
        joinRelationshipsQuery.put(MongoKeywords.FROM, RelationshipsFields.COLLECTION);
        joinRelationshipsQuery.put(MongoKeywords.LOCAL_FIELD, CompanyFields.COMPANY_ID);
        joinRelationshipsQuery.put(MongoKeywords.FOREIGN_FIELD, RelationshipsFields.SOURCE_REVERE_ID);
        joinRelationshipsQuery.put(MongoKeywords.AS, RelationshipsFields.COLLECTION);
        aggregationQuery.add(new Document(MongoKeywords.LOOKUP, new Document(joinRelationshipsQuery)));
        Document companyDoc = mongoCollection.aggregate(aggregationQuery).first();
        return companyDoc;
    }

    private Bson buildSortOption(String sortFieldName, String sortDirection) {
        Bson sorting = new Document();
        if(sortFieldName.equals(CompanyFields.SORT_NAME) || sortFieldName.equals(CompanyFields.TOTAL_REVENUE)) {
            if(sortDirection.equals(CompanyFields.ORDER_ASC)) {
                sorting = Sorts.ascending(sortFieldName);
            } else {
                sorting = Sorts.descending(sortFieldName);
            }
        }
        return sorting;
    }
}
