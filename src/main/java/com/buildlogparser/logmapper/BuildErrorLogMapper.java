package com.buildlogparser.logmapper;

import java.io.File;
import java.util.List;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutor;
import com.build.analyzer.entity.Travistorrent;
import com.buildlogparser.parser.AntLogParser;
import com.buildlogparser.parser.BaseLogParser;
import com.buildlogparser.parser.GradleLogParser;
import com.buildlogparser.parser.MavenLogParser;

public class BuildErrorLogMapper {

	public void updateBuildErrorLogForAllProject() {

		DBActionExecutor dbobj = new DBActionExecutor();
		long rowcount = dbobj.getTotalNumberofRows();

		System.out.println("Total Number of Rows: " + rowcount);

		// Testing with only first project data; in production code 218 will be
		// replaced by rowcount
		for (long index = 1; index <= rowcount; index++) {

			Travistorrent travisproj = dbobj.getEntityWithRowId(index);

			if (isBuildFailOrError(travisproj)) {
				String buildlogfilestr = getBuildLogFilePath(travisproj);

				File buildlogfile = new File(buildlogfilestr);

				if (buildlogfile.exists() && !buildlogfile.isDirectory()) {

					String builderrors = getErrorText(travisproj, buildlogfilestr);

					if (builderrors.length() > 0) {

						travisproj.setBlLog(builderrors);
						dbobj.updateExistingRecord(travisproj);

						System.out.println("@@Updated RowId:" + index);

					}
				} else {
					System.out.println("File Not Found: " + buildlogfilestr);
				}
			}
			System.out.println("Done for Row#: " + index);
		}
	}

	public String updateBuildErrorFullLogForProject(long ID) {

		String strlog = "";

		DBActionExecutor dbobj = new DBActionExecutor();

		Travistorrent travisproj = dbobj.getEntityWithRowId(ID);

		String buildlogfilestr = getBuildLogFilePath(travisproj);

		File buildlogfile = new File(buildlogfilestr);

		if (buildlogfile.exists() && !buildlogfile.isDirectory()) {

			strlog = getErrorFullText(travisproj, buildlogfilestr);

		} else {
			System.out.println("File Not Found: " + buildlogfilestr);
		}

		return strlog;
	}
	
	public String updateBuildErrorDifferentialLogForProject(long passID, long f2ID,boolean issame) {

		String strlog = "";

		DBActionExecutor dbobj = new DBActionExecutor();

		Travistorrent travispassproj = dbobj.getEntityWithRowId(passID);
		Travistorrent travisfailproj = dbobj.getEntityWithRowId(f2ID);	
		

		String buildlogpassfilestr = getBuildLogFilePath(travispassproj);
		String buildlogfailfilestr = getBuildLogFilePath(travisfailproj);		

		File buildlogpassfile = new File(buildlogpassfilestr);
		File buildlogfailfile = new File(buildlogfailfilestr);

		if (buildlogpassfile.exists() && !buildlogpassfile.isDirectory() && buildlogfailfile.exists() && !buildlogfailfile.isDirectory()) {

			strlog = getDiferentialLogText(travispassproj, buildlogpassfilestr,buildlogfailfilestr,issame);

		} else {
			System.out.println("File Not Found: " + buildlogpassfilestr+buildlogfailfilestr);
		}

		return strlog;
	}

	public void updateBatchBuildErrorFailProjects() {

		DBActionExecutor dbobj = new DBActionExecutor();
		List<Travistorrent> projects = dbobj.getErrorFailRows();

		/// System.out.println("Total Number of Rows: " + rowcount);

		// Testing with only first project data; in production code 218 will be
		// replaced by rowcount
		for (int index = 0; index < projects.size(); index++) {

			Travistorrent travisproj = projects.get(index);

			String buildlogfilestr = getBuildLogFilePath(travisproj);

			File buildlogfile = new File(buildlogfilestr);

			if (buildlogfile.exists() && !buildlogfile.isDirectory()) {

				String builderrors = getErrorText(travisproj, buildlogfilestr);

				if (builderrors.length() > 0) {

					travisproj.setBlLog(builderrors);
					projects.get(index).setBlLog(builderrors);

					// dbobj.updateExistingRecord(travisproj);

					System.out.println("@@Updated RowId:" + index);

				}
			} else {
				System.out.println("File Not Found: " + buildlogfilestr);
			}
			System.out.println("Done for Row#: " + index);
		}

		dbobj.updateBatchExistingRecord(projects);
	}

