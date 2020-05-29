package com.build.gradlescript;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.DBActionExecutor;
import com.build.analyzer.dtaccess.DBActionExecutorChangeData;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.analyzer.entity.Travistorrent;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.gradle.ast.selection.GradleSelectedASTEntities;
import com.util.sorting.SortingMgr;

public class GradleBuildScriptASTSelector {

	public void performGradleTextAnalysis() {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		DBActionExecutor dbobj = new DBActionExecutor();

		List<Gradlebuildfixdata> projects = dbexec.getTunningRows();

		for (int index = 0; index < projects.size(); index++) {

			String strlog;
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			FileContent filecontent;
			filecontent = cmtanalyzer.getGradleFileASTLabels(proj.getGitLastfailCommit(), proj.getF2row());

			Map<String, Integer> gradleastlabels = filecontent.getGradleastlabels();
			Travistorrent travispassproj = dbobj.getEntityWithRowId(proj.getF2row());

			String buildlogpassfilestr = getBuildLogFilePath(travispassproj);
			File filepass = new File(buildlogpassfilestr);

			List<String> lines = new ArrayList<>();
			if (filepass.exists() && !filepass.isDirectory()) {

				try {
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

						updateMatchingCount(gradleastlabels, line);
					}

					fileReader.close();
					bufferedReader.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("File Not Found: " + buildlogpassfilestr);
			}

			Map<String, Integer> sortedgradleastlabels = SortingMgr.sortByValue(gradleastlabels);

			String strfile = Config.logfileDir + "/" + "gradleastmath" + "/" + index + "_" + project + ".txt";

			writeToFile(strfile, sortedgradleastlabels, filecontent.getText());
		}

		SessionGenerator.closeFactory();
		// dbexec = new DBActionExecutorChangeData();
		// dbexec.updateBatchExistingRecord(projects);
	}
	
	
	public void generateStatistics() {
		DBActionExecutorChangeData dbexec = new DBActionExecutorChangeData();
		DBActionExecutor dbobj = new DBActionExecutor();

		List<Gradlebuildfixdata> projects = dbexec.getTunningRows();
		Map<String,Integer> dependencount=new HashMap<>();
		Map<String,Integer> subprojcount=new HashMap<>();
		Map<String,Integer> taskcount=new HashMap<>();
		Map<String,Integer> propcount=new HashMap<>();

		for (int index = 0; index < projects.size(); index++) {

			String strlog;
			int depcount1=0;
			int taskcount1=0;
			int propcount1=0;
			int subcount1=0;
			
			Gradlebuildfixdata proj = projects.get(index);

			String project = proj.getGhProjectName();
			project = project.replace('/', '@');

			CommitAnalyzer cmtanalyzer = null;

			try {
				cmtanalyzer = new CommitAnalyzer("test", project);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			FileContent filecontent;
			///filecontent = cmtanalyzer.getGradleFileASTLabels(proj.getGitLastfailCommit(), proj.getF2row());
			List<GradleSelectedASTEntities> selctedasts=cmtanalyzer.getSeletedGradleFileASTLabels(proj.getGitLastfailCommit(), proj.getF2row());

			GradleSelectedASTEntities mergedselctedasts=tokenizeAndMergeList(selctedasts);
			
			Travistorrent travispassproj = dbobj.getEntityWithRowId(proj.getF2row());

			String buildlogpassfilestr = getBuildLogFilePath(travispassproj);
			File filepass = new File(buildlogpassfilestr);
			String failpart=proj.getFailChange();
		
//			if (filepass.exists() && !filepass.isDirectory()) {
//
//				try {
//					FileReader fileReader = new FileReader(filepass);
//					BufferedReader bufferedReader = new BufferedReader(fileReader);
//					String line;		
//					while ((line = bufferedReader.readLine()) != null) {
//						// replace all no ASCI Characters
//						line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
//						line = line.replaceFirst("> Loading", "");
//						line = line.replaceAll("[^\\x00-\\x7F]", "");
//						line = line.replaceAll("&lt", "");
//						line = line.replaceAll("&gt", "");
//
//						depcount1+=countMatches(line,mergedselctedasts.getDependencyList());
//						taskcount1+=countMatches(line,mergedselctedasts.getTaskList());
//						propcount1+=countMatches(line,mergedselctedasts.getPropertyList());
//						subcount1+=countMatches(line,mergedselctedasts.getSubprojList());
//					}
//
//					fileReader.close();
//					bufferedReader.close();
//
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//			} else {
//				System.out.println("File Not Found: " + buildlogpassfilestr);
//			}

			String line = failpart.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
			line = line.replaceFirst("> Loading", "");
			line = line.replaceAll("[^\\x00-\\x7F]", "");
			line = line.replaceAll("&lt", "");
			line = line.replaceAll("&gt", "");
			
			depcount1+=countMatches(line,mergedselctedasts.getDependencyList());
			taskcount1+=countMatches(line,mergedselctedasts.getTaskList());
			propcount1+=countMatches(line,mergedselctedasts.getPropertyList());
			subcount1+=countMatches(line,mergedselctedasts.getSubprojList());
			
			dependencount.put(index + "_" + project, depcount1);			
			taskcount.put(index + "_" + project, taskcount1);
			propcount.put(index + "_" + project, propcount1);
			subprojcount.put(index + "_" + project, subcount1);		

		}

		SessionGenerator.closeFactory();
		
		int hasdep=0;
		int hastask=0;
		int hasprop=0;
		int hasproj=0;
		int totalhasdep=0;
		int totalhastask=0;
		int totalhasprop=0;
		int totalhasproj=0;
		for(String proj:dependencount.keySet())
		{
			if(dependencount.get(proj)>0)
			{
				hasdep++;
				totalhasdep=totalhasdep+dependencount.get(proj);
			}
			if(taskcount.get(proj)>0)
			{
				hastask++;
				totalhastask=totalhastask+taskcount.get(proj);
			}
			if(propcount.get(proj)>0)
			{
				hasprop++;
				totalhasprop=totalhasprop+propcount.get(proj);
			}
			if(subprojcount.get(proj)>0)
			{
				hasproj++;	
				totalhasproj=totalhasproj+subprojcount.get(proj);
			}
		}
		
		int size=dependencount.keySet().size();
		
		double projdep=((double)hasdep/size)*100;
		double projtask=((double)hastask/size)*100;
		double projprop=((double)hasprop/size)*100;
		double projsubproj=((double)hasproj/size)*100;
		
		double avgprojdep=((double)totalhasdep/size);
		double avgprojtask=((double)totalhastask/size);
		double avgprojprop=((double)totalhasprop/size);
		double avgprojsub=((double)totalhasproj/size);
		
		System.out.println("Dependency Matching: "+projdep);
		System.out.println("Task Matching: "+projtask);
		System.out.println("Properties Matching: "+projprop);
		System.out.println("SubTask Matching: "+projsubproj);
		
		System.out.println("==========================================");
		
		System.out.println("Average Per Project Dependency Matching: "+avgprojdep);
		System.out.println("Average Per Project Task Matching: "+avgprojtask);
		System.out.println("Average Per Project Properties Matching: "+avgprojprop);
		System.out.println("Average Per Project SubTask Matching: "+avgprojsub);
		
	}

	public void updateMatchingCount(Map<String, Integer> gradleastlabels, String line) {
		for (String label : gradleastlabels.keySet()) {
			int match = countMatches(line, label);

			if (match > 0) {
				gradleastlabels.put(label, gradleastlabels.get(label) + match);
			}
		}
	}

	public static int countMatches(String str, String sub) {
		if (isEmpty(str) || isEmpty(sub)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += sub.length();
		}
		return count;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	
	public static int countMatches(String str, List<String> subs)
	{
		int count=0;
		
		for(String sub:subs)
		{
			count+=countMatches(str,sub);
		}
		
		return count;
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

	public void writeToFile(String strfile, Map<String, Integer> sortedlabel, String code) {
		File fout = new File(strfile);

		if (fout.exists()) {
			fout.delete();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fout);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		try {
			for (String label : sortedlabel.keySet()) {
				String line = label + "=========>" + sortedlabel.get(label);

				bw.write(line);
				bw.newLine();

			}

			bw.newLine();
			bw.write("==============================code================================");
			bw.newLine();
			bw.write(code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private GradleSelectedASTEntities tokenizeAndMergeList(List<GradleSelectedASTEntities> astentities)
	{
		List<String> dependencytoken=new ArrayList<>();
		List<String> tasktoken=new ArrayList<>();
		List<String> propertytoken=new ArrayList<>();
		List<String> subprojecttoken=new ArrayList<>();
		
		for(GradleSelectedASTEntities entity:astentities)
		{
			List<String> deps=entity.getDependencyList();
			
			for(String dep:deps)
			{
				 StringTokenizer st = new StringTokenizer(dep," ,!*^/:'\"");
			     while (st.hasMoreTokens()) {
			    	 dependencytoken.add(st.nextToken());
			     }
			}
			
			List<String> tasks=entity.getTaskList();
			
			for(String task:tasks)
			{
				 StringTokenizer st = new StringTokenizer(task," ,!*^/:'\"");
			     while (st.hasMoreTokens()) {
			    	 String token=st.nextToken();
			    	 if(!token.contains("compile"))
			    		 		tasktoken.add(token);
			     }
			}
			
			List<String> props=entity.getPropertyList();
			
			for(String prop:props)
			{
				 StringTokenizer st = new StringTokenizer(prop," ,!*^/:'\"");
			     while (st.hasMoreTokens()) {
			    	 propertytoken.add(st.nextToken());
			     }
			}
			
			List<String> subprops=entity.getSubprojList();
			
			for(String subprop:subprops)
			{
				 StringTokenizer st = new StringTokenizer(subprop," ,!*^/:'\"");
			     while (st.hasMoreTokens()) {
			    	 subprojecttoken.add(st.nextToken());
			     }
			}
		}
		
		GradleSelectedASTEntities selectedentity=new GradleSelectedASTEntities(dependencytoken,tasktoken,propertytoken,subprojecttoken);
		
		return selectedentity;
		
		
	}

}
