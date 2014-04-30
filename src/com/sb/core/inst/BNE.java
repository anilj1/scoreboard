package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.IntegerRegister;

public class BNE extends Instruction {
	private IntegerRegister Reg1;
	private IntegerRegister Reg2;
	private String BneLabel;
	private int result = 0;
	private int intAdderIndex = 0;
	
	public BNE(Opcode code, int exeCycles, String label, 
			IntegerRegister reg1, IntegerRegister reg2, String bneLabel) {
		super(Opcode.BNE, exeCycles, label);
		Reg1 = reg1;
		Reg2 = reg2;
		BneLabel = bneLabel;
	}

	public BNE(IntegerRegister reg1, IntegerRegister reg2, String bneLabel) {
		super(Opcode.BNE, 0, "");
		Reg1 = reg1;
		Reg2 = reg2;
		BneLabel = bneLabel;
	}

	public IntegerRegister getReg1() {
		return Reg1;
	}

	public void setReg1(IntegerRegister reg1) {
		Reg1 = reg1;
	}

	public IntegerRegister getReg2() {
		return Reg2;
	}

	public void setReg2(IntegerRegister reg2) {
		Reg2 = reg2;
	}
	
	public String getBneLabel() {
		return BneLabel;
	}

	public void setBneLabel(String bneLabel) {
		BneLabel = bneLabel;
	}
	
	
	public int getIntAdderIndex() {
		return intAdderIndex;
	}
	
	public void setIntAdderIndex(int intAdderIndex) {
		this.intAdderIndex = intAdderIndex;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
	
	public void clearWARState() {
		// This won't go to WRITE stage. No WAR Hazard.
	}
	
	public void clearWAWState() {
		DataHazard.getInstance().removeFromWawBlock(getReg1());		
		DataHazard.getInstance().removeFromRawBlock(getReg2());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		str.concat(Reg1.toString());
		str.concat(Reg2.toString());
		System.out.println(BneLabel);
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release resources
		super.releaseResource();
		Reg1.setIdle();
		Reg2.setIdle();
		this.clearAllHazards();
	}
}
