package com.sb.core.fu;

public class IntAdder extends FuncUnit {

	public IntAdder(int index, FuState state) {
		super(index, state);
	}

	public int add (int src1, int src2) {
		return src1 + src2;
	}
	
	public int sub (int src1, int src2) {
		return src1 - src2;
	}
	
	public int and (int src1, int src2) {
		return src1 & src2;
	}
	
	public int or (int src1, int src2) {
		return src1 | src2;
	}
}
