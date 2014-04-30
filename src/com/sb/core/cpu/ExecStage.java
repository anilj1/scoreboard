package com.sb.core.cpu;

import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sb.core.fu.FuncUnitController;
import com.sb.core.fu.IntAdder;
import com.sb.core.inst.ADDD;
import com.sb.core.inst.AND;
import com.sb.core.inst.ANDI;
import com.sb.core.inst.DADD;
import com.sb.core.inst.DADDI;
import com.sb.core.inst.DIVD;
import com.sb.core.inst.DSUB;
import com.sb.core.inst.DSUBI;
import com.sb.core.inst.Instruction;
import com.sb.core.inst.LD;
import com.sb.core.inst.LW;
import com.sb.core.inst.MULD;
import com.sb.core.inst.OR;
import com.sb.core.inst.ORI;
import com.sb.core.inst.SD;
import com.sb.core.inst.SUBD;
import com.sb.core.inst.SW;
import com.sb.core.inst.State;
import com.sb.core.memory.DataMemory;
import com.sb.core.register.IntRegC;
import com.sb.writer.output.ResultMgr;

public class ExecStage extends PipelineStage {

	// Single instance.
	private static ExecStage instance = null;
	
	private Vector<Instruction> instQueue = null;
	private static float clockCounter = 1;
	private FuncUnitController fuCntl = FuncUnitController.getInstance();
	Logger log = Logger.getLogger(this.getClass().getName());
	private ResultMgr resultMgr = ResultMgr.getInstance();

	protected ExecStage() {
		instQueue = new Vector<Instruction>(5);
	}

	public static ExecStage getInstance() {
		if (instance == null) {
			instance = new ExecStage();
		}
		return instance;
	}
	
	@Override
	public int execute() {
		
		// Process all the instructions in the queue
		ListIterator<Instruction> itr = instQueue.listIterator();
		log.debug("EXEC Instruction queue size is: " + instQueue.size());

		for (int index = 0; itr.hasNext(); index++) {
			
			Instruction inst = itr.next();
			String opCode = inst.getOpcode().name();
			int instOrder = inst.getOrder();
			log.debug("Instruction being EXECed : " + opCode + "[" + instOrder + "]");
			
			switch (inst.getOpcode()) {
			case LD:
				LD ld = (LD) inst;
				processLDInst(ld);
				break;
			case SD:
				SD sd = (SD) inst;
				processSDInst(sd);
				break;
			case ADDD:
				ADDD addd = (ADDD) inst;
				processADDDInst(addd);
				break;
			case SUBD:
				SUBD subd = (SUBD) inst;
				processSUBDInst(subd);
				break;
			case MULD:
				MULD muld = (MULD) inst;
				processMULDInst(muld);
				break;
			case DIVD:
				DIVD divd = (DIVD) inst;
				processDIVDInst(divd);
				break;
			case DADD:
				DADD dadd = (DADD) inst;
				processDADDInst(dadd);
				break;
			case DADDI:
				DADDI daddi = (DADDI) inst;
				processDADDIInst(daddi);
				break;
			case DSUB:
				DSUB dsub = (DSUB) inst;
				processDSUBInst(dsub);
				break;
			case DSUBI:
				DSUBI dsubi = (DSUBI) inst;
				processDSUBIInst(dsubi);
				break;
			case LW:				
				LW lw = (LW) inst;
				processLWInst(lw);
				break;
			case SW:
				SW sw = (SW) inst;
				processSWInst(sw);
				break;
			case AND:
				AND and = (AND) inst;
				processANDInst(and);
				break;
			case ANDI:
				ANDI andi = (ANDI) inst;
				processANDIInst(andi);
				break;
			case OR:
				OR or = (OR) inst;
				processORInst(or);
				break;
			case ORI:
				ORI ori = (ORI) inst;
				processORIInst(ori);
				break;
			case BEQ:
				break;
			case BNE:
				break;
			case DEFAULT:
				break;
			case HLT:
				break;
			case J:
				break;
			default:
				break;
			}
			
			// Current state of Instruction
			log.debug("Instruction " + opCode + "[" + instOrder + "]" + " post processed State is: " + inst.getState().name());
						
			// Increment the inst clock counter.
			if (inst.getExecCounter() >= inst.getExecCycles()) {
				// Set the next state for instruction.
				inst.setState(State.EXEC_COMP);
				log.debug(opCode + "[" + instOrder + "]" + " EXEC stage completed at clock: " + this.getClock());
				resultMgr.setExecClock(inst.getRawInst(), this.getClock());
			}
				
			// Increment Issue counter, no matter what is outcome.
			// This stage visit should get recorded. 
			log.debug("Incremented " + opCode + "[" + instOrder + "]" + " EXEC counter.");
			log.debug(opCode + "[" + instOrder + "]" + " Exec Cycles are: " + inst.getExecCycles());
			log.debug(opCode + "[" + instOrder + "]" + " Exec Counter is: " + inst.getExecCounter());
			inst.IncrExecCounter();
		}
		
		// Test code to push EXEC completed instruction to next stage.
		for (int i = 0; i < instQueue.size(); i++) {
			Instruction in = instQueue.get(i);
			String opCode = in.getOpcode().name();
			int instOrder = in.getOrder();
			
			if (in.getState() == State.EXEC_COMP) {
				// Post the instruction to WRITE stage.
				in = instQueue.remove(i);
				WriteStage.getInstance().enQueue(in);
				log.debug("Instruction " + opCode + "[" + instOrder + "]" + " finished the EXEC Stage.");
				log.debug("Pushing instruction " + opCode + "[" + instOrder + "]" + " to WRITE stage.");
			} 
		}
		
		// Clock the stage counter.
		this.incrementClock();
		
		// Return success.
		return 0;
	}

