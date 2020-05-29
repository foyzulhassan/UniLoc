package com.buildlogparser.parser;

public class TestLogParser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GradleLogParser parser=new GradleLogParser();
		
		String str=parser.getDifferentialBuildLog("D:\\Researh_Works\\ASE_2018\\testing\\1.log", "D:\\Researh_Works\\ASE_2018\\testing\\2.log",false);
		
		
		System.out.println(str);

	}

}
