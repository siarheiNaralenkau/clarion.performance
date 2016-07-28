package com.wolterskluwer.dd.common.parsers;

import com.wolterskluwer.dd.common.beans.CompanyRelationshipIndex;
import com.wolterskluwer.dd.common.consts.CompanyFields;
import com.wolterskluwer.dd.common.consts.CompanySectorFields;
import com.wolterskluwer.dd.common.consts.RegionFields;
import com.wolterskluwer.dd.common.consts.RelationshipsFields;
import com.wolterskluwer.osa.common.odata.api.KeyValuePair;
import com.wolterskluwer.osa.common.odata.api.KeyValuePairList;
import com.wolterskluwer.osa.companyprofile.odata.api.*;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class CompanyParser {

    public Company parseEntity(Document compDoc, List<String> expands) {
        Company company = new Company();
        company.setId(compDoc.getString(CompanyFields.COMPANY_ID));
        company.setName(compDoc.getString(CompanyFields.NAME));
        @SuppressWarnings("unchecked")
        List<String> aliases = (ArrayList<String>) compDoc.get(CompanyFields.ALIASES);
        company.setAliases(aliases);
        // Receiving symbols
        company.setSymbols(getCompanySymbols(compDoc));
        // Receiving company type
        company.setCompanyType(getCompanyType(compDoc));

        Number totalRevenue = (Number) compDoc.get(CompanyFields.TOTAL_REVENUE);
        if(totalRevenue != null) {
            company.setTotalRevenue(totalRevenue.doubleValue());
        }
        Number marketShare = (Number) compDoc.get(CompanyFields.MARKET_SHARE);
        if(marketShare != null) {
            company.setMarketShare(marketShare.doubleValue());
        }

        handleExpands(compDoc, company, expands);
        return company;
    }

    // Method to parse company relationships data
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, List> parseRelationships(Collection<Document> relDocs) {        
		Map<String, List> relationships = new HashMap<>(relDocs.size()/2, 0.5F);
        Map<String, List<CompanyRelationshipIndex>> relCompanyIds = new HashMap<>(relDocs.size()/2, 0.5F);

        relationships.put(RelationshipsFields.TYPE_COMPETITOR, new ArrayList(relDocs.size()/2));
        relationships.put(RelationshipsFields.TYPE_CUSTOMER, new ArrayList(relDocs.size()/2));
        relationships.put(RelationshipsFields.TYPE_SUPPLIER, new ArrayList(relDocs.size()/2));
        relationships.put(RelationshipsFields.TYPE_PARTNER, new ArrayList(relDocs.size()/2));
        
        relCompanyIds.put(RelationshipsFields.TYPE_COMPETITOR, new ArrayList<CompanyRelationshipIndex>(relDocs.size()/2));
        relCompanyIds.put(RelationshipsFields.TYPE_CUSTOMER, new ArrayList<CompanyRelationshipIndex>(relDocs.size()/2));
        relCompanyIds.put(RelationshipsFields.TYPE_SUPPLIER, new ArrayList<CompanyRelationshipIndex>(relDocs.size()/2));
        relCompanyIds.put(RelationshipsFields.TYPE_PARTNER, new ArrayList<CompanyRelationshipIndex>(relDocs.size()/2));
        for(Document relDoc : relDocs) {
            String companyId = relDoc.getString(RelationshipsFields.TARGET_REVERE_ID);
            List<Document> relItems = (ArrayList<Document>)relDoc.get(RelationshipsFields.RELATIONSHIPS);
            for(Document relItem : relItems) {
                String relType = getRelationshipType(relItem);
//                String disclosureType = relItem.getString(RelationshipsFields.DISCLOSURE_TYPE);
//                CompanyRelationshipIndex relIndex = new CompanyRelationshipIndex(disclosureType, companyId);
//                if(!relCompanyIds.get(relType).contains(relIndex)) {
//                	relCompanyIds.get(relType).add(relIndex);
//                	CompanyRelationship rel = getRelationship(relItem, relType, companyId);
//                	relationships.get(relType).add(rel);
//                }
                CompanyRelationship rel = getRelationship(relItem, relType, companyId);
                relationships.get(relType).add(rel);
            }
        }
        return relationships;
    }

    public Map<String, Document> mapRelationships(List<Document> relationshipTargets) {
        Map<String, Document> relationshipsData = new LinkedHashMap<>(relationshipTargets.size());
        for(Document relTargetDoc : relationshipTargets) {
            String companyId = relTargetDoc.getString(RelationshipsFields.TARGET_REVERE_ID);
            if(relationshipsData.containsKey(companyId)) {
            	System.out.println("CompanyId: " + companyId + " is duplicated!");
            } else {
            	relationshipsData.put(companyId, relTargetDoc);
            }
        }
        return relationshipsData;
    }

    public String getRelationshipType(Document relItem) {
        String relType = relItem.getString(RelationshipsFields.RELATIONSHIP_TYPE);
        if(relType.equals(RelationshipsFields.TYPE_COMPETITOR) ||
                relType.equals(RelationshipsFields.TYPE_CUSTOMER) ||
                relType.equals(RelationshipsFields.TYPE_SUPPLIER)) {
            return relType;
        } else {
            return RelationshipsFields.TYPE_PARTNER;
        }
    }

    public CompanyRelationship getRelationship(Document relItem, String relType, String companyId) {
        CompanyRelationship relationship;
        if(relType.equals(RelationshipsFields.TYPE_COMPETITOR)) {
            relationship = new CompetitorRelationship();
        } else if(relType.equals(RelationshipsFields.TYPE_CUSTOMER)) {
            relationship = new CustomerRelationship();
        } else if(relType.equals(RelationshipsFields.TYPE_SUPPLIER)) {
            relationship = new SupplierRelationship();
        } else {
            relationship = new PartnerRelationship();
            ((PartnerRelationship)relationship).setPartnerType(relItem.getString(RelationshipsFields.PARTNER_TYPE));
            Number relRevenue = (Number)relItem.get(RelationshipsFields.RELATIONSHIP_REVENUE);
            if(relRevenue != null) {
                ((PartnerRelationship) relationship).setRelationshipRevenue(relRevenue.doubleValue());
            }
        }
        relationship.setId(relItem.getString(RelationshipsFields.RELATIONSHIP_ID));
        relationship.setDisclosureType(relItem.getString(RelationshipsFields.DISCLOSURE_TYPE));
        String sDisclosureDate = relItem.getString(RelationshipsFields.DISCLOSURE_DATE);
        try {
            Date disclosureDate = new SimpleDateFormat("M/d/yyyy").parse(sDisclosureDate);
            relationship.setDisclosureDate(disclosureDate);
            Calendar cDisclosureDate = Calendar.getInstance();
            cDisclosureDate.setTime(disclosureDate);
            Calendar cCurrentDate = Calendar.getInstance();
            int discloseDuration = cCurrentDate.get(Calendar.YEAR) - cDisclosureDate.get(Calendar.YEAR);
            relationship.setDuration(discloseDuration);
        } catch (Exception e) {
            relationship.setDisclosureDate(null);
            relationship.setDuration(0);
        }        
        return relationship;
    }

    // Method to handle fields to expand for Company.
    private void handleExpands(Document compDoc, Company company, List<String> expands) {
        // Expand Home Region if necessary.
        if (expands.contains(CompanyFields.HOME_REGION_EXPAND)) {
            company.setHomeRegion(getHomeRegionForCompany(compDoc));
        }
        // Expand Regions list if necessary.
        if (expands.contains(CompanyFields.REGIONS_EXPAND)) {
            company.setRegions(getRegionsForCompany(compDoc));
        }
        // Expand Focus Sector if necessary
        if (expands.contains(CompanyFields.FOCUS_SECTOR_EXPAND)) {
            company.setFocusSector(getFocusSectorForCompany(compDoc));
        }
        // Expand Sectors if necessary
        if (expands.contains(CompanyFields.SECTORS_EXPAND)) {
            company.setSectors(getSectorsForCompany(compDoc));
        }
    }

    private KeyValuePairList getCompanySymbols(Document compDoc) {
        List<KeyValuePair> symbolsList = new ArrayList<>();
        Document docSymbols = compDoc.get(CompanyFields.SYMBOLS, Document.class);
        for (String sKey : docSymbols.keySet()) {
            KeyValuePair symbol = new KeyValuePair();
            symbol.setKey(sKey);
            symbol.setValue(docSymbols.getString(sKey));
            symbolsList.add(symbol);
        }
        KeyValuePairList kpSymbolsList = new KeyValuePairList();
        kpSymbolsList.setItem(symbolsList);
        return kpSymbolsList;
    }

    @SuppressWarnings("unchecked")
    private List<String> getCompanyType(Document compDoc) {
        List<String> companyTypes = new ArrayList<>();
        List<Document> compTypeDocs = (ArrayList<Document>) compDoc.get(CompanyFields.COMPANY_TYPE);
        for (Document docType : compTypeDocs) {
            companyTypes.add(docType.getString(CompanyFields.TYPE_NAME));
        }
        return companyTypes;
    }

    @SuppressWarnings("unchecked")
    private CompanySector parseSector(Document sectorDoc) {
        CompanySector companySector = new CompanySector();        
        if ( (sectorDoc == null) || StringUtils.isEmpty(sectorDoc.getString(CompanySectorFields.SECTOR_ID))) {
            companySector.setId(CompanySectorFields.DEFAULT_SECTOR_ID);
        } else {
            companySector.setId(sectorDoc.getString(CompanySectorFields.SECTOR_ID));
            List<String> pathId = (ArrayList<String>) sectorDoc.get(CompanySectorFields.PATH_ID);
            companySector.setPathId(pathId);

            List<String> pathTitle = (ArrayList<String>) sectorDoc.get(CompanySectorFields.PATH_TITLE);
            companySector.setPathTitle(pathTitle);

            Number totalRevenue = (Number) sectorDoc.get(CompanySectorFields.TOTAL_REVENUE);
            companySector.setTotalRevenue(totalRevenue.doubleValue());

            Number percentRevenue = (Number) sectorDoc.get(CompanySectorFields.PERCENT_REVENUE);
            companySector.setPercentRevenue(percentRevenue.doubleValue());

            companySector.setLeaf(sectorDoc.getBoolean(CompanySectorFields.IS_LEAF));
        }        
        return companySector;
    }

    @SuppressWarnings("unchecked")
    private Region parseRegion(Document regionDoc) {
        Region region = new Region();        
        if ( (regionDoc == null) || StringUtils.isEmpty(regionDoc.getString(RegionFields.REGION_ID)) ) {
        	region.setId(RegionFields.DEFAULT_REGION_ID);        	      
        } else {
            region.setId(regionDoc.getString(RegionFields.REGION_ID));
            region.setRegionName(regionDoc.getString(RegionFields.REGION_NAME));

            // Receiving region paths
            List<RegionPath> regionPaths = new ArrayList<>();
            List<Document> pathDocs = (ArrayList<Document>) regionDoc.get(RegionFields.PATHS);
            if(pathDocs != null) {
	            for (Document pathDoc : pathDocs) {
	                RegionPath rPath = new RegionPath();
	                List<String> pathIds = (ArrayList<String>) pathDoc.get(RegionFields.PATH_ID);
	                List<String> pathTitles = (ArrayList<String>) pathDoc.get(RegionFields.PATH_TITLE);
	                rPath.setPathId(pathIds);
	                rPath.setPathTitle(pathTitles);
	                regionPaths.add(rPath);
	            }
	            region.setPaths(regionPaths);
            }

            // Receiving region revenue
            Document revenueDoc = regionDoc.get(RegionFields.REVENUE, Document.class);
            if (revenueDoc != null) {
                region.setRevenue(parseRegionRevenue(revenueDoc));
            }
        }
        return region;
    }

    private RegionRevenue parseRegionRevenue(Document revenueDoc) {
        RegionRevenue regionRevenue = new RegionRevenue();

        Document companyTotalDoc = revenueDoc.get(RegionFields.COMPANY_TOTAL, Document.class);
        regionRevenue.setCompanyTotal(fillRevenue(companyTotalDoc));
        Document companyFocusSectorDoc = revenueDoc.get(RegionFields.COMPANY_FOCUS_SECTOR, Document.class);
        regionRevenue.setCompanyFocusSector(fillRevenue(companyFocusSectorDoc));
        Document companyPeerAverageDoc = revenueDoc.get(RegionFields.PEER_AVERAGE, Document.class);
        regionRevenue.setPeerAverage(fillRevenue(companyPeerAverageDoc));

        return regionRevenue;
    }

    private Revenue fillRevenue(Document docRevenue) {
        Revenue revenue = new Revenue();
        if(docRevenue == null) {
            revenue.setPercentage(0.0);
            revenue.setValue(0.0);
        } else {
            Number totalValue = (Number) docRevenue.get(RegionFields.REVENUE);
            revenue.setValue(totalValue.doubleValue());
            Number totalPercent = (Number) docRevenue.get(RegionFields.PERCENT);
            revenue.setPercentage(totalPercent.doubleValue());
        }
        return revenue;
    }

    public CompanySector getFocusSectorForCompany(Document companyDoc) {
        Document focusSectorDoc = companyDoc.get(CompanyFields.FOCUS_SECTOR, Document.class);
        return parseSector(focusSectorDoc);
    }

    @SuppressWarnings("unchecked")
    public List<CompanySector> getSectorsForCompany(Document companyDoc) {
    	List<Document> sectorDocs = (ArrayList<Document>) companyDoc.get(CompanyFields.SECTORS);
        List<CompanySector> sectors = new ArrayList<>(sectorDocs.size());
        
        for (Document sectorDoc : sectorDocs) {
        	sectors.add(parseSector(sectorDoc));            
        }
        return sectors;
    }

    public Region getHomeRegionForCompany(Document companyDoc) {
        Document homeRegionDoc = companyDoc.get(CompanyFields.HOME_REGION, Document.class);
        return parseRegion(homeRegionDoc);
    }

    @SuppressWarnings("unchecked")
    public List<Region> getRegionsForCompany(Document companyDoc) {        
        List<Document> regionDocs = (ArrayList<Document>) companyDoc.get(CompanyFields.REGION);
        
        List<Region> regions = new ArrayList<>(regionDocs.size());
        for (Document regionDoc : regionDocs) {
            regions.add(parseRegion(regionDoc));
        }
        return regions;
    }
}
