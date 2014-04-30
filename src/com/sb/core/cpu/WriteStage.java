package com.sb.core.cpu;

import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sb.core.fu.FPAdder;
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
import com.sb.writer.output.ResultMgr;

public class WriteStage extends PipelineStage {

	// Single instance.
	private static WriteStage instance = null;
	
	private Vector<Instruction> instQueue;
	private static float clockCounter = 1;
	Logger log = Logger.getLogger(this.getClass().getName());
	private ResultMgr resultMgr = ResultMgr.getInstance();
	private FuncUnitController fuCntl = FuncUnitController.getInstance();
	private DataHazard dataHaz = DataHazard.getInstance();

	protected WriteStage() {
		instQueue = new Vector<Instruction>(5);
	}

	public static WriteStage getInstance() {
		if (instance == null) {
			instance = new WriteStage();
		}
		return instance;
	}
	
	@Override
	public int execute() {
		
		// Process all the instructions in the queue
		ListIterator<Instruction> itr = instQueue.listIterator();
		log.debug("WRITE Instruction queue size is: " + instQueue.size());

		for (int index = 0; itr.hasNext(); index++) {
			
			Instruction inst = itr.next();
			
			String opCode = inst.getOpcode().name();
			int instOrder = inst.getOrder();
			
			log.debug("Instruction being WRITEn : " + opCode + "[" + instOrder + "]");
			log.debug("Instruction " + opCode + "[" + instOrder + "]" + " state is: " + inst.getState().name());
			
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
			
			// Check if the clock cycle is complete.
			if (inst.getWriteCounter() >= inst.getWriteCycles()) {
				// Set the next state for instruction. Skip if hazard is detected.
				if (!inst.isWarHazard()) {
					cleanWAWHazard(inst);
					inst.setState(State.WRITE_COMP);
					log.debug(opCode + "[" + instOrder + "]" + " WRITE stage completed at clock: "	+ this.getClock());
					
					// Capture the statistics on instruction.
					resultMgr.setWriteClock(inst.getRawInst(), this.getClock());
					
					// Clean up.
					inst.releaseResource();
				}
			}
			
			// Increment Issue counter, no matter what is outcome.
			// This stage visit should get recorded. 
			log.debug("Incremented " + opCode + "[" + instOrder + "]" + " EXEC counter.");
			log.debug(opCode + "[" + instOrder + "]" + " Write Cycles are: " + inst.getWriteCycles());
			log.debug(opCode + "[" + instOrder + "]" + " Write Counter is: " + inst.getWriteCounter());
			inst.IncrWriteCounter();
		}
		
		// Code to push WRITE completed instruction to next stage.
		for (int i = 0; i < instQueue.size(); i++) {
			Instruction in = instQueue.get(i);
			String opCode = in.getOpcode().name();
			int instOrder = in.getOrder();
			
			if (in.getState().equals(State.WRITE_COMP)) {
				in = instQueue.remove(i);
				log.debug("Instruction " + opCode + "[" + instOrder + "]" + " finished the WRITE Stage.");
				log.debug("Removing instruction " + opCode + "[" + instOrder + "]" + " from WRITE stage.");
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
		
		if (ori.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!ori.getDestination().isBusy(ori)) {	
			if (!dataHaz.checkWARHazard(ori.getDestination(), ori)) {				
				// Write the result back to register.
				ori.getDestination().setValue(ori.getResult());
				ori.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + ori.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				ori.setState(State.WRITE);				
			} else {
				ori.setWarHazard();
				ResultMgr.getInstance().setWarHazard(ori.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + ori.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}
		
		return 0;
	}

	private int processORInst(OR or) {
		// Local variables.
		String opCode = or.getOpcode().name();
		int instOrder = or.getOrder();
		
		if (or.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!or.getDestination().isBusy(or)) {
			if (!dataHaz.checkWARHazard(or.getDestination(), or)) {
	
				// Write the result back to register.
				or.getDestination().setValue(or.getResult());
				or.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + or.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				or.setState(State.WRITE);				
			} else {
				or.setWarHazard();
				ResultMgr.getInstance().setWarHazard(or.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + or.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}
		
		return 0;		
	}

	private int processANDIInst(ANDI andi) {
		// Local variables.
		String opCode = andi.getOpcode().name();
		int instOrder = andi.getOrder();
		
		if (andi.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!andi.getDestination().isBusy(andi)) {
			if (!dataHaz.checkWARHazard(andi.getDestination(), andi)) {
		
				// Write the result back to register.
				andi.getDestination().setValue(andi.getResult());
				andi.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + andi.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				andi.setState(State.WRITE);				
			} else {
				andi.setWarHazard();
				ResultMgr.getInstance().setWarHazard(andi.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + andi.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}
		
		return 0;		
	}

	private int processANDInst(AND and) {
		// Local variables.
		String opCode = and.getOpcode().name();
		int instOrder = and.getOrder();
		
		if (and.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!and.getDestination().isBusy(and)) {
			if (!dataHaz.checkWARHazard(and.getDestination(), and)) {
	
				// Write the result back to register.
				and.getDestination().setValue(and.getResult());
				and.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + and.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				and.setState(State.WRITE);				
			} else {
				and.setWarHazard();
				ResultMgr.getInstance().setWarHazard(and.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + and.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}
	
		return 0;			
	}

	private int processDSUBIInst(DSUBI dsubi) {
		// Local variables.
		String opCode = dsubi.getOpcode().name();
		int instOrder = dsubi.getOrder();
		
		if (dsubi.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!dsubi.getDestination().isBusy(dsubi)) {
			if (!dataHaz.checkWARHazard(dsubi.getDestination(), dsubi)) {
	
				// Write the result back to register.
				dsubi.getDestination().setValue(dsubi.getResult());
				dsubi.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + dsubi.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				dsubi.setState(State.WRITE);				
			} else {
				dsubi.setWarHazard();
				ResultMgr.getInstance().setWarHazard(dsubi.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + dsubi.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;			
	}

	private int processDSUBInst(DSUB dsub) {
		// Local variables.
		String opCode = dsub.getOpcode().name();
		int instOrder = dsub.getOrder();
		
		if (dsub.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!dsub.getDestination().isBusy(dsub)) {
			if (!dataHaz.checkWARHazard(dsub.getDestination(), dsub)) {
		
				// Write the result back to register.
				dsub.getDestination().setValue(dsub.getResult());
				dsub.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + dsub.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				dsub.setState(State.WRITE);				
			} else {
				dsub.setWarHazard();
				ResultMgr.getInstance().setWarHazard(dsub.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + dsub.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;		
	}

	private int processDADDIInst(DADDI daddi) {
		// Local variables.
		String opCode = daddi.getOpcode().name();
		int instOrder = daddi.getOrder();
		
		if (daddi.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!daddi.getDestination().isBusy(daddi)) {
			if (!dataHaz.checkWARHazard(daddi.getDestination(), daddi)) {
	
				// Write the result back to register.
				daddi.getDestination().setValue(daddi.getResult());
				daddi.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + daddi.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				daddi.setState(State.WRITE);				
			} else {
				daddi.setWarHazard();
				ResultMgr.getInstance().setWarHazard(daddi.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + daddi.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;		
	}

	private int processDADDInst(DADD dadd) {
		// Local variables.
		String opCode = dadd.getOpcode().name();
		int instOrder = dadd.getOrder();
		
		if (dadd.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!dadd.getDestination().isBusy(dadd)) {
			if (!dataHaz.checkWARHazard(dadd.getDestination(), dadd)) {
	
				// Write the result back to register.
				dadd.getDestination().setValue(dadd.getResult());
				dadd.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + dadd.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				dadd.setState(State.WRITE);				
			} else {
				dadd.setWarHazard();
				ResultMgr.getInstance().setWarHazard(dadd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + dadd.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;		
	}

	private int processDIVDInst(DIVD divd) {
		// Local variables.
		String opCode = divd.getOpcode().name();
		int instOrder = divd.getOrder();
		
		if (divd.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!divd.getDestination().isBusy(divd)) {
			if (!dataHaz.checkWARHazard(divd.getDestination(), divd)) {
		
				// Write the result back to register. This is floating point SUB. 
				// No need to worry about the result storage.
				//muld.getDestination().setValue(opCode.getResult());
				divd.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + divd.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				divd.setState(State.WRITE);				
			} else {
				divd.setWarHazard();
				ResultMgr.getInstance().setWarHazard(divd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + divd.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}
		
		return 0;		
	}

	private int processMULDInst(MULD muld) {
		// Local variables.
		String opCode = muld.getOpcode().name();
		int instOrder = muld.getOrder();
		
		if (muld.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!muld.getDestination().isBusy(muld)) {
			if (!dataHaz.checkWARHazard(muld.getDestination(), muld)) {
		
				// Write the result back to register. This is floating point SUB. 
				// No need to worry about the result storage.
				//muld.getDestination().setValue(opCode.getResult());
				muld.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + muld.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				muld.setState(State.WRITE);				
			} else {
				muld.setWarHazard();
				ResultMgr.getInstance().setWarHazard(muld.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + muld.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processSUBDInst(SUBD subd) {
		// Local variables.
		String opCode = subd.getOpcode().name();
		int instOrder = subd.getOrder();
		
		if (subd.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!subd.getDestination().isBusy(subd)) {
			if (!dataHaz.checkWARHazard(subd.getDestination(), subd)) {
		
				// Write the result back to register. This is floating point SUB. 
				// No need to worry about the result storage.
				//subd.getDestination().setValue(opCode.getResult());
				subd.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + subd.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				subd.setState(State.WRITE);				
			} else {
				subd.setWarHazard();
				ResultMgr.getInstance().setWarHazard(subd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + subd.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processSWInst(SW sw) {
		// Local variables.
		String opCode = sw.getOpcode().name();
		int instOrder = sw.getOrder();
		
		if (sw.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			if (!sw.getSource().isBusy(sw)) {
				
				// Write the result back to memory.
				// This is storing the FLOAT, we do not really care but still done.
				int memAddr = sw.getEffectiveAddr();
				DataMemory.getInstance().setDataRelative(memAddr, sw.getSource().getValue());

				sw.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to memory: " + memAddr);
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				sw.setState(State.WRITE);				
			} else {
				sw.setWarHazard();
				ResultMgr.getInstance().setWarHazard(sw.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + sw.getSource().getName() + " already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processSDInst(SD sd) {
		// Local variables.
		String opCode = sd.getOpcode().name();
		int instOrder = sd.getOrder();
		
		if (sd.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			if (!sd.getSource().isBusy(sd)) {
				
				// Write the result back to memory.
				// This is storing the FLOAT, we do not really care but still done.
				int memAddr = sd.getEffectiveAddr();
				//DataMemory.getInstance().setDataRelative(memAddr, (int)sd.getSource().getValue());

				sd.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to memory: " + memAddr);
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				sd.setState(State.WRITE);				
			} else {
				sd.setWarHazard();
				ResultMgr.getInstance().setWarHazard(sd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + sd.getSource().getName() + " already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processLDInst(LD ld) {
		// Local variables.
		String opCode = ld.getOpcode().name();
		int instOrder = ld.getOrder();

		if (ld.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!ld.getDestination().isBusy(ld)) {
			if (!dataHaz.checkWARHazard(ld.getDestination(), ld)) {
		
				// Write the result back to register.
				ld.getDestination().setValue(ld.getResult());
				ld.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + ld.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				ld.setState(State.WRITE);				
			} else {
				ld.setWarHazard();
				ResultMgr.getInstance().setWarHazard(ld.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + ld.getDestination().getName() + " already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processADDDInst(ADDD addd) {
		// Local variables.
		String opCode = addd.getOpcode().name();
		int instOrder = addd.getOrder();
		
		if (addd.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!addd.getDestination().isBusy() || (addd.getOrder() == 1)) {
			//if (!addd.getDestination().isBusy(addd)) {
			if (!dataHaz.checkWARHazard(addd.getDestination(), addd)) {				
				// Write the result back to register.
				//addd.getDestination().setValue(opCode.getResult());
				addd.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + addd.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				addd.setState(State.WRITE);				
			} else {
				addd.setWarHazard();
				ResultMgr.getInstance().setWarHazard(addd.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + addd.getDestination().getName() + 
						" already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private int processLWInst(LW lw) {
		
		// Local variables.
		String opCode = lw.getOpcode().name();
		int instOrder = lw.getOrder();
		
		if (lw.getState().equals(State.EXEC_COMP)) {

			// Check if the register is already busy OR 
			// if its the first instruction of the program.
			// This is to check WAR hazard.
			//if (!lw.getDestination().isBusy(lw)) {
				if (!dataHaz.checkWARHazard(lw.getDestination(), lw)) {
		
				// Write the result back to register.
				lw.getDestination().setValue(lw.getResult());
				lw.clearWarHazard();
				log.debug(opCode + "[" + instOrder + "]" + " Writing result back to register: " + lw.getDestination().getName());
				
				// Mark the instruction as WRITE.
				// This is to make sure this is not handled in next tick.
				lw.setState(State.WRITE);				
			} else {
				lw.setWarHazard();
				ResultMgr.getInstance().setWarHazard(lw.getRawInst());
				log.debug(opCode + "[" + instOrder + "]" + " Register: " + lw.getDestination().getName() + " already busy - WAR hazard detected.");
			}
		}

		return 0;
	}

	private void cleanWAWHazard(Instruction inst) {
		
		switch (inst.getOpcode()) {
		case ADDD:
			ADDD addd = (ADDD) inst;
			addd.clearWAWState();
			break;
		case AND:
			AND and = (AND) inst;
			and.clearWAWState();
			break;
		case ANDI:
			ANDI andi = (ANDI) inst;
			andi.clearWAWState();
			break;
		case BEQ:
			//BEQ beq= (BEQ) inst;
			//beq.clearWAWState();
			break;
		case BNE:
			//BNE bne = (BNE) inst;
			//bne.clearWAWState();
			break;
		case DADD:
			DADD dadd = (DADD) inst;
			dadd.clearWAWState();
			break;
		case DADDI:
			DADDI daddi = (DADDI) inst;
			daddi.clearWAWState();
			break;
		case DIVD:
			DIVD divd = (DIVD) inst;
			divd.clearWAWState();
			break;
		case DSUB:
			DSUB dsub = (DSUB) inst;
			dsub.clearWAWState();
			break;
		case DSUBI:
			DSUBI dsubi = (DSUBI) inst;
			dsubi.clearWAWState();
			break;
		case HLT:
			//HLT hlt = (HLT) inst;
			//hlt.clearWAWState();
			break;
		case J:
			//JUMP jump = (JUMP) inst;
			//jump.clearWAWState();
			break;
		case LD:
			LD ld= (LD) inst;
			ld.clearWAWState();
			break;
		case LW:
			LW lw = (LW) inst;
			lw.clearWAWState();
			break;
		case MULD:
			MULD muld= (MULD) inst;
			muld.clearWAWState();
			break;
		case OR:
			OR or = (OR) inst;
			or.clearWAWState();
			break;
		case ORI:
			ORI ori = (ORI) inst;
			ori.clearWAWState();
			break;
		case SD:
			//SD sd = (SD) inst;
			//sd.clearWAWState();
			break;
		case SUBD:
			SUBD subd = (SUBD) inst;
			subd.clearWAWState();
			break;
		case SW:
			//SW sw = (SW) inst;
			//sw.clearWAWState();
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
		WriteStage.clockCounter += CPUConstants.CLOCK_STEP;
	}

	@Override
	public boolean isEmpty() {
		if (instQueue.isEmpty()) {
			log.debug("WRITE stage is empty");
			return true;
		} else {
			log.debug("WRITE stage is still running");
			return false;
		}
	}
}
