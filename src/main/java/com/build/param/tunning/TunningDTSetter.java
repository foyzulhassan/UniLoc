package com.build.param.tunning;

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

public class TunningDTSetter {

	public void setTunningDataTags(int count) throws Exception {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		int index = 0;

		while (index < projects.size()) {

			Gradlebuildfixdata proj = projects.get(index);
			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			if (index + 1 > count) {
				projects.get(index).setDtDatasetType("EVAL");
			} else {
				projects.get(index).setDtDatasetType("TUNNING");
			}
			index++;

		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);
	}

}