	// not working
	public void updateBatchBuildErrorFailProjectsv3() {

		DBActionExecutor dbobj = new DBActionExecutor();
		List<Travistorrent> projects = dbobj.getErrorFailRows();
		int counter = 1;

		/// System.out.println("Total Number of Rows: " + rowcount);

		// Testing with only first project data; in production code 218 will be
		// replaced by rowcount
		for (int index = 0; index < projects.size(); index++) {

			Travistorrent travisproj = projects.get(index);

			if (index > 0) {
				Travistorrent prevtravisproj = projects.get(index - 1);

				if (!travisproj.getGhProjectName().equals(prevtravisproj.getGhProjectName())) {
					counter = 1;
				}
			}

			String buildlogfilestr = getBuildLogFilePath(counter, travisproj);

			File buildlogfile = new File(buildlogfilestr);

			if (buildlogfile.exists() && !buildlogfile.isDirectory()) {

				String builderrors = getErrorText(travisproj, buildlogfilestr);

				if (builderrors.length() > 0) {

					travisproj.setBlLog(builderrors);
					projects.get(index).setBlLog(builderrors);

					// dbobj.updateExistingRecord(travisproj);

					System.out.println("@@Updated RowId:" + index);

				}
			} else {
				System.out.println("File Not Found: " + buildlogfilestr);
			}
			System.out.println("Done for Row#: " + index);

			if (index > 0) {
				Travistorrent prevtravisproj = projects.get(index - 1);

				if (travisproj.getTrBuildId() != prevtravisproj.getTrBuildId()
						&& travisproj.getGhProjectName().equals(prevtravisproj.getGhProjectName())) {
					counter++;
				}

			}

		}

		dbobj.updateBatchExistingRecord(projects);
	}

	private boolean isBuildFailOrError(Travistorrent project) {

		boolean haserror = false;

		if (project.getTrStatus().toLowerCase().contains("error")
				&& project.getTrLogLan().toLowerCase().contains("java")) {
			haserror = true;
		}

		if (project.getTrStatus().toLowerCase().contains("fail")
				&& project.getTrLogLan().toLowerCase().contains("java")) {
			haserror = true;
		}

		return haserror;

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

	private String getBuildLogFilePath(int counter, Travistorrent project) {
		String buildlogpath = "";

		String logfilename = Integer.toString(counter) + "_" + project.getTrBuildNumber().toString() + "_"
				+ project.getGitTriggerCommit() + "_" + project.getTrJobId().toString() + ".log";

		String projectfolder = project.getGhProjectName().replace('/', '@');

		buildlogpath = Config.workDir + projectfolder + "//" + logfilename;

		return buildlogpath;
	}

	private String getErrorText(Travistorrent travisproj, String buildlogfilestr) {
		StringBuilder errorstringbuilder = new StringBuilder();

		if (travisproj.getTrLogAnalyzer().toLowerCase().contains("maven")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("mvn")) {
			BaseLogParser logparser = new MavenLogParser("maven", buildlogfilestr);

			List<String> errorlist = logparser.getBuildErrors();

			for (int i = 0; i < errorlist.size(); i++) {
				if (i > 0)
					errorstringbuilder.append("\n");

				errorstringbuilder.append(errorlist.get(i));
			}

		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("gradle")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("gradlew")) {
			BaseLogParser logparser = new GradleLogParser("gradle", buildlogfilestr);

			List<String> errorlist = logparser.getBuildErrors();

			for (int i = 0; i < errorlist.size(); i++) {
				if (i > 0)
					errorstringbuilder.append("\n");

				errorstringbuilder.append(errorlist.get(i));
			}
		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("ant")) {
			BaseLogParser logparser = new AntLogParser("ant", buildlogfilestr);

			List<String> errorlist = logparser.getBuildErrors();

			for (int i = 0; i < errorlist.size(); i++) {
				if (i > 0)
					errorstringbuilder.append("\n");
				errorstringbuilder.append(errorlist.get(i));
			}
		}

		return errorstringbuilder.toString();
	}

	private String getErrorFullText(Travistorrent travisproj, String buildlogfilestr) {
		String strlog = "";

		if (travisproj.getTrLogAnalyzer().toLowerCase().contains("maven")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("mvn")) {
			BaseLogParser logparser = new MavenLogParser("maven", buildlogfilestr);

			strlog = logparser.getFullBuildLog();
		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("gradle")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("gradlew")) {
			BaseLogParser logparser = new GradleLogParser("gradle", buildlogfilestr);

			strlog = logparser.getFullBuildLog();
		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("ant")) {
			BaseLogParser logparser = new AntLogParser("ant", buildlogfilestr);

			strlog = logparser.getFullBuildLog();
		}

		return strlog;
	}
	
	
	private String getDiferentialLogText(Travistorrent travisproj, String buildlogpassfilestr, String buildlogfailfilestr,boolean issame) {
		String strlog = "";

		if (travisproj.getTrLogAnalyzer().toLowerCase().contains("maven")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("mvn")) {
			BaseLogParser logparser = new MavenLogParser("maven", buildlogpassfilestr);

			strlog = logparser.getFullBuildLog();
		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("gradle")
				|| travisproj.getTrLogAnalyzer().toLowerCase().contains("gradlew")) {
			BaseLogParser logparser = new GradleLogParser("gradle", buildlogpassfilestr);

			
			strlog = logparser.getDifferentialBuildLog(buildlogpassfilestr, buildlogfailfilestr,issame);
			
				
		}

		else if (travisproj.getTrLogAnalyzer().toLowerCase().contains("ant")) {
			BaseLogParser logparser = new AntLogParser("ant", buildlogpassfilestr);

			strlog = logparser.getFullBuildLog();
		}

		return strlog;
	}

}
