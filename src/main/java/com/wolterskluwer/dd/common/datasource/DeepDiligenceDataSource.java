package com.wolterskluwer.dd.common.datasource;

import com.mongodb.client.MongoDatabase;

public interface DeepDiligenceDataSource {
	MongoDatabase getDBConnection();
	void closeDBConnection();
}
