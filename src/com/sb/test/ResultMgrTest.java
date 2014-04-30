package com.sb.test;

import com.sb.core.prog.Program;
import com.sb.writer.output.ResultMgr;

public class ResultMgrTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ResultMgr mgr = ResultMgr.getInstance();
		String inst = "ADD R1, R2, R3";
		
		mgr.addInst(inst);
		mgr.setFetchClock(inst, 3);
		mgr.setIssueClock(inst, 3);
		mgr.setReadClock(inst,5);
		mgr.setExecClock(inst, 6);
		mgr.setWriteClock(inst, 8);
		
		Program prog = new Program();
		prog.setProgName("TestProg.txt");
		mgr.PrintResult(prog);
		mgr.setResultFile("E:\\aSpring-2013\\result.txt");
		mgr.PrintResultToFile(prog);
	}
}
