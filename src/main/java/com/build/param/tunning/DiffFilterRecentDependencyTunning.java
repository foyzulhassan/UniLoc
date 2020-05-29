package com.build.param.tunning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;
import com.util.sorting.SortingMgr;

public class DiffFilterRecentDependencyTunning {

	public void simAnalyzerWithFailPartLineSimRecentDependency() throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getTunningRows();
		//List<Gradlebuildfixdata> projects = dbexec.getRows();

		// List<Gradlebuildfixdata> projects=new
		// ArrayList<Gradlebuildfixdata>();
		// projects.clear();
		// Gradlebuildfixdata gproject =
		// dbexec.getEntityWithRowId((long)444640);
		// projects.add(gproject);
		List<Double> paramvalues = new ArrayList<Double>();
		paramvalues.add(0.0);
		paramvalues.add(0.5);
		paramvalues.add(0.6);
		paramvalues.add(0.7);
		paramvalues.add(0.8);
		paramvalues.add(0.9);

		List<Double> paramvalues1 = new ArrayList<Double>();
		paramvalues1.add(0.0);
		paramvalues1.add(0.1);
		paramvalues1.add(0.2);
		paramvalues1.add(0.3);
		paramvalues1.add(0.4);
		paramvalues1.add(0.5);
		paramvalues1.add(0.6);
		paramvalues1.add(0.7);
		paramvalues1.add(0.8);
		paramvalues1.add(0.9);
//		
//		List<Integer> paramvalues1 = new ArrayList<Integer>();
//		paramvalues1.add(0);
//		paramvalues1.add(1);
//		paramvalues1.add(2);
//		paramvalues1.add(3);
//		paramvalues1.add(4);
//		paramvalues1.add(5);
//		paramvalues1.add(6);
//		paramvalues1.add(7);
//		paramvalues1.add(8);
//		paramvalues1.add(9);
//		

		int totaltopn = 0;
		double totalmrr = 0.0;
		double totalmap = 0.0;

		String file = Config.getInspectionLogDir() + "geom_mean_final_May10.txt";

		Writer writer = null;

		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
		try {

			for (Double paramval : paramvalues) {

				for (Double paramval1 : paramvalues1) {

					totaltopn = 0;
					totalmrr = 0.0;
					totalmap = 0.0;
					Config.thresholdForSimFilter = paramval;
					Config.alphaparam = paramval1;

					int datasize = 0;
					for (int index = 0; index < projects.size(); index++) {
						// for (int index = 0; index < projects.size(); index++)
						// {
						Gradlebuildfixdata proj = projects.get(index);

						// checking loop
						//if (proj.getRow() == 3219332) {

						String project = proj.getGhProjectName();
						project = project.replace('/', '@');

						datasize++;
						//System.out.println(proj.getRow() + "=>" + project);

						CommitAnalyzer cmtanalyzer = null;

						try {
							cmtanalyzer = new CommitAnalyzer("test", project);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						List<String> recentchangefile = cmtanalyzer
								.extractFileChangeListInBetweenCommit(proj.getGitCommit(), proj.getGitLastfailCommit());

						Map<String, Double> simmap = cmtanalyzer.getTreeSimilarityMapWithBuildDependencyWithBuildAST(
								proj.getGitLastfailCommit(), proj.getF2row(), proj, recentchangefile);

						// // this map contains from having in common logdiff
						// Map<String, Double> samesimmap =
						// cmtanalyzer.getLogTreeSimilarityMapV2(proj.getGitLastfailCommit(),
						// proj.getF2row(), proj, false, true);

						String actualfixfile = proj.getF2passFilelist();
						String failintrofiles = proj.getFailFilelist();

						// This code to inhance
						// List<String> recentchangefile =
						// cmtanalyzer.extractFileChangeListInBetweenCommit(proj.getGitCommit(),
						// proj.getGitLastfailCommit());
						// failintrofiles = getCommaSeperated(recentchangefile);
						///

						String[] failfixs = failintrofiles.split(";");
						String[] actualfixs = actualfixfile.split(";");

						String difflog = proj.getFailChange();

						// Fail Introducing file change are geeting extra weight
//						for (String name : simmap.keySet()) {
//							int failindex = 0;
//
//							File f = new File(name);
//							while (failindex < failfixs.length) {
//
//								if (name.equals(failfixs[failindex]) || difflog.contains(f.getName())) {
//									//Double val = simmap.get(name) + param2 * simmap.get(name);
//									Double val=nthroot(param2,simmap.get(name));
//									simmap.put(name, val);
//									break;
//								}
//								failindex++;
//							}
//						}
						
						Map<String, Double> sortedsimmap = SortingMgr.sortByValue(simmap);
						
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

						sortedsimmap = SortingMgr.sortByValue(simmap);

						ArrayList<String> keys = new ArrayList<String>(sortedsimmap.keySet());

						int topn = rankmetric.getTopN(keys, actualfixs);
						double mrr = rankmetric.getMeanAverageReciprocal(keys, actualfixs);
						double map = rankmetric.getMeanAveragePrecision(keys, actualfixs);

						totaltopn = totaltopn + topn;
						totalmrr = totalmrr + mrr;
						totalmap = totalmap + map;
						
						if(topn>=999999999)
						{
							writer.write("\n"+proj.getRow());
						}
					 //} //checking loop

					}

					writer.write("\n*******For Param1: " + Config.thresholdForSimFilter + "********$$$$ "
							+ "*******For Param2: " + Config.alphaparam + "********$$$$");
					writer.write("\n");
					writer.write("TopN: " + (totaltopn / datasize) + " MRR: " + (totalmrr / datasize) + " MAP: "
							+ (totalmap / datasize));
					writer.write("\n\n");
				}
			}
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
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
	

    public double nthroot(int n, double x) 
    {
        return nthroot(n, x, .0001);
    }
    
    public double nthroot(int n, double x, double p) 
    {
        if(x < 0) 
        {
            System.err.println("Negative!");
            return -1;
        }
        if(x == 0) 
            return 0;
        double x1 = x;
        double x2 = x / n;  
        while (Math.abs(x1 - x2) > p) 
        {
            x1 = x2;
            x2 = ((n - 1.0) * x2 + x / Math.pow(x2, n - 1.0)) / n;
        }
        return x2;
    }

}
