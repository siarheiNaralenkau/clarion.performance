1) To build the perfomance tool just run "mvn clean install" from command line(maven3 or later should be installed).
2) To run the application - go to "target" folder and execute command "java -jar clarion.performance-0.0.1-SNAPSHOT.jar 777 1"(JDK 7 or later should be installed).
Where First parameter(777 in this case) - is the company ID(Apple Inc = 777, Toyota Motors Corp = 10260),
and Second(1 in our case) - the number of running Threads.