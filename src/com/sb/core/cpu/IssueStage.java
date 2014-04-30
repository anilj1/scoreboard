package com.sb.core.cpu;

import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sb.core.fu.FPAdder;
import com.sb.core.fu.FPDivider;
import com.sb.core.fu.FPMultiplier;
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
import com.sb.writer.output.ResultMgr;

public class IssueStage extends PipelineStage {

	// Single instance.
	private static IssueStage instance = null;

	private Vector<Instruction> instQueue = null;
	private static float clockCounter = 1;
	private FuncUnitController fuCntl = FuncUnitController.getInstance();
	Logger log = Logger.getLogger(this.getClass().getName());
	private ResultMgr resultMgr = ResultMgr.getInstance();
	private boolean issueStgStalled = false;
	private DataHazard dataHaz = DataHazard.getInstance();
	
	private Instruction stallCausedBy = null;
	private Instruction stallReleasedBy = null;
	private Instruction prevStallCausedBy = null;

	protected IssueStage() {
		instQueue = new Vector<Instruction>(5);
	}

	public static IssueStage getInstance() {
		if (instance == null) {
			instance = new IssueStage();
		}
		return instance;
	}

	@Override
	public int execute() {

		// Process all the instructions in the queue
		ListIterator<Instruction> itr = instQueue.listIterator();
		log.debug("ISSUE Instruction queue size is: " + instQueue.size());

		for (int index = 0; itr.hasNext(); index++) {
			Instruction inst = itr.next();

			String opCode = inst.getOpcode().name();
			int instOrder = inst.getOrder();

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
			log.debug("Instruction " + opCode + "[" + instOrder + "]"
					+ " post processed State is: " + inst.getState().name());

			// Check for stage completion.
			if (inst.getIssueCounter() >= inst.getIssueCycles()) {
				// Do not set the next state for instruction
				// if it is stalled due to structural hazard.
				if (!inst.isStructHazard() && !inst.isWawHazard()) {
					inst.setState(State.ISSUE_COMP);
					log.debug(opCode + "[" + instOrder + "]"
							+ " ISSUE stage completed at clock: "
							+ this.getClock());
					resultMgr.setIssueClock(inst.getRawInst(), this.getClock());
				}
			}

			// Increment Issue counter, no matter what is outcome.
			// This stage visit should get recorded.
			log.debug("Incremented " + opCode + "[" + instOrder + "]"
					+ " ISSUE counter.");
			
			log.debug(opCode + "[" + instOrder + "]" + " Issue Cycles are: "
					+ inst.getIssueCycles());
			
			log.debug(opCode + "[" + instOrder + "]" + " Issue Counter is: "
					+ inst.getIssueCounter());
			
			inst.IncrIssueCounter();
		} // End of For Loop.

		// Test code to push ISSUE completed instruction to next stage.
		for (int i = 0; i < instQueue.size(); i++) {
			Instruction in = instQueue.get(i);
			String opCode = in.getOpcode().name();
			int instOrder = in.getOrder();

			// Increment the inst clock counter.
			if (in.getState() == State.ISSUE_COMP) {
				// Post the instruction to READ stage.
				in = instQueue.remove(i);
				ReadStage.getInstance().enQueue(in);
				log.debug("Instruction " + opCode + "[" + instOrder + "]"
						+ " finished the ISSUE Stage.");
				log.debug("Pushing instruction " + opCode + "[" + instOrder
						+ "]" + " to READ stage.");
			}
		}

		// Clock the stage counter.
		this.incrementClock();

		// Return success.
		return 0;
	}

