package edu.utsa.logparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntBuildLogParser {

	public List<BuildTestResult> getTestResults(String logfilepath) {

		List<BuildTestResult> buildtests = new ArrayList<BuildTestResult>();

		try {
			File file = new File(logfilepath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// StringBuffer stringBuffer = new StringBuffer();
			String linetext;

			while ((linetext = bufferedReader.readLine()) != null) {

				linetext = linetext.toLowerCase();

				if (linetext.contains("failures") && linetext.contains("errors")
						&& linetext.contains("skipped")) {
					BuildTestResult buildresult = getTestResutFromLine(linetext);
					buildtests.add(buildresult);
				}

				else if (linetext.contains("failures") && linetext.contains("skips")
						&& linetext.contains("run")) {
					BuildTestResult buildresult = getTestResutFromLine(linetext);
					buildtests.add(buildresult);
				}

			}

			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return buildtests;

	}

	private BuildTestResult getTestResutFromLine(String line) {

		BuildTestResult testResult = new BuildTestResult();

		String regex1 = "tests\\s*run:\\s*(\\d*),\\s*failures:\\s*(\\d*),\\s*errors:\\s*(\\d*),\\s*skipped:\\s*(\\d*)";

		Pattern p = Pattern.compile(regex1);

		Matcher m = p.matcher(line);
		while (m.find()) {

			testResult.setTotalTestRun(testResult.getTotalTestRun() + Integer.parseInt(m.group(1)));
			testResult.setFailed(testResult.getFailed() + Integer.parseInt(m.group(2)));
			testResult.setErrored(testResult.getErrored() + Integer.parseInt(m.group(3)));
			testResult.setSkiped(testResult.getSkiped() + Integer.parseInt(m.group(4)));

			if (Integer.parseInt(m.group(2)) > 0 || Integer.parseInt(m.group(3))>0) {
				testResult.setBuildSuccess(false);
			}
		}

		String regex2 = "total\\s*tests\\s*run:\\s*(\\d+),\\s*failures:\\s*(\\d+),\\s*skips:\\s*(\\d+)";

		Pattern p2 = Pattern.compile(regex2);
		Matcher m2 = p2.matcher(line);

		while (m2.find()) {

			testResult.setTotalTestRun(testResult.getTotalTestRun() + Integer.parseInt(m.group(1)));
			testResult.setFailed(testResult.getFailed() + Integer.parseInt(m.group(2)));
			testResult.setSkiped(testResult.getSkiped() + Integer.parseInt(m.group(3)));

			if (Integer.parseInt(m.group(2)) > 0) {
				testResult.setBuildSuccess(false);
			}
		}

		return testResult;
	}

}
