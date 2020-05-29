package com.build.analyzer.entity;

import java.util.ArrayList;
import java.util.List;

public class DataFields {
	
	private String dataRelationName;
	
	private String day_of_week;	
	private String gh_team_size;
	private String gh_num_issue_comments;	
	private String gh_num_commit_comments;	
	private String gh_num_pr_comments;
	private String gh_src_churn;
	private String gh_test_churn;
	private String gh_files_added;
	private String gh_files_deleted;
	private String gh_files_modified;
	private String gh_tests_added;
	private String gh_tests_deleted;
	private String gh_src_files;
	private String gh_doc_files;
	private String gh_other_files;
	private String tr_status;
	private String bl_cluster;
	private String cmt_importchangecount;	
	private String cmt_classchangecount;
	private String cmt_methodchangecount;
	private String cmt_fieldchangecount;
	private String cmt_methodbodychangecount;
	private String cmt_buildfilechangecount;
	
	private String nxt_day_of_week;
	private String nxt_gh_team_size	;
	private String nxt_gh_num_issue_comments;	
	private String nxt_gh_num_commit_comments;	
	private String nxt_gh_num_pr_comments;
	private String nxt_gh_src_churn;
	private String nxt_gh_test_churn;
	private String nxt_gh_files_added;
	private String nxt_gh_files_deleted;
	private String nxt_gh_files_modified;
	private String nxt_gh_tests_added;
	private String nxt_gh_tests_deleted;
	private String nxt_gh_src_files;
	private String nxt_gh_doc_files;
	private String nxt_gh_other_files;
	private String nxt_cmt_importchangecount;
	private String nxt_cmt_classchangecount;
	private String nxt_cmt_methodchangecount;
	private String nxt_cmt_fieldchangecount;
	private String nxt_cmt_methodbodychangecount;
	private String nxt_cmt_buildfilechangecount;
	
	public String getDay_of_week() {
		return "day_of_week";
	}

	public void setDay_of_week(String day_of_week) {
		this.day_of_week = day_of_week;
	}

	
	public DataFields(String datarelationname)
	{
		this.dataRelationName=datarelationname;
	}
	
	public String getGh_team_size() {
		return "gh_team_size";
	}
	public String getGh_num_issue_comments() {
		return "gh_num_issue_comments";
	}
	public String getGh_num_commit_comments() {
		return "gh_num_commit_comments";
	}
	public String getGh_num_pr_comments() {
		return "gh_num_pr_comments";
	}
	public String getGh_src_churn() {
		return "gh_src_churn";
	}
	public String getGh_test_churn() {
		return "gh_test_churn";
	}
	public String getGh_files_added() {
		return "gh_files_added";
	}
	public String getGh_files_deleted() {
		return "gh_files_deleted";
	}
	public String getGh_files_modified() {
		return "gh_files_modified";
	}
	public String getGh_tests_added() {
		return "gh_tests_added";
	}
	public String getGh_tests_deleted() {
		return "gh_tests_deleted";
	}
	public String getGh_src_files() {
		return "gh_src_files";
	}
	public String getGh_doc_files() {
		return "gh_doc_files";
	}
	public String getGh_other_files() {
		return "gh_other_files";
	}
	public String getTr_status() {
		return "tr_status";
	}
	public String getBl_cluster() {
		return "bl_cluster";
	}
	
	public String getCmt_importchangecount() {
		return "cmt_importchangecount";
	}

	public void setCmt_importchangecount(String cmt_importchangecount) {
		this.cmt_importchangecount = cmt_importchangecount;
	}

	public String getCmt_classchangecount() {
		return "cmt_classchangecount";
	}

	public void setCmt_classchangecount(String cmt_classchangecount) {
		this.cmt_classchangecount = cmt_classchangecount;
	}

	public String getCmt_methodchangecount() {
		return "cmt_methodchangecount";
	}

	public void setCmt_methodchangecount(String cmt_methodchangecount) {
		this.cmt_methodchangecount = cmt_methodchangecount;
	}

	public String getCmt_fieldchangecount() {
		return "cmt_fieldchangecount";
	}

	public void setCmt_fieldchangecount(String cmt_fieldchangecount) {
		this.cmt_fieldchangecount = cmt_fieldchangecount;
	}

	public String getCmt_methodbodychangecount() {
		return "cmt_methodbodychangecount";
	}

	public void setCmt_methodbodychangecount(String cmt_methodbodychangecount) {
		this.cmt_methodbodychangecount = cmt_methodbodychangecount;
	}

	public String getCmt_buildfilechangecount() {
		return "cmt_buildfilechangecount";
	}

	public void setCmt_buildfilechangecount(String cmt_buildfilechangecount) {
		this.cmt_buildfilechangecount = cmt_buildfilechangecount;
	}

	public String getNxt_cmt_importchangecount() {
		return "nxt_cmt_importchangecount";
	}

	public void setNxt_cmt_importchangecount(String nxt_cmt_importchangecount) {
		this.nxt_cmt_importchangecount = nxt_cmt_importchangecount;
	}

	public String getNxt_cmt_classchangecount() {
		return "nxt_cmt_classchangecount";
	}

	public void setNxt_cmt_classchangecount(String nxt_cmt_classchangecount) {
		this.nxt_cmt_classchangecount = nxt_cmt_classchangecount;
	}

	public String getNxt_cmt_methodchangecount() {
		return "nxt_cmt_methodchangecount";
	}

