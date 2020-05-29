package edu.utsa.logparser;

public class BuildTestResult {
	private int totalTestRun;
	private int errored;
	private int failed;
	private int skiped;
	
	private boolean buildSuccess;
	
	public BuildTestResult()
	{
		this.totalTestRun=0;
		this.errored=0;
		this.failed=0;
		this.skiped=0;
		this.buildSuccess=true;
	}
	
	public boolean isBuildSuccess() {
		return buildSuccess;
	}
	public void setBuildSuccess(boolean buildSuccess) {
		this.buildSuccess = buildSuccess;
	}
	public int getTotalTestRun() {
		return totalTestRun;
	}
	public void setTotalTestRun(int totalTestRun) {
		this.totalTestRun = totalTestRun;
	}
	public int getErrored() {
		return errored;
	}
	public void setErrored(int errored) {
		this.errored = errored;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
	public int getSkiped() {
		return skiped;
	}
	public void setSkiped(int skiped) {
		this.skiped = skiped;
	}	

}
