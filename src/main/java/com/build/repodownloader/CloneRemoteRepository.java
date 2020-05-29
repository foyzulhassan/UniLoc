package com.build.repodownloader;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class CloneRemoteRepository {
	
	private static final String REMOTE_URL = "https://github.com/github/testrepo.git";
	
	public CloneRemoteRepository()
	{
		
		
	}
	
	public void DownloadRemoteRepository(String localfolder, String remoterepo) 
	{
		File localPath = null;
		localPath = new File(localfolder);
		localPath.mkdir();
      

        // then clone
        System.out.println("Cloning from " + remoterepo + " to " + localPath);
        try{
        		
        		Git result = Git.cloneRepository()
                .setURI(remoterepo)
                .setDirectory(localPath)                
                .call();
        		
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
	        System.out.println("Having repository: " + result.getRepository().getDirectory());

            // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=474093
	        result.getRepository().close();
        } catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      

  }
	
}
