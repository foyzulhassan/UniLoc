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

public class ParformanceAnalysis {
	
	public void simAnalyzerWithFailPartLineSimAndRecent() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		List<Double> paramvalues = new ArrayList<Double>();
//		paramvalues.add(0.0);
//		paramvalues.add(0.5);
//		paramvalues.add(0.6);
		paramvalues.add(0.7);
//		paramvalues.add(0.8);
//		paramvalues.add(0.9);

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;
		
		int totaltopn1 = 0;
		double totalmrr1 = 0.0;
		double totalmap1 = 0.0;
		int worse=0;

		int size=1;
		for (Double paramval : paramvalues) {
			totaltopn = 0;
			totalmrr = 0.0;
			totalmap = 0.0;
			
			totaltopn1 = 0;
			totalmrr1 = 0.0;
			totalmap1 = 0.0;
			size=0;
			
			Config.thresholdForSimFilter = paramval;

			for (int index = 0; index < projects.size(); index++) {

				Gradlebuildfixdata proj = projects.get(index);

				// checking loop
				//if (proj.getRow() == 427759) {
				String project = proj.getGhProjectName();
				project = project.replace('/', '@');
				size++;

				//System.out.println(proj.getRow() + "=>" + project);

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
				
				
				List<String> recentchangefile = cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						proj.getGitLastfailCommit());				

				
				Map<String, Double> simmap1 = cmtanalyzer
						.getTreeSimilarityMapWithBuildDependency(proj.getGitLastfailCommit(), proj.getF2row(), proj, recentchangefile);

				// // this map contains from having in common logdiff
//				 Map<String, Double> samesimmap =
//				 cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
//				 proj.getF2row(), proj, false, true);

				String actualfixfile1 = proj.getF2passFilelist();
				String failintrofiles1 = proj.getFailFilelist();

				// This code to inhance

				failintrofiles = getCommaSeperated(recentchangefile);
				///

				String[] failfixs1 = failintrofiles.split(";");
				String[] actualfixs1 = actualfixfile.split(";");


				// Fail Introducing file change are geeting extra weight
				for (String name1 : simmap1.keySet()) {
					int failindex1 = 0;

					while (failindex1 < failfixs1.length) {

						if (name1.equals(failfixs1[failindex1])) {
							Double val1 = simmap1.get(name1) + 0.5 * simmap1.get(name1);
							simmap1.put(name1, val1);
							//break;
						}
						failindex1++;
					}
				}

				String difflog1 = proj.getFailChange();

				for (String name1 : simmap1.keySet()) {

					File f = new File(name1);

					if (difflog.contains(f.getName())) {
						Double val = simmap1.get(name1) + 0.5 * simmap1.get(name1);
						simmap1.put(name1, val);
					}

				}
				
			

				Map<String, Double> sortedsimmap1 = SortingMgr.sortByValue(simmap1);

				ArrayList<String> keys1 = new ArrayList<String>(sortedsimmap1.keySet());

				int topn1 = rankmetric.getTopN(keys1, actualfixs);
				double mrr1 = rankmetric.getMeanAverageReciprocal(keys1, actualfixs);
				double map1 = rankmetric.getMeanAveragePrecision(keys1, actualfixs);
				
				//System.out.println("MRR old: "+mrr+" MRR new: "+mrr1);
				
				totaltopn1 = totaltopn1 + topn1;
				totalmrr1 = totalmrr1 + mrr1;
				totalmap1 = totalmap1 + map1;
				
				//System.out.println("MRR: "+mrr+"MRR1: "+mrr1);
				
				//This part is for cleanup
				if(topn>=999999999)
				{
					System.out.println(proj.getRow() + "=>" + project);
				}
				
//				if(mrr>mrr1)
//				{
//					System.out.println("\n\n$$$$$$$$$$$$$$$"+index+"=>"+proj.getRow() + "=>" + project+"$$$$$$$$$$$$$$$$");
//					worse++;
//				}
//				else if(mrr<mrr1)
//				{
//					System.out.println("\n\nGood=>"+index);
//				}

			 // } //checking loop

			}
			System.out.println("*******For Param: " + Config.thresholdForSimFilter + "********");
			System.out.println("TopN: " + (totaltopn / size) + " MRR: " + (totalmrr / size)
					+ " MAP: " + (totalmap / size));
			
			System.out.println("TopN: " + (totaltopn1 / size) + " MRR: " + (totalmrr1 / size)
					+ " MAP: " + (totalmap1 / size));
			
			System.out.println("Worse Count: "+worse);

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
