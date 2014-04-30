package com.sb.core.inst;

import com.sb.core.cpu.DataHazard;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.register.FPRegister;

public class SUBD extends Instruction {
	private FPRegister Destination;
	private FPRegister Src1;
	private FPRegister Src2;
	private int fpAdderIndex = 0;

	public SUBD(Opcode code, int exeCycles, String label, 
			    FPRegister dest, FPRegister src1, FPRegister src2) {
		super(Opcode.SUBD, exeCycles, label);
		Destination = dest;
		Src1 = src1;
		Src2 = src2;
	}

	public SUBD(FPRegister dest, FPRegister src1, FPRegister src2) {
		super(Opcode.SUBD, 2, "");
		Destination = dest;
		Src1 = src1;
		Src2 = src2;
	}

	public FPRegister getDestination() {
		return Destination;
	}
	
	public void setDestination(FPRegister destination) {
		Destination = destination;
	}
	
	public FPRegister getSrc1() {
		return Src1;
	}
	
	public void setSrc1(FPRegister src1) {
		Src1 = src1;
	}
	
	public FPRegister getSrc2() {
		return Src2;
	}
	
	public void setSrc2(FPRegister src2) {
		Src2 = src2;
	}
	
	public int getFpAdderIndex() {
		return fpAdderIndex;
	}
	
	public void setFpAdderIndex(int index) {
		fpAdderIndex = index;
	}
	
	public void clearWARState() {
		DataHazard.getInstance().removeFromWarBlock(getSrc1());
		DataHazard.getInstance().removeFromWarBlock(getSrc2());
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

	@Override
	public void releaseResource() {
		// Release the resources.
		super.releaseResource();
		Destination.setIdle();
		Src1.setIdle();
		Src2.setIdle();
		FuncUnitController.getInstance().getFPAdder(fpAdderIndex).setAvailable();
		this.clearAllHazards();		
	}

	public void clearWAWState() {
		DataHazard.getInstance().removeFromWawBlock(getDestination());				
		DataHazard.getInstance().removeFromRawBlock(getDestination());
	}
}