	public void setNxt_cmt_methodchangecount(String nxt_cmt_methodchangecount) {
		this.nxt_cmt_methodchangecount = nxt_cmt_methodchangecount;
	}

	public String getNxt_cmt_fieldchangecount() {
		return "nxt_cmt_fieldchangecount";
	}

	public void setNxt_cmt_fieldchangecount(String nxt_cmt_fieldchangecount) {
		this.nxt_cmt_fieldchangecount = nxt_cmt_fieldchangecount;
	}

	public String getNxt_cmt_methodbodychangecount() {
		return "nxt_cmt_methodbodychangecount";
	}

	public void setNxt_cmt_methodbodychangecount(String nxt_cmt_methodbodychangecount) {
		this.nxt_cmt_methodbodychangecount = nxt_cmt_methodbodychangecount;
	}

	public String getNxt_cmt_buildfilechangecount() {
		return "nxt_cmt_buildfilechangecount";
	}

	public void setNxt_cmt_buildfilechangecount(String nxt_cmt_buildfilechangecount) {
		this.nxt_cmt_buildfilechangecount = nxt_cmt_buildfilechangecount;
	}
	
	
	public String getNxt_day_of_week() {
		return "nxt_day_of_week";
	}

	public void setNxt_day_of_week(String nxt_day_of_week) {
		this.nxt_day_of_week = nxt_day_of_week;
	}
	public String getNxt_gh_team_size() {
		return "nxt_gh_team_size";
	}
	public String getNxt_gh_num_issue_comments() {
		return "nxt_gh_num_issue_comments";
	}
	public String getNxt_gh_num_commit_comments() {
		return "nxt_gh_num_commit_comments";
	}
	public String getNxt_gh_num_pr_comments() {
		return "nxt_gh_num_pr_comments";
	}
	public String getNxt_gh_src_churn() {
		return "nxt_gh_src_churn";
	}
	public String getNxt_gh_test_churn() {
		return "nxt_gh_test_churn";
	}
	public String getNxt_gh_files_added() {
		return "nxt_gh_files_added";
	}
	public String getNxt_gh_files_deleted() {
		return "nxt_gh_files_deleted";
	}
	public String getNxt_gh_files_modified() {
		return "nxt_gh_files_modified";
	}
	public String getNxt_gh_tests_added() {
		return "nxt_gh_tests_added";
	}
	public String getNxt_gh_tests_deleted() {
		return "nxt_gh_tests_deleted";
	}
	public String getNxt_gh_src_files() {
		return "nxt_gh_src_files";
	}
	public String getNxt_gh_doc_files() {
		return "nxt_gh_doc_files";
	}
	public String getNxt_gh_other_files() {
		return "nxt_gh_other_files";
	}
	
	public String getDataRelationName()
	{
		return this.dataRelationName;
	}
	
	public List<String> getAllFileds()
	{
		List<String> fileds=new ArrayList<String>();
		
		//fileds.add(this.getDay_of_week());
		//fileds.add(this.getGh_team_size());
		//fileds.add(this.getGh_num_issue_comments());
		//fileds.add(this.getGh_num_commit_comments());
		//fileds.add(this.getGh_num_pr_comments());
		fileds.add(this.getGh_src_churn());
		fileds.add(this.getGh_test_churn());
		//fileds.add(this.getGh_files_added());
		//fileds.add(this.getGh_files_deleted());
		//fileds.add(this.getGh_files_modified());
		//fileds.add(this.getGh_tests_added());
		//fileds.add(this.getGh_tests_deleted());
		//fileds.add(this.getGh_src_files());
		//fileds.add(this.getGh_doc_files());
		//fileds.add(this.getGh_other_files());
		fileds.add(this.getTr_status());
		fileds.add(this.getBl_cluster());		
		//fileds.add(this.getCmt_importchangecount());
		//fileds.add(this.getCmt_classchangecount());
		//fileds.add(this.getCmt_methodchangecount());
		//fileds.add(this.getCmt_methodbodychangecount());
		//fileds.add(this.getCmt_fieldchangecount());
		//fileds.add(this.getCmt_buildfilechangecount());		
		fileds.add(getNxt_day_of_week());
		//fileds.add("time_diff");
		fileds.add(this.getNxt_gh_team_size());
		//fileds.add(this.getNxt_gh_num_issue_comments());
		//fileds.add(this.getNxt_gh_num_commit_comments());
		//fileds.add(this.getNxt_gh_num_pr_comments());
		fileds.add(this.getNxt_gh_src_churn());
		fileds.add(this.getNxt_gh_test_churn());
		fileds.add(this.getNxt_gh_files_added());
		fileds.add(this.getNxt_gh_files_deleted());
		fileds.add(this.getNxt_gh_files_modified());
		//fileds.add(this.getNxt_gh_tests_added());
		//fileds.add(this.getNxt_gh_tests_deleted());
		fileds.add(this.getNxt_gh_src_files());
		fileds.add(this.getNxt_gh_doc_files());
		fileds.add(this.getNxt_gh_other_files());	
		fileds.add(this.getNxt_cmt_importchangecount());
		fileds.add(this.getNxt_cmt_classchangecount());
		fileds.add(this.getNxt_cmt_methodchangecount());
		fileds.add(this.getNxt_cmt_methodbodychangecount());
		fileds.add(this.getNxt_cmt_fieldchangecount());
		fileds.add(this.getNxt_cmt_buildfilechangecount());
		fileds.add("WithSameCommits");
		
		
		return fileds;
		
		
	}
	

}
