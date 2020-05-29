package com.build.analyzer.diff.gradle;

import java.util.List;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorGradle;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlepatch;
import com.build.commitanalyzer.CommitAnalyzer;



public class GradleCommitDataMgr {
	
	public void updateGradleFileChange() throws Exception {
		DBActionExecutorGradle dbexec = new DBActionExecutorGradle();

		List<Gradlepatch> projects = dbexec.getRows();

		for (int index = 0; index < projects.size(); index++) {
			Gradlepatch proj = projects.get(index);

			String commitlist = proj.getGitFixCommit();
			String parentcommit=proj.getGitCommit();

			//commitlist=proj.getGitFixCommit()+"#"+commitlist;
			
			String[] commits = commitlist.split("#");

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			//project="D:\\test\\appsly-android-rest";
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int cmtsize = 0; cmtsize < commits.length; cmtsize++) {
				
				
				
				cmtanalyzer.extractChangeInBetweenCommit(parentcommit, commits[cmtsize]);
				Config.debugPrint=false;
			}

			String grdalechange=cmtanalyzer.getGradleChanges();
			
			projects.get(index).setPatchData(grdalechange);
		}

		
		SessionGenerator.closeFactory();
		
		dbexec = new DBActionExecutorGradle();
		dbexec.updateBatchExistingRecord(projects);
	}

}
