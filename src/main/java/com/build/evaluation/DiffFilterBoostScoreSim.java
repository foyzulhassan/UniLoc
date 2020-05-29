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

public class DiffFilterBoostScoreSim {
	public void simAnalyzerWithFailPartLineSimBoostScore() throws Exception {

		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getEvalRows();

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

				Map<String, Double> simmap = cmtanalyzer.getFailLogPartTreeSimilarityMap(proj.getGitLastfailCommit(), proj.getF2row(), proj);

				String actualfixfile = proj.getF2passFilelist();
				String failintrofiles = proj.getFailFilelist();

				String[] failfixs = failintrofiles.split(";");
				String[] actualfixs = actualfixfile.split(";");

				String difflog = proj.getFailChange();
				
				for (String name : simmap.keySet()) {
					int failindex = 0;

					File f = new File(name);
					boolean match=false;
					while (failindex < failfixs.length) {

						if (name.equals(failfixs[failindex])) {
							// Double val = simmap.get(name) + param2 *
							// simmap.get(name);
							// Double val=nthroot(param2,simmap.get(name));
							Double val = Math.pow(simmap.get(name), Config.alphaparam)
									* Math.pow(1, (1.0 - Config.alphaparam));
							simmap.put(name, val);
							match=true;
							break;
						} 
						failindex++;
					}
					
					if(match==false)
					{
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
				double ndcg=rankmetric.calculateNDCG(keys, actualfixs);
				
				if(Config.updateTopN)
					projects.get(index).setEvDiffboostPos(topn);
				
				if(Config.updateMrr)
					projects.get(index).setEvDiffboostMrr(mrr);
				
				if(Config.updateMap)
					projects.get(index).setEvDiffboostMap(map);
				
				if(Config.updateNdcg)
					projects.get(index).setEvDiffboostNdcg(ndcg);

				totaltopn = totaltopn + topn;
				totalmrr = totalmrr + mrr;
				totalmap = totalmap + map;

			}

			System.out.println("\n\n\n*******Diff Filter+Boost Score********");
			System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size()) + " MAP: "
					+ (totalmap / projects.size()));
			
			SessionGenerator.closeFactory();
			dbexec = new DBActionExecutorChangeData();
			dbexec.updateBatchExistingRecord(projects);

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
