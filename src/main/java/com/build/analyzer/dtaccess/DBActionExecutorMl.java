package com.build.analyzer.dtaccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.build.analyzer.entity.Travistorrent;

public class DBActionExecutorMl {
	
	public List<Travistorrent> getRowsAnt() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and Tr.trLogAnalyzer=:ant";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");			
			query.setParameter("ant", "java-ant");	
			
			//query.setMaxResults(5);
			
			
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
	
	
	public List<Travistorrent>  getRowsMaven() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and Tr.trLogAnalyzer=:mvn";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");			
			query.setParameter("mvn", "java-maven");
			
			
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
	
	public List<Travistorrent>  getRowsGradle() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and  Tr.trLogAnalyzer=:gradle";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");		
			query.setParameter("gradle", "java-gradle");
			//query.setMaxResults(50);
			
			
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
	
	public List<String> getProjectNames(String buildconfigtype) {

		String SQL_QUERY = "";
		List<String> projects = new ArrayList<String>();

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;

		if (buildconfigtype.toLowerCase().equals("java-ant")) {
			SQL_QUERY = "SELECT distinct ghProjectName FROM Travistorrent where ghLang='java' and trLogAnalyzer='java-ant'";
		} else if (buildconfigtype.toLowerCase().equals("java-mvn") || buildconfigtype.toLowerCase().equals("java-maven")) {
			SQL_QUERY = "SELECT distinct ghProjectName FROM Travistorrent where ghLang='java' and trLogAnalyzer='java-maven'";
		} else if (buildconfigtype.toLowerCase().equals("java-gradle")) {
			SQL_QUERY = "SELECT distinct ghProjectName FROM Travistorrent where ghLang='java' and trLogAnalyzer='java-gradle'";
		}

		
		
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(SQL_QUERY);

			for (Iterator it = query.iterate(); it.hasNext();) {
				//Object[] row = (Object[]) it.next();
				//System.out.print("Course Name: " + row[0]);
				//System.out.println(" | Number of Students: " + row[1]);
				String row=(String) it.next();

				if(row!=null)
				{
					//System.out.println(row);
					projects.add(row);
				}


			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return projects;
	}
}
