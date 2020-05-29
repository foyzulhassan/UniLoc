package com.build.analyzer.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//public class ConfigDebug {
public class Config {
	
	
	public static String workDir = "./workdir/";
	public static String repoDir = "./repo/";	

	
	
	/***********************************Don't Change Here**************************************/
	/******************************************************************************************/
	public static String logList = workDir + "/filelist.txt";

	public static String antDataArff = workDir + "/antdatawithpass.arff";
	public static String mvnDataArff = workDir + "/mvndatawithpass.arff";
	public static String gradleDataArff = workDir + "/gradledatawithpass.arff";

	public static String trainantDataArff = workDir + "/trainantdatawithpass.arff";
	public static String trainmvnDataArff = workDir + "/trainmvndatawithpass.arff";
	public static String traingradleDataArff = workDir + "/traingradledatawithpass.arff";

	public static String testantDataArff = workDir + "/testantdatawithpass.arff";
	public static String testmvnDataArff = workDir + "/testmvndatawithpass.arff";
	public static String testgradleDataArff = workDir + "/testgradledatawithpass.arff";

	public static String logDir1 = "/media/AutoBuilder/log_mining_db1/";
	public static String logDir2 = "/media/AutoBuilder2/log_mining_db2/";
	public static String logDir3 = "/media/AutoBuilder3/log_mining_db3/";

	public static int trainCount = 135;
	public static int topNSimilarity = 100;

	public static String failedfolder = workDir + "failedfolder/";

	public static String tempFolder = "temp/";

	/*
	 * This Configs are used for parsing patern
	 */

	public static int lineCountWithoutErrorTag = 10;

	public static String mavenErrorPrefix = "[ERROR]";
	public static String mavenErrorEndPrefix = " ";

	public static String gradleErrorPrefix = "* What went wrong:";
	public static String gradleErrorEndPrefix = "* Try:";

	public static String buildSuccessPrefix = "BUILD SUCCESS";
	public static String buildFailPrefix = "BUILD FAIL";

	public static boolean debugPrint = false;

	public static String beginDoubleQuete = "#bdc#";
	public static String endDoubleQuete = "#edc#";
	public static String beginSingleQuete = "#bsc#";
	public static String endSingleQuete = "#esc#";
	public static String beginfwdSlash = "fwdsls";
	public static String endfwdSlash = "endsls";

	public static String beginCurlBrace = "bcubr";
	public static String endCurlBrace = "ecubr";

	public static String defTag = "grfun";
	public static String importTag = "grimp";

	/**************************************************************************/
	public static String buildEnvDir = "/home/foyzul/Research/builderrorreproduce/";
	public static String buildEnvLogDir = buildEnvDir + "buildlogs/";
	// public static String logDir = mainDir + "logs-lib";
	public static String script = "/home/foyzul/Research/script/" + "build-adv.py";

	/****************************************************************************/
	
	public static String lineSimSeperator="<=##=>";
	public static double thresholdForSimFilter=0.90;
	public static double alphaparam=0.1;
	

	public static String getLogFilePath(String projname, String filename) {
		return workDir + "//" + projname + "//" + filename;
	}

	// public static String
	// summaryLog="/home/foyzulhassan/Research/Data/autobuilder_testing/report.log";

	// public static void reconfig(String filepath) throws IOException {
	// BufferedReader in = new BufferedReader(new FileReader(filepath));
	// for (String line = in.readLine(); line != null; line = in.readLine()) {
	// if (line.startsWith("#") || line.trim().length() == 0) {
	// ;
	// } else {
	// if (line.startsWith("mainDir")) {
	// Config.mainDir = line.substring(line.indexOf('=') + 1).trim();
	// Config.logDir1 = Config.mainDir + "logs-lib";
	// Config.workDir = Config.mainDir + "workdir";
	// Config.outDir = Config.mainDir + "outputs";
	// }
	// }
	// }
	// in.close();
	// }

	// This part is for logging
	public static int logIndex = 1;
	public static String resultLogFolder = workDir;
	
	//This variabes are for debugging purpose
	public static boolean quickAnalysis=false;
	public static int quickAnalysisDataSize=5;
	//This variabes are for debugging purpose end

	public static String getResultLogFileName() {
		String filename = logIndex + "_log.txt";

		return filename;
	}

	public static String getResultRankFileName() {
		String filename = logIndex + "_Rank.txt";

		logIndex++;

		return filename;
	}

	public static String getInspectionLogDir() {
		String dir = workDir + resultLogFolder;

		return dir;
	}
	// End for Result Log Dir
	
	//for Lucene
	public static String luceneDir=workDir;
	public static String luceneDir2=workDir;
	
	public static String logfileDir=workDir;
	
	public static boolean updateTopN=true;
	public static boolean updateMrr=true;
	public static boolean updateMap=true;
	public static boolean updateNdcg=true;

}