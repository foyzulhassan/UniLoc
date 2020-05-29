package com.build.gradle.ast.selection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;
import com.util.sorting.SortingMgr;

public class DiffFilterDependencyWithBoostScoreSimBuildAST {
	public void simAnalyzerWithFailPartLineSimRecentDependency() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		// List<Gradlebuildfixdata> projects=dbexec.getRowsWithID(2490551);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		double totalndcg = 0.0;

		try {

			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;

			int datasize = 0;

			for (int index = 0; index < projects.size(); index++) {
				Gradlebuildfixdata proj = projects.get(index);

				String project = proj.getGhProjectName();
				project = project.replace('/', '@');

				datasize++;
				System.out.println(proj.getRow() + "=>" + project);

				CommitAnalyzer cmtanalyzer = null;

				try {
					cmtanalyzer = new CommitAnalyzer("test", project);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						proj.getGitLastfailCommit());

				Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBuildDependencyWithBuildAST(
						proj.getGitLastfailCommit(), proj.getF2row(), proj, recentchangefile);
				List<String> filementioned = cmtanalyzer.getFilesMentionedInFailPart(proj.getGitLastfailCommit(),
						proj.getF2row(), proj);

				// Map<String, Double> simmap =
				// cmtanalyzer.getTreeSimilarityMapWithBuildDependencyTFIDF(
				// proj.getGitLastfailCommit(), proj.getF2row(), proj,
				// recentchangefile);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				List<String> failfixslist = Arrays.asList(failfixs);

				List<String> mergedfiles = new ArrayList<>();

				mergedfiles.addAll(failfixslist);

				// mergedfiles.addAll(filementioned);

				String difflog = proj.getFailChange();

				for (String name : simmap.keySet()) {
					int failindex = 0;

					File f = new File(name);
					boolean match = false;
					while (failindex < mergedfiles.size()) {

						if (name.equals(mergedfiles.get(failindex))) {
							// Double val = simmap.get(name) + param2 *
							// simmap.get(name);
							// Double val=nthroot(param2,simmap.get(name));
							Double val = Math.pow(simmap.get(name), Config.alphaparam)
									* Math.pow(1, (1.0 - Config.alphaparam));
							simmap.put(name, val);
							match = true;
							break;
						}
						failindex++;
					}

					if (match == false) {
						Double val = Math.pow(simmap.get(name), Config.alphaparam)
								* Math.pow(simmap.get(name), (1.0 - Config.alphaparam));
						simmap.put(name, val);
					}
				}

				Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

				ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

				int topn = rankmetric.getTopN(keys, actualfixs);
				double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
				double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);
				double ndcg = rankmetric.calculateNDCG(keys, actualfixs);

				System.out.println("\n\n\nNDCG:");
				System.out.print(ndcg);

				if (Config.updateTopN)
					projects.get(index).setEvDiffDepBoostAstPos(topn);

				if (Config.updateMrr)
					projects.get(index).setEvDiffDepBoostAstMrr(mrr);

				if (Config.updateMap)
					projects.get(index).setEvDiffDepBoostAstMap(map);

				if (Config.updateNdcg) {
					projects.get(index).setEvDiffDepBoostAstNdcg(ndcg);
					System.out.println("\n\n\nSetting NDCG");
				}

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;
				totalndcg = totalndcg + ndcg;

			}

