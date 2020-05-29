package com.build.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import com.build.analyzer.config.Config;

public class LogResultWriter {

	public LogResultWriter() {
		File file = new File(Config.getInspectionLogDir());

		if (file.exists()) {
			// if directory, go inside and call recursively
			if (file.isDirectory()) {
				for (File f : file.listFiles()) {
					// call recursively
					recursiveDelete(f);
				}
			}
			// call delete to delete files and empty directory
			file.delete();
			file.mkdir();
		} else {
			file.mkdir();

		}

	}

	public void printResultLog(String projectname, String failcmtid, String fixcmtid, String[] fixfiles, String difflog,
			ArrayList<String> rankedfile) {
		String file = Config.getInspectionLogDir() + Config.getResultLogFileName();

		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			writer.write("Project Name:" + projectname);
			writer.write("\n");

			writer.write("Fail Commit ID:" + failcmtid);
			writer.write("\n");

			writer.write("Fix Commit ID:" + fixcmtid);
			writer.write("\n");

			writer.write("Fix Files:");
			writer.write("\n");

			int fixindex = 0;
			while (fixindex < fixfiles.length) {

				writer.write(fixfiles[fixindex]);
				writer.write("\n");
				fixindex++;
			}

			writer.write("\n\n\n=======================Log==========================\n");

			writer.write(difflog);

		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

		String resultfile = Config.getInspectionLogDir() + Config.getResultRankFileName();

		Writer resultwriter = null;

		try {
			resultwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultfile, true), "utf-8"));
			resultwriter.write("Rank of Files:");
			resultwriter.write("\n\n");

			int index = 0;

			while (index < rankedfile.size()) {
				String rankfile = rankedfile.get(index);

				resultwriter.write(rankfile);
				resultwriter.write("\n");

				index++;

				// if (index > 50)
				// break;

			}

		} catch (IOException ex) {
			// report
		} finally {
			try {
				resultwriter.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public void printDifferentTypeofLog(int counter, long rowid, String proj, String largelog, String failpart,
			String passpart, String failpartsim, String fixfilelist) {

		String file = Config.getInspectionLogDir() + counter + "_" + rowid + "_" + proj + "_largelog.txt";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));

			writer.write(largelog);

		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

		file = Config.getInspectionLogDir() + counter + "_" + rowid + "_" + proj + "_failpart.txt";
		writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));

			writer.write(failpart);

		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

		file = Config.getInspectionLogDir() + counter + "_" + rowid + "_" + proj + "_passpart.txt";
		writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));

			writer.write(passpart);

		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

		file = Config.getInspectionLogDir() + counter + "_" + rowid + "_" + proj + "_failsim.txt";
		writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));

			writer.write(failpartsim);
			
			writer.write("\n\n\nFix File List\n\n");
			writer.write(fixfilelist);

		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}

	}

	public static void recursiveDelete(File file) {
		// to end the recursive loop
		if (!file.exists())
			return;

		// if directory, go inside and call recursively
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				// call recursively
				recursiveDelete(f);
			}
		}
		// call delete to delete files and empty directory
		file.delete();
		System.out.println("Deleted file/folder: " + file.getAbsolutePath());
	}

}
