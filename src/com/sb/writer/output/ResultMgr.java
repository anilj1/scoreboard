package com.sb.writer.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.sb.core.config.SBConfig;
import com.sb.core.cpu.ExecStage;
import com.sb.core.cpu.FetchStage;
import com.sb.core.cpu.IssueStage;
import com.sb.core.cpu.ReadStage;
import com.sb.core.cpu.WriteStage;
import com.sb.core.engine.PipeLine;
import com.sb.core.prog.Program;
import com.sb.parser.config.ConfigParser;
import com.sb.parser.inst.ExecCycles;

public class ResultMgr {

	// Single instance.
	private static ResultMgr instance = null;
	private List<ResultSet> resultList;
	private String resultFile = "";

	public String getResultFile() {
		return resultFile;
	}

	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}

	protected ResultMgr() {
		resultList = new ArrayList<ResultSet>();
	}

	public static ResultMgr getInstance() {
		if (instance == null) {
			instance = new ResultMgr();
		}
		return instance;
	}
	
	public int addInst(String inst) {
		resultList.add(new ResultSet(inst));
		return 0;
	}
	
	public int setRawHazard(String inst) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setRawHazard(true);
			}
		}
		return 0;
	}
	
	public int setWarHazard(String inst) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setWarHazard(true);
			}
		}
		return 0;
	}
	
	public int setWawHazard(String inst) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setWawHazard(true);
			}
		}
		return 0;
	}
	
	public int setStructHazard(String inst) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setStructHazard(true);
			}
		}
		return 0;
	}
	
	public int setFetchClock(String inst, float fetchClock) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setFetchComplClock((int)fetchClock);
			}
		}
		return 0;
	}
	
	public int setIssueClock(String inst, float issueClock) {
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setIssueComplClock((int)issueClock);
			}
		}
		return 0;
	}
	
	public int setReadClock(String inst, float readClock) {
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setReadComplClock((int)readClock);
			}
		}		
		return 0;
	}
	
	public int setExecClock(String inst, float execClock) {
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setExecComplClock((int)execClock);
			}
		}
		return 0;
	}
	
	public int setWriteClock(String inst, float writeClock) {
		ListIterator<ResultSet> itr = resultList.listIterator();
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			if (result.getInstKey().equals(inst)) {
				result.setWriteComplClock((int)writeClock);
			}
		}
		return 0;
	}
	
	public void PrintResult(Program prog) {
		
		ListIterator<ResultSet> itr = resultList.listIterator();
		SBConfig conf = SBConfig.getInstance();
		String inst;
		String fetchClock;
		String issueClock;
		String readClock;
		String execClock;
		String writeClock;
		String structHaz;
		String wawHaz;
		String rawHaz;
		String warHaz;
		
		int noOfFPAdder;
		int noOfFPDiv;
		int noOfFPMult;
		int noOfIntAdder;
	
		noOfFPAdder = conf.getNoOfFPAdder();
		noOfFPDiv = conf.getNoOfFPDivider();
		noOfFPMult = conf.getNoOfFPMultiplier();
		noOfIntAdder = conf.getNoOfIntegerUnit();
		
		System.out.println();
		System.out.println(String.format("%-10s | %-25s", "Program: ", prog.getProgName()));
		System.out.println();
		System.out.println(String.format("%-20s | %-8s | %-12s", "Func Unit Type", "# Units", "EXEC Cycles"));
		System.out.println();

		
		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Adders", noOfFPAdder, ExecCycles.FP_ADDR_CYCLES));
		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Multipliers", noOfFPMult, ExecCycles.FP_MULTIPLIER_CYCLES));
		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Dividers", noOfFPDiv, ExecCycles.FP_DIVIDER_CYCLES));
		System.out.println(String.format("%-20s | %-8s | %-12s", "Integder Units", noOfIntAdder, ExecCycles.INT_ADDR_CYCLES));
		System.out.println(String.format("%-20s | %-8s | %-12s", "LOAD/STORE", "-- ", ExecCycles.LOAD_STORE_CYCLES));
		System.out.println(String.format("%-20s | %-8s | %-12s", "BRANCH", "-- ", ExecCycles.BRANCH_CYCLES));
		System.out.println();
		System.out.println();

		System.out.println(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
				"Instruction", "FETCH", "ISSUE", "READ", "EXEC", "WRITE",
				"RAW", "WAR", "WAW", "STRUCT"));
		System.out.println();
				
		while (itr.hasNext()) {
			ResultSet result = itr.next();
			
			inst = result.getInstKey(); 
			fetchClock = Integer.toString(result.getFetchComplClock());
			issueClock = Integer.toString(result.getIssueComplClock());
			readClock = Integer.toString(result.getReadComplClock());
			execClock = Integer.toString(result.getExecComplClock());
			writeClock = Integer.toString(result.getWriteComplClock());
			rawHaz = result.isRawHazard(); 
			warHaz = result.isWarHazard();
			wawHaz = result.isWawHazard();
			structHaz = result.isStructHazard();
			
			System.out.println(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
					inst, fetchClock, issueClock, readClock, execClock, writeClock,
					rawHaz, warHaz, wawHaz, structHaz));
		}
		
		System.out.println();
		System.out.println();
		System.out.println(String.format("%-20s | %-12s", "# Cache Requests", "Cache Hits"));
		System.out.println();
		System.out.println(String.format("%-20s | %-12s", prog.getProg().size(), (prog.getProg().size()/4) +1));
	}
	
	public void PrintResultToFile(Program prog) {
		ListIterator<ResultSet> itr = resultList.listIterator();
		SBConfig conf = SBConfig.getInstance();
		String inst;
		String fetchClock;
		String issueClock;
		String readClock;
		String execClock;
		String writeClock;
		String structHaz;
		String wawHaz;
		String rawHaz;
		String warHaz;
		
		int noOfFPAdder;
		int noOfFPDiv;
		int noOfFPMult;
		int noOfIntAdder;
	
		noOfFPAdder = conf.getNoOfFPAdder();
		noOfFPDiv = conf.getNoOfFPDivider();
		noOfFPMult = conf.getNoOfFPMultiplier();
		noOfIntAdder = conf.getNoOfIntegerUnit();
		
		try {
			 
			String content = "This is the content to write into file";
 
			File file = new File(getResultFile());
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.newLine();
			bw.write(String.format("%-10s | %-25s", "Program: ", prog.getProgName()));
			bw.newLine();
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "Func Unit Type", "# Units", "EXEC Cycles"));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "FP Adders", noOfFPAdder, ExecCycles.FP_ADDR_CYCLES));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "FP Multipliers", noOfFPMult, ExecCycles.FP_MULTIPLIER_CYCLES));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "FP Dividers", noOfFPDiv, ExecCycles.FP_DIVIDER_CYCLES));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "Integder Units", noOfIntAdder, ExecCycles.INT_ADDR_CYCLES));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "LOAD/STORE", "-- ", ExecCycles.LOAD_STORE_CYCLES));
			bw.newLine();
			bw.write(String.format("%-20s | %-8s | %-12s", "BRANCH", "-- ", ExecCycles.BRANCH_CYCLES));
			bw.newLine();
			bw.newLine();
			bw.write(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
					"Instruction", "FETCH", "ISSUE", "READ", "EXEC", "WRITE",
					"RAW", "WAR", "WAW", "STRUCT"));
			bw.newLine();

			while (itr.hasNext()) {
				ResultSet result = itr.next();
				
				inst = result.getInstKey(); 
				fetchClock = Integer.toString(result.getFetchComplClock());
				issueClock = Integer.toString(result.getIssueComplClock());
				readClock = Integer.toString(result.getReadComplClock());
				execClock = Integer.toString(result.getExecComplClock());
				writeClock = Integer.toString(result.getWriteComplClock());
				rawHaz = result.isRawHazard(); 
				warHaz = result.isWarHazard();
				wawHaz = result.isWawHazard();
				structHaz = result.isStructHazard();
				
				bw.write(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
						inst, fetchClock, issueClock, readClock, execClock, writeClock,
						rawHaz, warHaz, wawHaz, structHaz));
				bw.newLine();
			}
			
			bw.newLine();
			bw.newLine();
			bw.write(String.format("%-20s | %-12s", "# Cache Requests", "Cache Hits"));
			bw.newLine();
			bw.write(String.format("%-20s | %-12s", prog.getProg().size(), (prog.getProg().size()/4) +1));
			
			bw.newLine();
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println();
//		System.out.println(String.format("%-10s | %-25s", "Program: ", prog.getProgName()));
//		System.out.println();
//		System.out.println(String.format("%-20s | %-8s | %-12s", "# Unit", "Total", "EXEC Cycles"));
//		System.out.println();

		
//		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Adders", noOfFPAdder, ExecCycles.FP_ADDR_CYCLES));
//		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Multipliers", noOfFPMult, ExecCycles.FP_MULTIPLIER_CYCLES));
//		System.out.println(String.format("%-20s | %-8s | %-12s", "FP Dividers", noOfFPDiv, ExecCycles.FP_DIVIDER_CYCLES));
//		System.out.println(String.format("%-20s | %-8s | %-12s", "Integder Units", noOfIntAdder, ExecCycles.INT_ADDR_CYCLES));
//		System.out.println(String.format("%-20s | %-8s | %-12s", "LOAD/STORE", "-- ", ExecCycles.LOAD_STORE_CYCLES));
//		System.out.println(String.format("%-20s | %-8s | %-12s", "BRANCH", "-- ", ExecCycles.BRANCH_CYCLES));
//		System.out.println();
//		System.out.println();
//
//		System.out.println(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
//				"Instruction", "FETCH", "ISSUE", "READ", "EXEC", "WRITE",
//				"RAW", "WAR", "WAW", "STRUCT"));
//		System.out.println();
				
//		while (itr.hasNext()) {
//			ResultSet result = itr.next();
//			
//			inst = result.getInstKey(); 
//			fetchClock = Integer.toString(result.getFetchComplClock());
//			issueClock = Integer.toString(result.getIssueComplClock());
//			readClock = Integer.toString(result.getReadComplClock());
//			execClock = Integer.toString(result.getExecComplClock());
//			writeClock = Integer.toString(result.getWriteComplClock());
//			rawHaz = result.isRawHazard(); 
//			warHaz = result.isWarHazard();
//			wawHaz = result.isWawHazard();
//			structHaz = result.isStructHazard();
//			
//			System.out.println(String.format("%-20s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s | %-6s",
//					inst, fetchClock, issueClock, readClock, execClock, writeClock,
//					rawHaz, warHaz, wawHaz, structHaz));
//		}
	}
	public void PrintCacheReport(Program prog) {
//		System.out.println();
//		System.out.println();
//		System.out.println(String.format("%-20s | %-12s", "# Cache Requests", "Cache Hits"));
//		System.out.println();
//		System.out.println(String.format("%-20s | %-12s", prog.getProg().size(), (prog.getProg().size()/4) +1));
	}
}
