package com.wolterskluwer.dd.common.comparators;

import com.wolterskluwer.dd.common.consts.CompanyFields;
import com.wolterskluwer.dd.common.consts.CompanySectorFields;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompanySectorRevenueComparator implements Comparator<Document> {
    private String sectorId;
    private String order;

    public CompanySectorRevenueComparator(String sectorId, String order) {
        this.sectorId = sectorId;
        this.order = order;
    }

    @SuppressWarnings("unchecked")
	@Override
    public int compare(Document companyDoc1, Document companyDoc2) {
        Double doc1SectorRevenue = 0.0, doc2SectorRevenue = 0.0;
        List<Document> doc1Sectors = (ArrayList<Document>)companyDoc1.get("sectors");
        for(Document dSector : doc1Sectors) {
            if(dSector.getString(CompanySectorFields.SECTOR_ID).equals(sectorId)) {
                Number sectorRevenue = (Number)dSector.get(CompanySectorFields.TOTAL_REVENUE);
                doc1SectorRevenue = sectorRevenue.doubleValue();
                break;
            }
        }
        List<Document> doc2Sectors = (ArrayList<Document>)companyDoc1.get("sectors");
        for(Document dSector : doc2Sectors) {
            if(dSector.getString(CompanySectorFields.SECTOR_ID).equals(sectorId)) {
                Number sectorRevenue = (Number)dSector.get(CompanySectorFields.TOTAL_REVENUE);
                doc2SectorRevenue = sectorRevenue.doubleValue();
                break;
            }
        }

        if(doc1SectorRevenue > doc2SectorRevenue) {
            return order.equals(CompanyFields.ORDER_ASC) ? 1 : -1;
        } else if(doc1SectorRevenue < doc2SectorRevenue) {
            return order.equals(CompanyFields.ORDER_ASC) ? -1 : 1;
        } else {
            return 0;
        }
    }
}
