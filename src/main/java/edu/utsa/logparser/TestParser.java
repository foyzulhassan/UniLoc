package edu.utsa.logparser;

import java.util.ArrayList;
import java.util.List;

public class TestParser {
	
	public static void main(String[] args)
	{
		
		GradleBuildLogParser logparser=new GradleBuildLogParser();
		
		List<String> lines=new ArrayList<String>();
		
		
		lines.add("609 tests completed, 3 failed, 1 skipped");
		lines.add("Tests run: 1053, Failures: 1, Errors: 0, Skipped: 4, Time elapsed: 19.234 sec <<< FAILURE!");
		lines.add("8 tests completed, 2 failed");
		lines.add("Tests run: 2130, Failures: 2, Errors: 1, Skipped: 8");
		lines.add("1472 tests completed, 255 failed, 50 skipped");
		lines.add("1477 tests completed, 1 failed, 42 skipped");
		lines.add("Tests run: 2136, Failures: 0, Errors: 152, Skipped: 8");
		lines.add("169 tests completed, 1 failed");
		
		
		List<BuildTestResult> testres=logparser.getTestResults(lines);
		
		for(BuildTestResult res:testres)
		{
			
			if(res.isBuildSuccess()==false && (res.getFailed()>0 || res.getErrored()>0))
			{
				System.out.println("Failed");
			}
			else
			{
				System.out.println("not failed");
			}
		}	
		
	}

}
