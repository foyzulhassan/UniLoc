package com.build.param.tunning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;
import com.util.sorting.SortingMgr;

public class DiffFilterRecentTunning {

	public void simAnalyzerWithFailPartLineSimAndRecent() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		List<Double> paramvalues = new ArrayList<Double>();
		paramvalues.add(0.0);
		paramvalues.add(0.5);
		paramvalues.add(0.6);
		paramvalues.add(0.7);
		paramvalues.add(0.8);
		paramvalues.add(0.9);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		for (Double paramval : paramvalues) {
			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;
			Config.thresholdForSimFilter = paramval;

			for (int index = 0; index < projects.size(); index++) {

				Gradlebuildfixdata proj = projects.get(index);

				// checking loop
				// if (proj.getRow() == 949584) {
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

				Map<String, Double> simmap = cmtanalyzer.getFailLogPartTreeSimilarityMap(proj.getGitLastfailCommit(),
						proj.getF2row(), proj);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				// Fail Introducing file change are geeting extra weight
				for (String name : simmap.keySet()) {
					int failindex = 0;

					while (failindex < failfixs.length) {

						if (name.equals(failfixs[failindex])) {
							Double val = simmap.get(name) + 0.5 * simmap.get(name);
							simmap.put(name, val);
							// break;
						}
						failindex++;
					}
				}

				// For filename in log file
				String difflog = proj.getFailChange();
				for (String name : simmap.keySet()) {

					File f = new File(name);

					if (difflog.contains(f.getName())) {
						Double val = simmap.get(name) + 0.5 * simmap.get(name);
						simmap.put(name, val);
					}

				}

				Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

				ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

				int topn = rankmetric.getTopN(keys, actualfixs);
				double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
				double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;

				// } //checking loop

			}
			System.out.println("*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("TopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size())
					+ " MAP: " + (totalmap / projects.size()));

		}
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
