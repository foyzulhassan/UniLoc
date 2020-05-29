package com.build.evaluation;

import com.build.gradle.ast.selection.DiffFilterDependencyWithBoostScoreSimBuildAST;

public class EvaluationMgr {

	// For Reverting
	public static void FixWithBuildFailChangeEval() {
		FixWithBuildFailChange obj = new FixWithBuildFailChange();
		try {
			obj.fixWithBuildFailChangeAnalysis();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// For Full Log
	public static void FullLogFaultLocalizationEval() {
		FullLogFaultLocalization obj = new FullLogFaultLocalization();
		try {
			obj.simAnalyzerFullLog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// For Full Log+AST
	public static void FullLogASTFaultLocalizationEval() {
		FullLogASTFaultLocalization obj = new FullLogASTFaultLocalization();
		try {
			obj.simAnalyzerFullLogAST();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Diff Filter+Dependency+Boost
	public static void DiffFilterDependencyWithBoostScoreSimEval() {
		DiffFilterDependencyWithBoostScoreSim obj = new DiffFilterDependencyWithBoostScoreSim();
		try {
			obj.simAnalyzerWithFailPartLineSimRecentDependency();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Diff Filter+Dependency
	public static void DiffFilterDependencySimEval() {
		DiffFilterDependencySim obj = new DiffFilterDependencySim();
		try {
			obj.simAnalyzerWithFailPartLineSimRecentDependency();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Diff Filter+Boost
	public static void DiffFilterBoostScoreSimEval() {
		DiffFilterBoostScoreSim obj = new DiffFilterBoostScoreSim();
		try {
			obj.simAnalyzerWithFailPartLineSimBoostScore();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Full Log+Dependency+Boost
	public static void FullLogDependencyBoostScoreSimEval() {
		FullLogDependencyBoostScoreSim obj = new FullLogDependencyBoostScoreSim();
		try {
			obj.simAnalyzerWithFullLogBoostScore();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// baseline of our experiment
	// Saha et al.(fail part log+AST ranking(For Java AST and for Gradle full gradle file))
	public static void BaseLineForBluiR() {
		BaseLineEvaluation obj = new BaseLineEvaluation();
		try {
			obj.calculateBaseLineEvaluationScoreV2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// baseline of our experiment
	// File Name Mentioned in Error Log will get 1; others 0
	public static void BaseLineForFileName() {
		BaseLineEvaluation obj = new BaseLineEvaluation();
		try {
			obj.calculateFileMentionedBaseLineEvaluationScore();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// For Adding functionality of Build Script AST
	public static void DiffFilterDependencyWithBoostScoreBuildASTSimEval() {
		DiffFilterDependencyWithBoostScoreSimBuildAST obj = new DiffFilterDependencyWithBoostScoreSimBuildAST();
		try {
			obj.simAnalyzerWithFailPartLineSimRecentDependency();
			//obj.luceneScoring();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void UpdateFixFileCount() {
		BaseLineEvaluation obj = new BaseLineEvaluation();
		try {
			obj.updateFixFileCount();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void CommitTimeAnalysis(boolean sourceonly,String filename) {
		BaseLineEvaluation obj = new BaseLineEvaluation();
		try {
			obj.calculateCommitTime(sourceonly,filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void FixAnalysis(boolean sourceonly,String filename) {
		BaseLineEvaluation obj = new BaseLineEvaluation();
		try {
			obj.FixFileCountAnalysis(sourceonly,filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
