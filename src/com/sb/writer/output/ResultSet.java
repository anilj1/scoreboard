package com.sb.writer.output;

public class ResultSet {
	
	private String instKey;
	private int FetchComplClock;
	private int IssueComplClock;
	private int ReadComplClock;
	private int ExecComplClock;
	private int WriteComplClock;

	private boolean rawHazard;
	private boolean warHazard;
	private boolean wawHazard;
	private boolean structHazard;
	
	public ResultSet (String inst) {
		setInstKey(inst);
		FetchComplClock = 0;
		IssueComplClock = 0;
		ReadComplClock = 0;
		ExecComplClock = 0;
		WriteComplClock = 0;
		
		rawHazard = false;
		warHazard = false;
		wawHazard = false;
		structHazard = false;
	}
	
	// Getter setters.
	public int getFetchComplClock() {
		return FetchComplClock;
	}
	
	public void setFetchComplClock(int fetchComplClock) {
		FetchComplClock = fetchComplClock;
	}
	
	public int getIssueComplClock() {
		return IssueComplClock;
	}
	
	public void setIssueComplClock(int issueComplClock) {
		IssueComplClock = issueComplClock;
	}
	
	public int getReadComplClock() {
		return ReadComplClock;
	}
	
	public void setReadComplClock(int readComplClock) {
		ReadComplClock = readComplClock;
	}
	
	public int getExecComplClock() {
		return ExecComplClock;
	}
	
	public void setExecComplClock(int execComplClock) {
		ExecComplClock = execComplClock;
	}
	
	public int getWriteComplClock() {
		return WriteComplClock;
	}
	
	public void setWriteComplClock(int writeComplClock) {
		WriteComplClock = writeComplClock;
	}

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}
	
	// Hazard data
	public String isRawHazard() {
		if (rawHazard)
			return "Y";
		else 
			return "N";
	}
	
	public void setRawHazard(boolean rawHazard) {
		this.rawHazard = rawHazard;
	}

	public String isWarHazard() {
		if (warHazard)
			return "Y";
		else 
			return "N";
	}

	public void setWarHazard(boolean warHazard) {
		this.warHazard = warHazard;
	}

	public String isWawHazard() {
		if (wawHazard)
			return "Y";
		else 
			return "N";
	}

	public void setWawHazard(boolean wawHazard) {
		this.wawHazard = wawHazard;
	}

	public String isStructHazard() {
		if (structHazard)
			return "Y";
		else 
			return "N";
	}

	public void setStructHazard(boolean structHazard) {
		this.structHazard = structHazard;
	}
}
