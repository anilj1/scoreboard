package com.sb.core.inst;

import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.IntegerRegister;

public class SW extends Instruction {
	private IntegerRegister Source;
	private int Offset;
	private IntegerRegister BaseAddr;
	private int intAdderIndex = 0;
	private int effectiveAddr = 0;
	
	public SW(Opcode code, int exeCycles, String label, 
			IntegerRegister src, int offset, IntegerRegister baseAddr) {
		super(Opcode.SW, exeCycles, label);
		Source = src;
		Offset = offset;
		BaseAddr = baseAddr;
	}
	
	public SW(IntegerRegister dest, int offset, IntegerRegister baseAddr) {
		super(Opcode.SW, 1, "");
		Source = dest;
		Offset = offset;
		BaseAddr = baseAddr;
	}
	
	public IntegerRegister getSource() {
		return Source;
	}
	
	public void setSource(IntegerRegister source) {
		Source = source;
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
	
	public void setBaseAddr(IntegerRegister destination) {
		BaseAddr = destination;
	}
	
	public int getIntAdderIndex() {
		return intAdderIndex;
	}

	public void setIntAdderIndex(int intAdderIndex) {
		this.intAdderIndex = intAdderIndex;
	}

	public void setEffectiveAddr(int effAddr) {
		this.effectiveAddr = effAddr;
	}

	public int getEffectiveAddr() {
		return effectiveAddr;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		str.concat(super.toString());
		str.concat(Source.toString());
		System.out.println(Offset);
		str.concat(BaseAddr.toString());
		
		return str;
	}

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		Source.setIdle();
		BaseAddr.setIdle();
		FuncUnitController.getInstance().getIntAdder(intAdderIndex).setAvailable();
		this.clearAllHazards();		
	}
}
