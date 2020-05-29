package com.build.analyzer.dtaccess;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.build.analyzer.entity.Gradlepatch;
import com.build.analyzer.entity.Mavenpatch;
import com.build.analyzer.entity.Travistorrent;

public class DBActionExecutor {

	public long getTotalNumberofRows() {
		long rowcount = 0;
		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			rowcount = ((Long) session.createQuery("select count(*) from Travistorrent").uniqueResult()).intValue();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return rowcount;
	}
	 
	
	public Travistorrent getEntityWithRowId(long rowid) {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		int rowid1=(int)rowid;

		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "FROM Travistorrent Tr WHERE Tr.rowId = :row";
			Query query = session.createQuery(hql);
			query.setParameter("row", rowid1);
			List results = query.list();

			if (!results.isEmpty()) {
				travis = (Travistorrent) results.get(0);
			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return travis;
	}
	
	public List<Travistorrent> getErrorFailRows() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and (Tr.trStatus=:error or Tr.trStatus=:fail) and (Tr.trLogAnalyzer=:ant or Tr.trLogAnalyzer=:mvn or Tr.trLogAnalyzer=:gradle) and Tr.blLog is NULL";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");
			query.setParameter("error", "errored");
			query.setParameter("fail", "failed");
			query.setParameter("ant", "java-ant");
			query.setParameter("mvn", "java-maven");
			query.setParameter("gradle", "java-gradle");
			//query.setMaxResults(3);
			
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
	
	public List<Travistorrent> getErrorFailForAnt() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and (Tr.trStatus=:error or Tr.trStatus=:fail) and Tr.trLogAnalyzer=:ant and Tr.blLog is not NULL";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");
			query.setParameter("error", "errored");
			query.setParameter("fail", "failed");
			query.setParameter("ant", "java-ant");
			
			
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
	
	public List<Travistorrent> getErrorFailRowsForMaven() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and (Tr.trStatus=:error or Tr.trStatus=:fail) and Tr.trLogAnalyzer=:mvn and Tr.blLog is not NULL";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");
			query.setParameter("error", "errored");
			query.setParameter("fail", "failed");			
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
	
	public List<Travistorrent> getErrorFailRowsForGradle() {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and (Tr.trStatus=:error or Tr.trStatus=:fail)  and Tr.trLogAnalyzer=:gradle and Tr.blLog is not NULL";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", "java");
			query.setParameter("error", "errored");
			query.setParameter("fail", "failed");			
			query.setParameter("gradle", "java-gradle");
			
			
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
	
	
	public Travistorrent updateExistingRecord(Travistorrent travis) {
		//Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();			
			
			session.update(travis);
			tx.commit();			

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return travis;
	}
	
	public void updateBatchExistingRecord(List<Travistorrent> projects) {
		//Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();		
		

		Transaction tx = null;
		try {
			
			for(int index=0;index<projects.size();index++)
			{
				tx = session.beginTransaction();			
			
				session.update(projects.get(index));
				tx.commit();	
				
				//System.out.println("DB Update Row:"+index+"Row ID:"+projects.get(index).getRowId());
			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		
	}
	
	
	
	public void insertBatchMavenRecord(List<Mavenpatch> datas) {
		//Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();		
		

		Transaction tx = null;
		try {
			
			for(int index=0;index<datas.size();index++)
			{
				tx = session.beginTransaction();			
			
				session.save(datas.get(index));
				tx.commit();		
				
			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		
	}
	
	public List<Travistorrent> getAllProjectsOfLang(String lang, String analyzer) {
		Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Travistorrent> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Travistorrent Tr WHERE Tr.ghLang = :lag and Tr.trLogAnalyzer=:analyzer";
			
			
			Query query = session.createQuery(hql);
			query.setParameter("lag", lang);
			query.setParameter("analyzer", analyzer);
			//query.setMaxResults();
			
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
