package clarion.performance.main;

import java.net.URLDecoder;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.wolterskluwer.dd.common.dao.impl.CompanyDAOImpl;
import com.wolterskluwer.dd.common.datasource.mongo.MongoDBDataSource;
import com.wolterskluwer.dd.common.parsers.CompanyParserFactory;

public class TestMongoPerfomance {		
	static Logger logger = Logger.getLogger(GetExpandedCompanyTest.class);
	
	static final String DEFAULT_COMPANY_ID = "777";
	static final int DEFAULT_THREADS = 1;
	static final long DEFAULT_THREAD_TIMEOUT = 0;
	
	public static void main(String[] args) {
		String companyId;
		int numberOfThreads;
		long timeout;		
		
		if(args.length > 0) {
			companyId = args[0];
		} else {
			companyId = DEFAULT_COMPANY_ID;
		}
		
		if(args.length > 1) {
			numberOfThreads = Integer.valueOf(args[1]);
		} else {
			numberOfThreads = DEFAULT_THREADS;
		}	
		
		if(args.length > 2) {
			timeout = Long.valueOf(args[2]);
		} else {
			timeout = DEFAULT_THREAD_TIMEOUT;
		}
		
		initLogger(numberOfThreads);
		
		MongoDBDataSource dataSource = new MongoDBDataSource();
		dataSource.setHost("10.232.64.5");
		dataSource.setPort("27017");
		dataSource.setDatabase("deep_diligence");
		dataSource.setDbUser("ddAdmin");
		dataSource.setDbPassword("ddAdmin");
		dataSource.setMaxConnections("100");
		dataSource.setMinConnections("30");
		CompanyDAOImpl companyDao = new CompanyDAOImpl(dataSource);
		
		for(int i = 0; i < numberOfThreads; i++) {			
			Thread thread = new Thread(new GetExpandedCompanyTest(companyDao, companyId, logger, CompanyParserFactory.createParser()));
			thread.start();	
			if(timeout > 0) {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {				
					e.printStackTrace();
				}
			}
		}			
	}
	
	private static void initLogger(int numThreads) {
		String path = GetExpandedCompanyTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			String decodedPath = URLDecoder.decode(path, "UTF-8");		
			String logFilePath = decodedPath.substring(0, decodedPath.lastIndexOf("/")) + "/loadTestLog" + String.valueOf(numThreads) + ".txt";
			System.out.println("Log file path: " + logFilePath);
			SimpleLayout layout = new SimpleLayout();
			FileAppender appender = new FileAppender(layout, logFilePath, true);
			logger.addAppender(appender);
			logger.setLevel(Level.DEBUG);
		} catch(Exception e) {
			System.out.println("Unable to configure logger");
		}
	}
	
	public static void printMemoryUsage() {
		int mb = 1024*1024;
		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();		
//		System.out.println("##### Heap utilization statistics [MB] #####");		
		//Print used memory
		System.out.println("Used Memory:" 
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		//Print free memory
		System.out.println("Free Memory:" 
			+ runtime.freeMemory() / mb);		
		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);
		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
	}
}
