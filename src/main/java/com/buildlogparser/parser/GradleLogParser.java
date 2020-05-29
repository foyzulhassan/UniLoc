package com.buildlogparser.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.build.analyzer.config.Config;

import edu.utsa.buildlogparser.util.StringFilter;

public class GradleLogParser extends BaseLogParser {

	public GradleLogParser() {

	}

	public GradleLogParser(String logtype, String logfile) {
		this.buildLogFile = logfile;
		this.buildType = logtype;
	}

	@Override
	public List<String> getBuildErrors() {
		List<String> list = new ArrayList<String>();

		try {
			File file = new File(this.buildLogFile);
			FileReader fileReader = new FileReader(file);
			List<String> keepLastLine = new ArrayList<String>();

			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// StringBuffer stringBuffer = new StringBuffer();

			String line;
			int linecount = 0;
			boolean buildstatustag = false;
			boolean iserrorlog = false;
			boolean iserrortagfound = false;

			while ((line = bufferedReader.readLine()) != null) {
				// replace all no ASCI Characters
				line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
				line = line.replaceFirst("> Loading", "");

				int indexat = linecount % Config.lineCountWithoutErrorTag;

				if (keepLastLine.size() == Config.lineCountWithoutErrorTag) {
					for (int index = 1; index < Config.lineCountWithoutErrorTag; index++) {
						keepLastLine.set(index - 1, keepLastLine.get(index));
					}

					keepLastLine.set(Config.lineCountWithoutErrorTag - 1, line);
				}

				else if (keepLastLine.size() <= Config.lineCountWithoutErrorTag - 1) {
					keepLastLine.add(indexat, line);
				}

				if (line.contains(Config.gradleErrorPrefix)) {
					iserrorlog = true;
					iserrortagfound = true;
				}

				else if (line.contains(Config.gradleErrorEndPrefix)) {
					iserrorlog = false;
				}

				if (iserrorlog == true) {
					line = StringFilter.getStringRemvingFilePath(line);
					if (!list.contains(line)) {
						if (line.length() > 0)
							list.add(line);
					}
				}

				if (line.toLowerCase().contains(Config.buildSuccessPrefix)
						|| line.toLowerCase().contains(Config.buildFailPrefix)) {
					buildstatustag = true;
				}

				linecount++;
			}

			fileReader.close();

			if (buildstatustag == false && iserrortagfound == false) {
				for (int i = 0; i < keepLastLine.size(); i++) {
					if (keepLastLine.get(i).length() > 0)
						list.add(keepLastLine.get(i));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

}
