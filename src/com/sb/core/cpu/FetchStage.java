package com.sb.core.cpu;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.sb.core.inst.Instruction;
import com.sb.core.inst.Opcode;
import com.sb.core.inst.State;
import com.sb.core.prog.Program;
import com.sb.writer.output.ResultMgr;

public class FetchStage extends PipelineStage {

	// Single instance.
	private static FetchStage instance = null;
	private Program program = null;
	private static int progCnt = 0;
	private boolean progDone = false;
	
	private Vector<Instruction> instQueue;
	private static float clockCounter = 1;
	Logger log = Logger.getLogger(this.getClass().getName());
	
	protected FetchStage() {
		instQueue = new Vector<Instruction>(5);
	}

	public static FetchStage getInstance() {
		if (instance == null) {
			instance = new FetchStage();
		}
		return instance;
	}
	
	public void setProgram(Program prog) {
		program = prog;
	}
	
	@Override
	public int execute() {

		log.debug("Instruction FETCH CLOCK is: " + this.getClock());

		// Read next instruction exactly at clock tick.
		if ((this.getClock() % 1) == 0) {
			Instruction inst = program.getInst(progCnt);
			
			if (inst != null && (inst.getState().equals(State.IDLE) || inst.getState().equals(State.FETCH))) {
				
				String opCode = inst.getOpcode().name();
				int instOrder = inst.getOrder();
				inst.setState(State.FETCH);

				log.debug(opCode + "[" + instOrder + "]" + " Instruction hash code is: " + inst.hashCode());
				log.debug(opCode + "[" + instOrder + "]" + " FETCH at clock: " + this.getClock());
				
				// Check for stage completion.
				//if (inst.getFetchCounter() >= inst.getFetchCycles()) {
					// Do not set the next state for instruction
					// if it is stalled due to structural hazard.
					if (IssueStage.getInstance().enQueue(inst)) {
						
						inst.setState(State.FETCH_COMP);
						log.debug(opCode + "[" + instOrder + "]"
								+ " FETCH stage completed at clock: "
								+ this.getClock());
						
						log.debug("Pushing instruction " + opCode + "[" + instOrder
								+ "]" + " to ISSUE stage.");
						
						ResultMgr.getInstance().addInst(inst.getRawInst());
						ResultMgr.getInstance().setFetchClock(inst.getRawInst(), this.getClock());
						
						// Increment PC only when inst advanced to next stage.
						progCnt++;
					} else {
						log.debug("Issue stage is stalled, not fetchng/issueing next instruction.");
					}
				//}

				// Increment Issue counter, no matter what is outcome.
				// This stage visit should get recorded.
//				log.debug("Incremented " + opCode + "[" + instOrder + "]"
//						+ " FETCH counter.");
//				
//				log.debug(opCode + "[" + instOrder + "]" + " Fetch Cycles are: "
//						+ inst.getFetchCounter());
//				
//				log.debug(opCode + "[" + instOrder + "]" + " Fetch Counter is: "
//						+ inst.getFetchCounter());
//				inst.IncrFetchCounter();

			} else {
				log.debug("Nothing to fetch. Probably program is finished.");
				progDone = true;
			}
		}
		
		// Clock the stage counter.
		this.incrementClock();
		
		if (progDone) {
			return 1;
		} else {
			return 0;
		}
	}

	public void enQueue(Instruction inst) {
		instQueue.add(inst);
		log.debug("Instruction enqued to Fetch queue: " + inst.getOpcode().name());
	}
	
	@Override
	public float getClock() {
		// TODO Auto-generated method stub
		return clockCounter;
	}

	@Override
	public void incrementClock() {
		FetchStage.clockCounter += CPUConstants.CLOCK_STEP;		
	}

	@Override
	public boolean isEmpty() {
		if (instQueue.isEmpty()) {
			log.debug("FETCH stage is empty");
			return true;
		} else {
			log.debug("FETCH stage is still running");
			return false;
		}
	}

}
