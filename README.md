## UniLoc: Unified Fault Localization of Continuous Integration Failures
UniLoc, a unified technique to localize faults in both source code and build scripts given CI failures.

## Restore Data
To restore database server, install docker and docker-compose. Here is link to install docker and docker-compose:      

docker:   
https://docs.docker.com/docker-for-windows/install/   


docker-compose:   
https://docs.docker.com/compose/install/    

How to Restore Data:   
	1. Go to Dockerfile folder.
	2. In mysql-dump folder unzip the backup sql script and return to Dockerfile folder.   
	3. In Dockerfile folder run following command:
		docker-compose up   
		If you have permission issue, use sudo to run the command.     
		
Other useful commands:
	To stop and remove existing docker images, run following commands:       
		docker stop $(sudo docker ps -a -q)    		
		docker rm $(sudo docker ps -a -q)   
		
	To cleanup docker image data module, run following commmands:    
		docker system prune -a
		docker volume prune   
		
		
After restoring the data, our UniLoc tool is ready to execute. 		



## Build Project:   
To build the project execute:   
  mvn package    
  
To cleanup prior executable:    
  mvn clean   

  
  
## Run the Project:     
To run the project execute:   
  java -jar uniloc-1.0.0-SNAPSHOT.jar   
  
If you run the project for the first time, please select menu option 1 to download Git Repo for analysis. To execute other different approaches of fault localization, select appropriate id. Result for will stored in database, also summary will be shown in the command line.    

## Performance Evaluation:
For detailed performance analysis, you need to execute query using mysql-client or mysql-workbench. In project "scripts" folder you can find sql queries to perform analysis for different approaches.
