package com.build.logfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.docsim.CosineDocumentSimilarity;
import com.build.metrics.RankingCalculator;

public class LogFailPartSimGenMgr {

	public LogFailPartSimGenMgr() {

	}

	public void generateAndStoreSimilarity() {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		RankingCalculator rankmetric = new RankingCalculator();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		for (int index = 0; index < projects.size(); index++) {
			// for (int index = 0; index < projects.size(); index++) {
			Gradlebuildfixdata proj = projects.get(index);

			// if (proj.getRow() == 183221) {

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

			String faillog = proj.getFailChange();

			String passlog = proj.getFixChange();

			// For Cleanup
			String regex = "([a-zA-Z]+)(\\d)";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(faillog);
			while (m.find()) {
				faillog = faillog.replaceAll(m.group(), m.group(1) + " " + m.group(2));
			}
			// For Cleanup
			regex = "([a-zA-Z]+)(\\d)";
			p = Pattern.compile(regex);
			m = p.matcher(passlog);
			while (m.find()) {
				passlog = passlog.replaceAll(m.group(), m.group(1) + " " + m.group(2));
			}

			List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));

			List<String> buildpasslines = new ArrayList<String>(Arrays.asList(passlog.split("\n")));

			buildfaillines = getListAfterRemoveDuplicate(buildfaillines);
			buildpasslines = getListAfterRemoveDuplicate(buildpasslines);

			CosineDocumentSimilarity cosdocsim = new CosineDocumentSimilarity(buildpasslines, buildfaillines);

			List<String> filteredlines = new ArrayList<String>();

			for (int failindex = 0; failindex < buildfaillines.size(); failindex++) {

				int passindex = 0;				
				double maxsimval = 0.0;
				
				while (passindex < buildpasslines.size()) {
					try {
						double simval = 0.0;
						simval = cosdocsim.getCosineSimilarity(passindex, failindex);
						if (simval > maxsimval) {
							maxsimval = simval;
						}
						// }
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					passindex++;
				} // end while

				filteredlines.add(buildfaillines.get(failindex) + Config.lineSimSeperator + maxsimval);
			}

			StringBuilder strbuilder = new StringBuilder();
			
			for (int lineindex = 0; lineindex < filteredlines.size(); lineindex++) {
				strbuilder.append(filteredlines.get(lineindex));
				strbuilder.append("\n");
			}
			 projects.get(index).setFailPartSim(strbuilder.toString());			
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}

	public List<String> getListAfterRemoveDuplicate(List<String> originallist) {

		List<String> withoutduplicate = new ArrayList<String>();

		for (int index = 0; index < originallist.size(); index++) {
			String str = originallist.get(index);

			if (!withoutduplicate.contains(str)) {
				withoutduplicate.add(str);
			}
		}

		return withoutduplicate;
	}

}
