package com.build.analyzer.dtgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.build.analyzer.dtaccess.DBActionExecutor;
import com.build.analyzer.dtaccess.DBActionExecutorGradle;
import com.build.analyzer.dtaccess.DBActionExecutorMl;
import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.analyzer.entity.Gradlepatch;
import com.build.analyzer.entity.Mavenpatch;
import com.build.analyzer.entity.Travistorrent;

public class DataGenerationMngr {

	public void DataGenerationMngr() {

	}

	public void genenrateData() {
		generateBuildFixData();
		// generateASEMavenData();
	}

	public void generateBuildFixData() {
		DBActionExecutorMl dbobj = new DBActionExecutorMl();

		List<Travistorrent> projectsGradle = null;

		projectsGradle = dbobj.getRowsGradle();

		List<Gradlebuildfixdata> buildfixdatalist = new ArrayList<Gradlebuildfixdata>();

		for (int index = 0; index < projectsGradle.size(); index++) {
			Travistorrent project = projectsGradle.get(index);

			if (project.getTrStatus().toLowerCase().contains("errored")
					|| project.getTrStatus().toLowerCase().contains("failed")) {

				// int nextindex = index + 1;
				// int
				// nextindex=getNextSameProjBuildIndex(index,projectsGradle);

				int p1index = getPrevSameProjPassBuildIndex(index, projectsGradle);

				int p2index = getNextSameProjPassBuildIndex(index, projectsGradle);

				int f2index = getPrevSameProjFailBuildIndex(p2index, projectsGradle);

				if (index >= 0 && index < projectsGradle.size() && p1index >= 0 && p1index < projectsGradle.size()
						&& p2index >= 0 && p2index < projectsGradle.size() && f2index >= 0
						&& f2index < projectsGradle.size()) {

					/// Gradlepatch gradlepatch = new Gradlepatch();
					Gradlebuildfixdata buildfixdata = new Gradlebuildfixdata();

					buildfixdata.setRow(projectsGradle.get(p1index).getRowId());
					buildfixdata.setF1row(projectsGradle.get(index).getRowId());
					buildfixdata.setF2row(projectsGradle.get(f2index).getRowId());
					buildfixdata.setGitCommit(projectsGradle.get(p1index).getGitTriggerCommit());
					buildfixdata.setGitFailintroCommit(projectsGradle.get(index).getGitTriggerCommit());
					buildfixdata.setGitLastfailCommit(projectsGradle.get(f2index).getGitTriggerCommit());
					buildfixdata.setGitFixCommit(projectsGradle.get(p2index).getGitTriggerCommit());
					buildfixdata.setGhProjectName(projectsGradle.get(index).getGhProjectName());
					buildfixdata.setGitBranch(projectsGradle.get(index).getGitBranch());
					buildfixdata.setBlLog(projectsGradle.get(index).getBlLog());
					buildfixdata.setFailChange(null);
					buildfixdata.setFixChange(null);
					buildfixdata.setReveretedStatus(null);
					buildfixdata.setChangefileCount(0);
					buildfixdata.setRevertfileCount(0);
					buildfixdata.setFailFilelist(null);
					buildfixdata.setPassFilelist(null);
					buildfixdata.setBlLargelog(null);
					
					
					buildfixdatalist.add(buildfixdata);

				}

			}
		}

		DBActionExecutorGradle action = new DBActionExecutorGradle();
		action.insertBatchBuildFixRecord(buildfixdatalist);

	}

	private int getNextSameProjBuildIndex(int index, List<Travistorrent> projectsGradle) {
		int findindex = -1;
		int i = 0;

		if(index<0 || index>=projectsGradle.size())
			return findindex;
		
		Travistorrent project = projectsGradle.get(index);
		i = index + 1;

		while (i < projectsGradle.size()) {
			Travistorrent nextproject = projectsGradle.get(i);

			if (project.getGhProjectName().equals(nextproject.getGhProjectName())
					&& project.getTrLogAnalyzer().equals(nextproject.getTrLogAnalyzer())
					&& project.getGitBranch().equals(nextproject.getGitBranch())) {
				findindex = i;
				break;
			}

			i++;
		}

		return findindex;
	}

