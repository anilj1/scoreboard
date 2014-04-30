package com.sb.parser.inst;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sb.core.cpu.FloatingRegisterBank;
import com.sb.core.cpu.IntegerRegisterBank;
import com.sb.core.inst.ADDD;
import com.sb.core.inst.AND;
import com.sb.core.inst.ANDI;
import com.sb.core.inst.BEQ;
import com.sb.core.inst.BNE;
import com.sb.core.inst.DADD;
import com.sb.core.inst.DADDI;
import com.sb.core.inst.DIVD;
import com.sb.core.inst.DSUB;
import com.sb.core.inst.DSUBI;
import com.sb.core.inst.HLT;
import com.sb.core.inst.Instruction;
import com.sb.core.inst.JUMP;
import com.sb.core.inst.LD;
import com.sb.core.inst.LW;
import com.sb.core.inst.MULD;
import com.sb.core.inst.OR;
import com.sb.core.inst.ORI;
import com.sb.core.inst.SD;
import com.sb.core.inst.SUBD;
import com.sb.core.inst.SW;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntegerRegister;
import com.sb.parser.config.ConfigParser;

public class InstructionParser {

	// Single instance.
	private static InstructionParser instance = null;
	IntegerRegisterBank intRegBank = IntegerRegisterBank.getInstance();
	FloatingRegisterBank fpRegBank = FloatingRegisterBank.getInstance();
	Logger log = Logger.getLogger(this.getClass().getName());

	protected InstructionParser() {
	}

	public static InstructionParser getInstance() {
		if (instance == null) {
			instance = new InstructionParser();
		}
		return instance;
	}
	
	public Instruction parseInst(String str) {
		Instruction inst = null;
		Boolean containsLabel = false;
		String delim = "[ :,()]";
		
		if (str.contains(Constants.COLON)) {
			containsLabel = true;
		}
		
		// Parse the instruction line. 	
		String [] tokens = str.split(delim);
		List<String> list = new ArrayList<String>();
	    for(String s : tokens) {
	       if(s != null && s.length() > 0) {
	          list.add(s);
	       }
	    }
	    
	    // Prune the empty tokens from the string array.
	    tokens = list.toArray(new String[list.size()]);	
	    if (containsLabel && tokens.length == 5) {
	    	
	    	// After removing the LABEL, remaining tokens include: opcode, and operands.
	    	String label = tokens[0].trim();	    	
	    	String opCode = tokens[1].trim();
	    	String oprand1 = tokens[2].trim();
	    	String oprand2 = tokens[3].trim();
	    	String oprand3 = tokens[4].trim();
	    	inst = processThreeeOprandInst(opCode, oprand1, oprand2, oprand3);
	    	
			if (inst != null) {
				// Set the label for jump.
				inst.setLabel(label);
			} else {
				log.error("Invlaid instrucntion detected: " + opCode);
			}
	    	
	    } else if (tokens.length == 4) {
	    	String opCode = tokens[0].trim();
	    	String oprand1 = tokens[1].trim();
	    	String oprand2 = tokens[2].trim();
	    	String oprand3 = tokens[3].trim();
	    	inst = processThreeeOprandInst(opCode, oprand1, oprand2, oprand3);
	    	
	    } else if (tokens.length == 2) {
	    	String opCode = tokens[0].trim();
	    	String oprand1 = tokens[1].trim();
	    	inst = processOneOprandInst(opCode, oprand1);
	    	
	    } else if (tokens.length == 1) {
	    	String opCode = tokens[0].trim();
	    	inst = processZeroOprandInst(opCode);

	    } else {
	    	log.error("Invalid instruction detected");
	    }

	    // Return the processed instruction.
		return inst;
	}
	
	private static Instruction processOneOprandInst(String opCode, String oprand1) {
		
		Instruction inst = null;
		
    	if (opCode.equals(Constants.J)){
    		String jmpLabel = oprand1;
    		inst = new JUMP(jmpLabel);
    		inst.setExecCycles(ExecCycles.BRANCH_CYCLES);
    	}
		return inst;
	}
	
	private Instruction processZeroOprandInst(String opCode) {
		
		Instruction inst = null;
		
    	if (opCode.equals(Constants.HLT)){   		
    		inst = new HLT();
    		inst.setExecCycles(ExecCycles.BRANCH_CYCLES);
    	}
		return inst;
	}
	
