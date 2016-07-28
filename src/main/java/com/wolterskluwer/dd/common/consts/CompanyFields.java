package com.wolterskluwer.dd.common.consts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompanyFields {
    public static final String COLLECTION = "Company";
    public static final String COMPANY_ID = "dataSourceEntityId";
    public static final String NAME = "name";
    public static final String ALIASES = "aliases";
    public static final String COMPANY_TYPE = "companyType";
    public static final String TOTAL_REVENUE = "totalRevenue";
    public static final String MARKET_SHARE = "marketShare";
    public static final String SYMBOLS = "symbols";
    public static final String REGION = "region";
    public static final String HOME_REGION = "homeRegion";
    public static final String FOCUS_SECTOR = "focusSector";
    public static final String SECTORS = "sectors";
    public static final String TYPE_NAME = "typeName";

    public static final String HOME_REGION_EXPAND = "HomeRegion";
    public static final String REGIONS_EXPAND = "Regions";
    public static final String FOCUS_SECTOR_EXPAND = "FocusSector";
    public static final String SECTORS_EXPAND = "Sectors";

    public static final String COMPETITORS_EXPAND = "Competitors";
    public static final String CUSTOMERS_EXPAND = "Customers";
    public static final String PARTNERS_EXPAND = "Partners";
    public static final String SUPPLIERS_EXPAND = "Suppliers";

    public static final String SORT_NAME = "name";
    public static final String SORT_SECTOR_REVENUE = "sectorRevenue";
    public static final String SORT_TOTAL_REVENUE = "totalRevenue";
    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";
    public static final String SORT_SEPARATOR = "\\s+";


    public static final List<String> SORT_FIELDS = new ArrayList<>(Arrays.asList(new String[]{SORT_NAME, SORT_SECTOR_REVENUE, SORT_TOTAL_REVENUE}));
    public static final List<String> ORDERS = new ArrayList<>(Arrays.asList(new String[]{ORDER_ASC, ORDER_DESC}));

    public static final String INCORRECT_SORT_FIELD_MSG = "Incorrect field name for sorting(Should be \"name\" or \"sectorRevenue\" or \"totalRevenue\")";
    public static final String INCORRECT_SORT_DIRECTION_MSG = "Incorrect direction for sorting(Should be \"asc\" or \"desc\")";
    public static final String INCORRECT_TOP_MSG = "Incorrect \"top\" parameter value(Should be an integer number)";
    public static final String INCORRECT_SKIP_MSG = "Incorrect \"skip\" parameter value(Should be an integer number)";
}