	private int getPrevSameProjPassBuildIndex(int index, List<Travistorrent> projectsGradle) {
		int findindex = -1;
		int i = 0;

		if(index<0 || index>=projectsGradle.size())
			return findindex;
		
		Travistorrent project = projectsGradle.get(index);
		i = index - 1;

		while (i < projectsGradle.size() && i > 0) {
			Travistorrent prevproject = projectsGradle.get(i);

			if (project.getGhProjectName().equals(prevproject.getGhProjectName())
					&& project.getTrLogAnalyzer().equals(prevproject.getTrLogAnalyzer())
					&& project.getGitBranch().equals(prevproject.getGitBranch())) {
				if (prevproject.getTrStatus().equals("passed")) {
					findindex = i;
					break;
				} else {
					break;
				}
			}

			i--;
		}

		return findindex;
	}

	private int getNextSameProjPassBuildIndex(int index, List<Travistorrent> projectsGradle) {
		int findindex = -1;
		int i = 0;
		
		if(index<0 || index>=projectsGradle.size())
			return findindex;

		Travistorrent project = projectsGradle.get(index);
		i = index + 1;

		while (i < projectsGradle.size() && i > 0) {
			Travistorrent nextproject = projectsGradle.get(i);

			if (project.getGhProjectName().equals(nextproject.getGhProjectName())
					&& project.getTrLogAnalyzer().equals(nextproject.getTrLogAnalyzer())
					&& project.getGitBranch().equals(nextproject.getGitBranch())) {
				if (nextproject.getTrStatus().equals("passed")) {
					findindex = i;
					break;
				}
			}

			i++;
		}

		return findindex;
	}

	private int getPrevSameProjFailBuildIndex(int index, List<Travistorrent> projectsGradle) {
		int findindex = -1;
		int i = 0;

		if(index<0 || index>=projectsGradle.size())
			return findindex;
		
		Travistorrent project = projectsGradle.get(index);
		i = index - 1;

		while (i < projectsGradle.size() && i > 0) {
			Travistorrent prevproject = projectsGradle.get(i);

			if (project.getGhProjectName().equals(prevproject.getGhProjectName())
					&& project.getTrLogAnalyzer().equals(prevproject.getTrLogAnalyzer())
					&& project.getGitBranch().equals(prevproject.getGitBranch())) {
				if (prevproject.getTrStatus().toLowerCase().contains("errored")
						|| prevproject.getTrStatus().toLowerCase().contains("failed")) {
					findindex = i;
					break;
				}
			}

			i--;
		}

		return findindex;
	}

	public void generateASEMavenData() {
		DBActionExecutorMl dbobj = new DBActionExecutorMl();

		List<Travistorrent> projectsMaven = null;

		projectsMaven = dbobj.getRowsMaven();

		List<Mavenpatch> pathcommits = new ArrayList<Mavenpatch>();

		for (int index = 0; index < projectsMaven.size(); index++) {
			Travistorrent project = projectsMaven.get(index);

			if (project.getTrStatus().toLowerCase().contains("errored")
					|| project.getTrStatus().toLowerCase().contains("failed")) {

				int nextindex = index + 1;

				if (nextindex < projectsMaven.size()) {
					Travistorrent nextproject = projectsMaven.get(nextindex);

					if (project.getGhProjectName().equals(nextproject.getGhProjectName())
							&& project.getTrLogAnalyzer().equals(nextproject.getTrLogAnalyzer())
							&& project.getGitBranch().equals(nextproject.getGitBranch())
							&& nextproject.getTrStatus().equals("passed")) {

						if (nextproject.getGhDiffSrcFiles() <= 0 && nextproject.getCmtBuildfilechangecount() > 0) {
							Mavenpatch mavenpatch = new Mavenpatch();

							mavenpatch.setRow(project.getRowId());
							mavenpatch.setGitCommit(project.getGitTriggerCommit());
							mavenpatch.setGhProjectName(project.getGhProjectName());
							mavenpatch.setGitBranch(project.getGitBranch());
							mavenpatch.setGitCommits(project.getGitAllBuiltCommits());
							mavenpatch.setTrStatus(project.getTrStatus());
							mavenpatch.setBlLog(project.getBlLog());
							mavenpatch.setBlCluster(project.getBlCluster());
							mavenpatch.setGitFixBranch(nextproject.getGitBranch());
							mavenpatch.setGitFixCommit(nextproject.getGitTriggerCommit());
							mavenpatch.setGitFixCommits(nextproject.getGitAllBuiltCommits());
							mavenpatch.setTrFixStatus(nextproject.getTrStatus());
							mavenpatch.setPatchData(null);
							mavenpatch.setPatchParent(null);

							pathcommits.add(mavenpatch);
						}

					}

				}

			}
		}

		DBActionExecutor action = new DBActionExecutor();
		action.insertBatchMavenRecord(pathcommits);

	}
}
