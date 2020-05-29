package com.build.analyzer.dtgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.keyword.Keyword;
import com.build.keyword.TermExtractor;
import com.build.logfilter.FilterLogText;
import com.build.logger.LogResultWriter;
import com.build.metrics.RankingCalculator;
import com.build.revertanalyzer.ReverAnalyzer;
import com.buildlogparser.logmapper.BuildErrorLogMapper;
import com.github.gumtreediff.actions.model.Action;

public class SimGenerationMngr {

	public void simAnalyzerFullLog() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, true, false);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			projects.get(index).setTotalfileCount(sortedsimmap.size());
			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			projects.get(index).setFulllogPos(topn);
			projects.get(index).setFulllogMrr(mrr);
			projects.get(index).setFulllogMap(map);
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public void simAnalyzerFilteredLog() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);
			// project="D:\\test\\appsly-android-rest";
			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			int lastindex = 0;

			for (int fileindex = 0; fileindex < actualfixs.length; fileindex++) {
				for (int tindex = 0; tindex < keys.size(); tindex++) {
					String file = keys.get(tindex);

					if (file.equals(actualfixs[fileindex])) {
						if (lastindex < tindex) {
							lastindex = tindex;
						}

					}
				}

			}

			projects.get(index).setFilterlogdualPos(lastindex);

			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			projects.get(index).setFilterlogdualMrr(mrr);
			projects.get(index).setFilterlogdualMap(map);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public void simAnalyzerDifferemtialLog() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		// List<Gradlebuildfixdata> projects=new
		// ArrayList<Gradlebuildfixdata>();
		// projects.clear();
		// Gradlebuildfixdata gproject = dbexec.getEntityWithRowId(444640);
		// projects.add(gproject);

		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, false);

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			projects.get(index).setFilterlogPos(topn);
			projects.get(index).setFilterlogMrr(mrr);
			projects.get(index).setFilterlogMap(map);

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public void simAnalyzerDifferemtialLogWithChange() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		// List<Gradlebuildfixdata> projects=new
		// ArrayList<Gradlebuildfixdata>();
		// projects.clear();
		// Gradlebuildfixdata gproject =
		// dbexec.getEntityWithRowId((long)444640);
		// projects.add(gproject);

		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			//if (proj.getRow() == 183221) {

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(proj.getRow()+"=>"+project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, false);

			// this map contains from having in common logdiff
			Map<String, Double> samesimmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, true);

			String actualfixfile = proj.getF2passFilelist();
			String failintrofiles = proj.getFailFilelist();

			// This code to inhance
			List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
					proj.getGitLastfailCommit());
			failintrofiles = getCommaSeperated(recentchangefile);
			///

			String[] failfixs = failintrofiles.split(";");
			String[] actualfixs = actualfixfile.split(";");

			// Files those found for having in common logdiff
			for (String name : samesimmap.keySet()) {

				if (simmap.containsKey(name)) {
					Double val = simmap.get(name) - 0.5 * samesimmap.get(name);
					simmap.put(name, val);
					break;
				}

			}
			///////////////////////////////////////////////////

			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;

				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 0.5 * simmap.get(name);
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}

			//For filename in log file
			String difflog = proj.getFailChange();
			for (String name : simmap.keySet()) {

				File f = new File(name);

				if (difflog.contains(f.getName())) {
					Double val = simmap.get(name) + 0.9 * simmap.get(name);
					simmap.put(name, val);
				}

			}

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			projects.get(index).setFilterlogdualPos(topn);
			projects.get(index).setFilterlogdualMrr(mrr);
			projects.get(index).setFilterlogdualMap(map);
			
		 // }//for local testing

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}
	
	public void simAnalyzerWithFailPartLineSim() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();


		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			//if (proj.getRow() == 183221) {

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(proj.getRow()+"=>"+project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getFailLogPartTreeSimilarityMap(proj.getGitLastfailCommit(),
					proj.getF2row(), proj);

			// this map contains from having in common logdiff
			Map<String, Double> samesimmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, true);

			String actualfixfile = proj.getF2passFilelist();
			String failintrofiles = proj.getFailFilelist();

			// This code to inhance
			List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
					proj.getGitLastfailCommit());
			failintrofiles = getCommaSeperated(recentchangefile);
			///

			String[] failfixs = failintrofiles.split(";");
			String[] actualfixs = actualfixfile.split(";");

			// Files those found for having in common logdiff
			for (String name : samesimmap.keySet()) {

				if (simmap.containsKey(name)) {
					Double val = simmap.get(name) - 0.5 * samesimmap.get(name);
					simmap.put(name, val);
					break;
				}

			}
			///////////////////////////////////////////////////

			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;
				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 0.5 * simmap.get(name);
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}
			
			
			//This part is for file names mentioned in log file