	private Instruction processThreeeOprandInst(String opCode, String oprand1, 
			String oprand2, String oprand3) {
		
		Instruction inst = null;
	
		// Check for the type of instruction.
		if (opCode.equals(Constants.LW)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			int offSet = Integer.parseInt(oprand2);
			IntegerRegister src = intRegBank.getIntRegister(oprand3);
			inst = new LW(dest, offSet, src);
			inst.setExecCycles(ExecCycles.LOAD_STORE_CYCLES);
		} else if (opCode.equals(Constants.SW)) {
			IntegerRegister src = intRegBank.getIntRegister(oprand1);
			int offSet = Integer.parseInt(oprand2);
			IntegerRegister dest = intRegBank.getIntRegister(oprand3);
			inst = new SW(src, offSet, dest);
			inst.setExecCycles(ExecCycles.LOAD_STORE_CYCLES);
		} else if (opCode.equals(Constants.LD)) {
			FPRegister dest = fpRegBank.getFpRegister(oprand1);
			int offSet = Integer.parseInt(oprand2);
			IntegerRegister src = intRegBank.getIntRegister(oprand3);
			inst = new LD(dest, offSet, src);
			inst.setExecCycles(ExecCycles.LOAD_STORE_CYCLES);
		} else if (opCode.equals(Constants.SD)) {
			FPRegister src = fpRegBank.getFpRegister(oprand1);
			int offSet = Integer.parseInt(oprand2);
			IntegerRegister dest = intRegBank.getIntRegister(oprand3);
			inst = new SD(src, offSet, dest);
			inst.setExecCycles(ExecCycles.LOAD_STORE_CYCLES);
		} else if (opCode.equals(Constants.DADD)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			IntegerRegister src2 = intRegBank.getIntRegister(oprand3);
			inst = new DADD(dest, src1, src2);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.DADDI)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			int value = Integer.parseInt(oprand3.trim());
			inst = new DADDI(dest, src1, value);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.DSUB)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			IntegerRegister src2 = intRegBank.getIntRegister(oprand3);
			inst = new DSUB(dest, src1, src2);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.DSUBI)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			int value = Integer.parseInt(oprand3);
			inst = new DSUBI(dest, src1, value);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.AND)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			IntegerRegister src2 = intRegBank.getIntRegister(oprand3);
			inst = new AND(dest, src1, src2);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.ANDI)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			int value = Integer.parseInt(oprand3);
			inst = new ANDI(dest, src1, value);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.OR)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			IntegerRegister src2 = intRegBank.getIntRegister(oprand3);
			inst = new OR(dest, src1, src2);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.ORI)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			int value = Integer.parseInt(oprand3);
			inst = new ORI(dest, src1, value);
			inst.setExecCycles(ExecCycles.INT_ADDR_CYCLES);
		} else if (opCode.equals(Constants.ADDD)) {
			FPRegister dest = fpRegBank.getFpRegister(oprand1);
			FPRegister src1 = fpRegBank.getFpRegister(oprand2);
			FPRegister src2 = fpRegBank.getFpRegister(oprand3);
			inst = new ADDD(dest, src1, src2);
			inst.setExecCycles(ExecCycles.FP_ADDR_CYCLES);
		} else if (opCode.equals(Constants.SUBD)) {
			FPRegister dest = fpRegBank.getFpRegister(oprand1);
			FPRegister src1 = fpRegBank.getFpRegister(oprand2);
			FPRegister src2 = fpRegBank.getFpRegister(oprand3);
			inst = new SUBD(dest, src1, src2);
			inst.setExecCycles(ExecCycles.FP_ADDR_CYCLES);
		} else if (opCode.equals(Constants.MULD)) {
			FPRegister dest = fpRegBank.getFpRegister(oprand1);
			FPRegister src1 = fpRegBank.getFpRegister(oprand2);
			FPRegister src2 = fpRegBank.getFpRegister(oprand3);
			inst = new MULD(dest, src1, src2);
			inst.setExecCycles(ExecCycles.FP_MULTIPLIER_CYCLES);
		} else if (opCode.equals(Constants.DIVD)) {
			FPRegister dest = fpRegBank.getFpRegister(oprand1);
			FPRegister src1 = fpRegBank.getFpRegister(oprand2);
			FPRegister src2 = fpRegBank.getFpRegister(oprand3);
			inst = new DIVD(dest, src1, src2);
			inst.setExecCycles(ExecCycles.FP_DIVIDER_CYCLES);
		} else if (opCode.equals(Constants.BNE)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			String jmpLabel = oprand3;
			inst = new BNE(dest, src1, jmpLabel);
			inst.setExecCycles(ExecCycles.BRANCH_CYCLES);
		} else if (opCode.equals(Constants.BEQ)) {
			IntegerRegister dest = intRegBank.getIntRegister(oprand1);
			IntegerRegister src1 = intRegBank.getIntRegister(oprand2);
			String jmpLabel = oprand3;
			inst = new BEQ(dest, src1, jmpLabel);
			inst.setExecCycles(ExecCycles.BRANCH_CYCLES);
		} else {
			log.error("Invalid Opcode token found: " + opCode);
		}
		// Return the processed instruction.
		return inst;
	}
}
