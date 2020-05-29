package edu.utsa.logparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleBuildLogParser {

	public List<BuildTestResult> getTestResults(List<String> lines) {

		List<BuildTestResult> buildtests = new ArrayList<BuildTestResult>();

		String linetext;
		int index = 0;

		while (index < lines.size()) {

			linetext = lines.get(index);
			linetext = linetext.toLowerCase();

			if (linetext.contains("tests completed") && linetext.contains("failed")) {
				BuildTestResult buildresult = getTestResutFromLine(linetext);
				buildtests.add(buildresult);
			}

			else if (linetext.contains("tests run") && linetext.contains("failures")) {
				BuildTestResult buildresult = getTestResutFromLine(linetext);
				buildtests.add(buildresult);
			}

			index++;

		}

		return buildtests;

	}

	private BuildTestResult getTestResutFromLine(String line) {

		BuildTestResult testResult = new BuildTestResult();
		boolean matchfound = false;

		if (matchfound == false) {

			String regex = "tests\\s*run:\\s*(\\d+),\\s*failures:\\s*(\\d+),\\s*errors:\\s*(\\d+)";

			Pattern p = Pattern.compile(regex);

			Matcher m = p.matcher(line);
			while (m.find()) {

				testResult.setTotalTestRun(testResult.getTotalTestRun() + Integer.parseInt(m.group(1)));
				testResult.setFailed(testResult.getFailed() + Integer.parseInt(m.group(2)));
				testResult.setErrored(testResult.getErrored() + Integer.parseInt(m.group(3)));

				if (Integer.parseInt(m.group(2)) > 0 || Integer.parseInt(m.group(3)) > 0) {
					testResult.setBuildSuccess(false);
				}
				matchfound = true;
			}
		}
		
		

		if (matchfound == false) {

			String regex1 = "(\\d+)\\s*tests\\s*completed,\\s*(\\d+)\\s*failed";

			Pattern p = Pattern.compile(regex1);

			Matcher m = p.matcher(line);
			while (m.find()) {

				testResult.setTotalTestRun(testResult.getTotalTestRun() + Integer.parseInt(m.group(1)));
				testResult.setFailed(testResult.getFailed() + Integer.parseInt(m.group(2)));

				if (Integer.parseInt(m.group(2)) > 0) {
					testResult.setBuildSuccess(false);
				}
				matchfound = true;
			}
		}

		if (matchfound == false) {
			String regex2 = "tests\\s*run:\\s*(\\d+),\\s*failures:\\s*(\\d+)";

			Pattern p2 = Pattern.compile(regex2);
			Matcher m2 = p2.matcher(line);

			while (m2.find()) {

				testResult.setTotalTestRun(testResult.getTotalTestRun() + Integer.parseInt(m2.group(1)));
				testResult.setFailed(testResult.getFailed() + Integer.parseInt(m2.group(2)));

				if (Integer.parseInt(m2.group(2)) > 0) {
					testResult.setBuildSuccess(false);
				}
			}

		}
		return testResult;
	}

}
