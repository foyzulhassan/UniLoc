package com.build.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.build.analyzer.analysis.CSVReaderWriter;
import com.build.analyzer.analysis.LongEntity;
import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.util.sorting.SortingMgr;

/*
 * This class is for BaseLine Evaluation for ISSTA paper
 * BaseLine performance calculation Process: Failed Part Build Log+Java code AST(Saha et al.)+Then build scripts
 * at the end of ranking
 */
public class BaseLineEvaluation {

	public void calculateBaseLineEvaluationScore() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		for (int index = 0; index < projects.size(); index++) {

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

			Map<String, Double> simmapjava = cmtanalyzer
					.getBaselineFailLogPartTreeSimilarityMap(proj.getGitLastfailCommit(), proj.getF2row(), proj, true);

			Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmapjava);

			Map<String, Double> simmapgradle = cmtanalyzer
					.getBaselineFailLogPartTreeSimilarityMap(proj.getGitLastfailCommit(), proj.getF2row(), proj, false);

			// Appending Gradle files at the end of Sorted rank map
			for (Map.Entry<String, Double> entry : simmapgradle.entrySet()) {
				sortedsimmap.put(entry.getKey(), entry.getValue());
			}

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			projects.get(index).setTotalfileCount(sortedsimmap.size());
			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

			totaltopn = totaltopn + topn;
			totalmrr = totalmrr + mrr;
			totalmap = totalmap + map;

			projects.get(index).setEvBaselineBluiRPos(topn);
			projects.get(index).setEvBaselineBluiRMrr(mrr);
			projects.get(index).setEvBaselineBluiRMap(map);
		}

		System.out.println("\n\n\n*******Baseline ISSTA********");
		System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
		System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
				+ " MAP: " + (totalmap / projects.size()));

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}

	public void calculateBaseLineEvaluationScoreV2() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		 List<Gradlebuildfixdata> projects = dbexec.getEvalRows();
		//List<Gradlebuildfixdata> projects = dbexec.getTunningRows();

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		double totalndcg=0.0;

		for (int index = 0; index < projects.size(); index++) {

			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(proj.getRow() + "=>" + project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBluIRBaseLine(proj.getGitLastfailCommit(),
					proj.getF2row(), proj);

			Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			projects.get(index).setTotalfileCount(sortedsimmap.size());
			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);
			double ndcg=rankmetric.calculateNDCG(keys, actualfixs);

			totaltopn = totaltopn + topn;
			totalmrr = totalmrr + mrr;
			totalmap = totalmap + map;
			totalndcg=totalndcg+ndcg;

			if(Config.updateTopN)
				projects.get(index).setEvBaselineBluiRPos(topn);
			
			if(Config.updateMrr)
				projects.get(index).setEvBaselineBluiRMrr(mrr);
			
			if(Config.updateMap)
				projects.get(index).setEvBaselineBluiRMap(map);
			
			if(Config.updateNdcg)
				projects.get(index).setEvBaselineBluiRNdcg(ndcg);
		}

		System.out.println("\n\n\n*******Baseline2 BluiR********");
		System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
		System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
				+ " MAP: " + (totalmap / projects.size())+" NDCG: " + (totalndcg / projects.size()));
		
		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}

	public void calculateFileMentionedBaseLineEvaluationScore() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		 List<Gradlebuildfixdata> projects = dbexec.getEvalRows();
		//List<Gradlebuildfixdata> projects = dbexec.getTunningRows();

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		double totalndcg=0.0;

		for (int index = 0; index < projects.size(); index++) {

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

			String failintrofiles = proj.getFailFilelist();
			String[] failfixs = failintrofiles.split(";");
			List<String> failfixslist = Arrays.asList(failfixs);

			Map<String, Double> simmapjava = cmtanalyzer.getBaselineWithFileMentionedInFailLogPartTreeSimilarityMap(
					proj.getGitLastfailCommit(), proj.getF2row(), proj, failfixslist);

			Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmapjava);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			projects.get(index).setTotalfileCount(sortedsimmap.size());
			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);
			double ndcg=rankmetric.calculateNDCG(keys, actualfixs);

			totaltopn = totaltopn + topn;
			totalmrr = totalmrr + mrr;
			totalmap = totalmap + map;
			totalndcg=totalndcg+ndcg;

			if(Config.updateTopN)
			{
				projects.get(index).setEvBaselineFilePos(topn);
			}
			
			if(Config.updateMrr)
			{
				projects.get(index).setEvBaselineFileMrr(mrr);
			}
			
			if(Config.updateMap)
			{
				projects.get(index).setEvBaselineFileMap(map);
			}
			
			if(Config.updateNdcg)
			{
				projects.get(index).setEvBaselineFileNdcg(ndcg);
			}
		}

		System.out.println("\n\n\n*******Baseline1(File Name Mentioned in Failed Log Part)********");
		System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
		System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
				+ " MAP: " + (totalmap / projects.size())+" NDCG: " + (totalndcg / projects.size()));

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);		

	}

	public void updateFixFileCount() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		for (int index = 0; index < projects.size(); index++) {

			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			int fixcount = actualfixs.length;

			projects.get(index).setFixfileCount(fixcount);
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}
	
	
	public List<LongEntity> calculateCommitTime(boolean sourceonly,String filename) throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();
		List<Gradlebuildfixdata> projects;
		
		if(sourceonly)
			projects = dbexec.getOnlySourceRelatedFix();
		else
			projects = dbexec.getOnlyBuildFileRelatedFix();
		//List<Gradlebuildfixdata> projects = dbexec.getTunningRows();

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		List<LongEntity> timeList=new ArrayList<>();

		for (int index = 0; index < projects.size(); index++) {

			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(proj.getRow() + "=>" + project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long timeinmin= cmtanalyzer.getTimeDiffOfTwoCommitInMin(proj.getGitLastfailCommit(),proj.getGitFixCommit());
			LongEntity time=new LongEntity(timeinmin);
			timeList.add(time);			

		}
		
		CSVReaderWriter writer=new CSVReaderWriter();
		try {
			writer.writeListBean(timeList, filename);
		} catch (CsvDataTypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvRequiredFieldEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeList;
		
	}
	
	public void FixFileCountAnalysis(boolean sourceonly,String filename) throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		List<Gradlebuildfixdata> projects;
		
		if(sourceonly)
			projects = dbexec.getOnlySourceRelatedFix();
		else
			projects = dbexec.getOnlyBuildFileRelatedFix();

		List<LongEntity> fixcountlist=new ArrayList<>();
		for (int index = 0; index < projects.size(); index++) {

			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			System.out.println(project);

			String actualfixfile = proj.getF2passFilelist();

			String[] actualfixs = actualfixfile.split(";");

			int fixcount = actualfixs.length;
			
			fixcountlist.add(new LongEntity(fixcount));			
		}
		
		CSVReaderWriter writer=new CSVReaderWriter();
		try {
			writer.writeListBean(fixcountlist, filename);
		} catch (CsvDataTypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvRequiredFieldEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}
