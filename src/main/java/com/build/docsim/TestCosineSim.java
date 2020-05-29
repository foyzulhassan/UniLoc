package com.build.docsim;

import java.io.IOException;

public class TestCosineSim {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String file1="D:\\Researh_Works\\ASE_2018\\testing\\code1.txt";
		String file2="D:\\Researh_Works\\ASE_2018\\testing\\code2.txt";
		
		//CosineSimilarity simgen=new CosineSimilarity();
		
		double simval=0.0;
		
		try {
			simval=CosineDocumentSimilarity.getCosineSimilarity(file1, file2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//simval=simgen.getCosineSimilarity();
		
		System.out.println(simval);
	}

}
