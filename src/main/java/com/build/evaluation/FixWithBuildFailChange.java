package com.build.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;
import com.util.sorting.SortingMgr;

public class FixWithBuildFailChange {

	public void fixWithBuildFailChangeAnalysis() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		double totalndcg = 0.0;

		for (int index = 0; index < projects.size(); index++) {

			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			// System.out.println(proj.getRow() + "=>" + project);

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Double> simmap = cmtanalyzer.getAllFileSimAsZero(proj.getGitLastfailCommit(), proj.getF2row(),
					proj);

			String actualfixfile = proj.getF2passFilelist();
			String failintrofiles = proj.getFailFilelist();

			String[] failfixs = failintrofiles.split(";");
			String[] actualfixs = actualfixfile.split(";");

			// Fail Introducing file change are geeting extra weight
			for (String name : simmap.keySet()) {
				int failindex = 0;

				while (failindex < failfixs.length) {

					if (name.equals(failfixs[failindex])) {
						Double val = simmap.get(name) + 1.0;
						simmap.put(name, val);
						break;
					}
					failindex++;
				}
			}

			Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

			ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

			int topn = rankmetric.getTopN(keys, actualfixs);
			double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
			double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);
			double ndcg = rankmetric.calculateNDCG(keys, actualfixs);

			totaltopn = totaltopn + topn;
			totalmrr = totalmrr + mrr;
			totalmap = totalmap + map;
			totalndcg = totalndcg + ndcg;

			if (Config.updateTopN) {
				projects.get(index).setEvRevertingPos(topn);
			}

			if (Config.updateMrr) {
				projects.get(index).setEvRevertingMrr(mrr);
			}

			if (Config.updateMap) {
				projects.get(index).setEvRevertingMap(map);
			}

			if (Config.updateNdcg) {
				projects.get(index).setEvRevertingNdcg(ndcg);
			}

		}

		System.out.println("\n\n\n*******Reverting Based Fault Localization********");
		System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
		System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
				+ " MAP: " + (totalmap / projects.size()) + " NDCG: " + (totalndcg / projects.size()));

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
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
