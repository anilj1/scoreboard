package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntegerRegister;

public class LD extends Instruction {
	private FPRegister Destination;
	private int Offset;
	private IntegerRegister BaseAddr;
	private int result = 0;
	private int intAdderIndex = 0;
	
	public LD(Opcode code, int exeCycles, String label, 
			FPRegister dest, int offset, IntegerRegister baseAddr) {
		super(Opcode.LD, exeCycles, label);
		Destination = dest;
		Offset = offset;
		BaseAddr = baseAddr;
		setResult(0);
	}
	
	public LD(FPRegister dest, int offset, IntegerRegister baseAddr) {
		super(Opcode.LD, 1, "");
		Destination = dest;
		Offset = offset;
		BaseAddr = baseAddr;
		setResult(0);
	}
	
	public FPRegister getDestination() {
		return Destination;
	}
	
	public void setDestination(FPRegister destination) {
		Destination = destination;
	}
	
	public int getOffset() {
		return Offset;
	}
	
	public void setOffset(int offset) {
		Offset = offset;
	}
	
	public IntegerRegister getBaseAddr() {
		return BaseAddr;
	}
	
	public void setBaseAddr(IntegerRegister baseAddr) {
		BaseAddr = baseAddr;
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
		DataHazard.getInstance().removeFromWarBlock(getBaseAddr());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		str.concat(Destination.toString());
		System.out.println(Offset);
		str.concat(BaseAddr.toString());
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		Destination.setIdle();
		BaseAddr.setIdle();
		FuncUnitController.getInstance().getIntAdder(intAdderIndex).setAvailable();
		this.clearAllHazards();		
	}

	public void clearWAWState() {
		DataHazard.getInstance().removeFromWawBlock(getDestination());				
		DataHazard.getInstance().removeFromRawBlock(getDestination());
	}
}
