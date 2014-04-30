package com.sb.core.inst;

import com.sb.core.fu.FuncUnitController;

public class HLT extends Instruction {
	
	public HLT(){
		super(Opcode.HLT, 0, "");
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
	
		return str;
	}

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		this.clearAllHazards();	
	}
}
