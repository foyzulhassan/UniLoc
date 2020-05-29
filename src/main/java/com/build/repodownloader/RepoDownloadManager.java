package com.build.repodownloader;

import java.io.File;
import java.util.List;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;


public class RepoDownloadManager {
	
	public static final String GIT_BASE_PATH="https://github.com/";
	
	public void downloadProjects()
	{
		initDir();
		DBActionExecutorChangeData dbaction=new DBActionExecutorChangeData();		
		List<String> projectlist=dbaction.getProjectList();
		CloneRemoteRepository cloner=new CloneRemoteRepository();
		
		for(int index=0;index<projectlist.size();index++)
		{
			String projectname=projectlist.get(index);
			String localfolder=projectname.replace('/', '@');
			String localrepo=Config.repoDir+localfolder;
			String remoterepo=GIT_BASE_PATH+projectname+".git";
			
			File f = new File(localrepo);
			
			if (f.exists() && f.isDirectory()) {
				System.out.println("Repo Already downloaded:"+remoterepo);
			} else {
				cloner.DownloadRemoteRepository(localrepo, remoterepo);
			}	
			
		}		
	}
	
	private void initDir()
	{
		File f = new File(Config.repoDir);
		
		if (f.exists() && f.isDirectory()) {
			System.out.println("Repodir Already Created");
		} else {
			boolean dirCreated = f.mkdirs();
		}
		
		f = new File(Config.workDir);
		
		if (f.exists() && f.isDirectory()) {
			System.out.println("WorkDir Already Created");
		} else {
			boolean dirCreated = f.mkdirs();
		}	
		
	}
	
}
