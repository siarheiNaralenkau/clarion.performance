package com.wolterskluwer.dd.common.beans;

public class CompanyRelationshipIndex {	

	private String disclosureType;
	private String companyId;
	
	public CompanyRelationshipIndex(String disclosureType, String companyId) {
		super();
		this.disclosureType = disclosureType;
		this.companyId = companyId;
	}
	
	public CompanyRelationshipIndex() {
		super();		
	}
	
	public String getDisclosureType() {
		return disclosureType;
	}

	public void setDisclosureType(String disclosureType) {
		this.disclosureType = disclosureType;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((companyId == null) ? 0 : companyId.hashCode());
		result = prime * result + ((disclosureType == null) ? 0 : disclosureType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CompanyRelationshipIndex))
			return false;
		CompanyRelationshipIndex other = (CompanyRelationshipIndex) obj;
		if (companyId == null) {
			if (other.companyId != null)
				return false;
		} else if (!companyId.equals(other.companyId))
			return false;
		if (disclosureType == null) {
			if (other.disclosureType != null)
				return false;
		} else if (!disclosureType.equals(other.disclosureType))
			return false;
		return true;
	}	
	
	
}
