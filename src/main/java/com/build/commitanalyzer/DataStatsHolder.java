package com.build.commitanalyzer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Yassine Badache
 * 
 *         The StatsDataHolder class contains all the datas used to measure
 *         performance of the software, based on which information we need
 * 
 *         At the moment, we need the following, listed as parameters:
 *
 */
public class DataStatsHolder {
	/** Number of files that cannot be treated with Gumtree Spoon */
	private int nbFileErrors;

	/** Number of commits */
	private int nbCommits;

	/** Number of CtAssignment contained in all commits */
	private int nbAssignment;

	/** Number of CtLocalVar contained in all commits */
	private int nbLocalVar;

	/** Number of CtReturn contained in all commits */
	private int nbReturn;

	/** Number of CtFieldRead contained in all commits */
	private int nbFieldWrite;

	/** Number of commits that contain an error file diff */
	private int nbCommitsWithError;

	private int nbAssignmentOnlyOne;

	private int nbLocalVarOnlyOne;

	private int nbReturnOnlyOne;

	private int nbFieldWriteOnlyOne;

	private int nbOthers;

	private String errorString;

	/**
	 * Basic constructor
	 */
	public DataStatsHolder() {
		nbFileErrors = 0;
		nbCommits = 0;
		nbCommitsWithError = 0;

		nbFieldWrite = 0;
		nbAssignment = 0;
		nbLocalVar = 0;
		nbReturn = 0;
		nbOthers = 0;

		nbFieldWriteOnlyOne = 0;
		nbAssignmentOnlyOne = 0;
		nbLocalVarOnlyOne = 0;
		nbReturnOnlyOne = 0;

		errorString = "";
	}

	public void increment(String variableToIncrement) {
		errorString = "";

		switch (variableToIncrement) {
		case ("commit"):
			nbCommits++;
			break;

		case ("file_error"):
			nbFileErrors++;
			break;

		case ("Assignment"):
			nbAssignment++;
			break;

		case ("LocalVariable"):
			nbLocalVar++;
			break;

		case ("Return"):
			nbReturn++;
			break;

		case ("FieldWrite"):
			nbFieldWrite++;
			break;

		case ("commit_error"):
			nbCommitsWithError++;
			break;

		default:
			errorString = "non-existent";
			break;
		}

	}

	public void incrementOnlyOne(String variableToIncrement) {

		switch (variableToIncrement) {
		case ("Assignment"):
			nbAssignmentOnlyOne++;
			break;

		case ("LocalVariable"):
			nbLocalVarOnlyOne++;
			break;

		case ("Return"):
			nbReturnOnlyOne++;
			break;

		case ("FieldWrite"):
			nbFieldWriteOnlyOne++;
			break;

		default:
			nbOthers++;
			break;
		}
	}

	public void printResults() {
		System.out.println(nbFileErrors + " files with errors (=not treated)");
		System.out.println(nbCommits + " commits");
		System.out.println(nbCommitsWithError + " commits with errors");
		System.out.println("\n****************************\n");
		System.out.println(nbAssignment + " updates or insert of assignments");
		System.out.println(nbLocalVar + " updates or insert of local variables");
		System.out.println(nbReturn + " updates or insert of returns");
		System.out.println(nbFieldWrite + " updates or insert of field written");
		System.out.println("\n****************************\n");
		System.out.println(nbAssignmentOnlyOne + " commits with ONLY ONE assignment");
		System.out.println(nbLocalVarOnlyOne + " commits with ONLY ONE local variables");
		System.out.println(nbReturnOnlyOne + " commits with ONLY ONE returns");
		System.out.println(nbFieldWriteOnlyOne + " commits with ONLY ONE field written");
		System.out.println(nbOthers + " commits with ONLY ONE change (!= from the four others)");
	}

	public int getNbCommitsWithError() {
		return nbCommitsWithError;
	}

	public void setNbCommitsWithError(int nbCommitsWithError) {
		this.nbCommitsWithError = nbCommitsWithError;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setError(String errorString) {
		this.errorString = errorString;
	}

	public int getNbFileErrors() {
		return nbFileErrors;
	}

	public void setNbFileErrors(int nbFileErrors) {
		this.nbFileErrors = nbFileErrors;
	}

	public int getNbCommits() {
		return nbCommits;
	}

	public void setNbCommits(int nbCommits) {
		this.nbCommits = nbCommits;
	}

	public int getNbAssignment() {
		return nbAssignment;
	}

	public void setNbAssignment(int nbAssignment) {
		this.nbAssignment = nbAssignment;
	}

	public int getNbLocalVar() {
		return nbLocalVar;
	}

	public void setNbLocalVar(int nbLocalVar) {
		this.nbLocalVar = nbLocalVar;
	}

	public int getNbReturn() {
		return nbReturn;
	}

	public void setNbReturn(int nbReturn) {
		this.nbReturn = nbReturn;
	}

	public int getNbFieldWrite() {
		return nbFieldWrite;
	}

	public void setNbFieldWrite(int nbFieldWrite) {
		this.nbFieldWrite = nbFieldWrite;
	}

	public int getCommitsWithError() {
		return nbCommitsWithError;
	}

	public void setCommitsWithError(int commitsWithError) {
		nbCommitsWithError = commitsWithError;
	}

	public void saveResults(String project, String mode) throws IOException {
		File save = new File("results/" + mode + "/by-projects/" + project + "/README.md");
		FileUtils.writeStringToFile(save, "\n" + nbFileErrors + " files with errors (=not treated)\n" + nbCommits + " commits\n" + nbCommitsWithError
				+ " commits with errors\n****************************\n" + nbAssignment + " updates or insert of assignments\n" + nbLocalVar
				+ " updates or insert of local variables\n" + nbReturn + " updates or insert of returns\n" + nbFieldWrite
				+ " updates or insert of field written\n****************************\n" + nbAssignmentOnlyOne + " commits with ONLY ONE assignment\n"
				+ nbLocalVarOnlyOne + " commits with ONLY ONE local variables\n" + nbReturnOnlyOne + " commits with ONLY ONE returns\n" + nbFieldWriteOnlyOne
				+ " commits with ONLY ONE field written\n" + nbOthers + " commits with ONLY ONE change != from the four others");
	}

	public void reset() {
		nbFileErrors = 0;
		nbCommits = 0;
		nbCommitsWithError = 0;

		nbFieldWrite = 0;
		nbAssignment = 0;
		nbLocalVar = 0;
		nbReturn = 0;

		nbFieldWriteOnlyOne = 0;
		nbAssignmentOnlyOne = 0;
		nbLocalVarOnlyOne = 0;
		nbReturnOnlyOne = 0;

		errorString = "";
	}
}
