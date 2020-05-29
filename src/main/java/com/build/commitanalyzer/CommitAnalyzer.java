package com.build.commitanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

//import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.build.ASTAnalyzer.JavaASTEntity;
import com.build.ASTAnalyzer.JavaASTParser;
import com.build.ASTAnalyzer.JavaTypeWiseASTParser;
import com.build.analyzer.config.Config;
import com.build.analyzer.diff.gradle.GradleChange;
import com.build.analyzer.diff.gradle.GradlePatchGenMngr;
import com.build.analyzer.diff.java.JavaPatchGenMngr;
import com.build.analyzer.entity.CommitChange;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.analyzer.entity.Gradlepatch;
import com.build.builddependency.BuildDependencyGenerator;
import com.build.docsim.CosineDocumentSimilarity;
import com.build.docsim.CosineDocumentSimilarityTFIDF;
//import com.build.docsim.CosineSimilarity;
import com.build.docsimilarity.DocumentSimilarity;
import com.build.gradle.ast.selection.EntityToStringGenerator;
import com.build.gradle.ast.selection.GradleExtractSelectedAST;
import com.build.gradle.ast.selection.GradleSelectedASTEntities;
import com.build.gradlescript.FileContent;
import com.build.java.ast.selection.JavaSelectedASTEntities;
import com.build.keyword.Keyword;
import com.build.keyword.TermExtractor;
import com.build.logfilter.FilterLogText;
import com.build.lucene.score.CosineSimilarityGenerator;
import com.build.lucene.score.LuceneSearcing;
import com.build.util.StringAnalyzer;

//import edu.utsa.data.DataResultsHolder;
//import edu.utsa.data.DataStatsHolder;
//import edu.utsa.main.MainClass;

import com.github.gumtreediff.actions.model.Action;
import com.ir.assignment.LuceneMain;

import edu.utsa.buildlogparser.util.TextFileReaderWriter;
import edu.utsa.gradlediff.GradleASTParseMngr;
import edu.utsa.gradlediff.SASTNode;
import edu.utsa.gradlediff.StringMenupulator;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * 
 * @author Foyzul Hassan
 *
 * 
 */

public class CommitAnalyzer {

	/** Various methods encapsulating methods to treats Git and commits datas */
	private CommitAnalyzingUtils commitAnalyzingUtils;

	/** All the statistical datas (number of faulty commit, actions, etc) */
	private DataStatsHolder statsHolder;

	/** File managing object for tables */
	private DataResultsHolder resultsHolder;

	/** Name of the project */
	private String project;

	/** Owner of the project (necessary for Markdown parsing) */
	private String projectOwner;

	/** Path to the directory */
	private String directoryPath;

	/** Repository object, representing the directory */
	private Repository repository;

	/** Git entity to treat with the Repository data */
	private Git git;

	/** Revision walker from JGit */
	private RevWalk rw;

	private CommitChange commitChangeTracker;

	private String gradleChanges;

	/** Classic constructor */
	public CommitAnalyzer(String projectOwner, String project) throws Exception {
		this.projectOwner = projectOwner;
		this.project = project;

		directoryPath = Config.repoDir + project + "/.git";

		commitAnalyzingUtils = new CommitAnalyzingUtils();
		statsHolder = new DataStatsHolder();
		repository = commitAnalyzingUtils.setRepository(directoryPath);
		git = new Git(repository);
		rw = new RevWalk(repository);
		this.commitChangeTracker = new CommitChange();
		this.gradleChanges = "";
	}

	public CommitChange getCommitChangeTracker() {
		return commitChangeTracker;
	}

	public void commitSampleTry(String ID) {
		List<Action> totalactions = new ArrayList<Action>();
		List<Action> act = new ArrayList<Action>();
		List<String> debugging = new ArrayList<String>();
		String r = "";
		// File debug = new File("debug-" + ID + ".txt");

		try {
			ObjectId objectid = repository.resolve(ID);

			if (objectid == null)
				return;

			RevCommit commit = rw.parseCommit(objectid);

			// System.out.println(commit.getFullMessage());

			if (commit.getParentCount() > 0) {
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

				DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

				List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

				for (DiffEntry diff : diffs) {
					if (diff.getNewPath().contains("build.gradle")) {

						commitChangeTracker.setBuildFileChange(commitChangeTracker.getBuildFileChange() + 1);

					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public Map<String, Double> getLogTreeSimilarityMap(String ID, long rowid, Gradlebuildfixdata fixdata,
			boolean islarge) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		if (islarge)
			logcontent = fixdata.getBlLargelog();
		else
			logcontent = fixdata.getFailChange();

		logcontent = logcontent.replaceAll("/", " ");
		logcontent = logcontent.replaceAll("&gt", " ");
		logcontent = logcontent.replaceAll("&lt", " ");
		logcontent = logcontent.replaceAll(".", " ");

		// try {
		// if(logcontent!=null && logcontent.length()>0)
		// {
		// List<Keyword> keywords=TermExtractor.guessFromString(logcontent);
		// logcontent=getAllContent(keywords);
		// }
		//
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			// TreeWalk treeWalk = new TreeWalk(repository);
			// treeWalk.addTree(tree);
			// treeWalk.setRecursive(false);
			// treeWalk.setPostOrderTraversal(false);

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			// treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				// System.out.println("found:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java") || treeWalk.getPathString().contains(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					// and then one can the loader to read the file
					// loader.copyTo(System.out);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);
					//
					// if (str != null && str.length() > 0) {
					// List<Keyword> keywords =
					// TermExtractor.guessFromString(str);
					// str = getAllContent(keywords);
					// }

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());

			DocumentSimilarity docsim = new DocumentSimilarity(files);

			// find similarity in between source file and log file
			Map<String, Double> simMap = docsim.findSimilarityMap(files);

			// simmap.put(treeWalk.getPathString(), simval);

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;

	}

	public Map<String, Double> getLogTreeSimilarityMapV2(String ID, long rowid, Gradlebuildfixdata fixdata,
			boolean islarge, boolean issame) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		if (islarge) {
			logcontent = fixdata.getBlLargelog();
		} else {
			if (!issame) {
				logcontent = fixdata.getFailChange();
				// FilterLogText filter = new FilterLogText();
				// logcontent = filter.performFilteringV3(fixdata);
			} else {
				logcontent = fixdata.getFixChange();
			}
		}

		// try {
		// if(logcontent!=null && logcontent.length()>0)
		// {
		// List<Keyword> keywords=TermExtractor.guessFromString(logcontent);
		// logcontent=getAllContent(keywords);
		// }
		//
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		// logcontent=logcontent.replaceAll("/", " ");
		// logcontent=logcontent.replaceAll("&gt", " ");
		// logcontent=logcontent.replaceAll("&lt", " ");
		// logcontent=logcontent.replaceAll(".", " ");

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			// TreeWalk treeWalk = new TreeWalk(repository);
			// treeWalk.addTree(tree);
			// treeWalk.setRecursive(false);
			// treeWalk.setPostOrderTraversal(false);

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			// treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				// System.out.println("found No6:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java") || treeWalk.getPathString().contains(".gradle")) {
					// System.out.println(treeWalk.getPathString());
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					// and then one can the loader to read the file
					// loader.copyTo(System.out);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					// if (str != null && str.length() > 0) {
					// List<Keyword> keywords =
					// TermExtractor.guessFromString(str);
					// str = getAllContent(keywords);
					// }

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					simMap.put(f2.toString(), sim);
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			//
			// DocumentSimilarity docsim = new DocumentSimilarity(files);
			//
			// // find similarity in between source file and log file
			// Map<String, Double> simMap = docsim.findSimilarityMap(files);
			//
			// new CosineSimilarity().getCosineSimilarity(file1, file2)

			// simmap.put(treeWalk.getPathString(), simval);

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getLogTreeASTSimilarityMapV2(String ID, long rowid, Gradlebuildfixdata fixdata,
			boolean islarge, boolean issame) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		if (islarge) {
			logcontent = fixdata.getBlLargelog();
		} else {
			if (!issame) {
				logcontent = fixdata.getFailChange();
				// FilterLogText filter = new FilterLogText();
				// logcontent = filter.performFilteringV3(fixdata);
			} else {
				logcontent = fixdata.getFixChange();
			}
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {
				// System.out.println("found No6:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")) {
					// System.out.println(treeWalk.getPathString());
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();

						List<String> asts = javaparser.parseJavaMethodDecs(str);

						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}

				else if (treeWalk.getPathString().contains(".gradle")) {
					// System.out.println(treeWalk.getPathString());
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();
				}
			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getFailLogPartTreeSimilarityMap(String ID, long rowid, Gradlebuildfixdata fixdata) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					// if (str.length() > 0) {
					//
					// JavaASTParser javaparser = new JavaASTParser();
					//
					// List<String> asts = javaparser.parseJavaMethodDecs(str);
					//
					// str = String.join(" ", asts);
					// } else {
					// str = "blankfile";
					// }

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());

				} else if (treeWalk.getPathString().contains(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}
			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getBaselineFailLogPartTreeSimilarityMap(String ID, long rowid,
			Gradlebuildfixdata fixdata, boolean isJavaFile) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java") && isJavaFile == true) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();

						List<String> asts = javaparser.parseJavaMethodDecs(str);

						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());

				} else if (treeWalk.getPathString().contains(".gradle") && isJavaFile == false) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = 0.0;

					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			// All Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			// files.add(f1.toString());

			int count = 0;
			// Add similarity value for Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			// delete Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}
			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBluIRBaseLine(String ID, long rowid,
			Gradlebuildfixdata fixdata) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);

							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					// filemap.put(treeWalk.getPathString(), sourcefile);
					double simimp = 0.0;
					double simclass = 0.0;
					double simmet = 0.0;
					double simdec = 0.0;

