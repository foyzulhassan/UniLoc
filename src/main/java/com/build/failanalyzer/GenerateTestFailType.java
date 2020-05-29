package com.build.failanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutor;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.analyzer.entity.Travistorrent;
import com.buildlogparser.parser.TextFileReaderWriter;

import edu.utsa.logparser.BuildTestResult;
import edu.utsa.logparser.GradleBuildLogParser;

public class GenerateTestFailType {

	public void generateFailType() {
		
		GradleBuildLogParser gradlelogparser = new GradleBuildLogParser();
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		List<String> statuslist=TextFileReaderWriter.GetFileContentByLine(Config.workDir+"status _last.csv");

		List<Gradlebuildfixdata> projects = dbexec.getRows();

		int index = 0;

		while (index < projects.size()) {

			Gradlebuildfixdata proj = projects.get(index);
			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

//			DBActionExecutor dbobj = new DBActionExecutor();//
//			Travistorrent travisfailproj = dbobj.getEntityWithRowId(proj.getF2row());
//			String buildlogfailfilestr = getBuildLogFilePath(travisfailproj);//
//			List<BuildTestResult> results = getTestFailureErrors(buildlogfailfilestr);
			
			String faillog = proj.getBlLargelog();		
			List<String> buildfaillines = new ArrayList<String>(Arrays.asList(faillog.split("\n")));			
			List<BuildTestResult> results = gradlelogparser.getTestResults(buildfaillines);
			
			if (!proj.getDtFailType().contains("TEST FAIL")) {

				String logfile = Config.logfileDir + "log_" + index + ".txt";

				File f = new File(logfile);

				if (f.exists()) {
					f.delete();
				}

				f = new File(logfile);

				try {
					FileUtils.writeStringToFile(f, faillog);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//
//			boolean testfailfound = false;
//
//			int resindex = 0;
//
//			while (resindex < results.size()) {
//				if (results.get(resindex).getFailed() > 0) {
//					projects.get(index).setDtFailType("TEST FAIL");
//					testfailfound = true;
//					break;
//				}
//
//				resindex++;
//			}
//
//			if (testfailfound == false) {
//				resindex = 0;
//				while (resindex < results.size()) {
//					if (results.get(resindex).getErrored() > 0) {
//						projects.get(index).setDtFailType("TEST ERROR");
//						testfailfound = true;
//						break;
//					}
//					resindex++;
//				}
//			}
//
//			if (testfailfound == false) {
//				projects.get(index).setDtFailType("OTHER FAILURE");
//			}
			
			String status=statuslist.get(index);
			
			String[] statuspart= status.split(",");
			
			if(statuspart.length>0)
			{
				if(statuspart[1].contains("fail"))
				{
					projects.get(index).setDtFailType("TEST FAIL");
				}
				else
				{
					projects.get(index).setDtFailType("OTHER FAILURE");
				}
			}

			index++;
		}

		SessionGenerator.closeFactory();
		dbexec = new DBActionExecutorChangeData();
		dbexec.updateBatchExistingRecord(projects);

	}

	private String getBuildLogFilePath(Travistorrent project) {

		String buildlogpath = " ";

		// For Log Dir1
		String buildlogpath1 = "";

		String logfilename1 = project.getTrBuildNumber().toString() + "_" + project.getGitTriggerCommit() + "_"
				+ project.getTrJobId().toString() + ".log";

		String projectfolder1 = project.getGhProjectName().replace('/', '@');

		buildlogpath1 = Config.logDir1 + projectfolder1 + "//" + logfilename1;

		// For Log Dir2
		String buildlogpath2 = "";

		String logfilename2 = project.getTrBuildNumber().toString() + "_" + project.getTrBuildId() + "_"
				+ project.getGitTriggerCommit() + "_" + project.getTrJobId().toString() + ".log";

		String projectfolder2 = project.getGhProjectName().replace('/', '@');

		buildlogpath2 = Config.logDir2 + projectfolder2 + "//" + logfilename2;

		// For Log Dir3
		String buildlogpath3 = "";

		String logfilename3 = project.getTrBuildNumber().toString() + "_" + project.getTrBuildId() + "_"
				+ project.getGitTriggerCommit() + "_" + project.getTrJobId().toString() + ".log";

		String projectfolder3 = project.getGhProjectName().replace('/', '@');

		buildlogpath3 = Config.logDir3 + projectfolder3 + "//" + logfilename3;

		////////////////////////////////////////////////////////////////////////////

		File buildlogfile1 = new File(buildlogpath1);
		File buildlogfile2 = new File(buildlogpath2);
		File buildlogfile3 = new File(buildlogpath3);

		if (buildlogfile2.exists() && !buildlogfile2.isDirectory()) {

			buildlogpath = buildlogpath2;
		} else if (buildlogfile3.exists() && !buildlogfile3.isDirectory()) {

			buildlogpath = buildlogpath3;
		} else if (buildlogfile1.exists() && !buildlogfile1.isDirectory()) {

			buildlogpath = buildlogpath1;
		}

		return buildlogpath;
	}

	private List<BuildTestResult> getTestFailureErrors(String filepath) {
		List<String> lines = new ArrayList<String>();
		GradleBuildLogParser gradlelogparser = new GradleBuildLogParser();
		try {
			File filepass = new File(filepath);
			FileReader fileReader = new FileReader(filepass);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				// replace all no ASCI Characters
				line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
				line = line.replaceFirst("> Loading", "");
				line = line.replaceAll("[^\\x00-\\x7F]", "");
				line = line.replaceAll("&lt", "");
				line = line.replaceAll("&gt", "");

				// If line length is over 3000 character we can consider those
				// as noise
				if (line.length() < 4000 && line.length() > 0) {
					lines.add(line);
				}

			}

			fileReader.close();
			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		List<BuildTestResult> results = gradlelogparser.getTestResults(lines);

		return results;
	}
}
