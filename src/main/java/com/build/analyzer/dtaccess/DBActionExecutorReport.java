package com.build.analyzer.dtaccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.build.analyzer.entity.DocClusters;
import com.build.analyzer.entity.Travistorrent;

public class DBActionExecutorReport {
	
	

	public List<DocClusters> getBuildClusterIDWithCount(String buildconfigtype) {

		String SQL_QUERY = "";
		List<DocClusters> clusterresults = new ArrayList<DocClusters>();

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;

		if (buildconfigtype.toLowerCase().equals("ant")) {
			SQL_QUERY = "SELECT count(*) AS num_log, bl_cluster FROM Travistorrent where gh_lang='java' and ( tr_status='failed' or tr_status='errored') and tr_analyzer='java-ant' GROUP BY bl_cluster ORDER by count(*) DESC";
		} else if (buildconfigtype.toLowerCase().equals("mvn") || buildconfigtype.toLowerCase().equals("maven")) {
			SQL_QUERY = "SELECT count(*) AS num_log, bl_cluster FROM Travistorrent where gh_lang='java' and ( tr_status='failed' or tr_status='errored') and tr_analyzer='java-maven' GROUP BY bl_cluster ORDER by count(*) DESC";
		} else if (buildconfigtype.toLowerCase().equals("gradle")) {
			SQL_QUERY = "SELECT count(*) AS num_log, bl_cluster FROM Travistorrent where gh_lang='java' and ( tr_status='failed' or tr_status='errored') and tr_analyzer='java-gradle' GROUP BY bl_cluster ORDER by count(*) DESC";
		}

		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(SQL_QUERY);

			for (Iterator it = query.iterate(); it.hasNext();) {
				Object[] row = (Object[]) it.next();
				//System.out.print("Course Name: " + row[0]);
				//System.out.println(" | Number of Students: " + row[1]);
				DocClusters clusterresult = new DocClusters();

				if(row[1]!=null && row[0]!=null)
				{
					clusterresult.setClusterName(row[1].toString());
					clusterresult.setNumOfItems(Integer.parseInt(row[0].toString()));
					clusterresults.add(clusterresult);
				}


			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return clusterresults;
	}	
	
	public List<Travistorrent>  getProjectsWithCluster(String clustername) {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			//String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and  Tr.trAnalyzer=:gradle";
			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and ( Tr.trStatus=:fail or Tr.trStatus=:errored) and  Tr.clusterName=:cluster";
			//SELECT * FROM travistorrent.travistorrent_27_10_2016 where gh_lang='java' and ( tr_status='failed' or tr_status='errored') and  bl_cluster='gradlecl24';
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");		
			query.setParameter("fail", "fail");		
			query.setParameter("error", "error");
			query.setParameter("cluster", clustername);		
			
			results = query.list();			

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return results;
	}
	
	
	public List<Travistorrent>  getProjectsBuildStatusAndConfigType(String buildstatus, String buildconfigtyp) {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();		
			
			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and Tr.trStatus=:status and Tr.trAnalyzer=:analyzer";			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");		
			query.setParameter("status", buildstatus);			
			query.setParameter("analyzer", buildconfigtyp);	
			//query.setMaxResults(10);
			
			results = query.list();			

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return results;
	}
	
	public List<String> getJavaProjectList() {

		String SQL_QUERY = "";
		List<String> projectlists = new ArrayList<String>();

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;

		
		SQL_QUERY = "SELECT distinct ghProjectName FROM Travistorrent where ghLang='java'";
		//SQL_QUERY = "SELECT distinct gh_project_name FROM Travistorrent where gh_lang='java'";		

		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(SQL_QUERY);

			for (Iterator it = query.iterate(); it.hasNext();) {
				String row=(String) it.next();

				if(row!=null)
				{
					//System.out.println(row);
					projectlists.add(row);
				}

			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return projectlists;
	}
	
	public List<Travistorrent>  getJavaProjects() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");		
			
			
			results = query.list();

			

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return results;
	}	
	
}
