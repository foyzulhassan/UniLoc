package com.buildlogparser.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.build.analyzer.config.Config;

import edu.utsa.buildlogparser.util.StringFilter;

public class AntLogParser extends BaseLogParser {

	public AntLogParser() {

	}

	public AntLogParser(String logtype, String logfile) {
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

			while ((line = bufferedReader.readLine()) != null) {
				// replace all no ASCI Characters
				line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
				line = line.replaceFirst("> Loading", "");
				line = line.replaceAll("[^\\x00-\\x7F]", "");

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

				if (iserrorlog == true) {
					line = StringFilter.getStringRemvingFilePath(line);
					if (!list.contains(line)) {
						list.add(line);
					}
				}

				if (line.toLowerCase().contains(Config.buildSuccessPrefix)) {
					buildstatustag = true;
				} else if (line.toLowerCase().contains(Config.buildFailPrefix)) {
					buildstatustag = true;
					iserrorlog = true;
				}

				linecount++;
			}

			fileReader.close();

			if (buildstatustag == false) {
				for (int i = 0; i < keepLastLine.size(); i++) {
					list.add(keepLastLine.get(i));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

}
