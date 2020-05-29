package com.build.builddependency;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.build.analyzer.config.Config;
import com.build.analyzer.diff.gradle.GradlePatchGenMngr;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.commitanalyzer.CommitAnalyzer;
import com.build.commitanalyzer.CommitAnalyzingUtils;
import com.build.docsim.CosineDocumentSimilarity;
import com.github.gumtreediff.tree.TreeContext;

import edu.utsa.buildlogparser.util.TextFileReaderWriter;
import edu.utsa.gradlediff.GradleASTParseMngr;
import edu.utsa.gradlediff.StringMenupulator;

public class BuildDependencyGenerator {

	public void generateBuildDependency() {

		StringMenupulator strmenu = new StringMenupulator();

		// File f1 = new File(
		// "D:\\Researh_Works\\ASE_2018\\dependency_analysis\\Sample_Project\\spockframework\\spock\\spock-guice\\guice.gradle");

		File f1 = new File("D:\\Researh_Works\\ASE_2018\\dependency_analysis\\Sample_Project\\build1.gradle");

		List<String> strlist = TextFileReaderWriter.GetFileContentByLine(f1.toString());
		for (int index = 0; index < strlist.size(); index++) {
			String str = strlist.get(index);

			str = strmenu.getMarkedString(str);

			strlist.set(index, str);
		}

		TreeContext tsrc = GradleASTParseMngr.getTreeContext(strlist);

		System.out.println(tsrc.toString());

	}

	public List<String> getAllSubProjects(Gradlebuildfixdata proj, String ID, long rowid) {
		List<String> subprojs = new ArrayList<String>();

		subprojs = generateSubProjectList(proj, ID, rowid);

		return subprojs;
	}

	public String getRootProjName(Gradlebuildfixdata proj, String ID, long rowid) {
		String rootproject;

		rootproject = getRootProjectName(proj, ID, rowid);

		return rootproject;
	}


	public List<String> getDependentProjectListWithChange(Gradlebuildfixdata proj, String ID, long rowid,
			List<String> recentchanges) {

		List<String> depsubprojs = new ArrayList<String>();
		List<String> depsubprojsrecent = new ArrayList<String>();

		List<String> lastprojexecuted = new ArrayList<String>();
		List<String> recentchangeproj = new ArrayList<String>();

		List<String> finalsubprojs = new ArrayList<String>();

		List<String> subprojs = generateSubProjectList(proj, ID, rowid);

		if (subprojs != null && subprojs.size() > 0) {
			lastprojexecuted = getLastProjectExecuted(proj, subprojs);
		}

		// //This part is for fail subproject include
		recentchangeproj = getRecentChangeProjects(proj, subprojs, recentchanges);
		/// lastprojexecuted.addAll(recentchangeproj);
		// //end

		List<String> lastprojexecuted1 = new ArrayList<>(new HashSet<>(lastprojexecuted));

		Map<String, List<String>> projconnection = generateSubProjectConnectivity(proj, ID, rowid, subprojs);

		for (String lastproj : lastprojexecuted1) {
			List<String> indepsubprojs = getFailSubProjDependencies(lastproj, subprojs, projconnection);

			if (indepsubprojs != null && indepsubprojs.size() > 0) {
				depsubprojs.addAll(indepsubprojs);
			}
		}

		for (String lastproj : recentchangeproj) {
			List<String> indepsubprojs = getFailSubProjDependencies(lastproj, subprojs, projconnection);

			if (indepsubprojs != null && indepsubprojs.size() > 0) {
				depsubprojsrecent.addAll(indepsubprojs);
			}
		}

		List<String> distinctprojs = new ArrayList<>(new HashSet<>(depsubprojs));
		List<String> distinctprojsrec = new ArrayList<>(new HashSet<>(depsubprojsrecent));

		for (String subproj : distinctprojs) {
			if (distinctprojsrec.contains(subproj)) {
				finalsubprojs.add(subproj);
			}
		}

		if (lastprojexecuted.size() > 0)
			finalsubprojs.add(lastprojexecuted.get(0));

		finalsubprojs = new ArrayList<>(new HashSet<>(finalsubprojs));

		return finalsubprojs;
	}

