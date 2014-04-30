package com.sb.core.fu;

import org.apache.log4j.Logger;

import com.sb.core.inst.Instruction;
import com.sb.core.register.RegState;

public class FuncUnit {
	private int index = 0;
	private FuState state = FuState.AVAILABLE;
	private int hashOfReservingInst = 0;
	private String opcodeOfReservingInst;
	private int orderOfReservingInst = 0;
	Logger log = Logger.getLogger(this.getClass().getName());
	private Instruction reservingInst = null;

	public FuncUnit(int index, FuState state) {
		this.index = index;
		this.state = state;
	}
	
	public void setReserve(Instruction inst) {
		state = FuState.RESERVED;
		setReservingInst(inst);
		hashOfReservingInst = inst.hashCode();
		opcodeOfReservingInst = inst.getOpcode().name();
		orderOfReservingInst = inst.getOrder();
		log.debug("Function Unit reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
	}
	
	public void setAvailable() {
		state = FuState.AVAILABLE;
	}
	
	public boolean isReserve() {
		if (state.equals(FuState.RESERVED)) {
			log.debug("Function Unit reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isAvailable() {
		if (state.equals(FuState.AVAILABLE)) {
			return true;
		} else {
			log.debug("Function Unit reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
			return false;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Instruction getReservingInst() {
		return reservingInst;
	}

	public void setReservingInst(Instruction reservingInst) {
		this.reservingInst = reservingInst;
	}
}