	private int processBNEInst(BNE bne) {
		// TODO Auto-generated method stub
		// Local variable.
		String opCode = bne.getOpcode().name();
		int instOrder = bne.getOrder();

		if (bne.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (ori.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(bne.getReg1(), bne)) {
					
					// Add source Reg for checking RAW hazard.
					dataHaz.addToRawBlock(bne.getReg2(), bne);
					//dataHaz.addToWarBlock(ori.getSrc1(), ori);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = bne;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(bne);
					bne.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//ori.getDestination().setBusy(ori);
					
					// Clear the structural hazard flag, if it was set earlier.
					bne.clearStructHazard();
					bne.clearWawHazard();
					
					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					bne.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ bne.getReg1().getName());
				} else {
					// Return stalling of the state.
					bne.setWawHazard();
					resultMgr.setWawHazard(bne.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = bne;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ bne.getReg1().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				bne.setStructHazard();
				resultMgr.setStructHazard(bne.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = bne;
				bne.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processBEQInst(BEQ beq) {
		// Local variable.
		String opCode = beq.getOpcode().name();
		int instOrder = beq.getOrder();

		if (beq.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (ori.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(beq.getReg1(), beq)) {
					
					// Add source Reg for checking RAW hazard.
					dataHaz.addToRawBlock(beq.getReg2(), beq);
					//dataHaz.addToWarBlock(ori.getSrc1(), ori);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = beq;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(beq);
					beq.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//ori.getDestination().setBusy(ori);
					
					// Clear the structural hazard flag, if it was set earlier.
					beq.clearStructHazard();
					beq.clearWawHazard();
					
					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					beq.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ beq.getReg1().getName());
				} else {
					// Return stalling of the state.
					beq.setWawHazard();
					resultMgr.setWawHazard(beq.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = beq;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ beq.getReg1().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				beq.setStructHazard();
				resultMgr.setStructHazard(beq.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = beq;
				beq.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processORIInst(ORI ori) {
		// Local variable.
		String opCode = ori.getOpcode().name();
		int instOrder = ori.getOrder();

		if (ori.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (ori.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(ori.getDestination(), ori)) {
					
					// Add source Reg for checking RAW hazard.
					dataHaz.addToRawBlock(ori.getDestination(), ori);
					//dataHaz.addToWarBlock(ori.getSrc1(), ori);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = ori;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(ori);
					ori.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//ori.getDestination().setBusy(ori);
					
					// Clear the structural hazard flag, if it was set earlier.
					ori.clearStructHazard();
					ori.clearWawHazard();
					
					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					ori.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ ori.getDestination().getName());
				} else {
					// Return stalling of the state.
					ori.setWawHazard();
					resultMgr.setWawHazard(ori.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = ori;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ ori.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				ori.setStructHazard();
				resultMgr.setStructHazard(ori.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = ori;
				ori.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processORInst(OR or) {
		// Local variable.
		String opCode = or.getOpcode().name();
		int instOrder = or.getOrder();

		if (or.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (or.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(or.getDestination(), or)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(or.getDestination(), or);
					//dataHaz.addToWarBlock(or.getSrc1(), or);
					//dataHaz.addToWarBlock(or.getSrc2(), or);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = or;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(or);
					or.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//or.getDestination().setBusy(or);

					// Clear the structural hazard flag, if it was set earlier.
					or.clearStructHazard();
					or.clearWawHazard();
					
					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					or.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ or.getDestination().getName());
				} else {
					// Return stalling of the state.
					or.setWawHazard();
					resultMgr.setWawHazard(or.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = or;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ or.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				or.setStructHazard();
				resultMgr.setStructHazard(or.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = or;
				or.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processANDIInst(ANDI andi) {
		// Local variable.
		String opCode = andi.getOpcode().name();
		int instOrder = andi.getOrder();

		if (andi.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (andi.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(andi.getDestination(), andi)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(andi.getDestination(), andi);
					//dataHaz.addToWarBlock(andi.getSrc1(), andi);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = andi;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(andi);
					andi.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//andi.getDestination().setBusy(andi);

					// Clear the structural hazard flag, if it was set earlier.
					andi.clearStructHazard();
					andi.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					andi.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ andi.getDestination().getName());
				} else {
					// Return stalling of the state.
					andi.setWawHazard();
					resultMgr.setWawHazard(andi.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = andi;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ andi.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				andi.setStructHazard();
				resultMgr.setStructHazard(andi.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = andi;
				andi.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processANDInst(AND and) {
		// Local variable.
		String opCode = and.getOpcode().name();
		int instOrder = and.getOrder();

		if (and.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (and.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(and.getDestination(), and)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(and.getDestination(), and);
					//dataHaz.addToWarBlock(and.getSrc1(), and);
					//dataHaz.addToWarBlock(and.getSrc2(), and);

					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = and;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(and);
					and.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//and.getDestination().setBusy(and);

					// Clear the structural hazard flag, if it was set earlier.
					and.clearStructHazard();
					and.clearWawHazard();
					
					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					and.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ and.getDestination().getName());
				} else {
					// Return stalling of the state.
					and.setWawHazard();
					resultMgr.setWawHazard(and.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = and;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ and.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				and.setStructHazard();
				resultMgr.setStructHazard(and.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = and;
				and.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processDSUBIInst(DSUBI dsubi) {
		// Local variable.
		String opCode = dsubi.getOpcode().name();
		int instOrder = dsubi.getOrder();

		if (dsubi.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (dsubi.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(dsubi.getDestination(), dsubi)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(dsubi.getDestination(), dsubi);
					//dataHaz.addToWarBlock(dsubi.getSrc1(), dsubi);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = dsubi;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(dsubi);
					dsubi.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//dsubi.getDestination().setBusy(dsubi);

					// Clear the structural hazard flag, if it was set earlier.
					dsubi.clearStructHazard();
					dsubi.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					dsubi.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ dsubi.getDestination().getName());
				} else {
					// Return stalling of the state.
					dsubi.setWawHazard();
					resultMgr.setWawHazard(dsubi.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = dsubi;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ dsubi.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				dsubi.setStructHazard();
				resultMgr.setStructHazard(dsubi.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = dsubi;
				dsubi.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processDSUBInst(DSUB dsub) {
		// Local variable.
		String opCode = dsub.getOpcode().name();
		int instOrder = dsub.getOrder();

		if (dsub.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (dsub.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(dsub.getDestination(), dsub)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(dsub.getDestination(), dsub);
					//dataHaz.addToWarBlock(dsub.getSrc1(), dsub);
					//dataHaz.addToWarBlock(dsub.getSrc2(), dsub);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = dsub;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(dsub);
					dsub.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//dsub.getDestination().setBusy(dsub);

					// Clear the structural hazard flag, if it was set earlier.
					dsub.clearStructHazard();
					dsub.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					dsub.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ dsub.getDestination().getName());
				} else {
					// Return stalling of the state.
					dsub.setWawHazard();
					resultMgr.setWawHazard(dsub.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = dsub;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ dsub.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				dsub.setStructHazard();
				resultMgr.setStructHazard(dsub.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = dsub;
				dsub.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processDADDIInst(DADDI daddi) {

		// Local variable.
		String opCode = daddi.getOpcode().name();
		int instOrder = daddi.getOrder();

		if (daddi.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (daddi.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(daddi.getDestination(), daddi)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(daddi.getDestination(), daddi);
					//dataHaz.addToWarBlock(daddi.getSrc1(), daddi);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = daddi;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(daddi);
					daddi.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//daddi.getDestination().setBusy(daddi);

					// Clear the structural hazard flag, if it was set earlier.
					daddi.clearStructHazard();
					daddi.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					daddi.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ daddi.getDestination().getName());
				} else {
					// Return stalling of the state.
					daddi.setWawHazard();
					resultMgr.setWawHazard(daddi.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = daddi;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ daddi.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				daddi.setStructHazard();
				resultMgr.setStructHazard(daddi.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = daddi;
				daddi.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processDADDInst(DADD dadd) {
		// Local variable.
		String opCode = dadd.getOpcode().name();
		int instOrder = dadd.getOrder();

		if (dadd.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (dadd.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(dadd.getDestination(), dadd)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(dadd.getDestination(), dadd);
					//dataHaz.addToWarBlock(dadd.getSrc1(), dadd);
					//dataHaz.addToWarBlock(dadd.getSrc2(), dadd);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = dadd;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(dadd);
					dadd.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//dadd.getDestination().setBusy(dadd);

					// Clear the structural hazard flag, if it was set earlier.
					dadd.clearStructHazard();
					dadd.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					dadd.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ dadd.getDestination().getName());
				} else {
					// Return stalling of the state.
					dadd.setWawHazard();
					resultMgr.setWawHazard(dadd.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = dadd;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ dadd.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				dadd.setStructHazard();
				resultMgr.setStructHazard(dadd.getRawInst());
				this.issueStgStalled = false;
				stallCausedBy = dadd;
				dadd.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT FU not available.");
			}
		}

		return 0;
	}

	private int processDIVDInst(DIVD divd) {
		// Local variable.
		String opCode = divd.getOpcode().name();
		int instOrder = divd.getOrder();

		if (divd.getState().equals(State.FETCH_COMP)) {

			// Check if DIV FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the src registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			FPDivider fpDiv = fuCntl.getFPDivider();
			if (fpDiv != null) {
				//if (divd.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(divd.getDestination(), divd)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(divd.getDestination(), divd);
					//dataHaz.addToWarBlock(divd.getSrc1(), divd);
					//dataHaz.addToWarBlock(divd.getSrc2(), divd);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = divd;

					// Reserve the Integer Adder, and pass it to inst.
					fpDiv.setReserve(divd);
					divd.setFpDividerIndex(fpDiv.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//divd.getDestination().setBusy(divd);

					// Clear the structural hazard flag, if it was set earlier.
					divd.clearStructHazard();
					divd.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					divd.setState(State.ISSUE);
					log.debug("FP DIVIDER found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ divd.getDestination().getName());
				} else {
					// Return stalling of the state.
					divd.setWawHazard();
					resultMgr.setWawHazard(divd.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = divd;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ divd.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				divd.setStructHazard();
				resultMgr.setStructHazard(divd.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = divd;
				divd.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; FP DIVIDER not available.");
			}
		}

		return 0;
	}

	private int processMULDInst(MULD muld) {
		// Local variable.
		String opCode = muld.getOpcode().name();
		int instOrder = muld.getOrder();

		if (muld.getState().equals(State.FETCH_COMP)) {

			// Check if MULT FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the src registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			FPMultiplier fpMult = fuCntl.getFPMultiplier();
			if (fpMult != null) {
				//if (muld.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(muld.getDestination(), muld)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(muld.getDestination(), muld);
					//dataHaz.addToWarBlock(muld.getSrc1(), muld);
					//dataHaz.addToWarBlock(muld.getSrc2(), muld);

					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = muld;

					// Reserve the Integer Adder, and pass it to inst.
					fpMult.setReserve(muld);
					muld.setFpMultiplierIndex(fpMult.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//muld.getDestination().setBusy(muld);

					// Clear the structural hazard flag, if it was set earlier.
					muld.clearStructHazard();
					muld.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					muld.setState(State.ISSUE);
					log.debug("FP MULTIPLIER found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ muld.getDestination().getName());
				} else {
					// Return stalling of the state.
					muld.setWawHazard();
					resultMgr.setWawHazard(muld.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = muld;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ muld.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				muld.setStructHazard();
				resultMgr.setStructHazard(muld.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = muld;
				muld.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; FP MULTIPLIER not available.");
			}
		}

		return 0;
	}

	private int processSUBDInst(SUBD subd) {

		// Local variable.
		String opCode = subd.getOpcode().name();
		int instOrder = subd.getOrder();

		if (subd.getState().equals(State.FETCH_COMP)) {

			// Check if FP Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			FPAdder fpAdder = fuCntl.getFPAdder();
			if (fpAdder != null) {
				//if (subd.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(subd.getDestination(), subd)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(subd.getDestination(), subd);
					//dataHaz.addToWarBlock(subd.getSrc1(), subd);
					//dataHaz.addToWarBlock(subd.getSrc2(), subd);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = subd;

					// Reserve the Integer Adder, and pass it to inst.
					fpAdder.setReserve(subd);
					subd.setFpAdderIndex(fpAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//subd.getDestination().setBusy(subd);

					// Clear the structural hazard flag, if it was set earlier.
					subd.clearStructHazard();
					subd.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					subd.setState(State.ISSUE);
					log.debug("FP SUBTRACTER found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ subd.getDestination().getName());
				} else {
					// Return stalling of the state.
					subd.setWawHazard();
					resultMgr.setWawHazard(subd.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = subd;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ subd.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				subd.setStructHazard();
				resultMgr.setStructHazard(subd.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = subd;
				subd.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; FP SUBTRACTER not available.");
			}
		}

		return 0;
	}

	private int processSWInst(SW sw) {
		// Local variable.
		String opCode = sw.getOpcode().name();
		int instOrder = sw.getOrder();

		if (sw.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {

				// Resources are free now. Unstall the stage.
				this.issueStgStalled = false;
				stallReleasedBy = sw;

				// Reserve the Integer Adder, and pass it to inst.
				intAdder.setReserve(sw);
				sw.setIntAdderIndex(intAdder.getIndex());

				// Set the dest register busy to avoid WAW hazard.
				sw.getSource().setBusy(sw);

				// Clear the structural hazard flag, if it was set earlier.
				sw.clearStructHazard();
				sw.clearWawHazard();

				// Mark the instruction as ISSUEing.
				// This is to make sure this is not handled in next tick.
				sw.setState(State.ISSUE);
				log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
				log.debug("Source register " + opCode + "[" + instOrder + "]"
						+ " is: " + sw.getSource().getName());
			} else {
				// Return stalling of the state.
				sw.setStructHazard();
				resultMgr.setStructHazard(sw.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = sw;
				sw.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT Adder not available.");
			}
		}

		return 0;
	}

	private int processSDInst(SD sd) {
		// Local variable.
		String opCode = sd.getOpcode().name();
		int instOrder = sd.getOrder();

		if (sd.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			// if (intAdder != null && sd.getSource().isIdle()) {
			if (intAdder != null) {

				// Resources are free now. Unstall the stage.
				this.issueStgStalled = false;
				stallReleasedBy = sd;

				// Reserve the Integer Adder, and pass it to inst.
				intAdder.setReserve(sd);
				sd.setIntAdderIndex(intAdder.getIndex());

				// Clear the structural hazard flag, if it was set earlier.
				sd.clearStructHazard();
				sd.clearWawHazard();
				
				// Mark the instruction as ISSUEing.
				// This is to make sure this is not handled in next tick.
				sd.setState(State.ISSUE);
				log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
				log.debug("Source register " + opCode + "[" + instOrder + "]"
						+ " is: " + sd.getSource().getName());

			} else {
				// Return stalling of the state.
				sd.setStructHazard();
				resultMgr.setStructHazard(sd.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = sd;
				sd.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT Adder not available.");
			}
		}

		return 0;
	}

	private int processLDInst(LD ld) {
		// Local variable.
		String opCode = ld.getOpcode().name();
		int instOrder = ld.getOrder();

		if (ld.getState().equals(State.FETCH_COMP)) {

			// Check if Int Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			IntAdder intAdder = fuCntl.getIntAdder();
			if (intAdder != null) {
				//if (ld.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(ld.getDestination(), ld)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(ld.getDestination(), ld);
					//dataHaz.addToWarBlock(ld.getBaseAddr(), ld);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = ld;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(ld);
					ld.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//ld.getDestination().setBusy(ld);

					// Clear the structural hazard flag, if it was set earlier.
					ld.clearStructHazard();
					ld.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					ld.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ ld.getDestination().getName());
				} else {
					ld.setWawHazard();
					resultMgr.setWawHazard(ld.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = ld;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ ld.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				ld.setStructHazard();
				resultMgr.setStructHazard(ld.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = ld;
				ld.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT Adder not available.");
			}
		}

		return 0;
	}

	private int processADDDInst(ADDD addd) {
		// Local variable.
		String opCode = addd.getOpcode().name();
		int instOrder = addd.getOrder();

		if (addd.getState().equals(State.FETCH_COMP)) {

			// Check if FP Adder FU for this, and reserve it.
			// If FU not available, stall the instruction.

			// We do not want to stall if the registers are busy.
			// That will be checked in the READ stage. However, we
			// need to reserve the registers being used in this inst.
			FPAdder fpAdder = fuCntl.getFPAdder();
			if (fpAdder != null) {
				//if (addd.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(addd.getDestination(), addd)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(addd.getDestination(), addd);
					//dataHaz.addToWarBlock(addd.getSrc1(), addd);
					//dataHaz.addToWarBlock(addd.getSrc2(), addd);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = addd;
					
					// Reserve the Integer Adder, and pass it to inst.
					fpAdder.setReserve(addd);
					addd.setFpAdderIndex(fpAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//addd.getDestination().setBusy(addd);
					//addd.getSrc1().setReading(addd);

					// Clear the structural hazard flag, if it was set earlier.
					addd.clearStructHazard();
					addd.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					addd.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ addd.getDestination().getName());
				} else {
					addd.setWawHazard();
					resultMgr.setWawHazard(addd.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = addd;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ addd.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				addd.setStructHazard();
				resultMgr.setStructHazard(addd.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = addd;
				addd.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT Adder not available.");
			}
		}

		return 0;
	}

	private int processLWInst(LW lw) {

		// Local variable.
		String opCode = lw.getOpcode().name();
		int instOrder = lw.getOrder();

		if (lw.getState().equals(State.FETCH_COMP)) {

			// Get Int Adder FU for this. Check if dest register is available.
			IntAdder intAdder = fuCntl.getIntAdder();

			if (intAdder != null) {
				//if (lw.getDestination().isIdle()) {
				if (dataHaz.addToWawBlock(lw.getDestination(), lw)) {
					
					// Add source Reg for checking RAW hazards.
					dataHaz.addToRawBlock(lw.getDestination(), lw);
					//dataHaz.addToWarBlock(lw.getBaseAddr(), lw);
					
					// Resources are free now. Unstall the stage.
					this.issueStgStalled = false;
					stallReleasedBy = lw;

					// Reserve the Integer Adder, and pass it to inst.
					intAdder.setReserve(lw);
					lw.setIntAdderIndex(intAdder.getIndex());

					// Set the dest register busy to avoid WAW hazard.
					//lw.getDestination().setBusy(lw);

					// Clear the structural hazard flag, if it was set earlier.
					lw.clearStructHazard();
					lw.clearWawHazard();

					// Mark the instruction as ISSUEing.
					// This is to make sure this is not handled in next tick.
					lw.setState(State.ISSUE);
					log.debug("INT Adder found. No STRUCT or WAW hazard, resources allocated.");
					log.debug("Destination register " + opCode + "["
							+ instOrder + "]" + " is: "
							+ lw.getDestination().getName());
				} else {
					lw.setWawHazard();
					resultMgr.setWawHazard(lw.getRawInst());
					this.issueStgStalled = true;
					stallCausedBy = lw;
					log.debug(opCode + "[" + instOrder + "]" + " WAW hazard; "
							+ lw.getDestination().getName() + " is busy.");
				}
			} else {
				// Return stalling of the state.
				lw.setStructHazard();
				ResultMgr.getInstance().setStructHazard(lw.getRawInst());
				this.issueStgStalled = true;
				stallCausedBy = lw;
				lw.setPrevStallCausedBy(fuCntl.getINTAddrStallingInst());
				log.debug(opCode + "[" + instOrder + "]"
						+ " STRUCT hazard; INT Adder not available.");
			}
		}

		return 0;
	}

	public boolean enQueue(Instruction inst) {

		if (!issueStgStalled || stallReleasedBy.getState().equals(State.WRITE_COMP)) {
			instQueue.add(inst);
			return true;
		} else if (inst.getPrevStallCausedBy() != null) {
			if (inst.getPrevStallCausedBy().getState().equals(State.WRITE_COMP)) {
				instQueue.add(inst);
				return true;
			} else {
				return false;
			}
		}/*else if (checkCurrentStalling(inst) != null) {
			if (checkCurrentStalling(inst).getState().equals(State.WRITE_COMP)) {
				instQueue.add(inst);
				return true;
			} else {
				return false;
			}
		}  */else {
			log.debug("Issue stage is stalled by: " + stallCausedBy.getOpcode() + "[" + stallCausedBy.getOrder() + "]");
			return false;
		}
	}

	public float getClock() {
		return clockCounter;
	}

	public void incrementClock() {
		IssueStage.clockCounter += CPUConstants.CLOCK_STEP;
	}

	@Override
	public boolean isEmpty() {
		if (instQueue.isEmpty()) {
			log.debug("ISSUE stage is empty");
			return true;
		} else {
			log.debug("ISSUE stage is still running");
			return false;
		}
	}
	
	private Instruction checkCurrentStalling(Instruction inst) {
		
		Instruction in = null;
		
		switch (inst.getOpcode()) {
		case ADDD:
		case SUBD:
			in = fuCntl.getFPAddrStallingInst();
			break;
		case MULD:
			in = fuCntl.getFPMultStallingInst();
			break;
		case DIVD:
			in = fuCntl.getFPDivStallingInst();
			break;
		case AND:
		case ANDI:
		case DADD:
		case DADDI:
		case DSUB:
		case DSUBI:
		case OR:
		case ORI:
			in = fuCntl.getINTAddrStallingInst();
			break;
		case LD:
		case LW:
		case SD:
		case SW:
			break;
		case BEQ:
		case BNE:
		case HLT:
		case J:
			break;
		case DEFAULT:
			break;
		default:
			break;
		}
		
		
		return in;
	}
}