	public List<String> getDependentProjectList(Gradlebuildfixdata proj, String ID, long rowid,
			List<String> recentchanges) {

		List<String> depsubprojs = new ArrayList<String>();
		List<String> lastprojexecuted = new ArrayList<String>();
		List<String> recentchangeproj = new ArrayList<String>();

		List<String> subprojs = generateSubProjectList(proj, ID, rowid);

		if (subprojs != null && subprojs.size() > 0) {
			lastprojexecuted = getLastProjectExecuted(proj, subprojs);
		}

		// //This part is for fail subproject include
		// recentchangeproj=getRecentChangeProjects(proj, subprojs,
		// recentchanges);
		/// lastprojexecuted.addAll(recentchangeproj);
		// //end

		List<String> lastprojexecuted1 = new ArrayList<>(new HashSet<>(lastprojexecuted));

		Map<String, List<String>> projconnection = generateSubProjectConnectivity(proj, ID, rowid, subprojs);

		if (projconnection!=null && lastprojexecuted1.size()>0 && !projconnection.containsKey(lastprojexecuted1.get(0))) {
			List<String> connt = new ArrayList<String>();
			projconnection.put(lastprojexecuted1.get(0), connt);
		}

		// This part of new dep
		for (String str : projconnection.keySet()) {

			//if (proj.getFailChange().contains(str)) {
				if (lastprojexecuted1 != null && lastprojexecuted1.size() > 0 && !lastprojexecuted1.contains(str)) {
					List<String> dependencies = projconnection.get(str);

					for (String strdep : dependencies) {
						if (lastprojexecuted1.contains(strdep)) {
							if (projconnection.containsKey(lastprojexecuted1.get(0)))
								projconnection.get(lastprojexecuted1.get(0)).add(str);

						}
					}
				}
			//}
		}
		// This part of new dep

		for (String lastproj : lastprojexecuted1) {
			List<String> indepsubprojs = getFailSubProjDependencies(lastproj, subprojs, projconnection);

			if (indepsubprojs != null && indepsubprojs.size() > 0) {
				depsubprojs.addAll(indepsubprojs);
			}
		}
		
		//For mentioned file
		if(depsubprojs.size()>0)
		{
			List<String> mentionedfiles=getFileListMentionedFailLogPart(proj, ID, rowid);
			if(mentionedfiles!=null && mentionedfiles.size()>0)
			{
				depsubprojs.addAll(mentionedfiles);
			}
		}
		//This part
		List<String> distinctprojs = new ArrayList<>(new HashSet<>(depsubprojs));

		return distinctprojs;
	}

	public List<String> getLastProjectExecuted(Gradlebuildfixdata proj, List<String> subprojs1) {
		List<String> executedproj = new ArrayList<String>();
		List<String> subprojs = new ArrayList<String>();

		for (String subproj : subprojs1) {
			String pathsubproj = getPathFromSubProj(subproj);
			subprojs.add(pathsubproj);
		}

		if (subprojs != null && subprojs.size() > 0) {

			String largelog = proj.getFailChange();

			List<String> buildlines = new ArrayList<String>(Arrays.asList(largelog.split("\n")));

			int lineindex = buildlines.size() - 1;
			boolean match = false;

			while (lineindex >= 0) {
				String strline = buildlines.get(lineindex);

				int subprojindex = 0;

				while (subprojindex < subprojs.size()) {
					String subprojstr = subprojs.get(subprojindex);
					String subprojstr1 = subprojs1.get(subprojindex);

					// if (strline.contains(subprojstr) ||
					// strline.contains(subprojstr1)) {
					if (strline.contains(subprojstr1)) {
						executedproj.add(subprojstr1);
						match = true;
						break;
					}

					subprojindex++;
				}

				if (match == true)
					break;

				lineindex--;
			}
		}

		// removing duplicate
		List<String> deDupStringList = new ArrayList<>(new HashSet<>(executedproj));

		return deDupStringList;

	}

