package com.build.analyzer.dtaccess;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.build.analyzer.entity.Gradlebuildfixdata;
import com.build.analyzer.entity.Gradlepatch;
import com.build.analyzer.entity.Travistorrent;

public class DBActionExecutorGradle {

	public List<Gradlepatch> getRows() {		

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Gradlepatch> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Gradlepatch gp";
			
			
			Query query = session.createQuery(hql);			
			
			
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
	
	public Gradlepatch getEntityWithRowId(long rowid) {
		Gradlepatch proj = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "FROM Gradlepatch Tr WHERE Tr.row = :row";
			Query query = session.createQuery(hql);
			query.setParameter("row", rowid);
			List results = query.list();

			if (!results.isEmpty()) {
				proj = (Gradlepatch) results.get(0);
			}

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return proj;
	}
	
	public List<Gradlepatch> getRowsWithPatch() {		

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Gradlepatch> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			String hql = "FROM Gradlepatch gp where length(patch_data)>0";
			
			
			Query query = session.createQuery(hql);			
			
			
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
	
	public List<Gradlepatch> getTestRows() {		

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();
		List<Gradlepatch> results = null;
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			
			////SELECT count(*) FROM travistorrent.travistorrent_27_10_2016 where gh_lang="java" and (tr_status="errored" or tr_status="failed") and (tr_analyzer="java-ant" or tr_analyzer="java-maven" or tr_analyzer="java-gradle") and  bl_log is NULL ;

			//String hql = "FROM Gradlepatch gp where patch_data is not NULL and patch_parent is not NULL";
			String hql = "FROM Gradlepatch gp where patch_parent is not NULL";
			
			
			Query query = session.createQuery(hql);			
			
			
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
	
	
	public void insertBatchGradleRecord(List<Gradlepatch> datas) {		

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
	
	public void insertBatchBuildFixRecord(List<Gradlebuildfixdata> datas) {		

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
	
	public void updateBatchExistingRecord(List<Gradlepatch> projects) {
		//Travistorrent travis = null;

		Session session = SessionGenerator.getSessionFactoryInstance().openSession();		
		

		Transaction tx = null;
		try {
			
			for(int index=0;index<projects.size();index++)
			{
				tx = session.beginTransaction();			
			
				session.update(projects.get(index));
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
}
