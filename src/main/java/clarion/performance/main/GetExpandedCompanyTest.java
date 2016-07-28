package clarion.performance.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.wolterskluwer.dd.common.consts.CompanyFields;
import com.wolterskluwer.dd.common.consts.RelationshipsFields;
import com.wolterskluwer.dd.common.dao.CompanyDAO;
import com.wolterskluwer.dd.common.parsers.CompanyParser;
import com.wolterskluwer.osa.companyprofile.odata.api.Company;
import com.wolterskluwer.osa.companyprofile.odata.api.CompanyRelationship;
import com.wolterskluwer.osa.companyprofile.odata.api.CompetitorRelationship;
import com.wolterskluwer.osa.companyprofile.odata.api.CustomerRelationship;
import com.wolterskluwer.osa.companyprofile.odata.api.PartnerRelationship;
import com.wolterskluwer.osa.companyprofile.odata.api.SupplierRelationship;

public class GetExpandedCompanyTest implements Runnable {	
	private final String APPLE_ID = "777";
	
	private Logger logger;
	
	private CompanyParser companyParser;
	private String companyId;
	private CompanyDAO companyDAO;
	
	public String getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(String cId) {
		companyId = cId;
	}
	
	public CompanyParser getCompanyParser() {
		return companyParser;
	}

	public void setCompanyParser(CompanyParser companyParser) {
		this.companyParser = companyParser;
	}
	
	public GetExpandedCompanyTest(CompanyDAO companyDAO) {
		this.companyId = APPLE_ID;	
		this.companyDAO = companyDAO;
	}
	
	public GetExpandedCompanyTest(CompanyDAO companyDAO, String companyId, Logger logger, CompanyParser companyParser) {
		this(companyDAO);
		this.companyId = companyId;
		this.logger = logger;
		this.companyParser = companyParser;
	}		
	
	@SuppressWarnings("unchecked")
	public Company testGetCompanyPerfomance() {
//		MongoDBDataSource dataSource = companyDAO.getDataSource();		
//		MongoClient client = dataSource.getMongoClient();		
//		int maxConnections = client.getMongoClientOptions().getConnectionsPerHost();
//		int minConnections = client.getMongoClientOptions().getMinConnectionsPerHost();				
//		
//		System.out.println("Max connections per host: " + String.valueOf(maxConnections));
//		System.out.println("Min connections per host: " + String.valueOf(minConnections));
		System.out.println(Thread.currentThread().getName() + " started!");
		List<String> expandFields = Arrays.asList("Competitors", "HomeRegion", "Customers", "Partners", "FocusSector", "Regions", "Sectors", "Suppliers");
		long compSearchStart = System.currentTimeMillis();
		Document expandedCompanyDoc = companyDAO.getCompanyWithRelationships(companyId, CompanyFields.COMPANY_ID);
		long compSearchEnd = System.currentTimeMillis();
		logger.debug(String.format("Company retreiving took: %d s, %d ms %n", (compSearchEnd-compSearchStart)/1000, (compSearchEnd-compSearchStart)%1000));		
		Company company = companyParser.parseEntity(expandedCompanyDoc, expandFields);
		List<Document> relationshipsDoc = (ArrayList<Document>)expandedCompanyDoc.get(RelationshipsFields.COLLECTION);
		List<Document> relationshipTargets = (ArrayList<Document>)relationshipsDoc.get(0).get(RelationshipsFields.RELATIONSHIP_TARGETS);
		fillRelationships(company, relationshipTargets);
		
		TestMongoPerfomance.printMemoryUsage();
		System.out.println(Thread.currentThread().getName() + " finished!");
		return company;
	}
	
	@SuppressWarnings({"unchecked"})
	private void fillRelationships(Company company, List<Document> relationshipTargets) {
        Map<String, Document> relationships = companyParser.mapRelationships(relationshipTargets);        
        Set<String> refCompanyIds = relationships.keySet();
        company.setCompetitors(new ArrayList<CompetitorRelationship>());
        company.setCustomers(new ArrayList<CustomerRelationship>());
        company.setSuppliers(new ArrayList<SupplierRelationship>());
        company.setPartners(new ArrayList<PartnerRelationship>());
                
//        Map<String, Document> relCompanyDocs = companyDAO.getCompaniesByIds(refCompanyIds);
        String[] expandsArray = {CompanyFields.FOCUS_SECTOR_EXPAND, CompanyFields.HOME_REGION_EXPAND};
        List<String> relExpands = new ArrayList<>(Arrays.asList(expandsArray));
        for(String cId : refCompanyIds) {
        	Document companyDoc = companyDAO.getDocumentById(cId, CompanyFields.COMPANY_ID);
        	Company rCompany = companyParser.parseEntity(companyDoc, relExpands);
        	Document relDoc = relationships.get(cId);
        	List<Document> relItems = (ArrayList<Document>)relDoc.get(RelationshipsFields.RELATIONSHIPS);
        	for(Document relItem : relItems) {
                String relType = companyParser.getRelationshipType(relItem);
                CompanyRelationship rel = companyParser.getRelationship(relItem, relType, companyId);
                rel.setCompany(rCompany);
                if(relType.equals(RelationshipsFields.TYPE_COMPETITOR)) {
                	company.getCompetitors().add((CompetitorRelationship)rel);
                } else if(relType.equals(RelationshipsFields.TYPE_CUSTOMER)) {
                	company.getCustomers().add((CustomerRelationship)rel);
                } else if(relType.equals(RelationshipsFields.TYPE_SUPPLIER)) {
                	company.getSuppliers().add((SupplierRelationship)rel);
                } else {
                	company.getPartners().add((PartnerRelationship)rel);
                }
        	}        	
        }                
    }
	
	private void fillCompaniesForRelationships(List<? extends CompanyRelationship> relList, Map<String, Document> relCompanyDocs) {
        String[] expandsArray = {CompanyFields.FOCUS_SECTOR_EXPAND, CompanyFields.HOME_REGION_EXPAND};
        List<String> relExpands = new ArrayList<>(Arrays.asList(expandsArray));
        for(int i = 0; i < relList.size(); i++) {
            CompanyRelationship relationship = relList.get(i);
            Document relCompanyDoc = relCompanyDocs.get(relationship.getCompany().getId());
            if(relCompanyDoc != null) {
            	Company relatedCompany = companyParser.parseEntity(relCompanyDoc, relExpands);
            	relationship.setCompany(relatedCompany);
            }            
        }
    }	

	@Override
	public void run() {
		testGetCompanyPerfomance();
		System.out.println("Search company thread is finished!");		
	}
		
}
