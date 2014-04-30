package com.sb.core.cpu;

import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sb.core.fu.FuncUnitController;
import com.sb.core.fu.IntAdder;
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
import com.sb.core.inst.Opcode;
import com.sb.core.inst.SD;
import com.sb.core.inst.SUBD;
import com.sb.core.inst.SW;
import com.sb.core.inst.State;
import com.sb.core.memory.RegisterMemory;
import com.sb.writer.output.ResultMgr;

public class ReadStage extends PipelineStage {

	// Single instance.
	private static ReadStage instance = null;
	
	private Vector<Instruction> instQueue = null;
	private static float clockCounter = 1;
	private FuncUnitController fuCntl = FuncUnitController.getInstance();
	private RegisterMemory regMem = RegisterMemory.getInstance();
	Logger log = Logger.getLogger(this.getClass().getName());
	private ResultMgr resultMgr = ResultMgr.getInstance();
	private DataHazard dataHaz = DataHazard.getInstance();
	private int bneResult = 0;

	protected ReadStage() {
		instQueue = new Vector<Instruction>(5);
	}

	public static ReadStage getInstance() {
		if (instance == null) {
			instance = new ReadStage();
		}
		return instance;
	}
	
	@Override
	public int execute() {
		
		// Process all the instructions in the queue
		ListIterator<Instruction> itr = instQueue.listIterator();

		log.debug("READ Instruction queue size is: " + instQueue.size());		
		for (int index = 0; itr.hasNext(); index++) {
			
			Instruction inst = itr.next();
			
			String opCode = inst.getOpcode().name();
			int instOrder = inst.getOrder();
			log.debug("Instruction being READ : " + opCode + "[" + instOrder + "]");
			
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
				BEQ beq = (BEQ) inst;
				processBEQInst(beq);
				break;
			case BNE:
				BNE bne = (BNE) inst;
				processBNEInst(bne);
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
			if (inst.getReadCounter() >= inst.getReadCycles()) {
				if (!inst.isRawHazard()) {
					// Set the next state for instruction.
					cleanWARHazard(inst);
					inst.setState(State.READ_COMP);
					log.debug(opCode + "[" + instOrder + "]" + " READ stage completed at clock: " + this.getClock());
					resultMgr.setReadClock(inst.getRawInst(), this.getClock());
				}
			}
				
			// Increment Issue counter, no matter what is outcome.
			// This stage visit should get recorded. 
			log.debug("Incremented " + opCode + "[" + instOrder + "]" + " READ counter.");
			log.debug(opCode + "[" + instOrder + "]" + " Read Cycles are: " + inst.getReadCycles());
			log.debug(opCode + "[" + instOrder + "]" + " Read Counter is: " + inst.getReadCounter());
			inst.IncrReadCounter();
		}
		
		// Test code to push READ completed instruction to next stage.
		for (int i = 0; i < instQueue.size(); i++) {
			Instruction in = instQueue.get(i);
			String opCode = in.getOpcode().name();
			int instOrder = in.getOrder();
			
			if (in.getState() == State.READ_COMP) {
				// Post the instruction to READ stage.
				in = instQueue.remove(i);
				if (in.getOpcode().equals(Opcode.BNE)) {
					if (((BNE)in).getResult() == 5) {
						
					}
				}
				ExecStage.getInstance().enQueue(in);
				log.debug("Instruction " + opCode + "[" + instOrder + "]" + " finished the READ Stage.");
				log.debug("Pushing instruction " + opCode + "[" + instOrder + "]" + " to EXEC stage.");
			} 
		}
		
		// Clock the stage counter.
		this.incrementClock();
		
		// Return success.
		return 0;
	}

