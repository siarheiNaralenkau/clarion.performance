package com.wolterskluwer.dd.common.datasource.mongo;

import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.wolterskluwer.dd.common.datasource.DeepDiligenceDataSource;;

public class MongoDBDataSource implements DeepDiligenceDataSource {		
	
	private String host;
	private String port;
	private String database;
	private String dbUser;
	private String dbPassword;
	private String maxConnections;
	private String minConnections;
	
	public String getMinConnections() {
		return minConnections;
	}

	public void setMinConnections(String minConnections) {
		this.minConnections = minConnections;
	}

	private MongoClient mongoClient;
	
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}	

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}	
	
	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	
	public String getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(String maxConnections) {
		this.maxConnections = maxConnections;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
	
	public MongoClient getMongoClient() {
		return this.mongoClient;
	}
			
	public MongoDBDataSource() {}
	
	public MongoDatabase getDBConnection() {
		if(mongoClient == null) {
			MongoCredential credentials = MongoCredential.createCredential(dbUser, database, dbPassword.toCharArray());			
			ServerAddress serverAddress = new ServerAddress(host, Integer.valueOf(port));
			MongoClientOptions clientOptions = MongoClientOptions.builder()
					.minConnectionsPerHost(Integer.valueOf(minConnections))
					.connectionsPerHost(Integer.valueOf(maxConnections))
					.connectTimeout(1000*1000)
					.serverSelectionTimeout(1000*1000)
					.socketTimeout(1000*1000)
					.build();
			mongoClient = new MongoClient(Arrays.asList(serverAddress), Arrays.asList(credentials), clientOptions);			
		}		
		MongoDatabase mongoDb = mongoClient.getDatabase(database);
        return mongoDb;
	}
	
	public void closeDBConnection() {
		mongoClient.close();
	}	
}
