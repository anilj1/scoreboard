package com.sb.core.memory;

import com.sb.core.inst.Instruction;

public class InstCache {

	// Single instance.
	private static InstCache instance = null;
	Instruction[][] instCacheMem = null;
	
	protected InstCache () {
		instCacheMem = new Instruction[4][4];

		// Initialize the cache block. 
		for (int r=0; r<4; r++) {
		     for (int c=0; c<4; c++) {
		    	 instCacheMem[r][c] = null;
		     }
		 }
	}

	public static InstCache getInstance() {
		if (instance == null) {
			instance = new InstCache();
		}
		return instance;
	}
	
	public Instruction getInstruction(int progCnt) {
		
		int row = 0;
		int col = 0;
		
		if (progCnt % 4 == 0) {
			col = 4;
			row = progCnt / 4;
		} else {
			row = (progCnt / 4) + 1;
			col = progCnt % 4;
		}
		
		System.out.println("For PC: " + progCnt + " Row: " + row + " Col: " + col);
		return instCacheMem[row-1][col-1];
	}
	
	
}
