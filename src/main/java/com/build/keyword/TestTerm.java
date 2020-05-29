package com.build.keyword;

import java.io.IOException;
import java.util.List;

import com.buildlogparser.parser.GradleLogParser;

public class TestTerm {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GradleLogParser parser=new GradleLogParser("gradle","D:\\Researh_Works\\ASE_2018\\testing\\code1.txt");
		
		String str=parser.getFullBuildLog();
		
		List<Keyword> list=null;
		try {
			list = TermExtractor.guessFromString(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int index=0;index<list.size();index++)
		{
			System.out.println(list.get(index).getStem()+"-->"+list.get(index).getFrequency());
		}
		
		

	}

}
