package com.sb.core.cpu;

import com.sb.core.inst.Instruction;

public class ReserveBlock {

	private String RegName;
	private Instruction ReservedBy;
	
	public ReserveBlock(String regName, Instruction inst) {
		RegName = regName;
		ReservedBy = inst;
	}
	
	public String getRegName() {
		return RegName;
	}

	public void setRegName(String regName) {
		RegName = regName;
	}

	public Instruction getReservedBy() {
		return ReservedBy;
	}

	public void setReservedBy(Instruction reservedBy) {
		ReservedBy = reservedBy;
	}
}
