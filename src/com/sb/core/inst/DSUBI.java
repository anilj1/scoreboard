package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.IntegerRegister;

public class DSUBI extends Instruction {
	private IntegerRegister Destination;
	private IntegerRegister Src1;
	private int Value;
	private int result = 0;
	private int intAdderIndex = 0;
	
	public DSUBI(Opcode code, int exeCycles, String label, 
			     IntegerRegister dest, IntegerRegister src1, int value) {
		super(Opcode.DSUBI, exeCycles, label);
		Destination = dest;
		Src1 = src1;
		Value = value;
	}

	public DSUBI(IntegerRegister dest, IntegerRegister src1, int value) {
		super(Opcode.DSUBI, 1, "");
		Destination = dest;
		Src1 = src1;
		Value = value;
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
	public int getValue() {
		return Value;
	}
	public void setValue(int value) {
		Value = value;
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
		DataHazard.getInstance().removeFromWarBlock(getSrc1());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		str.concat(Destination.toString());
		str.concat(Src1.toString());
		System.out.println(Value);
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		Destination.setIdle();
		Src1.setIdle();
		FuncUnitController.getInstance().getIntAdder(intAdderIndex).setAvailable();
		this.clearAllHazards();		
	}

	public void clearWAWState() {
		DataHazard.getInstance().removeFromWawBlock(getDestination());				
		DataHazard.getInstance().removeFromRawBlock(getDestination());
	}
}
