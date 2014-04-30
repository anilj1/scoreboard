package com.sb.core.inst;

import com.sb.core.fu.FuncUnitController;

public class JUMP extends Instruction {
	
	private String JmpLabel;
	
	public JUMP(Opcode code, int exeCycles, String label) {
		super(Opcode.J, exeCycles, label);
	}

	public JUMP(String jmpLabel) {
		super(Opcode.J, 0, "");
		JmpLabel = jmpLabel;
	}

	public String getJmpLabel() {
		return JmpLabel;
	}

	public void setJmpLabel(String jmpLabel) {
		this.JmpLabel = jmpLabel;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		System.out.println(JmpLabel);
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		this.clearAllHazards();	
	}
}
