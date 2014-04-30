package com.sb.core.inst;

import org.apache.log4j.Logger;

public abstract class Instruction {
	private String rawInst;
	private Opcode Opcode;
	private String Label;
	private State State;
	private int Order;

	private int FetchCycles;
	private int IssueCycles;
	private int ReadCycles;
	private int ExecCycles;
	private int WriteCycles;

	private float FetchCounter;
	private float IssueCounter;
	private float ReadCounter;
	private float ExecCounter;
	private float WriteCounter;
	
	private boolean StructHazard;
	private boolean WarHazard;
	private boolean WawHazard;
	private boolean RawHazard;
	
	private static final float FetchCounterStep = (float) 1;
	private static final float IssueCounterStep = (float) 1;
	private static final float ReadCounterStep = (float) 1;
	private static final float ExecCounterStep = (float) 1;
	private static final float WriteCounterStep = (float) 1;

	Instruction prevStallCausedBy = null;
	Logger log = Logger.getLogger(this.getClass().getName());

	public Instruction() {
		// TODO Auto-generated constructor stub
		Opcode = Opcode.DEFAULT;
		Label = "";
		State = State.IDLE;
		
		FetchCycles = 0;
		IssueCycles = 1;
		ReadCycles = 1;
		ExecCycles = 1;
		WriteCycles = 1;
		
		FetchCounter = 0;
		IssueCounter = 0;
		ReadCounter = 0;
		ExecCounter = 0;
		WriteCounter = 0;
	}
	
	public Instruction (Opcode code, int exeCycles, String label) {
		Opcode = code;
		ExecCycles = exeCycles;
		Label = label;
		State = State.IDLE;
		
		FetchCycles = 0;
		IssueCycles = 1;
		ReadCycles = 1;
		ExecCycles = 1;
		WriteCycles = 1;
	}
	
	// Instruction opcode.
	public Opcode getOpcode() {
		return Opcode;
	}
	
	public void setOpcode(Opcode code) {
		Opcode = code;
	}

	// Jump label.
	public String getLabel() {
		return Label;
	}
	
	public void setLabel(String label) {
		Label = label;
	}
	
	// Get current state.
	public State getState() {
		return State;
	}

	public void setState(State state) {
		this.State = state;
	}
	
	// Instruction Cycles.
	public int getFetchCycles() {
		return FetchCycles;
	}

	public void setFetchCycles(int fetchCycles) {
		FetchCycles = fetchCycles;
	}
	
	public int getIssueCycles() {
		return IssueCycles;
	}

	public void setIssueCycles(int issueCycles) {
		IssueCycles = issueCycles;
	}

	public int getReadCycles() {
		return ReadCycles;
	}

	public void setReadCycles(int readCycles) {
		ReadCycles = readCycles;
	}

	public int getExecCycles() {
		return ExecCycles;
	}
	
	public void setExecCycles(int execCycles) {
		ExecCycles = execCycles;
	}

	public int getWriteCycles() {
		return WriteCycles;
	}

	public void setWriteCycles(int writeCycles) {
		WriteCycles = writeCycles;
	}
	
	// Cycle Counter.
	public float getFetchCounter() {
		return FetchCounter;
	}

	public void setFetchCounter(int fetchCounter) {
		FetchCounter = fetchCounter;
	}
	
	public void IncrFetchCounter() {
		FetchCounter += FetchCounterStep;
	}

	public float getIssueCounter() {
		return IssueCounter;
	}

	public void setIssueCounter(int issueCounter) {
		IssueCounter = issueCounter;
	}
	
	public void IncrIssueCounter() {
		IssueCounter += IssueCounterStep;
	}
	
	public float getReadCounter() {
		return ReadCounter;
	}

	public void setReadCounter(int readCounter) {
		ReadCounter = readCounter;
	}
	
	public void IncrReadCounter() {
		ReadCounter += ReadCounterStep;
	}
	
	public float getExecCounter() {
		return ExecCounter;
	}

	public void setExecCounter(int execCounter) {
		ExecCounter = execCounter;
	}
	
	public void IncrExecCounter() {
		ExecCounter += ExecCounterStep;
	}
	
	public float getWriteCounter() {
		return WriteCounter;
	}

	public void setWriteCounter(int writeCounter) {
		WriteCounter = writeCounter;
	}
	
	public void IncrWriteCounter() {
		WriteCounter += WriteCounterStep;
	}

	public int getOrder() {
		return Order;
	}

	public void setOrder(int order) {
		Order = order;
	}

	// Hazards
	public boolean isStructHazard() {
		return StructHazard;
	}

	public void setStructHazard() {
		StructHazard = true;
	}
	
	public void clearStructHazard() {
		StructHazard = false;
	}
	
	public boolean isWarHazard() {
		return WarHazard;
	}

	public void setWarHazard() {
		WarHazard = true;
	}

	public void clearWarHazard() {
		WarHazard = false;
	}

	public boolean isWawHazard() {
		return WawHazard;
	}

	public void setWawHazard() {
		WawHazard = true;
	}

	public void clearWawHazard() {
		WawHazard = false;
	}
	
	public boolean isRawHazard() {
		return RawHazard;
	}

	public void setRawHazard() {
		RawHazard = true;
	}

	public void clearRawHazard() {
		RawHazard = false;
	}
	
	public String getRawInst() {
		return rawInst;
	}

	public void setRawInst(String rawInst) {
		this.rawInst = rawInst;
	}
	
	
	public Instruction getPrevStallCausedBy() {
		return prevStallCausedBy;
	}

	public void setPrevStallCausedBy(Instruction prevStallCausedBy) {
		this.prevStallCausedBy = prevStallCausedBy;
	}

	public void clearAllHazards() {
		this.clearRawHazard();
		this.clearStructHazard();
		this.clearWarHazard();
		this.clearWawHazard();
	}
	
	public void releaseResource() {
		//...
		log.debug("Releasing resource of " + Opcode + "[" + Order + "]");
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		System.out.println(Label);
		System.out.println(Opcode.name());
		//str.concat(Label.toString());
		//str.concat(Code.name().toString());
		
		return str;
	}

	protected void finalize() {
		Opcode = Opcode.DEFAULT;
		Label = "";
		State = State.IDLE;
		Order = 0;

		FetchCycles = 0;
		IssueCycles = 0;
		ReadCycles = 0;
		ExecCycles = 0;
		WriteCycles = 0;

		FetchCounter = 0;
		IssueCounter = 0;
		ReadCounter = 0;
		ExecCounter = 0;
		WriteCounter = 0;
		
		StructHazard = false;
		WarHazard = false;
		WawHazard = false;
		RawHazard = false;
	}
}
