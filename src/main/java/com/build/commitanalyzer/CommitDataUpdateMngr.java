package com.build.commitanalyzer;

import java.util.List;

import com.build.analyzer.dtaccess.DBActionExecutor;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.CommitChange;
import com.build.analyzer.entity.Travistorrent;



public class CommitDataUpdateMngr {

	public void updateProgCommitData(String analyzer) throws Exception {
		DBActionExecutor dbexec = new DBActionExecutor();

		List<Travistorrent> projects = dbexec.getAllProjectsOfLang("java",analyzer);

		for (int index = 0; index < projects.size(); index++) {
			Travistorrent proj = projects.get(index);

			String commitlist = proj.getGitAllBuiltCommits();
			
			//commitlist=proj.getGitCommit()+"#"+commitlist;

			String[] commits = commitlist.split("#");

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int cmtsize = 0; cmtsize < commits.length; cmtsize++) {
				cmtanalyzer.commitSampleTry(commits[cmtsize]);

			}

			CommitChange cmtchange = cmtanalyzer.getCommitChangeTracker();

//			projects.get(index).setCmtImportChangeCount(cmtchange.getImportChange());
//			projects.get(index).setCmtClassChangeCount(cmtchange.getClassChange());
//			projects.get(index).setCmtMethodChangeCount(cmtchange.getMethodChange());
//			projects.get(index).setCmtFieldChangeCount(cmtchange.getFieldChange());
//			projects.get(index).setCmtMethodBodyChangeCount(cmtchange.getMethodBodyChange());
			projects.get(index).setCmtBuildfilechangecount(cmtchange.getBuildFileChange());
			//projects.get(index).setCmtBuildfilechangecount(cmtBuildfilechangecount);

		}

		
		SessionGenerator.closeFactory();
		
		dbexec = new DBActionExecutor();
		dbexec.updateBatchExistingRecord(projects);
	}

}
