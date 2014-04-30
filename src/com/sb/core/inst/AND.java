package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.IntegerRegister;

public class AND extends Instruction {
	private IntegerRegister Destination;
	private IntegerRegister Src1;
	private IntegerRegister Src2;
	private int result = 0;
	private int intAdderIndex = 0;
	
	public AND(Opcode code, int exeCycles, String label, 
			IntegerRegister dest, IntegerRegister src1, IntegerRegister src2) {
		super(Opcode.AND, exeCycles, label);
		Destination = dest;
		Src1 = src1;
		Src2 = src2;
	}

	public AND(IntegerRegister dest, IntegerRegister src1, IntegerRegister src2) {
		super(Opcode.AND, 1, "");
		Destination = dest;
		Src1 = src1;
		Src2 = src2;
	}

	public IntegerRegister getDestination() {
		return Destination;
	}
	
	public void setDestination(IntegerRegister destination) {
		Destination = destination;
	}
	
	public IntegerRegister getSrc1() {
		return Src1;
	}
	
	public void setSrc1(IntegerRegister src1) {
		Src1 = src1;
	}
	
	public IntegerRegister getSrc2() {
		return Src2;
	}
	
	public void setSrc2(IntegerRegister src2) {
		Src2 = src2;
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
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		str.concat(Destination.toString());
		str.concat(Src1.toString());
		str.concat(Src2.toString());
		
		return str;
	}
	
	public void clearWARState() {
		DataHazard.getInstance().removeFromWarBlock(getSrc1());
		DataHazard.getInstance().removeFromWarBlock(getSrc2());
	}
	
	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		Destination.setIdle();
		Src1.setIdle();
		Src2.setIdle();
		FuncUnitController.getInstance().getIntAdder(intAdderIndex).setAvailable();
		this.clearAllHazards();		
	}

	public void clearWAWState() {
		DataHazard.getInstance().removeFromWawBlock(getDestination());
		DataHazard.getInstance().removeFromRawBlock(getDestination());
	}
}
