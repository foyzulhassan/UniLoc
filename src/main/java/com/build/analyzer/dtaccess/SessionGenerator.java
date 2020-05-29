package com.build.analyzer.dtaccess;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionGenerator {
	private static SessionFactory factory;

	private SessionGenerator() {

	}

	public static SessionFactory getSessionFactoryInstance() {
		if (factory == null) {
			try {
				//Configuration  configuration = new Configuration().configure( "./hibernate.cfg.xml");
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Throwable ex) {
				System.err.println("Failed to create sessionFactory object." + ex);
				throw new ExceptionInInitializerError(ex);
			}
		}
		
		return factory;
	}
	
	public static void closeFactory()
	{
		if(factory!=null)
		{
			factory.close();
			factory=null;
		}
	}
}
