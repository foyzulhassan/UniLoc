package com.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringJoinTest {
	
	public static void main(String[] args)
	{

		List<String> strlist=new ArrayList<>();
		strlist.add("abc");
		
		
		
		String str=String.join(" ", strlist);
		str=str.trim();
		
		System.out.println(str);
		
		
	}

}