	private int processORIInst(ORI ori) {
		// Local variables.
		String opCode = ori.getOpcode().name();
		int instOrder = ori.getOrder();

		if (ori.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(ori.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = ori.getSrc1().getValue();
			int src2 = ori.getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int intAnd = intAdder.or(src1, src2);
			log.debug("Subtraction is: " + intAnd);
			ori.setResult(intAnd);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			ori.setState(State.EXEC);
		}

		return 0;		
	}

	private int processORInst(OR or) {
		// Local variables.
		String opCode = or.getOpcode().name();
		int instOrder = or.getOrder();

		if (or.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(or.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = or.getSrc1().getValue();
			int src2 = or.getSrc2().getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int intOr = intAdder.or(src1, src2);
			log.debug("Bit wise OR is: " + intOr);
			or.setResult(intOr);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			or.setState(State.EXEC);
		}

		return 0;			
	}

	private int processANDIInst(ANDI andi) {
		// Local variables.
		String opCode = andi.getOpcode().name();
		int instOrder = andi.getOrder();

		if (andi.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(andi.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = andi.getSrc1().getValue();
			int src2 = andi.getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int intAnd = intAdder.and(src1, src2);
			log.debug("Subtraction is: " + intAnd);
			andi.setResult(intAnd);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			andi.setState(State.EXEC);
		}

		return 0;			
	}

	private int processANDInst(AND and) {
		// Local variables.
		String opCode = and.getOpcode().name();
		int instOrder = and.getOrder();

		if (and.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(and.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = and.getSrc1().getValue();
			int src2 = and.getSrc2().getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int intAnd = intAdder.and(src1, src2);
			log.debug("Bit wise AND is: " + intAnd);
			and.setResult(intAnd);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			and.setState(State.EXEC);
		}

		return 0;			
	}

	private int processDSUBIInst(DSUBI dsubi) {
		// Local variables.
		String opCode = dsubi.getOpcode().name();
		int instOrder = dsubi.getOrder();

		if (dsubi.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(dsubi.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = dsubi.getSrc1().getValue();
			int src2 = dsubi.getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int Subtraction = intAdder.sub(src1, src2);
			log.debug("Subtraction is: " + Subtraction);
			dsubi.setResult(Subtraction);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			dsubi.setState(State.EXEC);
		}

		return 0;			
	}

	private int processDSUBInst(DSUB dsub) {
		// Local variables.
		String opCode = dsub.getOpcode().name();
		int instOrder = dsub.getOrder();

		if (dsub.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(dsub.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = dsub.getSrc1().getValue();
			int src2 = dsub.getSrc2().getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int subtraction = intAdder.sub(src1, src2);
			log.debug("Subtraction is: " + subtraction);
			dsub.setResult(subtraction);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			dsub.setState(State.EXEC);
		}

		return 0;			
	}

	private int processDADDIInst(DADDI daddi) {
		// Local variables.
		String opCode = daddi.getOpcode().name();
		int instOrder = daddi.getOrder();

		if (daddi.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(daddi.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = daddi.getSrc1().getValue();
			int src2 = daddi.getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int addition = intAdder.add(src1, src2);
			log.debug("Sum is: " + addition);
			daddi.setResult(addition);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			daddi.setState(State.EXEC);
		}

		return 0;		
	}

	private int processDADDInst(DADD dadd) {
		// Local variables.
		String opCode = dadd.getOpcode().name();
		int instOrder = dadd.getOrder();

		if (dadd.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(dadd.getIntAdderIndex());
			
			// Calculate the sum of two numbers.
			int src1 = dadd.getSrc1().getValue();
			int src2 = dadd.getSrc2().getValue();
			log.debug("Src1: " + src1);
			log.debug("Src2: " + src2);
			
			int addition = intAdder.add(src1, src2);
			log.debug("Sum is: " + addition);
			dadd.setResult(addition);
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			dadd.setState(State.EXEC);
		}

		return 0;		
	}

	private int processDIVDInst(DIVD divd) {
		// Local variables.
		String opCode = divd.getOpcode().name();
		int instOrder = divd.getOrder();
		
		if (divd.getState().equals(State.READ_COMP)) {
	
			// We are not worried about the MULD execution, as its result
			// is not processed or used in the execution logic.
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			divd.setState(State.EXEC);
		}
		
		return 0;
	}

	private int processMULDInst(MULD muld) {
		// Local variables.
		String opCode = muld.getOpcode().name();
		int instOrder = muld.getOrder();
		
		if (muld.getState().equals(State.READ_COMP)) {
	
			// We are not worried about the MULD execution, as its result
			// is not processed or used in the execution logic.
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			muld.setState(State.EXEC);
		}
		
		return 0;	
	}

	private int processSUBDInst(SUBD subd) {
		// Local variables.
		String opCode = subd.getOpcode().name();
		int instOrder = subd.getOrder();
		
		if (subd.getState().equals(State.READ_COMP)) {
	
			// We are not worried about the SUBD execution, as its result
			// is not processed or used in the execution logic.
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			subd.setState(State.EXEC);
		}
		
		return 0;
	}

	private int processSWInst(SW sw) {
		// Local variables.
		String opCode = sw.getOpcode().name();
		int instOrder = sw.getOrder();

		if (sw.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(sw.getIntAdderIndex());
			
			// Calculate the effective address, and access the data memory.
			int effcAddr = intAdder.add(sw.getBaseAddr().getValue(), sw.getOffset());
			log.debug("Base address for " + opCode + "[" + instOrder + "] " + sw.getBaseAddr().getName() + " " + sw.getBaseAddr().getValue());
			log.debug("Offse for " + opCode + "[" + instOrder + "] " + sw.getBaseAddr().getName() + " " + sw.getOffset());
			log.debug("Effective address: " + Integer.toString(effcAddr));

			// Prepare the mem addr to be stored in the write back stage.
			sw.setEffectiveAddr(effcAddr);
			
			log.debug("Effective address is calculated. Result is fetched from Data Memory");
			log.debug("Effective address: " + Integer.toString(effcAddr));
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			sw.setState(State.EXEC);
		}

		return 0;
	}

	private int processSDInst(SD sd) {
		
		// Local variables.
		String opCode = sd.getOpcode().name();
		int instOrder = sd.getOrder();

		if (sd.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(sd.getIntAdderIndex());
			
			// Calculate the effective address, and access the data memory.
			int effcAddr = intAdder.add(sd.getBaseAddr().getValue(), sd.getOffset());
			log.debug("Base address for " + opCode + "[" + instOrder + "] " + sd.getBaseAddr().getName() + " " + sd.getBaseAddr().getValue());
			log.debug("Offset for " + opCode + "[" + instOrder + "] + sd.getBaseAddr().getName() " + " " + sd.getOffset());
			log.debug("Effective address: " + Integer.toString(effcAddr));

			// Prepare the mem addr to be stored in the write back stage.
			sd.setEffectiveAddr(effcAddr);
			
			log.debug("Effective address is calculated. Result is fetched from Data Memory");
			log.debug("Effective address: " + Integer.toString(effcAddr));
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			sd.setState(State.EXEC);
		}

		return 0;
	}

	private int processLDInst(LD ld) {

		// Local variables.
		String opCode = ld.getOpcode().name();
		int instOrder = ld.getOrder();

		if (ld.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(ld.getIntAdderIndex());
			
			// Calculate the effective address, and access the data memory.
			int effcAddr = intAdder.add(ld.getBaseAddr().getValue(), ld.getOffset());
			log.debug("Base address for " + opCode + "[" + instOrder + "] " + ld.getBaseAddr().getName() + " " + ld.getBaseAddr().getValue());
			log.debug("Offset for " + opCode + "[" + instOrder + "] " + ld.getBaseAddr().getName() + " " + ld.getOffset());
			log.debug("Effective address: " + Integer.toString(effcAddr));

			int result = DataMemory.getInstance().getDataRelative(effcAddr).intValue();
			ld.setResult(result);
			
			log.debug("Effective address is calculated. Result is fetched from Data Memory");
			log.debug("Effective address: " + Integer.toString(effcAddr));
			log.debug("Memory contents: " + Integer.toString(result));
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			ld.setState(State.EXEC);
		}

		return 0;
	}

	private int processADDDInst(ADDD addd) {
		// Local variables.
		String opCode = addd.getOpcode().name();
		int instOrder = addd.getOrder();
		
		if (addd.getState().equals(State.READ_COMP)) {
	
			// We are not worried about the ADDD execution, as its result
			// is not processed or used in the execution logic.
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			addd.setState(State.EXEC);
		}
		
		return 0;
	}

	private int processLWInst(LW lw) {
		
		// Local variables.
		String opCode = lw.getOpcode().name();
		int instOrder = lw.getOrder();
				
		if (lw.getState().equals(State.READ_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			IntAdder intAdder = fuCntl.getIntAdder(lw.getIntAdderIndex());
			
			// Calculate the effective address, and access the data memory.
			int effcAddr = intAdder.add(lw.getBaseAddr().getValue(), lw.getOffset());
			log.debug("Base address for " + opCode + "[" + instOrder + "] " + lw.getBaseAddr().getName() + " " + lw.getBaseAddr().getValue());
			log.debug("Offset for " + opCode + "[" + instOrder + "] " + lw.getBaseAddr().getName() + " " + lw.getOffset());
			log.debug("Effective address: " + Integer.toString(effcAddr));

			int result = DataMemory.getInstance().getDataRelative(effcAddr).intValue();
			lw.setResult(result);
			
			log.debug("Effective address is calculated. Result is fetched from Data Memory");
			log.debug("Effective address: " + Integer.toString(effcAddr));
			log.debug("Memory contents: " + Integer.toString(result));
			
			// Mark the instruction as READing.
			// This is to make sure this is not handled in next tick.
			lw.setState(State.EXEC);
		}
		
		return 0;
	}

	public void enQueue(Instruction inst) {
		instQueue.add(inst);
	}
	
	@Override
	public float getClock() {
		// TODO Auto-generated method stub
		return clockCounter;
	}

	@Override
	public void incrementClock() {
		ExecStage.clockCounter += CPUConstants.CLOCK_STEP;		
	}

	@Override
	public boolean isEmpty() {
		if (instQueue.isEmpty()) {
			log.debug("EXEC stage is empty");
			return true;
		} else {
			log.debug("EXEC stage is still running");
			return false;
		}
	}
}