//			String difflog = proj.getFailChange();
//			
//			for (String name : simmap.keySet()) {
//				File f = new File(name);
//				if (difflog.contains(f.getName())) {
//					Double val = simmap.get(name) + 0.9 * simmap.get(name);
//					simmap.put(name, val);
//				}
//			}

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);
			
			System.out.println("Mrr="+mrr+" MAP= "+map);

			projects.get(index).setFailpartsimPos(topn);
			projects.get(index).setFailpartsimMrr(mrr);
			projects.get(index).setFailpartsimMap(map);
			
		 // }//for local testing

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	public void simAnalyzerDifferemtialLogWithChangeforLogging() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		LogResultWriter logwriter = new LogResultWriter();
		// List<Gradlebuildfixdata> projects=new
		// ArrayList<Gradlebuildfixdata>();
		// projects.clear();
		// Gradlebuildfixdata gproject =
		// dbexec.getEntityWithRowId((long)444640);
		// projects.add(gproject);

		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, false);

			// this map contains from having in common logdiff
			Map<String, Double> samesimmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, false, true);

			String actualfixfile = proj.getF2passFilelist();
			String failintrofiles = proj.getFailFilelist();

			// This code to inhance
			List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
					proj.getGitLastfailCommit());
			failintrofiles = getCommaSeperated(recentchangefile);
			///

			String[] failfixs = failintrofiles.split(";");
			String[] actualfixs = actualfixfile.split(";");

			// Files those found for having in common logdiff
			for (String name : samesimmap.keySet()) {

				if (simmap.containsKey(name)) {
					Double val = simmap.get(name) - 0.5 * samesimmap.get(name);
					simmap.put(name, val);
					break;
				}

			}
			///////////////////////////////////////////////////

			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;

				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 0.5 * simmap.get(name);
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}

			String difflog = proj.getFailChange();
			
			FilterLogText filter=new FilterLogText();
			difflog=filter.performFiltering(proj);

			for (String name : simmap.keySet()) {

				File f = new File(name);

				if (difflog.contains(f.getName())) {
					Double val = simmap.get(name) + 0.9 * simmap.get(name);
					simmap.put(name, val);
				}

			}

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			// String[] failfixs = failintrofiles.split(";");
			// String[] actualfixs = actualfixfile.split(";");
			// String difflog = proj.getFailChange();
			String failcommit = proj.getGitLastfailCommit();
			String passcommit = proj.getGitFixCommit();
			// double mrr=proj.getFilterlogdualMrr();

			if (mrr < 0.04) {
				logwriter.printResultLog(project, failcommit, passcommit, actualfixs, difflog, keys);
			}

		}

	}
	
	
	public void printDifferentLogs() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
	
		List<Gradlebuildfixdata> projects = dbexec.getRows();
		LogResultWriter logwriter = new LogResultWriter();
		
		
		int index=0;
		while(index < projects.size()) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);			
			
			String largelog=proj.getBlLargelog();
			String failpart=proj.getFailChange();
			String passpart=proj.getFixChange();
			String failpartsim=proj.getFailPartSim();
			String actualfixfile = proj.getF2passFilelist();

			logwriter.printDifferentTypeofLog(index+1,proj.getRow(), project, largelog, failpart, passpart, failpartsim,actualfixfile);
			
			if(index>100)
				break;

			index++;
		}

	}

	public void simtesting() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			String logtext = proj.getBlLargelog();

			List<Keyword> keywords = TermExtractor.guessFromString("getValue textValue");

			for (int in = 0; in < keywords.size(); in++) {
				System.out.println(keywords.get(in).getStem() + "->" + keywords.get(in).getFrequency());
			}
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}
	
	public void simAnalyzerWithBuildDependency() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		// List<Gradlebuildfixdata> projects=new
		// ArrayList<Gradlebuildfixdata>();
		// projects.clear();
		// Gradlebuildfixdata gproject =
		// dbexec.getEntityWithRowId((long)444640);
		// projects.add(gproject);

		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			//if (proj.getRow() == 183221) {

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(proj.getRow()+"=>"+project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
					proj.getGitLastfailCommit());
			
			Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBuildDependency(proj.getGitLastfailCommit(),proj.getF2row(), proj,recentchangefile);


//			// this map contains from having in common logdiff
//			Map<String, Double> samesimmap = cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
//					proj.getF2row(), proj, false, true);

			String actualfixfile = proj.getF2passFilelist();
			String failintrofiles = proj.getFailFilelist();

			// This code to inhance

			failintrofiles = getCommaSeperated(recentchangefile);
			///

			String[] failfixs = failintrofiles.split(";");
			String[] actualfixs = actualfixfile.split(";");

//			// Files those found for having in common logdiff
//			for (String name : samesimmap.keySet()) {
//
//				if (simmap.containsKey(name)) {
//					Double val = simmap.get(name) - 0.5 * samesimmap.get(name);
//					simmap.put(name, val);
//					break;
//				}
//
//			}
			///////////////////////////////////////////////////

			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;

				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 0.5 * simmap.get(name);
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}

			String difflog = proj.getFailChange();

			for (String name : simmap.keySet()) {

				File f = new File(name);

				if (difflog.contains(f.getName())) {
					Double val = simmap.get(name) + 0.5 * simmap.get(name);
					simmap.put(name, val);
				}

			}

			Map<String, Double> sortedsimmap = sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			projects.get(index).setFilterlogdualPos(topn);
			projects.get(index).setFilterlogdualMrr(mrr);
			projects.get(index).setFilterlogdualMap(map);
			
			System.out.println(project+"<=>"+mrr);
			System.out.println(project+"<=>"+map);
			
		 // }//for local testing

		}

//		SessionGenerator.closeFactory();
//		dbexec = new DBActionExecutorChangeData();
//		dbexec.updateBatchExistingRecord(projects);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;

	}

	public String getAllContent(List<Keyword> keywords) {

		StringBuilder strbuilder = new StringBuilder();

		for (int in = 0; in < keywords.size(); in++) {
			strbuilder.append(keywords.get(in).getStem());
			strbuilder.append(" ");
		}

		return strbuilder.toString();

	}

	public String getCommaSeperated(List<String> list) {

		StringBuilder strbuilder = new StringBuilder();

		for (int in = 0; in < list.size(); in++) {
			strbuilder.append(list.get(in));
			strbuilder.append(";");
		}

		return strbuilder.toString();

	}

}
