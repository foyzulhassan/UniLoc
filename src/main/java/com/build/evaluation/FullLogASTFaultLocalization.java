package com.build.evaluation;

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

public class FullLogASTFaultLocalization {
	
	public void simAnalyzerFullLogAST() throws Exception {
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
			
			Map<String, Double> simmap = cmtanalyzer.getLogTreeASTSimilarityMapV2(proj.getGitLastfailCommit(),
					proj.getF2row(), proj, true, false);

			Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);

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


			projects.get(index).setEvFulllogastPos(topn);
			projects.get(index).setEvFulllogastMrr(mrr);
			projects.get(index).setEvFulllogastMap(map);
		}
		
		System.out.println("\n\n\n*******FULL LOG+AST********");
		System.out.println("\n*******For Param: " + Config.thresholdForSimFilter + "********");
		System.out.println("\nTopN: " + (totaltopn / projects.size()) + " MRR: " + (totalmrr / projects.size()) + " MAP: "
				+ (totalmap / projects.size()));
		
		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}

}