	private int processBNEInst(BNE bne) {
		// Local variables.
		String opCode = bne.getOpcode().name();
		int instOrder = bne.getOrder();
		String src1n = "";
		String src2n = "";

		if (bne.getState().equals(State.ISSUE_COMP)) {

			// Self reservation is not a RAW hazard. 
			//boolean src1 = !dsub.getSrc1().isBusy(dsub.hashCode());
			//boolean src2 = !dsub.getSrc2().isBusy(dsub.hashCode());
			boolean src1 = !dataHaz.checkRAWHazard(bne.getReg1(), bne);
			boolean src2 = !dataHaz.checkRAWHazard(bne.getReg2(), bne);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(bne.getReg1().getId());
				Long src2v = regMem.getRegValue(bne.getReg2().getId());

				bne.getReg1().setValue(src1v.intValue());
				bne.getReg2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + bne.getReg1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + bne.getReg2().getId().name());
				
				// Clear RAW hazard is already set.
				bne.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				bne.setState(State.READ);
				
				// Execute Branch if not equal in READ stage itself.
				if (src1v.intValue() != src2v.intValue()) {
					bne.setResult(5);
				}
			} else {
				bne.setRawHazard();
				ResultMgr.getInstance().setRawHazard(bne.getRawInst());

				if (!src1) {
					src1n = bne.getReg1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = bne.getReg2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}

			}
		} 
		
		return 0;		
	}

	private int processBEQInst(BEQ beq) {
		// Local variables.
		String opCode = beq.getOpcode().name();
		int instOrder = beq.getOrder();
		String src1n = "";
		String src2n = "";

		if (beq.getState().equals(State.ISSUE_COMP)) {

			// Self reservation is not a RAW hazard. 
			//boolean src1 = !dsub.getSrc1().isBusy(dsub.hashCode());
			//boolean src2 = !dsub.getSrc2().isBusy(dsub.hashCode());
			boolean src1 = !dataHaz.checkRAWHazard(beq.getReg1(), beq);
			boolean src2 = !dataHaz.checkRAWHazard(beq.getReg2(), beq);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(beq.getReg1().getId());
				Long src2v = regMem.getRegValue(beq.getReg2().getId());

				beq.getReg1().setValue(src1v.intValue());
				beq.getReg2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + beq.getReg1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + beq.getReg2().getId().name());
				
				// Clear RAW hazard is already set.
				beq.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				beq.setState(State.READ);				
			} else {
				beq.setRawHazard();
				ResultMgr.getInstance().setRawHazard(beq.getRawInst());

				if (!src1) {
					src1n = beq.getReg1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = beq.getReg2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}

			}
		} 
		
		return 0;			
	}

	private int processORIInst(ORI ori) {
		// Local variables.
		String opCode = ori.getOpcode().name();
		int instOrder = ori.getOrder();
		String src1n = "";

		if (ori.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = ori.getSrc1().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(ori.getSrc1(), ori);
			
			// Check for RAW hazard.
			if (src1) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(ori.getSrc1().getId());
				ori.getSrc1().setValue(src1v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + ori.getSrc1().getId().name());
				log.debug("Immidiate value is: " + opCode + "[" + instOrder + "]" + ori.getValue());
				
				// Clear RAW hazard is already set.
				ori.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				ori.setState(State.READ);				
			} else {
				ori.setRawHazard();
				ResultMgr.getInstance().setRawHazard(ori.getRawInst());

				if (!src1) {
					src1n = ori.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " " + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;		
	}

	private int processORInst(OR or) {
		// Local variables.
		String opCode = or.getOpcode().name();
		int instOrder = or.getOrder();
		String src1n = "";
		String src2n = "";

		if (or.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = or.getSrc1().isIdle();
			//boolean src2 = or.getSrc2().isIdle();
			
			boolean src1 = !dataHaz.checkRAWHazard(or.getSrc1(), or);
			boolean src2 = !dataHaz.checkRAWHazard(or.getSrc2(), or);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(or.getSrc1().getId());
				Long src2v = regMem.getRegValue(or.getSrc2().getId());

				or.getSrc1().setValue(src1v.intValue());
				or.getSrc2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + or.getSrc1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + or.getSrc2().getId().name());
				
				// Clear RAW hazard is already set.
				or.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				or.setState(State.READ);				
			} else {
				or.setRawHazard();
				ResultMgr.getInstance().setRawHazard(or.getRawInst());

				if (!src1) {
					src1n = or.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = or.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;		
	}

	private int processANDIInst(ANDI andi) {
		// Local variables.
		String opCode = andi.getOpcode().name();
		int instOrder = andi.getOrder();
		String src1n = "";

		if (andi.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = andi.getSrc1().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(andi.getSrc1(), andi);
			
			// Check for RAW hazard.
			if (src1) {
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(andi.getSrc1().getId());
				andi.getSrc1().setValue(src1v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + andi.getSrc1().getId().name());
				log.debug("Immidiate value is: " + opCode + "[" + instOrder + "]" + andi.getValue());
				
				// Clear RAW hazard is already set.
				andi.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				andi.setState(State.READ);				
			} else {
				andi.setRawHazard();
				ResultMgr.getInstance().setRawHazard(andi.getRawInst());

				if (!src1) {
					src1n = andi.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " " + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;		
	}

	private int processANDInst(AND and) {
		// Local variables.
		String opCode = and.getOpcode().name();
		int instOrder = and.getOrder();
		String src1n = "";
		String src2n = "";

		if (and.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = and.getSrc1().isIdle();
			//boolean src2 = and.getSrc2().isIdle();
			
			boolean src1 = !dataHaz.checkRAWHazard(and.getSrc1(), and);
			boolean src2 = !dataHaz.checkRAWHazard(and.getSrc2(), and);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(and.getSrc1().getId());
				Long src2v = regMem.getRegValue(and.getSrc2().getId());

				and.getSrc1().setValue(src1v.intValue());
				and.getSrc2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + and.getSrc1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + and.getSrc2().getId().name());
				
				// Clear RAW hazard is already set.
				and.clearRawHazard();
						
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				and.setState(State.READ);				
			} else {
				and.setRawHazard();
				ResultMgr.getInstance().setRawHazard(and.getRawInst());

				if (!src1) {
					src1n = and.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = and.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;		
	}

	private int processDSUBIInst(DSUBI dsubi) {
		// Local variables.
		String opCode = dsubi.getOpcode().name();
		int instOrder = dsubi.getOrder();
		String src1n = "";

		if (dsubi.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = dsubi.getSrc1().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(dsubi.getSrc1(), dsubi);
			
			// Check for RAW hazard.
			if (src1) {
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(dsubi.getSrc1().getId());
				dsubi.getSrc1().setValue(src1v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + dsubi.getSrc1().getId().name());
				log.debug("Immidiate value is: " + opCode + "[" + instOrder + "]" + dsubi.getValue());
				
				// Clear RAW hazard is already set.
				dsubi.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				dsubi.setState(State.READ);				
			} else {
				dsubi.setRawHazard();
				ResultMgr.getInstance().setRawHazard(dsubi.getRawInst());

				if (!src1) {
					src1n = dsubi.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " " + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;		
	}

	private int processDSUBInst(DSUB dsub) {
		// Local variables.
		String opCode = dsub.getOpcode().name();
		int instOrder = dsub.getOrder();
		String src1n = "";
		String src2n = "";

		if (dsub.getState().equals(State.ISSUE_COMP)) {

			// Self reservation is not a RAW hazard. 
			//boolean src1 = !dsub.getSrc1().isBusy(dsub.hashCode());
			//boolean src2 = !dsub.getSrc2().isBusy(dsub.hashCode());
			boolean src1 = !dataHaz.checkRAWHazard(dsub.getSrc1(), dsub);
			boolean src2 = !dataHaz.checkRAWHazard(dsub.getSrc2(), dsub);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(dsub.getSrc1().getId());
				Long src2v = regMem.getRegValue(dsub.getSrc2().getId());

				dsub.getSrc1().setValue(src1v.intValue());
				dsub.getSrc2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + dsub.getSrc1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + dsub.getSrc2().getId().name());
				
				// Clear RAW hazard is already set.
				dsub.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				dsub.setState(State.READ);				
			} else {
				dsub.setRawHazard();
				ResultMgr.getInstance().setRawHazard(dsub.getRawInst());

				if (!src1) {
					src1n = dsub.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = dsub.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}

			}
		} 
		
		return 0;		
	}

	private int processDADDIInst(DADDI daddi) {
		// Local variables.
		String opCode = daddi.getOpcode().name();
		int instOrder = daddi.getOrder();
		String src1n = "";

		if (daddi.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = daddi.getSrc1().isIdle();
			//boolean src1 = !daddi.getSrc1().isBusy(daddi.hashCode());
			boolean src1 = !dataHaz.checkRAWHazard(daddi.getSrc1(), daddi);

			// Check for RAW hazard.
			if (src1) {
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(daddi.getSrc1().getId());
				daddi.getSrc1().setValue(src1v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + daddi.getSrc1().getId().name());
				log.debug("Immidiate value is: " + opCode + "[" + instOrder + "]" + daddi.getValue());
				
				// Clear RAW hazard is already set.
				daddi.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				daddi.setState(State.READ);				
			} else {
				daddi.setRawHazard();
				ResultMgr.getInstance().setRawHazard(daddi.getRawInst());

				if (!src1) {
					src1n = daddi.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " " + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;
	}

	private int processDADDInst(DADD dadd) {
		// Local variables.
		String opCode = dadd.getOpcode().name();
		int instOrder = dadd.getOrder();
		String src1n = "";
		String src2n = "";

		if (dadd.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = dadd.getSrc1().isIdle();
			//boolean src2 = dadd.getSrc2().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(dadd.getSrc1(), dadd);
			boolean src2 = !dataHaz.checkRAWHazard(dadd.getSrc2(), dadd);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				
				// Read the values of source operands.
				Long src1v = regMem.getRegValue(dadd.getSrc1().getId());
				Long src2v = regMem.getRegValue(dadd.getSrc2().getId());

				dadd.getSrc1().setValue(src1v.intValue());
				dadd.getSrc2().setValue(src2v.intValue());

				log.debug("opCode " + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Source1 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + dadd.getSrc1().getId().name());
				log.debug("Source2 value is: " + opCode + "[" + instOrder + "]" + src1v.intValue() + " Register: " + dadd.getSrc2().getId().name());
				
				// Clear RAW hazard is already set.
				dadd.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				dadd.setState(State.READ);				
			} else {
				dadd.setRawHazard();
				ResultMgr.getInstance().setRawHazard(dadd.getRawInst());

				if (!src1) {
					src1n = dadd.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = dadd.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		} 
		
		return 0;
	}

	private int processDIVDInst(DIVD divd) {
		// Local variables.
		String opCode = divd.getOpcode().name();
		int instOrder = divd.getOrder();
		String src1n = "";
		String src2n = "";

		if (divd.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = divd.getSrc1().isIdle();
			//boolean src2 = divd.getSrc2().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(divd.getSrc1(), divd);
			boolean src2 = !dataHaz.checkRAWHazard(divd.getSrc2(), divd);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				// This is Floating Point DIV. We are not going to monitor
				// any of the status of FP register or related instructions.
				// ...
				
				// Clear RAW hazard is already set.
				divd.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				divd.setState(State.READ);				
			} else {
				divd.setRawHazard();
				ResultMgr.getInstance().setRawHazard(divd.getRawInst());

				if (!src1) {
					src1n = divd.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = divd.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		}
		
		return 0;		
	}

	private int processMULDInst(MULD muld) {
		// Local variables.
		String opCode = muld.getOpcode().name();
		int instOrder = muld.getOrder();
		String src1n = "";
		String src2n = "";

		if (muld.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = muld.getSrc1().isIdle();
			//boolean src2 = muld.getSrc2().isIdle();
			//boolean src1 = !muld.getSrc1().isBusy(muld.hashCode());
			//boolean src2 = !muld.getSrc2().isBusy(muld.hashCode());
			boolean src1 = !dataHaz.checkRAWHazard(muld.getSrc1(), muld);
			boolean src2 = !dataHaz.checkRAWHazard(muld.getSrc2(), muld);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				// This is Floating Point MUL. We are not going to monitor
				// any of the status of FP register or related instructions.
				// ...
				
				// Clear RAW hazard is already set.
				muld.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				muld.setState(State.READ);				
			} else {
				muld.setRawHazard();
				ResultMgr.getInstance().setRawHazard(muld.getRawInst());

				if (!src1) {
					src1n = muld.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = muld.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		}
		
		return 0;	
	}

	private int processSUBDInst(SUBD subd) {
		// Local variables.
		String opCode = subd.getOpcode().name();
		int instOrder = subd.getOrder();
		String src1n = "";
		String src2n = "";

		if (subd.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = subd.getSrc1().isIdle();
			//boolean src2 = subd.getSrc2().isIdle();
			boolean src1 = !dataHaz.checkRAWHazard(subd.getSrc1(), subd);
			boolean src2 = !dataHaz.checkRAWHazard(subd.getSrc2(), subd);
			
			// Check for RAW hazard.
			if (src1 && src2) {
				// This is Floating Point SUB. We are not going to monitor
				// any of the status of FP register or related instructions.
				// ...
				
				// Clear RAW hazard is already set.
				subd.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				subd.setState(State.READ);				
			} else {
				subd.setRawHazard();
				ResultMgr.getInstance().setRawHazard(subd.getRawInst());

				if (!src1) {
					src1n = subd.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = subd.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}
			}
		}
		
		return 0;
	}

	private int processSWInst(SW sw) {
		
		// Local variables.
		String opCode = sw.getOpcode().name();
		int instOrder = sw.getOrder();
		
		// Check if there is RAW hazard possible, and stall if required.
		if (sw.getState().equals(State.ISSUE_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			//boolean src = sw.getSource().isIdle();
			if (sw.getBaseAddr().isIdle()) {
				
				// Read the values of source operands.
				Long rval = regMem.getRegValue(sw.getBaseAddr().getId());
				sw.getBaseAddr().setValue(rval.intValue());

				log.debug("Base address for " + opCode + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Base address for " + opCode + "[" + instOrder + "] " + sw.getBaseAddr().getName() + " " + sw.getBaseAddr().getValue());
				
				// Clear RAW hazard is already set.
				sw.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				sw.setState(State.READ);				
			} else {
				sw.setRawHazard();
				resultMgr.setRawHazard(sw.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + "Register " + sw.getBaseAddr().getName() + " is already busy. RAW hazard detected.");
			}
		} 

		return 0;				
	}

	private int processSDInst(SD sd) {
		
		// Local variables.
		String opCode = sd.getOpcode().name();
		int instOrder = sd.getOrder();
		
		// Check if there is RAW hazard possible, and stall if required.
		if (sd.getState().equals(State.ISSUE_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			//boolean src = sd.getSource().isIdle();
			//if (sd.getBaseAddr().isIdle()) {
			if (!sd.getSource().isBusy(sd.hashCode())) {
				
				// Read the values of source operands.
				Long rval = regMem.getRegValue(sd.getBaseAddr().getId());
				sd.getBaseAddr().setValue(rval.intValue());

				log.debug("Base address for " + opCode + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Base address for " + opCode + "[" + instOrder + "] " + sd.getBaseAddr().getName() + " " + sd.getBaseAddr().getValue());
				
				// Clear RAW hazard is already set.
				sd.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				sd.setState(State.READ);				
			} else {
				sd.setRawHazard();
				resultMgr.setRawHazard(sd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + "Register " + sd.getSource().getName() + " is already busy. RAW hazard detected.");
			}
		} 

		return 0;
	}

	private int processLDInst(LD ld) {
		// Local variables.
		String opCode = ld.getOpcode().name();
		int instOrder = ld.getOrder();
		
		// Check if there is RAW hazard possible, and stall if required.
		if (ld.getState().equals(State.ISSUE_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			boolean src1 = !dataHaz.checkRAWHazard(ld.getBaseAddr(), ld);
			
			//if (ld.getBaseAddr().isIdle()) {
			if (src1) {	
				// Read the values of source operands.
				Long rval = regMem.getRegValue(ld.getBaseAddr().getId());
				ld.getBaseAddr().setValue(rval.intValue());

				log.debug("Base address for " + opCode + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Base address for " + opCode + "[" + instOrder + "] " + ld.getBaseAddr().getName() + " " + ld.getBaseAddr().getValue());
				
				// Clear RAW hazard is already set.
				ld.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				ld.setState(State.READ);				
			} else {
				ld.setRawHazard();
				resultMgr.setRawHazard(ld.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + "Register " + ld.getBaseAddr().getName() + " is already busy. RAW hazard detected.");
			}
		} 

		return 0;		
	}

	private int processADDDInst(ADDD addd) {
		// Local variables.
		String opCode = addd.getOpcode().name();
		int instOrder = addd.getOrder();
		String src1n = "";
		String src2n = "";

		if (addd.getState().equals(State.ISSUE_COMP)) {

			//boolean src1 = addd.getSrc1().isIdle();
			//boolean src2 = addd.getSrc2().isIdle();
			//boolean src1 = !addd.getSrc1().isBusy(addd.hashCode());
			//boolean src2 = !addd.getSrc2().isBusy(addd.hashCode());
			//boolean src2 = !(addd.getSrc2().isBusy(addd.hashCode()) || addd.getSrc2().isReading());
			boolean src1 = !dataHaz.checkRAWHazard(addd.getSrc1(), addd);
			boolean src2 = !dataHaz.checkRAWHazard(addd.getSrc2(), addd);

			// Check for RAW hazard.
			if (src1 && src2) {
				// This is Floating Point ADD. We are not going to monitor
				// any of the status of FP register or related instructions.
				// ...
				
				// Clear RAW hazard is already set.
				addd.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				addd.setState(State.READ);				
			} else {
				addd.setRawHazard();
				ResultMgr.getInstance().setRawHazard(addd.getRawInst());

				if (!src1) {
					src1n = addd.getSrc1().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src1n + " is already busy. RAW hazard detected.");
				}
				if (!src2) {
					src2n = addd.getSrc2().getName();
					log.debug(opCode  + "[" + instOrder + "]" + " Source registers " + 
							src2n + " is already busy. RAW hazard detected.");
				}

			}
		} 
		
		return 0;
	}

	private int processLWInst(LW lw) {
		
		// Local variables.
		String opCode = lw.getOpcode().name();
		int instOrder = lw.getOrder();
		
		// Check if there is RAW hazard possible, and stall if required.
		if (lw.getState().equals(State.ISSUE_COMP)) {

			// Check if the source operands are already BUSY.
			// There are no 'source' operands in LD/LW/SD/SW.
			// Offset is already populated in the instruction.
			boolean src1 = !dataHaz.checkRAWHazard(lw.getBaseAddr(), lw);
			
			//if (lw.getBaseAddr().isIdle()) {
			if (src1) {
				
				// Read the values of source operands.
				Long rval = regMem.getRegValue(lw.getBaseAddr().getId());
				lw.getBaseAddr().setValue(rval.intValue());

				log.debug("Base address for " + opCode + "[" + instOrder + "]" + " instruction read from Register Memory.");
				log.debug("Base address for " + opCode + "[" + instOrder + "] " + lw.getBaseAddr().getName() + " " + lw.getBaseAddr().getValue());
				
				// Clear RAW hazard is already set.
				lw.clearRawHazard();
				
				// Mark the instruction as READing.
				// This is to make sure this is not handled in next tick.
				lw.setState(State.READ);				
			} else {
				lw.setRawHazard();
				ResultMgr.getInstance().setRawHazard(lw.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + "Register " + lw.getBaseAddr().getName() + " is already busy. RAW hazard detected.");
			}
		} 

		return 0;
	}

	private void cleanWARHazard(Instruction inst) {
		
		switch (inst.getOpcode()) {
		case ADDD:
			ADDD addd = (ADDD) inst;
			addd.clearWARState();
			break;
		case AND:
			AND and = (AND) inst;
			and.clearWARState();
			break;
		case ANDI:
			ANDI andi = (ANDI) inst;
			andi.clearWARState();
			break;
		case BEQ:
			//BEQ beq= (BEQ) inst;
			//beq.clearWARState();
			break;
		case BNE:
			//BNE bne = (BNE) inst;
			//bne.clearWARState();
			break;
		case DADD:
			DADD dadd = (DADD) inst;
			dadd.clearWARState();
			break;
		case DADDI:
			DADDI daddi = (DADDI) inst;
			daddi.clearWARState();
			break;
		case DIVD:
			DIVD divd = (DIVD) inst;
			divd.clearWARState();
			break;
		case DSUB:
			DSUB dsub = (DSUB) inst;
			dsub.clearWARState();
			break;
		case DSUBI:
			DSUBI dsubi = (DSUBI) inst;
			dsubi.clearWARState();
			break;
		case HLT:
			//HLT hlt = (HLT) inst;
			//hlt.clearWARState();
			break;
		case J:
			//JUMP jump = (JUMP) inst;
			//jump.clearWARState();
			break;
		case LD:
			LD ld= (LD) inst;
			ld.clearWARState();
			break;
		case LW:
			LW lw = (LW) inst;
			lw.clearWARState();
			break;
		case MULD:
			MULD muld= (MULD) inst;
			muld.clearWARState();
			break;
		case OR:
			OR or = (OR) inst;
			or.clearWARState();
			break;
		case ORI:
			ORI ori = (ORI) inst;
			ori.clearWARState();
			break;
		case SD:
			//SD sd = (SD) inst;
			//sd.clearWARState();
			break;
		case SUBD:
			SUBD subd = (SUBD) inst;
			subd.clearWARState();
			break;
		case SW:
			//SW sw = (SW) inst;
			//sw.clearWARState();
			break;
		case DEFAULT:
			break;
		default:
			break;
		}
	}
	
	public void enQueue(Instruction inst) {
		instQueue.add(inst);
	}
	
	public float getClock() {
		return clockCounter;
	}

	public void incrementClock() {
		ReadStage.clockCounter += CPUConstants.CLOCK_STEP;
	}

	@Override
	public boolean isEmpty() {
		if (instQueue.isEmpty()) {
			log.debug("READ stage is empty");
			return true;
		} else {
			log.debug("READ stage is still running");
			return false;
		}
	}

}