					try {
						str = String.join(" ", asts.getImportList());
						str = str.trim();
						if (str.length() > 0) {
							f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
							simimp = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
						}

						str = String.join(" ", asts.getClassName());
						str = str.trim();
						if (str.length() > 0) {
							f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
							simclass = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
						}

						str = String.join(" ", asts.getMethodList());
						str = str.trim();
						if (str.length() > 0) {
							f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
							simmet = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
						}

						str = String.join(" ", asts.getDecList());
						str = str.trim();
						if (str.length() > 0) {
							f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
							simdec = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
						}
					} catch (Exception e) {
						System.out.println("exception " + e.getMessage());
					}

					double sim = 0.0;

					sim = simimp + simclass + simmet + simdec;
					sim = sim / 4.0;

					// sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					str = str.trim();
					if (str.length() <= 0) {
						str = "blankfile";
					}

					str = str.trim();
					if (str.length() <= 0)
						str = filecontent;
					if (str.length() <= 0) {
						str = "blankfile";
					}

					str = str.trim();
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = 0.0;
					try {
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					} catch (Exception e) {
						System.out.println(e.getMessage() + "Gradle Exception");
					}

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
					index++;

				}
			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getBaselineWithFileMentionedInFailLogPartTreeSimilarityMap(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> changedfiles1) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (logcontent.contains(treeWalk.getPathString()) && (treeWalk.getPathString().endsWith(".java")
						|| treeWalk.getPathString().endsWith(".gradle"))) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = 1.0;

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					index++;

				} else if (!logcontent.contains(treeWalk.getPathString()) && (treeWalk.getPathString().endsWith(".java")
						|| treeWalk.getPathString().endsWith(".gradle"))) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = 0.0;
					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			// All Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			// files.add(f1.toString());

			int count = 0;
			// Add similarity value for Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			// delete Java files
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}
			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public List<String> getFilesMentionedInFailPart(String ID, long rowid, Gradlebuildfixdata fixdata) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);
		List<String> filenamementioned = new ArrayList<>();

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (logcontent.contains(treeWalk.getPathString()) && (treeWalk.getPathString().endsWith(".java")
						|| treeWalk.getPathString().endsWith(".gradle"))) {
					filenamementioned.add(treeWalk.getPathString());

				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return filenamementioned;
	}

	public Map<String, Double> getAllFileSimAsZero(String ID, long rowid, Gradlebuildfixdata fixdata) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = 0.0;
					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());

				} else if (treeWalk.getPathString().contains(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = 0.0;

					simMap.put(f2.toString(), sim);
					filemap.put(treeWalk.getPathString(), f2.toString());
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}
			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, String> getChangedFileStructure(String ID, long rowid, String[] failfixs) {

		File f1 = null;
		File f2 = null;
		int index = 0;
		JavaASTParser jparser = new JavaASTParser();

		Map<String, String> filemap = new HashMap<String, String>();

		Map<String, String> filestructmap = new HashMap<String, String>();

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			// treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				// System.out.println("found:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (StringAnalyzer.isArrayContainsString(failfixs, treeWalk.getPathString())
						&& (treeWalk.getPathString().contains(".java")
								|| treeWalk.getPathString().contains(".gradle"))) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					// and then one can the loader to read the file
					// loader.copyTo(System.out);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					if (treeWalk.getPathString().contains(".java")) {
						List<String> astList = jparser.parseJavaMethodDecs(f2);

						String filestruct = StringAnalyzer.getStringFromList(astList);

						filestructmap.put(treeWalk.getPathString(), filestruct);
					}

				}

			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}
		} catch (Exception ex) {
			System.out.print("getChangedFileStructure() " + ex.getMessage());
		}

		return filestructmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependency(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() > 0) {

						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}

					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					// double sim =
					// double sim = minprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					// double sim =
					// double sim = minprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyTFIDF(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		CosineDocumentSimilarityTFIDF ifidfindex = getIndexing(ID, rowid, fixdata);

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();

						List<String> asts = javaparser.parseJavaMethodDecs(str);

						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = ifidfindex.getTFIDFCosineSimilarity(f1.toString(), treeWalk.getPathString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = ifidfindex.getTFIDFCosineSimilarity(f1.toString(), treeWalk.getPathString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();
						List<String> asts = javaparser.parseJavaMethodDecs(str);
						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					// double sim =
					// double sim = minprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					// double sim =
					// double sim = minprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithFullLogBuildDependency(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();

		logcontent = fixdata.getBlLargelog();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);

		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();

						List<String> asts = javaparser.parseJavaMethodDecs(str);

						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = maxprob * CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = maxprob * CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();
						List<String> asts = javaparser.parseJavaMethodDecs(str);
						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// double sim = 0.0;
					double sim = minprob * CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().contains(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// double sim = 0.0;
					double sim = minprob * CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					// sim=0.5*sim;
					// double sim = 0.2;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getImpactAnalysisSimilarityMap(String ID, long rowid, Gradlebuildfixdata fixdata,
			boolean islarge, boolean issame) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		if (islarge) {
			logcontent = fixdata.getBlLargelog();
		} else {
			if (!issame)
				logcontent = fixdata.getFailChange();
			else {
				logcontent = fixdata.getFixChange();
			}
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			// TreeWalk treeWalk = new TreeWalk(repository);
			// treeWalk.addTree(tree);
			// treeWalk.setRecursive(false);
			// treeWalk.setPostOrderTraversal(false);

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			// treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				// System.out.println("found:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java") || treeWalk.getPathString().contains(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					// and then one can the loader to read the file
					// loader.copyTo(System.out);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					// if (str != null && str.length() > 0) {
					// List<Keyword> keywords =
					// TermExtractor.guessFromString(str);
					// str = getAllContent(keywords);
					// }

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					// CosineDocumentSimilarity csm=new
					// CosineDocumentSimilarity(file1,file2);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					simMap.put(f2.toString(), sim);
				}

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			//
			// DocumentSimilarity docsim = new DocumentSimilarity(files);
			//
			// // find similarity in between source file and log file
			// Map<String, Double> simMap = docsim.findSimilarityMap(files);
			//
			// new CosineSimilarity().getCosineSimilarity(file1, file2)

			// simmap.put(treeWalk.getPathString(), simval);

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getLogTreeSimilarityMapV3(String ID, long rowid, Gradlebuildfixdata fixdata,
			boolean islarge) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		if (islarge)
			logcontent = fixdata.getBlLargelog();
		else
			logcontent = fixdata.getFailChange();

		// try {
		// if(logcontent!=null && logcontent.length()>0)
		// {
		// List<Keyword> keywords=TermExtractor.guessFromString(logcontent);
		// logcontent=getAllContent(keywords);
		// }
		//
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		// logcontent=logcontent.replaceAll("/", " ");
		// logcontent=logcontent.replaceAll("&gt", " ");
		// logcontent=logcontent.replaceAll("&lt", " ");
		// logcontent=logcontent.replaceAll(".", " ");

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			// TreeWalk treeWalk = new TreeWalk(repository);
			// treeWalk.addTree(tree);
			// treeWalk.setRecursive(false);
			// treeWalk.setPostOrderTraversal(false);

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			// treeWalk.setRecursive(true);

			while (treeWalk.next()) {
				// System.out.println("found:" + treeWalk.getPathString());

				if (treeWalk.isSubtree()) {
					// System.out.println("dir: " + treeWalk.getPathString());
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java") || treeWalk.getPathString().contains(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);

					// and then one can the loader to read the file
					// loader.copyTo(System.out);

					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					// if (str != null && str.length() > 0) {
					// List<Keyword> keywords =
					// TermExtractor.guessFromString(str);
					// str = getAllContent(keywords);
					// }

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					filemap.put(treeWalk.getPathString(), sourcefile);

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

				}

			}

			LuceneMain test = new LuceneMain();

			simMap = test.getRankings(logcontent, Config.workDir + "index", Config.workDir + Config.tempFolder);

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			//
			// DocumentSimilarity docsim = new DocumentSimilarity(files);
			//
			// // find similarity in between source file and log file
			// Map<String, Double> simMap = docsim.findSimilarityMap(files);
			//
			// new CosineSimilarity().getCosineSimilarity(file1, file2)

			// simmap.put(treeWalk.getPathString(), simval);

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;

	}

	public Map<String, List<Action>> extractChangeInBetweenCommit(String ID1, String ID2) {

		Map<String, List<Action>> filechangemap = new HashMap<String, List<Action>>();
		GradlePatchGenMngr gradlechgmgr = new GradlePatchGenMngr();
		JavaPatchGenMngr javachngmsr = new JavaPatchGenMngr();

		try {
			ObjectId objectid1 = repository.resolve(ID1);
			ObjectId objectid2 = repository.resolve(ID2);

			if (objectid2 == null)
				return null;

			if (objectid1 == null)
				return null;

			RevCommit commit1 = rw.parseCommit(objectid1);
			RevCommit commit2 = rw.parseCommit(objectid2);

			/// System.out.println(commit.getFullMessage());

			// RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

			DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

			List<DiffEntry> diffs = df.scan(commit1.getTree(), commit2.getTree());

			for (DiffEntry diff : diffs) {

				if (diff.getNewPath().contains(".gradle")) {

					String currentContent = getFileContentAtCommit(ID2, diff);
					String previousContent = getFileContentAtCommit(ID1, diff);

					// String currentContent =
					// commitAnalyzingUtils.getContentOnCommit(repository, diff,
					// commit2);
					// String previousContent =
					// commitAnalyzingUtils.getContentOnCommit(repository, diff,
					// commit1);

					File f1 = commitAnalyzingUtils.writeContentInFile("g1.gradle", currentContent);
					File f2 = commitAnalyzingUtils.writeContentInFile("g2.gradle", previousContent);

					if (f1 != null && f2 != null) {
						try {

							// gradlechgmgr.generatePatch(f2.toString(),
							// f1.toString());
							List<Action> changes = new ArrayList<Action>();

							changes = gradlechgmgr.getGradleChanges(f2.toString(), f1.toString());

							if (changes != null)
								filechangemap.put(diff.getNewPath(), changes);

							// gradleChanges = gradleChanges + change;

							f1.delete();
							f2.delete();
						}

						catch (Exception e) {
							System.out.println("Exception Parent Commit ID:" + ID1 + "Exception Child Commit ID:" + ID2
									+ ">>>" + e.getMessage());

						}

					}
				} else if (diff.getNewPath().contains(".java")) {

					String currentContent = getFileContentAtCommit(ID2, diff);
					String previousContent = getFileContentAtCommit(ID1, diff);
					// String currentContent =
					// commitAnalyzingUtils.getContentOnCommit(repository, diff,
					// commit2);
					// String previousContent =
					// commitAnalyzingUtils.getContentOnCommit(repository, diff,
					// commit1);

					File f1 = commitAnalyzingUtils.writeContentInFile("j1.java", currentContent);
					File f2 = commitAnalyzingUtils.writeContentInFile("j2.java", previousContent);

					if (f1 != null && f2 != null) {
						try {

							// gradlechgmgr.generatePatch(f2.toString(),
							// f1.toString());
							List<Action> changes = javachngmsr.getJavaChanges(f2.toString(), f1.toString());

							if (changes.size() > 0)
								filechangemap.put(diff.getNewPath(), changes);

							// gradleChanges = gradleChanges + change;

							f1.delete();
							f2.delete();
						}

						catch (Exception e) {
							System.out.println("Exception Parent Commit ID:" + ID1 + "Exception Child Commit ID:" + ID2
									+ ">>>" + e.getMessage());

						}

					}

				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return filechangemap;
	}

	public List<String> extractFileChangeListInBetweenCommit(String ID1, String ID2) {

		List<String> filelist = new ArrayList<String>();

		try {
			ObjectId objectid1 = repository.resolve(ID1);
			ObjectId objectid2 = repository.resolve(ID2);

			if (objectid2 == null)
				return null;

			if (objectid1 == null)
				return null;

			RevCommit commit1 = rw.parseCommit(objectid1);
			RevCommit commit2 = rw.parseCommit(objectid2);

			/// System.out.println(commit.getFullMessage());

			// RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

			DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

			List<DiffEntry> diffs = df.scan(commit1.getTree(), commit2.getTree());

			for (DiffEntry diff : diffs) {

				if (diff.getNewPath().contains(".gradle")) {

					filelist.add(diff.getNewPath());

					// gradleChanges = gradleChanges + change;

				} else if (diff.getNewPath().contains(".java")) {

					filelist.add(diff.getNewPath());
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return filelist;
	}

	public void extractGradleFileChange(String ID) {
		// File debug = new File("debug-" + ID + ".txt");

		try {
			ObjectId objectid = repository.resolve(ID);

			if (objectid == null)
				return;

			RevCommit commit = rw.parseCommit(objectid);

			/// System.out.println(commit.getFullMessage());

			if (commit.getParentCount() > 0) {
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
				GradlePatchGenMngr gradlechgmgr = new GradlePatchGenMngr();
				DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

				List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

				for (DiffEntry diff : diffs) {

					if (diff.getNewPath().contains(".gradle")) {
						String currentContent = commitAnalyzingUtils.getContent(repository, diff, commit)[0];
						String previousContent = commitAnalyzingUtils.getContent(repository, diff, commit)[1];

						File f1 = commitAnalyzingUtils.writeContentInFile("g1.gradle", currentContent);
						File f2 = commitAnalyzingUtils.writeContentInFile("g2.gradle", previousContent);

						if (f1 != null && f2 != null) {
							try {

								gradlechgmgr.generatePatch(f2.toString(), f1.toString());

								// gradleChanges = gradleChanges + change;

								f1.delete();
								f2.delete();
							}

							catch (Exception e) {
								System.out.println("Exception Commit ID:" + ID);

							}

						}
					}

				}
				gradleChanges = gradlechgmgr.getXMLChange();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	public String getStringFile(RevTree tree, String filter) throws IOException {
		// now try to find a specific file
		try (TreeWalk treeWalk = new TreeWalk(repository)) {

			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);

			treeWalk.setFilter(PathFilter.create(filter));
			if (!treeWalk.next()) {
				throw new IllegalStateException("Did not find expected file:" + filter);
			}

			// FileMode specifies the type of file, FileMode.REGULAR_FILE for
			// normal file, FileMode.EXECUTABLE_FILE for executable bit
			// set
			FileMode fileMode = treeWalk.getFileMode(0);
			ObjectLoader loader = repository.open(treeWalk.getObjectId(0));

			// loader.copyTo(System.out);
			byte[] butestr = loader.getBytes();

			String str = new String(butestr);

			return str;

		}
	}

	public String getFileContentAtCommit(String commitid, DiffEntry diff) {
		String content = "";
		try {
			ObjectId objectid1 = repository.resolve(commitid);

			if (objectid1 == null)
				return null;

			RevCommit parent = rw.parseCommit(objectid1);

			RevTree tree = getTree(commitid);

			content = getStringFile(tree, diff.getNewPath());

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return content;
	}

	public String extractContaintOfTrainFile(String ID1, String ID2, String fixstatement, boolean withfix) {

		String traincontent = null;
		boolean foundflag = false;

		try {
			ObjectId objectid1 = repository.resolve(ID1);
			ObjectId objectid2 = repository.resolve(ID2);

			if (objectid2 == null)
				return null;

			if (objectid1 == null)
				return null;

			RevCommit parent = rw.parseCommit(objectid1);
			RevCommit commit = rw.parseCommit(objectid2);

			if (commit.getParentCount() > 0) {
				parent = rw.parseCommit(commit.getParent(0).getId());
			}

			DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

			List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

			GradlePatchGenMngr gradlechgmgr = new GradlePatchGenMngr();
			for (DiffEntry diff : diffs) {

				if (diff.getNewPath().contains(".gradle")) {

					String currentContent = getFileContentAtCommit(ID2, diff);
					String previousContent = getFileContentAtCommit(ID1, diff);

					File f1 = commitAnalyzingUtils.writeContentInFile("g1.gradle", currentContent);
					File f2 = commitAnalyzingUtils.writeContentInFile("g2.gradle", previousContent);

					if (f1 != null && f2 != null) {
						try {

							gradlechgmgr.generatePatch(f2.toString(), f1.toString());

							// gradleChanges = gradleChanges + change;

							f1.delete();
							f2.delete();

							List<GradleChange> gradlechanges = gradlechgmgr.getGradlechanges();

							for (int index = 0; index < gradlechanges.size(); index++) {
								GradleChange change = gradlechanges.get(index);

								if (change.getStatementExp().equals(fixstatement) && foundflag == false) {

									if (withfix)
										traincontent = currentContent;
									else
										traincontent = previousContent;

									foundflag = true;
								}
							}

						}

						catch (Exception e) {
							System.out.println("Exception Parent Commit ID:" + ID1 + "Exception Child Commit ID:" + ID2
									+ ">>>" + e.getMessage());

						}

					}
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return traincontent;
	}

	public List<Action> extractActions(String ID1, String ID2) {

		List<Action> actions = null;

		try {
			ObjectId objectid1 = repository.resolve(ID1);
			ObjectId objectid2 = repository.resolve(ID2);

			if (objectid2 == null)
				return null;

			if (objectid1 == null)
				return null;

			RevCommit parent = rw.parseCommit(objectid1);
			RevCommit commit = rw.parseCommit(objectid2);

			if (commit.getParentCount() > 0) {
				parent = rw.parseCommit(commit.getParent(0).getId());
			}

			DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

			List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

			GradlePatchGenMngr gradlechgmgr = new GradlePatchGenMngr();
			for (DiffEntry diff : diffs) {

				if (diff.getNewPath().contains(".gradle")) {

					String currentContent = getFileContentAtCommit(ID2, diff);
					String previousContent = getFileContentAtCommit(ID1, diff);

					File f1 = commitAnalyzingUtils.writeContentInFile("g1.gradle", currentContent);
					File f2 = commitAnalyzingUtils.writeContentInFile("g2.gradle", previousContent);

					if (f1 != null && f2 != null) {
						try {

							gradlechgmgr.generatePatch(f2.toString(), f1.toString());

							// gradleChanges = gradleChanges + change;

							f1.delete();
							f2.delete();

							actions = gradlechgmgr.changeActions;

						}

						catch (Exception e) {
							System.out.println("Exception Parent Commit ID:" + ID1 + "Exception Child Commit ID:" + ID2
									+ ">>>" + e.getMessage());

						}

					}
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return actions;
	}

	public CommitAnalyzingUtils getCommitAnalyzingUtils() {
		return commitAnalyzingUtils;
	}

	public void setCommitAnalyzingUtils(CommitAnalyzingUtils commitAnalyzingUtils) {
		this.commitAnalyzingUtils = commitAnalyzingUtils;
	}

	public String getGradleChanges() {
		return gradleChanges;
	}

	public void setCommitChangeTracker(CommitChange commitChangeTracker) {
		this.commitChangeTracker = commitChangeTracker;
	}

	/** Main method, probes all commits of a given repo and analyzes it */
	public void probeAllCommits() throws Exception {
		long startTime = System.nanoTime();
		resultsHolder = new DataResultsHolder(project, projectOwner, "all-commits");

		Iterable<RevCommit> commits = commitAnalyzingUtils.getAllCommits(git);

		/** Goes through every commit of a given branch */
		for (RevCommit commit : commits) {
			boolean assigned = false;
			boolean returned = false;
			boolean fielded = false;
			boolean localed = false;
			String action = "";
			boolean faulty = false;
			List<Action> totalactions = new ArrayList<Action>();

			statsHolder.increment("commit");
			// System.out.println("\n-------------------------------------");
			// System.out
			// .println("--- Files of commit n°" + statsHolder.getNbCommits() +
			// " with ID : " + commit.getName());
			// System.out.println("-------------------------------------");

			if (commit.getParentCount() > 0) {
				RevCommit targetCommit = rw.parseCommit(repository.resolve(commit.getName()));
				RevCommit targetParent = rw.parseCommit(commit.getParent(0).getId());

				DiffFormatter df = commitAnalyzingUtils.setDiffFormatter(repository, true);

				List<DiffEntry> diffs = df.scan(targetParent.getTree(), targetCommit.getTree());

				for (DiffEntry diff : diffs) {
					String currentContent = commitAnalyzingUtils.getContent(repository, diff, commit)[0];
					String previousContent = commitAnalyzingUtils.getContent(repository, diff, commit)[1];

					if (diff.getNewPath().contains(".java")) {
						File f1 = commitAnalyzingUtils.writeContentInFile("c1.java", currentContent);
						File f2 = commitAnalyzingUtils.writeContentInFile("c2.java", previousContent);

						if (f1 != null && f2 != null) {
							try {

								f1.delete();
								f2.delete();
							}

							catch (Exception e) {
								statsHolder.increment("file_error");
								faulty = true;
							}
						}
					}
				}

				if (faulty)
					statsHolder.increment("commit_error");

				if (totalactions.size() == 1) {
					resultsHolder.addOneOnly(action, commit);
					statsHolder.incrementOnlyOne(action);

					// System.out.println("Hello !");
					action = "";
				}
			}

			if (statsHolder.getNbCommits() % 20 == 0) {
				// System.out.println("Save !");
				statsHolder.saveResults(project, "all-commits");
				resultsHolder.saveResults();
			}
		}

		statsHolder.printResults();
		resultsHolder.saveResults();

		statsHolder.reset();
		long endTime = System.nanoTime();

		long duration = (endTime - startTime) / 1000000;
		System.out.println("Execution time : " + duration + "ms (" + duration / 1000 + "s)");
	}

	public void probeOddCodeCommit(String filepath) throws Exception {
		long startTime = System.nanoTime();
		resultsHolder = new DataResultsHolder(project, projectOwner, "odd-code");

		for (String line : Files.readAllLines(Paths.get("../bugfixRepoSamples/" + project + "/" + filepath), null)) {
			boolean assigned = false;
			boolean returned = false;
			boolean fielded = false;
			boolean localed = false;
			String action = "";
			boolean faulty = false;

			List<Action> totalactions = new ArrayList<Action>();
			String[] parts = line.split(",");

			// System.out.println("\n-------------------------------------");
			// System.out.println("--- Files of commit " + parts[0]);
			// System.out.println("-------------------------------------");

			RevCommit bf_sha = rw.parseCommit(repository.resolve(parts[0]));
			RevCommit bi_sha = rw.parseCommit(repository.resolve(parts[1]));

			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

			df.setRepository(repository);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(true);

			List<DiffEntry> diffs = df.scan(bf_sha.getTree(), bi_sha.getTree());

			for (DiffEntry diff : diffs) {
				String currentContent = commitAnalyzingUtils.getContent(repository, diff, bf_sha)[0];
				String previousContent = commitAnalyzingUtils.getContent(repository, diff, bi_sha)[0];

				if (diff.getNewPath().contains(".java")) {
					File f1 = commitAnalyzingUtils.writeContentInFile("c1.java", currentContent);
					File f2 = commitAnalyzingUtils.writeContentInFile("c2.java", previousContent);

					if (f1 != null && f2 != null) {
						try {

							f1.delete();
							f2.delete();
						}

						catch (Exception e) {
							statsHolder.increment("file_error");
							faulty = true;
						}
					}
				}
			}
			if (faulty)
				statsHolder.increment("commit_error");

			if (totalactions.size() == 1) {
				resultsHolder.addOneOnly(action, bf_sha);
				statsHolder.incrementOnlyOne(action);

				action = "";
			}

			if (statsHolder.getNbCommits() % 20 == 0) {
				System.out.println("Save !");
				statsHolder.saveResults(project, "odd-code");
				resultsHolder.saveResults();
			}
		}

		statsHolder.printResults();
		resultsHolder.saveResults();

		statsHolder.reset();
		long endTime = System.nanoTime();

		long duration = (endTime - startTime) / 1000000;
		System.out.println("Execution time : " + duration + "ms (" + duration / 1000 + "s)");
	}

	public void probeFileCommit(String filepath) throws Exception {
		resultsHolder = new DataResultsHolder(project, projectOwner, "xmls-commits");

		for (String line : Files.readAllLines(Paths.get("../bugfixRepoSamples/" + project + "/" + filepath), null)) {
			boolean assigned = false;
			boolean returned = false;
			boolean fielded = false;
			boolean localed = false;
			String action = "";
			boolean faulty = false;
			List<Action> totalactions = new ArrayList<Action>();
			// System.out.println("\n-------------------------------------");
			// System.out.println("--- Files of commit " + line);
			// System.out.println("-------------------------------------");

			RevCommit commit = rw.parseCommit(repository.resolve(line));

			if (commit.getParentCount() > 0) {
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

				DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);

				List<DiffEntry> diffs = df.scan(commit.getTree(), parent.getTree());

				for (DiffEntry diff : diffs) {
					String currentContent = commitAnalyzingUtils.getContent(repository, diff, commit)[0];
					String previousContent = commitAnalyzingUtils.getContent(repository, diff, commit)[1];

					if (diff.getNewPath().contains(".java")) {
						File f1 = commitAnalyzingUtils.writeContentInFile("c1.java", currentContent);
						File f2 = commitAnalyzingUtils.writeContentInFile("c2.java", previousContent);

						if (f1 != null && f2 != null) {
							try {

								f1.delete();
								f2.delete();

							}

							catch (Exception e) {
								statsHolder.increment("file_error");
								faulty = true;
							}
						}
					}
				}
				if (faulty)
					statsHolder.increment("commit_error");

				if (totalactions.size() == 1) {
					System.out.println("Hello !");
					resultsHolder.addOneOnly(action, commit);
					statsHolder.incrementOnlyOne(action);

					action = "";
				}

				if (statsHolder.getNbCommits() % 20 == 0) {
					System.out.println("Save !");
					statsHolder.saveResults(project, "xmls-commits");
					resultsHolder.saveResults();
				}
			}
			statsHolder.saveResults(project, "xmls-commits");
			resultsHolder.saveResults();

			statsHolder.reset();
		}
	}

	public RevTree getTree(String cmtid) throws IOException {
		ObjectId lastCommitId = repository.resolve(cmtid);

		// a RevWalk allows to walk over commits based on some filtering
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevCommit commit = revWalk.parseCommit(lastCommitId);

			System.out.println("Time of commit (seconds since epoch): " + commit.getCommitTime());

			// and using commit's tree find the path
			RevTree tree = commit.getTree();
			System.out.println("Having tree: " + tree);
			return tree;
		}
	}

	public String getStringAsFile(RevTree tree, String foldername) throws IOException {
		// now try to find a specific file

		try (TreeWalk treeWalk = new TreeWalk(repository)) {

			String filter = "";

			if (foldername != null && foldername.length() > 0) {
				filter = foldername + "/" + "build.gradle";
			} else {
				filter = "build.gradle";
			}
			treeWalk.addTree(tree);
			treeWalk.setRecursive(true);

			treeWalk.setFilter(PathFilter.create(filter));
			if (!treeWalk.next()) {
				// throw new IllegalStateException("Did not find expected
				// file:"+filter);

				filter = "build.gradle";
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(filter));

				if (!treeWalk.next()) {

					throw new IllegalStateException("Did not find expected file:" + filter);
				}
			}

			// FileMode specifies the type of file, FileMode.REGULAR_FILE for
			// normal file, FileMode.EXECUTABLE_FILE for executable bit
			// set
			FileMode fileMode = treeWalk.getFileMode(0);
			ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
			// System.out.println("README.md: " + getFileMode(fileMode) + ",
			// type: " + fileMode.getObjectType() + ", mode: " + fileMode +
			// " size: " + loader.getSize());

			// loader.copyTo(System.out);
			byte[] butestr = loader.getBytes();

			String str = new String(butestr);

			return str;

		}
	}

	public String getStringAsFile(RevCommit commit, String foldername) throws IOException {
		// now try to find a specific file
		String str = "";
		TreeWalk treeWalk = null;
		String filter = "";

		if (foldername != null && foldername.length() > 0) {
			filter = foldername + "/" + "build.gradle";
		} else {
			filter = "build.gradle";
		}

		try {
			treeWalk = TreeWalk.forPath(repository, filter, commit.getTree());

			if (treeWalk == null)
				treeWalk = TreeWalk.forPath(repository, "build.gradle", commit.getTree());

			InputStream inputStream = repository.open(treeWalk.getObjectId(0), Constants.OBJ_BLOB).openStream();

			StringWriter writer = new StringWriter();
			String encoding = "UTF-8";
			IOUtils.copy(inputStream, writer, encoding);

			str = writer.toString();

			// Read more:
			// http://javarevisited.blogspot.com/2012/08/convert-inputstream-to-string-java-example-tutorial.html#ixzz4qenalXkH
			treeWalk.close(); // use release() in JGit < 4.0

		} catch (IOException e) {
			e.printStackTrace();
		}

		return str;
	}

	public long getTimeDiffOfTwoCommitInMin(String commit1, String commit2) {
		long diffMinutes = 0;
		try {
			ObjectId objectid1 = repository.resolve(commit1);
			ObjectId objectid2 = repository.resolve(commit2);

			if (objectid2 == null)
				return -1;

			if (objectid1 == null)
				return -1;

			RevCommit parent = rw.parseCommit(objectid1);
			RevCommit commit = rw.parseCommit(objectid2);

			Date d1 = new Date(parent.getCommitTime() * 1000L);

			Date d2 = new Date(commit.getCommitTime() * 1000L);

			long diff = d2.getTime() - d1.getTime();

			diffMinutes = diff / (60 * 1000);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return diffMinutes;

	}

	private int length(String foldername) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * Getters and setters Below this point
	 */
	public CommitAnalyzingUtils getBugfixUtils() {
		return commitAnalyzingUtils;
	}

	public void setBugfixUtils(CommitAnalyzingUtils commitAnalyzingUtils) {
		this.commitAnalyzingUtils = commitAnalyzingUtils;
	}

	public DataStatsHolder getStatsHolder() {
		return statsHolder;
	}

	public void setStatsHolder(DataStatsHolder statsHolder) {
		this.statsHolder = statsHolder;
	}

	public DataResultsHolder getResultsHolder() {
		return resultsHolder;
	}

	public void setResultsHolder(DataResultsHolder resultsHolder) {
		this.resultsHolder = resultsHolder;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getProjectOwner() {
		return projectOwner;
	}

	public void setProjectOwner(String projectOwner) {
		this.projectOwner = projectOwner;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public Git getGit() {
		return git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public RevWalk getRw() {
		return rw;
	}

	public void setRw(RevWalk rw) {
		this.rw = rw;
	}

	public String getAllContent(List<Keyword> keywords) {

		StringBuilder strbuilder = new StringBuilder();

		for (int in = 0; in < keywords.size(); in++) {
			strbuilder.append(keywords.get(in).getStem());
			strbuilder.append(" ");
		}

		return strbuilder.toString();

	}

	// This part is for Gradle Build Dependency based filtering
	public TreeWalk getCommitTree(String ID, long rowid, Gradlebuildfixdata fixdata) {

		TreeWalk treeWalk = null;

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);
			RevTree tree = commit.getTree();
			treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return treeWalk;
	}

	// end This part is for Gradle Build Dependency based filtering

	private boolean isInDependencySubProject(String strpath, List<String> depprojlist, String rootproj) {
		boolean independency = false;

		int index = 0;

		while (index < depprojlist.size()) {
			String deppath = getPathFromSubProj(depprojlist.get(index));
			String deppathroot = getSubPartForRootProject(deppath, rootproj);

			if (strpath.contains(deppath) || strpath.contains(deppathroot) || deppath.contains(strpath)) {
				independency = true;
				break;
			}

			index++;
		}

		if (!strpath.contains("/")) {
			independency = true;
		}

		if (depprojlist.size() <= 0)
			independency = true;

		return independency;
	}

	private String getSubPartForRootProject(String subproj, String rootproj) {
		String subprojwithoutroot = "a1b2c3";
		String retsubprojwithoutroot = "a1b2c3";
		List<String> rootprojtoken = new ArrayList<String>();

		if (rootproj != null) {
			if (!rootproj.contains("-")) {
				rootprojtoken.add(rootproj);
			} else {
				String[] tokens = rootproj.split("-");

				for (String str : tokens) {
					rootprojtoken.add(str);
				}
			}

			if (subproj != null && subproj.length() > 0)
				subprojwithoutroot = subproj;

			for (String rootstr : rootprojtoken) {
				if (subprojwithoutroot.contains(rootstr)) {
					subprojwithoutroot = subprojwithoutroot.replace(rootstr + "-", "");
					subprojwithoutroot = subprojwithoutroot.replace(":" + rootstr + ":", "");
				}
			}

			if (subprojwithoutroot.startsWith(":") || subprojwithoutroot.startsWith("-")) {
				subprojwithoutroot = subprojwithoutroot.substring(1);

			}

			if (subprojwithoutroot.length() > 0)
				retsubprojwithoutroot = subprojwithoutroot;
		}

		return retsubprojwithoutroot;
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

	// // ***********************************This part for Dependency
	// // analysis******************************************************
	//
	// public List<String> getDependentProjectList(Gradlebuildfixdata proj,
	// String ID, long rowid) {
	//
	// List<String> depsubprojs = new ArrayList<String>();
	// String lastprojexecuted = "";
	//
	// List<String> subprojs = generateSubProjectList(proj, ID, rowid);
	//
	// System.out.println("List of Subprojects:=>" + subprojs);
	//
	// if (subprojs != null && subprojs.size() > 0) {
	// lastprojexecuted = getLastProjectExecuted(proj, subprojs);
	// }
	//
	// System.out.println("Last Executed:=>" + lastprojexecuted);
	//
	// Map<String, List<String>> projconnection =
	// generateSubProjectConnectivity(proj, ID, rowid);
	//
	// depsubprojs = getFailSubProjDependencies(lastprojexecuted, subprojs,
	// projconnection);
	//
	// return depsubprojs;
	// }
	//
	// public String getLastProjectExecuted(Gradlebuildfixdata proj,
	// List<String> subprojs) {
	// String executedproj = null;
	//
	// if (subprojs != null && subprojs.size() > 0) {
	//
	// String largelog = proj.getBlLargelog();
	//
	// List<String> buildlines = new
	// ArrayList<String>(Arrays.asList(largelog.split("\n")));
	//
	// int lineindex = buildlines.size() - 1;
	// boolean match = false;
	//
	// while (lineindex >= 0) {
	// String strline = buildlines.get(lineindex);
	//
	// int subprojindex = 0;
	//
	// while (subprojindex < subprojs.size()) {
	// String subprojstr = subprojs.get(subprojindex);
	//
	// if (strline.contains(subprojstr + ":")) {
	// executedproj = subprojstr;
	// match = true;
	// }
	//
	// subprojindex++;
	// }
	//
	// if (match)
	// break;
	//
	// lineindex--;
	// }
	// }
	//
	// return executedproj;
	//
	// }
	//
	// public List<String> generateSubProjectList(Gradlebuildfixdata proj,
	// String ID, long rowid) {
	//
	// File gradlefile = null;
	// StringMenupulator strmenu = new StringMenupulator();
	//
	// List<String> subprojlist = new ArrayList<String>();
	//
	// int index = 0;
	//
	// try {
	// ObjectId objectid = repository.resolve(ID);
	// RevCommit commit = rw.parseCommit(objectid);
	//
	// RevTree tree = commit.getTree();
	//
	// TreeWalk treeWalk = new TreeWalk(repository);
	// treeWalk.addTree(commit.getTree());
	// treeWalk.setRecursive(false);
	//
	// while (treeWalk.next()) {
	// System.out.println("in test=>" + treeWalk.getPathString());
	//
	// if (treeWalk.isSubtree()) {
	// treeWalk.enterSubtree();
	//
	// } else if (treeWalk.getPathString().contains(".gradle")) {
	// ObjectId objectId = treeWalk.getObjectId(0);
	// ObjectLoader loader = repository.open(objectId);
	//
	// if (treeWalk.getPathString().contains("common.gradle")) {
	// String testabc = treeWalk.getPathString();
	// }
	//
	// byte[] butestr = loader.getBytes();
	//
	// String str = new String(butestr);
	//
	// String sourcefile = Config.workDir + Config.tempFolder + "buildscriptdep"
	// + index + ".txt";
	// index++;
	//
	// gradlefile = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
	//
	// List<String> strlist =
	// TextFileReaderWriter.GetFileContentByLine(gradlefile.toString());
	//
	// for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {
	// String strline = strlist.get(lineindex);
	// strline = strmenu.getMarkedString(strline);
	// strlist.set(lineindex, strline);
	// }
	//
	// List<String> subprjs = GradleASTParseMngr.getSubProjList(strlist);
	//
	// if (subprjs != null && subprjs.size() > 0)
	// subprojlist.addAll(subprjs);
	//
	// if (gradlefile.exists())
	// gradlefile.delete();
	// }
	//
	// }
	//
	// treeWalk.reset();
	// } catch (Exception ex) {
	// System.out.print(ex.getMessage());
	// }
	//
	// subprojlist = new ArrayList<>(new HashSet<>(subprojlist));
	//
	// return subprojlist;
	// }
	//
	// public Map<String, List<String>>
	// generateSubProjectConnectivity(Gradlebuildfixdata proj, String ID, long
	// rowid) {
	//
	// String project = proj.getGhProjectName();
	// project = project.replace('/', '@');
	//
	// File gradlefile = null;
	// StringMenupulator strmenu = new StringMenupulator();
	//
	// List<String> subprojlist = new ArrayList<String>();
	//
	// Map<String, List<String>> projectDependencyies = new HashMap<String,
	// List<String>>();
	//
	// int index = 0;
	//
	// try {
	// ObjectId objectid = repository.resolve(ID);
	// RevCommit commit = rw.parseCommit(objectid);
	//
	// RevTree tree = commit.getTree();
	// TreeWalk treeWalk = new TreeWalk(repository);
	// treeWalk.addTree(commit.getTree());
	// treeWalk.setRecursive(false);
	//
	// while (treeWalk.next()) {
	//
	// if (treeWalk.isSubtree()) {
	// treeWalk.enterSubtree();
	// } else if (treeWalk.getPathString().contains(".gradle")) {
	// System.out.println("File in Connection=> " + treeWalk.getPathString());
	//
	// if (treeWalk.getPathString().contains("specs.gradle")) {
	// int testabc = 1;
	// }
	// ObjectId objectId = treeWalk.getObjectId(0);
	// ObjectLoader loader = repository.open(objectId);
	//
	// byte[] butestr = loader.getBytes();
	//
	// String str = new String(butestr);
	//
	// String sourcefile = Config.workDir + Config.tempFolder + "buildscriptdep"
	// + index + ".txt";
	// index++;
	//
	// gradlefile = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
	//
	// List<String> strlist =
	// TextFileReaderWriter.GetFileContentByLine(gradlefile.toString());
	//
	// for (int lineindex = 0; lineindex < strlist.size(); lineindex++) {
	// String strline = strlist.get(lineindex);
	// strline = strmenu.getMarkedString(strline);
	// strlist.set(lineindex, strline);
	// }
	//
	// String gitfile = treeWalk.getPathString();
	// File f = new File(gitfile);
	// String parent = null;
	//
	// if (f.getParentFile() != null) {
	// parent = f.getParentFile().getName();
	// } else {
	// parent = "root";
	// }
	//
	// Map<String, List<String>> projconnection =
	// GradleASTParseMngr.getSubProjConnectivity(strlist,
	// parent);
	//
	// if (projconnection != null && projconnection.keySet().size() > 0) {
	// projectDependencyies = mergeDependencyMap(projectDependencyies,
	// projconnection);
	// }
	//
	// if (gradlefile.exists())
	// gradlefile.delete();
	// }
	//
	// }
	//
	// treeWalk.reset();
	//
	// } catch (Exception ex) {
	// System.out.print(ex.getMessage());
	// }
	//
	// subprojlist = new ArrayList<>(new HashSet<>(subprojlist));
	//
	// return projectDependencyies;
	// }
	//
	// public Map<String, List<String>> mergeDependencyMap(Map<String,
	// List<String>> projectDependencyies,
	// Map<String, List<String>> depdencymap) {
	//
	// for (String key : depdencymap.keySet()) {
	// if (projectDependencyies.containsKey(key)) {
	// List<String> deps = projectDependencyies.get(key);
	//
	// deps.addAll(depdencymap.get(key));
	//
	// List<String> deDupStringList = new ArrayList<>(new HashSet<>(deps));
	//
	// projectDependencyies.put(key, deDupStringList);
	// } else {
	// projectDependencyies.put(key, depdencymap.get(key));
	// }
	//
	// }
	//
	// return projectDependencyies;
	// }
	//
	// // BFS implementation to find Gradle Fail project dependency
	// public List<String> getFailSubProjDependencies(String failedsubproj,
	// List<String> subprojs,
	// Map<String, List<String>> projconnection) {
	// List<String> failsubdependencies = new ArrayList<String>();
	// Queue<String> queue = new LinkedList<>();
	//
	// // A HashMap to keep track already visited subproject;
	// Map<String, Boolean> visitedstatus = new HashMap<String, Boolean>();
	// for (String key : subprojs) {
	// visitedstatus.put(key, false);
	// }
	//
	// // initilize with root node that is failed projects
	// if (visitedstatus.containsKey(failedsubproj)) {
	// visitedstatus.put(failedsubproj, true);
	// queue.add(failedsubproj);
	// }
	//
	// while (!queue.isEmpty()) {
	// // Dequeue a vertex from queue and print it
	// String node = queue.peek();
	// failsubdependencies.add(node);
	// queue.remove();
	//
	// // Get all adjacent vertices of the dequeued
	// // vertex s. If a adjacent has not been visited,
	// // then mark it visited and enqueue it
	// List<String> connections = projconnection.get(node);
	//
	// if (connections != null) {
	// for (String subnode : connections) {
	// subnode = subnode.replaceAll(":", "");
	// if (visitedstatus.get(subnode) == false) {
	// visitedstatus.put(subnode, true);
	// queue.add(subnode);
	// }
	// }
	// }
	// }
	//
	// return failsubdependencies;
	//
	// }
	//
	// // **************************************End Dependency
	// // Analysis*************************************************************

	public Map<String, Double> getDependentProjProbability(List<String> allprojlist, List<String> depprojlist) {
		double iniprob = (1.0 / allprojlist.size());

		Map<String, Double> probmap = new HashMap<String, Double>();

		for (String proj : allprojlist) {
			probmap.put(proj, iniprob);
		}

		for (String depproj : depprojlist) {
			if (probmap.containsKey(depproj)) {
				Double prob = probmap.get(depproj);
				probmap.put(depproj, prob + 1.0);
			}
		}

		double sum = 0.0;

		for (String proj : probmap.keySet()) {
			sum = sum + probmap.get(proj);
		}

		// sum=sum/probmap.keySet().size();
		for (String proj : probmap.keySet()) {
			Double prob = probmap.get(proj) / sum;
			probmap.put(proj, prob);
		}

		return probmap;
	}

	public CosineDocumentSimilarityTFIDF getIndexing(String ID, long rowid, Gradlebuildfixdata fixdata) {
		File f1 = null;
		String logcontent = "";
		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);
		CosineDocumentSimilarityTFIDF tfidfindex = null;
		try {
			tfidfindex = new CosineDocumentSimilarityTFIDF();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			tfidfindex.addDocument(f1.toString(), logcontent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().contains(".java")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String str = new String(butestr);

					if (str.length() > 0) {

						JavaASTParser javaparser = new JavaASTParser();

						List<String> asts = javaparser.parseJavaMethodDecs(str);

						str = String.join(" ", asts);
					} else {
						str = "blankfile";
					}

					tfidfindex.addDocument(treeWalk.getPathString(), str);

				} else if (treeWalk.getPathString().contains(".gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() <= 0) {
						str = "blankfile";
					}

					tfidfindex.addDocument(treeWalk.getPathString(), str);
				}

			}
			treeWalk.reset();
			f1.delete();
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		tfidfindex.closeIndexWriter();

		return tfidfindex;

	}

	public FileContent getGradleFileASTLabels(String ID, long rowid) {
		FileContent filecontent = new FileContent();
		StringBuilder sourcecode = new StringBuilder();
		File f1 = null;
		int index = 0;
		Map<String, Integer> gradleastlbl = new HashMap<>();

		try {

			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					f1 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					sourcecode.append(str);
					sourcecode.append("\n\n\n=========================================\n\n\n");

					GradleASTParseMngr astParser = new GradleASTParseMngr();

					List<SASTNode> nodes = astParser.getGradleASTNodes(sourcefile);
					for (SASTNode node : nodes) {
						if (node.getCurrent().getNodeMetaData("lbl") != null) {
							if (!gradleastlbl.containsKey(node.getCurrent().getNodeMetaData("lbl").toString())) {
								gradleastlbl.put(node.getCurrent().getNodeMetaData("lbl").toString(), 0);
							}
						}
					}

					if (f1.exists()) {
						f1.delete();
					}

					index++;
				}

			}

			treeWalk.reset();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		filecontent.setText(sourcecode.toString());
		filecontent.setGradleastlabels(gradleastlbl);

		return filecontent;
	}

	public List<GradleSelectedASTEntities> getSeletedGradleFileASTLabels(String ID, long rowid) {
		FileContent filecontent = new FileContent();
		File f1 = null;
		int index = 0;
		List<GradleSelectedASTEntities> selectedASTs = new ArrayList<>();
		try {

			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".gradle")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f1 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					GradleExtractSelectedAST astextractor = new GradleExtractSelectedAST();
					GradleSelectedASTEntities entities = astextractor.getGradleASTNodes(sourcefile,
							treeWalk.getPathString());
					selectedASTs.add(entities);

					if (f1.exists()) {
						f1.delete();
					}

					index++;
				}

			}

			treeWalk.reset();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return selectedASTs;
	}

	public JavaSelectedASTEntities getSeletedJavaFileASTLabels(String ID, long rowid) {
		FileContent filecontent = new FileContent();
		File f1 = null;
		int index = 0;
		List<String> importlist = new ArrayList<>();
		List<String> classlist = new ArrayList<>();
		List<String> methlist = new ArrayList<>();
		List<String> declist = new ArrayList<>();
		JavaSelectedASTEntities selectedasts = new JavaSelectedASTEntities();
		try {

			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);
			RevTree tree = commit.getTree();
			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {
				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);

							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					if (asts != null) {
						importlist.addAll(asts.getImportList());
						classlist.addAll(asts.getClassName());
						methlist.addAll(asts.getMethodList());
						declist.addAll(asts.getDecList());
					}

					index++;
				}

			}

			treeWalk.reset();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		selectedasts.setImportList(importlist);
		selectedasts.setClassList(classlist);
		selectedasts.setMethodList(methlist);
		selectedasts.setDecList(declist);

		return selectedasts;

	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyWithBuildAST(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);

							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					// filemap.put(treeWalk.getPathString(), sourcefile);
					double simimp = 0.0;
					double simclass = 0.0;
					double simmet = 0.0;
					double simdec = 0.0;

					try {
						str = String.join(" ", asts.getImportList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simimp = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Import Matching Exception");
						}

						str = String.join(" ", asts.getClassName());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simclass = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Class Matching Exception");
						}

						str = String.join(" ", asts.getMethodList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simmet = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Method Matching Exception");
						}

						str = String.join(" ", asts.getDecList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simdec = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Dec Matching Exception");
						}
					} catch (Exception e) {
						System.out.println("Error for AST Similarity");
					}

					double sim = 0.0;

					sim = simimp + simclass + simmet + simdec;
					sim = sim / 4.0;

					// sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;
					str = str.trim();
					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}

						if (f2.exists())
							f2.delete();
					}			

					str = str.trim();
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					if (entities != null) {
						double simdep = 0.0;
						double simtask = 0.0;
						double simprop = 0.0;
						double simsub = 0.0;
						
						String str1 = EntityToStringGenerator.getStringFromEntity(entities);
						str1 = str1.trim();
						str = str1;
						
						str = str.trim();
						if (str.length() <= 0)
							str = filecontent;
						if (str.length() <= 0) {
							str = "blankfile";
						}
						
						String strdep = String.join(" ", entities.getDependencyList());
						strdep = strdep.trim();
						String strtask = String.join(" ", entities.getTaskList());
						strtask = strtask.trim();
						String strprop = String.join(" ", entities.getPropertyList());
						strprop = strprop.trim();

						String strsub = String.join(" ", entities.getSubprojList());
						strsub = strsub.trim();

						try {
							try {
								if (strdep.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strdep);
									simdep = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Dep Matching Exception");
							}

							try {
								if (strtask.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strtask);
									simtask = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
											f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Task Matching Exception");
							}

							try {
								if (strprop.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strprop);
									simprop = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
											f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Prop Matching Exception");
							}

							try {
								if (strsub.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strsub);
									simsub = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle SubProj Matching Exception");
							}
						} catch (Exception e) {
							System.out.println("Error for Gradle AST Similarity");
						}

						sim = simdep + simtask + simprop + simsub;
						sim = sim / 4.0;

					} else {
						str = str.trim();
						if (str.length() <= 0)
							str = filecontent;
						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
								f2.toString());			
					}

					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
					index++;

				} else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}
					// else
					// {
					// f2 = commitAnalyzingUtils.writeContentInFile(sourcefile,
					// str);
					// GradleExtractSelectedAST gradleast=new
					// GradleExtractSelectedAST();
					// GradleSelectedASTEntities
					// entities=gradleast.getGradleASTNodes(sourcefile);
					// String depstr=String.join(" ",
					// entities.getDependencyList());
					// String propstr=String.join(" ",
					// entities.getPropertyList());
					// String taskstr=String.join(" ", entities.getTaskList());
					// String substr=String.join(" ",
					// entities.getSubprojList());
					// str=depstr+" "+propstr+" "+taskstr+" "+substr;
					//
					// if(f2.exists())
					// f2.delete();
					//
					// }

					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = 0.0;
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}
				// else
				// {
				// filemap.put(treeWalk.getPathString(), "no java gradle");
				// }

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}
	
	
	public Map<String, Double> getTreeSimilarityMapWithFullLogBuildDependencyWithBuildAST(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		//FilterLogText filter = new FilterLogText();
		//logcontent = filter.performFilteringOnSimValue(fixdata);

		logcontent=fixdata.getBlLargelog();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);

							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					// filemap.put(treeWalk.getPathString(), sourcefile);
					double simimp = 0.0;
					double simclass = 0.0;
					double simmet = 0.0;
					double simdec = 0.0;

					try {
						str = String.join(" ", asts.getImportList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simimp = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Import Matching Exception");
						}

						str = String.join(" ", asts.getClassName());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simclass = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Class Matching Exception");
						}

						str = String.join(" ", asts.getMethodList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simmet = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Method Matching Exception");
						}

						str = String.join(" ", asts.getDecList());
						str = str.trim();
						try {
							if (str.length() > 0) {
								f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
								simdec = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
							}
						} catch (Exception e) {
							System.out.println("Java Dec Matching Exception");
						}
					} catch (Exception e) {
						System.out.println("Error for AST Similarity");
					}

					double sim = 0.0;

					sim = simimp + simclass + simmet + simdec;
					sim = sim / 4.0;

					// sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;
					str = str.trim();
					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}

						if (f2.exists())
							f2.delete();
					}			

					str = str.trim();
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					if (entities != null) {
						double simdep = 0.0;
						double simtask = 0.0;
						double simprop = 0.0;
						double simsub = 0.0;
						
						String str1 = EntityToStringGenerator.getStringFromEntity(entities);
						str1 = str1.trim();
						str = str1;
						
						str = str.trim();
						if (str.length() <= 0)
							str = filecontent;
						if (str.length() <= 0) {
							str = "blankfile";
						}
						
						String strdep = String.join(" ", entities.getDependencyList());
						strdep = strdep.trim();
						String strtask = String.join(" ", entities.getTaskList());
						strtask = strtask.trim();
						String strprop = String.join(" ", entities.getPropertyList());
						strprop = strprop.trim();

						String strsub = String.join(" ", entities.getSubprojList());
						strsub = strsub.trim();

						try {
							try {
								if (strdep.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strdep);
									simdep = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Dep Matching Exception");
							}

							try {
								if (strtask.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strtask);
									simtask = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
											f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Task Matching Exception");
							}

							try {
								if (strprop.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strprop);
									simprop = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
											f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle Prop Matching Exception");
							}

							try {
								if (strsub.length() > 0) {
									f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, strsub);
									simsub = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
								}
							} catch (Exception e) {
								System.out.println("Gradle SubProj Matching Exception");
							}
						} catch (Exception e) {
							System.out.println("Error for Gradle AST Similarity");
						}

						sim = simdep + simtask + simprop + simsub;
						sim = sim / 4.0;

					} else {
						str = str.trim();
						if (str.length() <= 0)
							str = filecontent;
						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
								f2.toString());			
					}

					// double sim =
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
					index++;

				} else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}
					// else
					// {
					// f2 = commitAnalyzingUtils.writeContentInFile(sourcefile,
					// str);
					// GradleExtractSelectedAST gradleast=new
					// GradleExtractSelectedAST();
					// GradleSelectedASTEntities
					// entities=gradleast.getGradleASTNodes(sourcefile);
					// String depstr=String.join(" ",
					// entities.getDependencyList());
					// String propstr=String.join(" ",
					// entities.getPropertyList());
					// String taskstr=String.join(" ", entities.getTaskList());
					// String substr=String.join(" ",
					// entities.getSubprojList());
					// str=depstr+" "+propstr+" "+taskstr+" "+substr;
					//
					// if(f2.exists())
					// f2.delete();
					//
					// }

					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = 0.0;
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}
				// else
				// {
				// filemap.put(treeWalk.getPathString(), "no java gradle");
				// }

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyWithBuildASTV2(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		logcontent = logcontent.replaceAll("\\", "SP");

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);
							str = String.join(" ", asts.getAllAST());
							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String path = treeWalk.getPathString();
					path = path.replaceAll("\\", "SP");
					str = str + " " + path;
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;

					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}

						if (f2.exists())
							f2.delete();
					}

					double sim = 0.0;

					if (entities != null) {
						str = EntityToStringGenerator.getStringFromEntity(entities);
						str = str.trim();

						if (str.length() <= 0)
							str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();

						String path = treeWalk.getPathString();
						path = path.replaceAll("\\", "SP");
						str = str + " " + path;
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					} else {
						str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						String path = treeWalk.getPathString();
						path = path.replaceAll("\\", "SP");
						str = str + " " + path;
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					}

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}
					// else
					// {
					// f2 = commitAnalyzingUtils.writeContentInFile(sourcefile,
					// str);
					// GradleExtractSelectedAST gradleast=new
					// GradleExtractSelectedAST();
					// GradleSelectedASTEntities
					// entities=gradleast.getGradleASTNodes(sourcefile);
					// String depstr=String.join(" ",
					// entities.getDependencyList());
					// String propstr=String.join(" ",
					// entities.getPropertyList());
					// String taskstr=String.join(" ", entities.getTaskList());
					// String substr=String.join(" ",
					// entities.getSubprojList());
					// str=depstr+" "+propstr+" "+taskstr+" "+substr;
					//
					// if(f2.exists())
					// f2.delete();
					//
					// }

					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = 0.0;
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}
				// else
				// {
				// filemap.put(treeWalk.getPathString(), "no java gradle");
				// }

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getLuceneScoring(String ID, long rowid, Gradlebuildfixdata fixdata,
			List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		/// logcontent=fixdata.getFailChange();
		logcontent = logcontent.trim();

		if (logcontent.length() <= 0) {
			logcontent = "zero data";
		}

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		CosineSimilarityGenerator lucenecos = new CosineSimilarityGenerator();
		List<String> addedfiles = new ArrayList<>();

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> allrepofiles = new ArrayList<String>();
		LuceneSearcing lssearch = new LuceneSearcing();

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);
							str = String.join(" ", asts.getAllAST());
							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					lssearch.addToIndex(sourcefile, treeWalk.getPathString());
					addedfiles.add(treeWalk.getPathString());
					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;

					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}
					}

					double sim = 0.0;

					if (entities != null) {
						str = EntityToStringGenerator.getStringFromEntity(entities);
						str = str.trim();

						if (str.length() <= 0)
							str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();

						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						// sim =
						// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
						// f2.toString());
						lssearch.addToIndex(sourcefile, treeWalk.getPathString());
						addedfiles.add(treeWalk.getPathString());

					} else {
						str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						// sim =
						// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
						// f2.toString());
						lssearch.addToIndex(sourcefile, treeWalk.getPathString());
						addedfiles.add(treeWalk.getPathString());
					}
					index++;
				}

				if (treeWalk.getPathString().endsWith(".java") || treeWalk.getPathString().endsWith(".gradle")) {
					allrepofiles.add(treeWalk.getPathString());

					// if(logcontent.contains(treeWalk.getPathString()))
					// {
					// File f=new File(treeWalk.getPathString());
					// String name=f.getName();
					// logcontent=logcontent.replaceAll(treeWalk.getPathString(),
					// " "+name);
					// }
				}

			}

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		try {
			String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
			f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, logcontent);
			lssearch.addToIndex(sourcefile, "log.txt");
			lssearch.closeIndexer();
			// List<String> buildfaillines =
			// filter.getListFilteringOnSimValue(fixdata);
			// simMap=lssearch.searchAndGetScore(buildfaillines, allrepofiles);
			simMap = lucenecos.getCosineSimilarityMap(addedfiles, allrepofiles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return simMap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyWithBuildASTFiltering(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges, List<String> filteredfiles) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& filteredfiles.contains(treeWalk.getPathString())) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);
							str = String.join(" ", asts.getAllAST());
							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& filteredfiles.contains(treeWalk.getPathString())) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;

					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}

						if (f2.exists())
							f2.delete();
					}

					double sim = 0.0;

					if (entities != null) {
						str = EntityToStringGenerator.getStringFromEntity(entities);
						str = str.trim();

						if (str.length() <= 0)
							str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();

						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					} else {
						str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					}

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".java")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

				} else if (treeWalk.getPathString().endsWith(".gradle")) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}
					// else
					// {
					// f2 = commitAnalyzingUtils.writeContentInFile(sourcefile,
					// str);
					// GradleExtractSelectedAST gradleast=new
					// GradleExtractSelectedAST();
					// GradleSelectedASTEntities
					// entities=gradleast.getGradleASTNodes(sourcefile);
					// String depstr=String.join(" ",
					// entities.getDependencyList());
					// String propstr=String.join(" ",
					// entities.getPropertyList());
					// String taskstr=String.join(" ", entities.getTaskList());
					// String substr=String.join(" ",
					// entities.getSubprojList());
					// str=depstr+" "+propstr+" "+taskstr+" "+substr;
					//
					// if(f2.exists())
					// f2.delete();
					//
					// }

					index++;
					// filemap.put(treeWalk.getPathString(), sourcefile);
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					// double sim = maxprob *
					// CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());
					double sim = 0.0;
					// double sim =
					// maxprob*CosineDocumentSimilarity.getCosineSimilarity(f1.toString(),
					// f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}
				// else
				// {
				// filemap.put(treeWalk.getPathString(), "no java gradle");
				// }

			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyWithBuildASTV2(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges, List<String> searchedfiles) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)
						&& searchedfiles.contains(treeWalk.getPathString())) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);
							str = String.join(" ", asts.getAllAST());
							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)
						&& searchedfiles.contains(treeWalk.getPathString())) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					GradleSelectedASTEntities entities = null;

					if (str.length() <= 0) {
						str = "blankfile";
					} else {
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						GradleExtractSelectedAST gradleast = new GradleExtractSelectedAST(rootproject);

						try {
							entities = gradleast.getGradleASTNodes(sourcefile, treeWalk.getPathString());
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Gradle File Parsing Error");
							entities = null;
						}

						if (f2.exists())
							f2.delete();
					}

					double sim = 0.0;

					if (entities != null) {
						str = EntityToStringGenerator.getStringFromEntity(entities);
						str = str.trim();

						if (str.length() <= 0)
							str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();

						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					} else {
						str = filecontent;

						if (str.length() <= 0) {
							str = "blankfile";
						}
						str = str.trim();
						f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
						sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					}

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if ((treeWalk.getPathString().endsWith(".gradle") || treeWalk.getPathString().endsWith(".java"))
						&& !searchedfiles.contains(treeWalk.getPathString())) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}

					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);
				}
			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

	public Map<String, Double> mergeSimMaps(String ID, long rowid, Gradlebuildfixdata fixdata,
			Map<String, Double> simMap1, Map<String, Double> simMap2, Map<String, Double> simMap3) {

		Map<String, Double> simMap = new HashMap<String, Double>();

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				} else if (treeWalk.getPathString().endsWith(".java") || treeWalk.getPathString().endsWith(".gradle")) {
					Double sum = simMap1.get(treeWalk.getPathString()) + simMap2.get(treeWalk.getPathString())
							+ simMap3.get(treeWalk.getPathString());
					simMap.put(treeWalk.getPathString(), sum);
				}
			}
		} catch (Exception e) {
			System.out.println("Map Merging Exception" + e.getMessage());
		}

		return simMap;
	}

	public Map<String, Double> getTreeSimilarityMapWithBuildDependencyWithBuildASTV3(String ID, long rowid,
			Gradlebuildfixdata fixdata, List<String> recentchanges) {

		String logcontent = "";
		File f1 = null;
		File f2 = null;
		File f3 = null;
		int index = 0;

		Map<String, String> filemap = new HashMap<String, String>();
		Map<String, Double> filteredmap = new HashMap<String, Double>();

		Map<String, Double> simMap = new HashMap<String, Double>();
		// CosineSimilarity csm=new CosineSimilarity();

		FilterLogText filter = new FilterLogText();
		logcontent = filter.performFilteringOnSimValue(fixdata);

		// logcontent=fixdata.getFailChange();

		BuildDependencyGenerator depgen = new BuildDependencyGenerator();

		String rootproject = depgen.getRootProjName(fixdata, ID, rowid);
		List<String> allprojlist = depgen.getAllSubProjects(fixdata, ID, rowid);

		List<String> depprojlist = depgen.getDependentProjectList(fixdata, ID, rowid, recentchanges);

		Double minprob = 1.0;
		Double maxprob = 1.0;

		if (allprojlist.size() > 0 && depprojlist.size() > 0) {
			Map<String, Double> probmap = getDependentProjProbability(allprojlist, depprojlist);
			minprob = Collections.min(probmap.values());
			maxprob = Collections.max(probmap.values());
		}

		try {
			f1 = commitAnalyzingUtils.writeContentInFile("log.text", logcontent);
			f3 = commitAnalyzingUtils.writeContentInFile("logpass.text", fixdata.getFixChange());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ObjectId objectid = repository.resolve(ID);
			RevCommit commit = rw.parseCommit(objectid);

			RevTree tree = commit.getTree();

			TreeWalk treeWalk = new TreeWalk(repository);
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(false);

			while (treeWalk.next()) {

				if (treeWalk.isSubtree()) {
					treeWalk.enterSubtree();
				}

				else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;
					JavaASTEntity asts = null;

					if (str.length() > 0) {

						try {
							// JavaASTParser javaparser = new JavaASTParser();
							JavaTypeWiseASTParser javaparser = new JavaTypeWiseASTParser();

							asts = javaparser.parseJavaMethodDecs(str);
							str = String.join(" ", asts.getAllAST());
							// str = String.join(" ", asts);
							// str=str+" "+treeWalk.getPathString();
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
							System.out.println("Java File Parsing Error");
							str = "blankfile";
						}

					} else {
						str = "blankfile";
					}

					str = str.trim();

					if (str.length() <= 0) {
						str = filecontent;
					}
					if (str.length() <= 0) {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;

					sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject)) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();

					String filecontent = new String(butestr);
					String str = filecontent;

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					str = str.trim();

					if (str.length() <= 0) {
						str = "blankfile";
					}
					str = str.trim();

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = CosineDocumentSimilarity.getCosineSimilarity(f1.toString(), f2.toString());

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

					index++;

				} else if (treeWalk.getPathString().endsWith(".java")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);

					if (str.length() > 0) {
						try {
							JavaASTParser javaparser = new JavaASTParser();

							List<String> asts = javaparser.parseJavaMethodDecs(str);

							str = String.join(" ", asts);
						} catch (Exception ex) {
							System.out.print(ex.getMessage());
						}
					} else {
						str = "blankfile";
					}

					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";
					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);

					double sim = 0.0;
					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();

				} else if (treeWalk.getPathString().endsWith(".gradle")
						&& isInDependencySubProject(treeWalk.getPathString(), depprojlist, rootproject) == false) {

					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					byte[] butestr = loader.getBytes();
					String str = new String(butestr);
					String sourcefile = Config.workDir + Config.tempFolder + "sourcecode" + index + ".txt";

					if (str.length() <= 0) {
						str = "blankfile";
					}

					index++;

					f2 = commitAnalyzingUtils.writeContentInFile(sourcefile, str);
					double sim = 0.0;

					filemap.put(treeWalk.getPathString(), f2.toString());
					simMap.put(f2.toString(), sim);

					if (f2.exists())
						f2.delete();
				}
			}

			ArrayList<String> files = new ArrayList<String>();

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				files.add(entry.getValue());
			}

			// log file content
			files.add(f1.toString());
			files.add(f3.toString());

			int count = 0;
			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				if (simMap.containsKey(entry.getValue())) {
					filteredmap.put(entry.getKey(), simMap.get(entry.getValue()));
				} else {
					filteredmap.put(entry.getKey(), 0.0);
				}
			}

			for (Map.Entry<String, String> entry : filemap.entrySet()) {
				// System.out.println(entry.getKey() + ":" + entry.getValue());
				File f = new File(entry.getValue());
				if (f.exists()) {
					boolean flag = f.delete();

				}

			}

			treeWalk.reset();
			f1.delete();
			f3.delete();

		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		}

		return filteredmap;
	}

}
