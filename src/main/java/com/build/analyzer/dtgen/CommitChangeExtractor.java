package com.build.analyzer.dtgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.keyword.Keyword;
import com.build.keyword.TermExtractor;
import com.build.revertanalyzer.ReverAnalyzer;
import com.buildlogparser.logmapper.BuildErrorLogMapper;
import com.github.gumtreediff.actions.model.Action;

public class CommitChangeExtractor {
	
	public void testCommit()
	{
		
		CommitAnalyzer cmtanalyzer = null;

		try {
			cmtanalyzer = new CommitAnalyzer("test", "oblac@jodd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		Map<String, List<Action>> lastfixchangemap = cmtanalyzer.extractChangeInBetweenCommit("cb2f5d554478572cdb760b80bf03fe936063045b",
																							   
				"ebf1c35700cc68abde93434b64940470bc8515d0");
		
		Iterator<Entry<String, List<Action>>> it = lastfixchangemap.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<String, List<Action>> entry = (Entry<String, List<Action>>) it.next();
			String key = entry.getKey();
			System.out.println(key);
		}
		
		
	}

	public void updateCommitChange() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		ReverAnalyzer revertcheker = new ReverAnalyzer();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String commit = proj.getGitCommit();
			String failintrocommit = proj.getGitFailintroCommit();

			String lastfailcommit = proj.getGitLastfailCommit();
			String passcommit = proj.getGitFixCommit();

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			// project="D:\\test\\appsly-android-rest";
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Get the changes in between Pass to Fail Transition commit
			Map<String, List<Action>> failchangemap = cmtanalyzer.extractChangeInBetweenCommit(commit, failintrocommit);

			// Get the changes in between Fail to Pass Transition commit
			Map<String, List<Action>> fixchangemap = cmtanalyzer.extractChangeInBetweenCommit(passcommit,
					failintrocommit);
			
			Map<String, List<Action>> lastfixchangemap = cmtanalyzer.extractChangeInBetweenCommit(passcommit,
					lastfailcommit);

			if (revertcheker.isCodeRevereted(failchangemap, fixchangemap)) {
				projects.get(index).setReveretedStatus("YES");
			} else {
				projects.get(index).setReveretedStatus("NO");
			}

			int revertfilecount = revertcheker.getRevertedFileCount(failchangemap, fixchangemap);
			int changefilecount = failchangemap.size();
			
			String failchangefiles=revertcheker.getChangeFileList(failchangemap);
			String fixchangefiles=revertcheker.getChangeFileList(fixchangemap);
			String lastfixchangefiles=revertcheker.getChangeFileList(lastfixchangemap);
			
			
			projects.get(index).setFailFilelist(failchangefiles);
			projects.get(index).setPassFilelist(fixchangefiles);
			projects.get(index).setF2passFilelist(lastfixchangefiles);
			projects.get(index).setChangefileCount(changefilecount);
			projects.get(index).setRevertfileCount(revertfilecount);
			
			BuildErrorLogMapper logmapper=new BuildErrorLogMapper();
			
			String strlog=logmapper.updateBuildErrorFullLogForProject(projects.get(index).getF2row());
			
			//List<Keyword> keywords=TermExtractor.guessFromString(strlog);
			//strlog=TermExtractor.getAllContent(keywords);
			
			projects.get(index).setBlLargelog(strlog);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}
	
	public void updateDifferentialCommitChange() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		//ReverAnalyzer revertcheker = new ReverAnalyzer();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		
//		List<Gradlebuildfixdata> projects=new ArrayList<Gradlebuildfixdata>();
//		projects.clear();		
//		Gradlebuildfixdata gproject = dbexec.getEntityWithRowId((long)444640);
//		projects.add(gproject);

		for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

//			String commit = proj.getGitCommit();
//			String failintrocommit = proj.getGitFailintroCommit();
//
//			String lastfailcommit = proj.getGitLastfailCommit();
//			String passcommit = proj.getGitFixCommit();
//
			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			System.out.println(project);
//			// project="D:\\test\\appsly-android-rest";
//			CommitAnalyzer cmtanalyzer = null;
//
//			try {
//				cmtanalyzer = new CommitAnalyzer("test", project);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			// Get the changes in between Pass to Fail Transition commit
//			Map<String, List<Action>> failchangemap = cmtanalyzer.extractChangeInBetweenCommit(commit, failintrocommit);
//
//			// Get the changes in between Fail to Pass Transition commit
//			Map<String, List<Action>> fixchangemap = cmtanalyzer.extractChangeInBetweenCommit(passcommit,
//					failintrocommit);
//
//			Map<String, List<Action>> lastfixchangemap = cmtanalyzer.extractChangeInBetweenCommit(passcommit,
//					lastfailcommit);
//
//			if (revertcheker.isCodeRevereted(failchangemap, fixchangemap)) {
//				projects.get(index).setReveretedStatus("YES");
//			} else {
//				projects.get(index).setReveretedStatus("NO");
//			}
//
//			int revertfilecount = revertcheker.getRevertedFileCount(failchangemap, fixchangemap);
//			int changefilecount = failchangemap.size();
//
//			String failchangefiles = revertcheker.getChangeFileList(failchangemap);
//			String fixchangefiles = revertcheker.getChangeFileList(fixchangemap);
//			String lastfixchangefiles = revertcheker.getChangeFileList(lastfixchangemap);
//
//			projects.get(index).setFailFilelist(failchangefiles);
//			projects.get(index).setPassFilelist(fixchangefiles);
//			projects.get(index).setF2passFilelist(lastfixchangefiles);
//			projects.get(index).setChangefileCount(changefilecount);
//			projects.get(index).setRevertfileCount(revertfilecount);

			BuildErrorLogMapper logmapper = new BuildErrorLogMapper();

			String strlog = logmapper.updateBuildErrorDifferentialLogForProject(projects.get(index).getRow(),
					projects.get(index).getF2row(),false);
			
			String strlogsame = logmapper.updateBuildErrorDifferentialLogForProject(projects.get(index).getRow(),
					projects.get(index).getF2row(),true);

			projects.get(index).setFailChange(strlog);
			projects.get(index).setFixChange(strlogsame);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

}