	public List<String> getRecentChangeProjects(Gradlebuildfixdata proj, List<String> subprojs1,
			List<String> recentchanges) {
		List<String> executedproj = new ArrayList<String>();
		List<String> subprojs = new ArrayList<String>();

		for (String subproj : subprojs1) {
			String pathsubproj = getPathFromSubProj(subproj);
			subprojs.add(pathsubproj);
		}

		if (subprojs != null && subprojs.size() > 0) {

			String largelog = proj.getFailChange();

			List<String> buildlines = new ArrayList<String>();
			buildlines.addAll(recentchanges);

			int lineindex = buildlines.size() - 1;
			boolean match = false;

			while (lineindex >= 0) {
				String strline = buildlines.get(lineindex);

				int subprojindex = 0;

				while (subprojindex < subprojs.size()) {
					String subprojstr = subprojs.get(subprojindex);
					String subprojstr1 = subprojs1.get(subprojindex);

					if (strline.contains(subprojstr) || strline.contains(subprojstr1)) {
						executedproj.add(subprojstr1);
						match = true;
					}

					subprojindex++;
				}

				lineindex--;
			}
		}

		// removing duplicate
		List<String> deDupStringList = new ArrayList<>(new HashSet<>(executedproj));

		return deDupStringList;

	}

	public List<String> generateSubProjectList(Gradlebuildfixdata proj, String ID, long rowid) {

		String project = proj.getGhProjectName();
		project = project.replace('/', '@');
		CommitAnalyzer cmtanalyzer = null;
		TreeWalk treeWalk = null;
		Repository repository = null;

		File gradlefile = null;
		CommitAnalyzingUtils commitAnalyzingUtils = new CommitAnalyzingUtils();
		StringMenupulator strmenu = new StringMenupulator();

		List<String> subprojlist = new ArrayList<String>();

		try {
			cmtanalyzer = new CommitAnalyzer("test", project);

			treeWalk = cmtanalyzer.getCommitTree(proj.getGitLastfailCommit(), proj.getF2row(), proj);
			repository = cmtanalyzer.getRepository();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int index = 0;

		try {
			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else if (treeWalk.getPathString().contains(".gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "buildscriptdep" + index + ".txt";
					index++;

					gradlefile = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					List<String> strlist = TextFileReaderWriter.GetFileContentByLine(gradlefile.toString());

					for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {
						String strline = strlist.get(lineindex);
						strline = strmenu.getMarkedString(strline);
						strlist.set(lineindex, strline);
					}

					List<String> subprjs=new ArrayList<>();
					try
					{
						subprjs= GradleASTParseMngr.getSubProjList(strlist);
					}catch (Exception ex) {
						System.out.print(ex.getMessage());
					}

					if (subprjs != null && subprjs.size() > 0)
						subprojlist.addAll(subprjs);

					if (gradlefile.exists())
						gradlefile.delete();
				}

			}

			treeWalk.reset();
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		// for removing duplicates
		subprojlist = new ArrayList<>(new HashSet<>(subprojlist));

		return subprojlist;
	}
	
	public List<String> getFileListMentionedFailLogPart(Gradlebuildfixdata proj, String ID, long rowid) {

		String project = proj.getGhProjectName();
		project = project.replace('/', '@');
		CommitAnalyzer cmtanalyzer = null;
		TreeWalk treeWalk = null;
		Repository repository = null;

		File gradlefile = null;
		CommitAnalyzingUtils commitAnalyzingUtils = new CommitAnalyzingUtils();
		StringMenupulator strmenu = new StringMenupulator();

		List<String> mentionedfilelist = new ArrayList<String>();

		try {
			cmtanalyzer = new CommitAnalyzer("test", project);

			treeWalk = cmtanalyzer.getCommitTree(proj.getGitLastfailCommit(), proj.getF2row(), proj);
			repository = cmtanalyzer.getRepository();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int index = 0;
		String failpart=proj.getFailChange();

		try {
			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else if (treeWalk.getPathString().contains(".gradle") || treeWalk.getPathString().contains(".java")) {

					if(failpart.contains(treeWalk.getPathString()))
					{
						mentionedfilelist.add(treeWalk.getPathString());
					}
				}

			}

			treeWalk.reset();
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		// for removing duplicates
		mentionedfilelist = new ArrayList<>(new HashSet<>(mentionedfilelist));

		return mentionedfilelist;
	}
	
	public String getRootProjectName(Gradlebuildfixdata proj, String ID, long rowid) {

		String project = proj.getGhProjectName();
		project = project.replace('/', '@');
		CommitAnalyzer cmtanalyzer = null;
		TreeWalk treeWalk = null;
		Repository repository = null;

		File gradlefile = null;
		CommitAnalyzingUtils commitAnalyzingUtils = new CommitAnalyzingUtils();
		StringMenupulator strmenu = new StringMenupulator();

		String rootproject=null;

		try {
			cmtanalyzer = new CommitAnalyzer("test", project);

			treeWalk = cmtanalyzer.getCommitTree(proj.getGitLastfailCommit(), proj.getF2row(), proj);
			repository = cmtanalyzer.getRepository();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int index = 0;

		try {
			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else if (treeWalk.getPathString().toLowerCase().contains("settings.gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "buildscriptdep" + index + ".txt";
					index++;

					gradlefile = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					List<String> strlist = TextFileReaderWriter.GetFileContentByLine(gradlefile.toString());

					for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {
						String strline = strlist.get(lineindex);
						strline = strmenu.getMarkedString(strline);
						strlist.set(lineindex, strline);
					}

					rootproject = GradleASTParseMngr.getRootProjectName(strlist);

					if (gradlefile.exists())
						gradlefile.delete();
				}

			}

			treeWalk.reset();
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}


		return rootproject;
	}

	public Map<String, List<String>> generateSubProjectConnectivity(Gradlebuildfixdata proj, String ID, long rowid,
			List<String> subprojs) {

		String project = proj.getGhProjectName();
		project = project.replace('/', '@');
		CommitAnalyzer cmtanalyzer = null;
		TreeWalk treeWalk = null;
		Repository repository = null;

		File gradlefile = null;
		CommitAnalyzingUtils commitAnalyzingUtils = new CommitAnalyzingUtils();
		StringMenupulator strmenu = new StringMenupulator();

		Map<String, List<String>> projectDependencyies = new HashMap<String, List<String>>();

		try {
			cmtanalyzer = new CommitAnalyzer("test", project);

			treeWalk = cmtanalyzer.getCommitTree(proj.getGitLastfailCommit(), proj.getF2row(), proj);
			repository = cmtanalyzer.getRepository();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int index = 0;

		try {
			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else if (treeWalk.getPathString().contains(".gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "buildscriptdep" + index + ".txt";
					index++;

					gradlefile = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					List<String> strlist = TextFileReaderWriter.GetFileContentByLine(gradlefile.toString());

					for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {
						String strline = strlist.get(lineindex);
						strline = strmenu.getMarkedString(strline);
						strlist.set(lineindex, strline);
					}

					String gitfile = treeWalk.getPathString();
					File f = new File(gitfile);
					String parent = null;

					if (f.getParentFile() != null) {
						
						int sindex = 0;
						boolean match = false;

						if (subprojs != null & subprojs.size() > 0) {
							while (sindex < subprojs.size()) {
								if (subprojs.get(sindex).contains(f.getParentFile().getName())) {
									parent = subprojs.get(sindex);
									match = true;
									break;
								}

								sindex++;

							}
						}

						if (match == false) {
							parent = ":" + f.getParentFile().getName();
						}
					} else {
						parent = ":" + "root";
					}

					Map<String, List<String>> projconnection=null;
					try
					{
						projconnection= GradleASTParseMngr.getSubProjConnectivity(strlist,
							parent);
					}catch (Exception ex) {
						System.out.print(ex.getMessage());
					}

					if (projconnection != null && projconnection.keySet().size() > 0) {
						projectDependencyies = mergeDependencyMap(projectDependencyies, projconnection);
					} else {
						List<String> bootforcelist = new ArrayList<String>();
						for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {

							for (String subproj : subprojs) {
								if (strlist.get(lineindex).contains("project(\"" + subproj + "\")")
										|| strlist.get(lineindex).contains("project(\'" + subproj + "\')")) {
									bootforcelist.add(subproj);
								}
							}
						}

						if (bootforcelist.size() > 0) {
							Map<String, List<String>> bootforceprojconnection = new HashMap<>();
							bootforceprojconnection.put(parent, bootforcelist);

							if (bootforceprojconnection != null && bootforceprojconnection.keySet().size() > 0) {
								projectDependencyies = mergeDependencyMap(projectDependencyies,
										bootforceprojconnection);
							}

						}
					}

					if (gradlefile.exists())
						gradlefile.delete();
				}

			}

			treeWalk.reset();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		// subprojlist = new ArrayList<>(new HashSet<>(subprojlist));

		return projectDependencyies;
	}

	public Map<String, List<String>> mergeDependencyMap(Map<String, List<String>> projectDependencyies,
			Map<String, List<String>> depdencymap) {

		for (String key : depdencymap.keySet()) {
			if (projectDependencyies.containsKey(key)) {
				List<String> deps = projectDependencyies.get(key);

				deps.addAll(depdencymap.get(key));

				List<String> deDupStringList = new ArrayList<>(new HashSet<>(deps));

				projectDependencyies.put(key, deDupStringList);
			} else {
				projectDependencyies.put(key, depdencymap.get(key));
			}

		}

		return projectDependencyies;
	}

	// BFS implementation to find Gradle Fail project dependency
	public List<String> getFailSubProjDependencies(String failedsubproj, List<String> subprojs,
			Map<String, List<String>> projconnection) {

		// String failedsubproj = "";
		//
		// if (failedsubproj1 != null && failedsubproj1.length() > 0)
		// failedsubproj = failedsubproj1.replace(":", "");
		//
		// List<String> subprojs = new ArrayList<String>();
		// for (String subproj : subprojs1) {
		// if (subproj != null && subproj.length() > 0) {
		// subproj = subproj.replaceAll(":", "");
		// subprojs.add(subproj);
		// }
		// }
		//
		// Map<String, List<String>> projconnection = new HashMap<String,
		// List<String>>();
		//
		// for (String key : projconnection1.keySet()) {
		//
		// List<String> connections = projconnection1.get(key);
		// List<String> newconnections = new ArrayList<String>();
		//
		// for (String conn : connections) {
		// if (conn != null && conn.length() > 0) {
		// conn = conn.replaceAll(":", "");
		// newconnections.add(conn);
		// }
		// }
		//
		// key = key.replaceAll(":", "");
		// projconnection.put(key, newconnections);
		// }

		List<String> failsubdependencies = new ArrayList<String>();
		Queue<String> queue = new LinkedList<>();

		// A HashMap to keep track already visited subproject;
		Map<String, Boolean> visitedstatus = new HashMap<String, Boolean>();
		for (String key : subprojs) {
			visitedstatus.put(key, false);
		}

		// initilize with root node that is failed projects
		if (visitedstatus.containsKey(failedsubproj)) {
			visitedstatus.put(failedsubproj, true);
			queue.add(failedsubproj);
		}

		while (!queue.isEmpty()) {
			// Dequeue a vertex from queue and print it
			String node = queue.peek();
			failsubdependencies.add(node);
			queue.remove();

			// Get all adjacent vertices of the dequeued
			// vertex s. If a adjacent has not been visited,
			// then mark it visited and enqueue it
			List<String> connections = projconnection.get(node);

			if (connections != null) {
				for (String subnode : connections) {
					// subnode = subnode.replaceAll(":", "");
					if (subnode != null && subnode.length() > 0 && visitedstatus.containsKey(subnode)
							&& visitedstatus.get(subnode) == false) {
						visitedstatus.put(subnode, true);
						queue.add(subnode);
					}
				}
			}
		}

		return failsubdependencies;

	}

	private String getPathFromSubProj(String subproj) {
		String path = "";

		if (subproj.startsWith(":")) {
			path = subproj.substring(1, subproj.length());
			path = path.replaceAll(":", "/");
		} else {
			path = subproj.replaceAll(":", "/");
		}

		return path;
	}

}
