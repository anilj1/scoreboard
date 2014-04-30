package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.IntegerRegister;

public class BEQ extends Instruction {
	private IntegerRegister Reg1;
	private IntegerRegister Reg2;
	private String BeqLabel;
	private int result = 0;
	private int intAdderIndex = 0;
	
	public BEQ(Opcode code, int exeCycles, String label, 
			IntegerRegister reg1, IntegerRegister reg2, String beqLabel) {
		super(Opcode.BEQ, exeCycles, label);
		Reg1 = reg1;
		Reg2 = reg2;
		BeqLabel = beqLabel;
	}

	public BEQ(IntegerRegister reg1, IntegerRegister reg2, String beqLabel) {
		super(Opcode.BEQ, 0, "");
		Reg1 = reg1;
		Reg2 = reg2;
		BeqLabel = beqLabel;
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
		return BeqLabel;
	}

	public void setBneLabel(String bneLabel) {
		BeqLabel = bneLabel;
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
		System.out.println(BeqLabel);
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release resources.
		super.releaseResource();
		Reg1.setIdle();
		Reg2.setIdle();
		this.clearAllHazards();
	}
}
