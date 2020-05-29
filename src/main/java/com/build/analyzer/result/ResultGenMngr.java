package com.build.analyzer.result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.metrics.RankingCalculator;

public class ResultGenMngr {
	
	public void performResultAnalysis()
	{
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();
		
		double full_logmrr=0.0;
		double full_logmap=0.0;
		
		double diff_logmrr=0.0;
		double diff_logmap=0.0;
		
		double diffsim_logmrr=0.0;
		double diffsim_logmap=0.0;
				
			

		for (int index = 0; index < projects.size(); index++) {
			
			Gradlebuildfixdata proj = projects.get(index);
			String project = proj.getGhProjectName();
			project = project.replace('/', '@');
			System.out.println(proj.getRow()+"=>"+project);

			full_logmrr=full_logmrr+proj.getFulllogMrr();
			full_logmap=full_logmap+proj.getFulllogMap();
			
			diff_logmrr=diff_logmrr+proj.getFilterlogdualMrr();
			diff_logmap=diff_logmap+proj.getFilterlogdualMap();
			
			diffsim_logmrr=diffsim_logmrr+proj.getFailpartsimMrr();
			diffsim_logmap=diffsim_logmap+proj.getFailpartsimMap();

		}
		
		System.out.println("Full Log Performence : "+(full_logmrr/projects.size())+" "+(full_logmap/projects.size()));
		
		System.out.println("Diff Log Performence : "+(diff_logmrr/projects.size())+" "+(diff_logmap/projects.size()));
		
		System.out.println("Filtered Log Performence : "+(diffsim_logmrr/projects.size())+" "+(diffsim_logmap/projects.size()));

		SessionGenerator.closeFactory();
		
	}

}
