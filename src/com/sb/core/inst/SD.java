package com.sb.core.inst;

import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntegerRegister;

public class SD extends Instruction {
	private FPRegister Source;
	private int Offset;
	private IntegerRegister BaseAddr;
	private int result = 0;
	private int intAdderIndex = 0;
	private int effectiveAddr = 0;
	
	public SD(Opcode code, int exeCycles, String label, 
			FPRegister src, int offset, IntegerRegister baseAddr) {
		super(Opcode.SD, exeCycles, label);
		Source = src;
		Offset = offset;
		BaseAddr = baseAddr;
	}
	
	public SD(FPRegister src, int offset, IntegerRegister baseAddr) {
		super(Opcode.SD, 1, "");
		Source = src;
		Offset = offset;
		BaseAddr = baseAddr;
	}
	
	public FPRegister getSource() {
		return Source;
	}
	
	public void setSource(FPRegister source) {
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
