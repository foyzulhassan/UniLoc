package com.build.analyzer.main;

import java.util.Scanner;

import com.build.analyzer.config.Config;
import com.build.analyzer.dtaccess.SessionGenerator;
import com.build.analyzer.dtgen.CommitChangeExtractor;
import com.build.evaluation.EvaluationMgr;
import com.build.failanalyzer.GenerateTestFailType;
import com.build.gradlescript.GradleBuildScriptASTSelector;
import com.build.java.ast.selection.JavaASTSelector;
import com.build.param.tunning.TunningDTSetter;
import com.build.repodownloader.RepoDownloadManager;

public class Main {
	public static void main(String[] args) {
		System.out.println("Enter your action:");

		System.out.println("1->Download Project Repo" + "\n2->Execute BaseLine1" + "\n3->Execute BaseLine1"
				+ "\n4->Change History based" + "\n5->Execute UniLoc");

		Scanner cin = new Scanner(System.in);

		System.out.println("Enter an integer: ");
		int inputid = cin.nextInt();

		if (inputid == 1) {
			RepoDownloadManager rp = new RepoDownloadManager();
			rp.downloadProjects();
		} else if (inputid == 2) {
			EvaluationMgr.BaseLineForFileName();
		} else if (inputid == 3) {
			EvaluationMgr.BaseLineForBluiR();
		} else if (inputid == 4) {
			EvaluationMgr.FixWithBuildFailChangeEval();
		} else if (inputid == 5) {
			EvaluationMgr.DiffFilterDependencyWithBoostScoreBuildASTSimEval();
		}

		cleanupResource();
	}

	private static void cleanupResource() {
		SessionGenerator.closeFactory();
		System.exit(0);
	}
}