			System.out.println("\n\n\n*******UniLoc********");
			System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
					+ " MAP: " + (totalmap / projects.size()) + " NDCG: " + (totalndcg / projects.size()));

			SessionGenerator.closeFactory();
			dbexec = new DBActionExecutorChangeData();
			dbexec.updateBatchExistingRecord(projects);

		} catch (Exception ex) {
			System.out.println("Exception in Calling Method");
			/* ignore */}

	}

	public void simAnalyzerWithFailPartLineSimRecentDependencySumming() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		// List<Gradlebuildfixdata> projects=dbexec.getRowsWithID(2490551);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		try {

			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;

			int datasize = 0;

			for (int index = 0; index < projects.size(); index++) {
				Gradlebuildfixdata proj = projects.get(index);

				String project = proj.getGhProjectName();
				project = project.replace('/', '@');

				datasize++;
				System.out.println(proj.getRow() + "=>" + project);

				CommitAnalyzer cmtanalyzer = null;

				try {
					cmtanalyzer = new CommitAnalyzer("test", project);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						proj.getGitLastfailCommit());
				// Map<String, Double> simmap =
				// cmtanalyzer.getTreeSimilarityMapWithBuildDependencyTFIDF(
				// proj.getGitLastfailCommit(), proj.getF2row(), proj,
				// recentchangefile);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				List<String> failfixslist = Arrays.asList(failfixs);

				List<String> mergedlist = new ArrayList<>();

				mergedlist.addAll(recentchangefile);
				mergedlist.addAll(failfixslist);

				Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBuildDependencyWithBuildASTFiltering(
						proj.getGitLastfailCommit(), proj.getF2row(), proj, recentchangefile, mergedlist);

				Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

				ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

				int topn = rankmetric.getTopN(keys, actualfixs);
				double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
				double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

				projects.get(index).setEvDiffDepBoostAstPos(topn);
				projects.get(index).setEvDiffDepBoostAstMrr(mrr);
				projects.get(index).setEvDiffDepBoostAstMap(map);

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;

			}

			System.out.println("\n\n\n*******Diff Filter+Dependency+BoostScore+BuildScript AST********");
			System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
					+ " MAP: " + (totalmap / projects.size()));

			SessionGenerator.closeFactory();
			dbexec = new DBActionExecutorChangeData();
			dbexec.updateBatchExistingRecord(projects);

		} catch (Exception ex) {
			/* ignore */}

	}

	public void luceneScoring() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		// List<Gradlebuildfixdata> projects=dbexec.getRowsWithID(2490551);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		try {

			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;

			int datasize = 0;

			for (int index = 0; index < projects.size(); index++) {
				Gradlebuildfixdata proj = projects.get(index);

				String project = proj.getGhProjectName();
				project = project.replace('/', '@');

				datasize++;
				System.out.println(proj.getRow() + "=>" + project);

				CommitAnalyzer cmtanalyzer = null;

				try {
					cmtanalyzer = new CommitAnalyzer("test", project);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						proj.getGitLastfailCommit());
				// Map<String, Double> simmap =
				// cmtanalyzer.getTreeSimilarityMapWithBuildDependencyTFIDF(
				// proj.getGitLastfailCommit(), proj.getF2row(), proj,
				// recentchangefile);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				List<String> failfixslist = Arrays.asList(failfixs);

				List<String> mergedlist = new ArrayList<>();

				mergedlist.addAll(recentchangefile);
				mergedlist.addAll(failfixslist);

				Map<String, Double> simmap = cmtanalyzer.getLuceneScoring(proj.getGitLastfailCommit(), proj.getF2row(),
						proj, recentchangefile);

				List<String> mergedfiles = new ArrayList<>();

				mergedfiles.addAll(failfixslist);

				// mergedfiles.addAll(filementioned);

				String difflog = proj.getFailChange();

				for (String name : simmap.keySet()) {
					int failindex = 0;

					File f = new File(name);
					boolean match = false;
					while (failindex < mergedfiles.size()) {

						if (name.equals(mergedfiles.get(failindex))) {
							// Double val = simmap.get(name) + param2 *
							// simmap.get(name);
							// Double val=nthroot(param2,simmap.get(name));
							Double val = Math.pow(simmap.get(name), Config.alphaparam)
									* Math.pow(1, (1.0 - Config.alphaparam));
							simmap.put(name, val);
							match = true;
							break;
						}
						failindex++;
					}

					if (match == false) {
						Double val = Math.pow(simmap.get(name), Config.alphaparam)
								* Math.pow(simmap.get(name), (1.0 - Config.alphaparam));
						simmap.put(name, val);
					}
				}

				Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

				ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

				int topn = rankmetric.getTopN(keys, actualfixs);
				double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
				double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

				projects.get(index).setEvDiffDepBoostAstPos(topn);
				projects.get(index).setEvDiffDepBoostAstMrr(mrr);
				projects.get(index).setEvDiffDepBoostAstMap(map);

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;

			}

			System.out.println("\n\n\n*******Diff Filter+Dependency+BoostScore+BuildScript AST********");
			System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
					+ " MAP: " + (totalmap / projects.size()));

			SessionGenerator.closeFactory();
			dbexec = new DBActionExecutorChangeData();
			dbexec.updateBatchExistingRecord(projects);

		} catch (Exception ex) {
			/* ignore */}

	}

	public void simAnalyzerWithFailPartLineSimRecentDependencyNoHistory() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		// List<Gradlebuildfixdata> projects=dbexec.getRowsWithID(2490551);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		try {

			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;

			int datasize = 0;

			for (int index = 0; index < projects.size(); index++) {
				Gradlebuildfixdata proj = projects.get(index);

				String project = proj.getGhProjectName();
				project = project.replace('/', '@');

				datasize++;
				System.out.println(proj.getRow() + "=>" + project);

				CommitAnalyzer cmtanalyzer = null;

				try {
					cmtanalyzer = new CommitAnalyzer("test", project);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						proj.getGitLastfailCommit());

				Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBuildDependencyWithBuildAST(
						proj.getGitLastfailCommit(), proj.getF2row(), proj, recentchangefile);

				// Map<String, Double> simmap =
				// cmtanalyzer.getTreeSimilarityMapWithBuildDependencyTFIDF(
				// proj.getGitLastfailCommit(), proj.getF2row(), proj,
				// recentchangefile);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				// for (String name : simmap.keySet()) {
				// int failindex = 0;
				//
				// File f = new File(name);
				// boolean match=false;
				// while (failindex < failfixs.length) {
				//
				// if (name.equals(failfixs[failindex])) {
				// // Double val = simmap.get(name) + param2 *
				// // simmap.get(name);
				// // Double val=nthroot(param2,simmap.get(name));
				// Double val = Math.pow(simmap.get(name), Config.alphaparam)
				// * Math.pow(1, (1.0 - Config.alphaparam));
				// simmap.put(name, val);
				// match=true;
				// break;
				// }
				// failindex++;
				// }
				//
				// if(match==false)
				// {
				// Double val = Math.pow(simmap.get(name), Config.alphaparam)
				// * Math.pow(simmap.get(name), (1.0 - Config.alphaparam));
				// simmap.put(name, val);
				// }
				// }

				Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

				ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

				int topn = rankmetric.getTopN(keys, actualfixs);
				double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
				double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

				// projects.get(index).setEvDiffdepboostPos(topn);
				// projects.get(index).setEvDiffdepboostMrr(mrr);
				// projects.get(index).setEvDiffdepboostMap(map);

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;

			}

			System.out.println("\n\n\n*******Diff Filter+Dependency+BoostScore+BuildScript AST********");
			System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
					+ " MAP: " + (totalmap / projects.size()));

			SessionGenerator.closeFactory();
			// dbexec = new DBActionExecutorChangeData();
			// dbexec.updateBatchExistingRecord(projects);

		} catch (Exception ex) {
			/* ignore */}

	}

	public String getCommaSeperated(List<String> list) {

		StringBuilder strbuilder = new StringBuilder();

		for (int in = 0; in < list.size(); in++) {
			strbuilder.append(list.get(in));
			strbuilder.append(";");
		}

		return strbuilder.toString();

	}

	public double nthroot(int n, double x) {
		return nthroot(n, x, .0001);
	}

	public double nthroot(int n, double x, double p) {
		if (x < 0) {
			System.err.println("Negative!");
			return -1;
		}
		if (x == 0)
			return 0;
		double x1 = x;
		double x2 = x / n;
		while (Math.abs(x1 - x2) > p) {
			x1 = x2;
			x2 = ((n - 1.0) * x2 + x / Math.pow(x2, n - 1.0)) / n;
		}
		return x2;
	}

}
