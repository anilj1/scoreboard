package com.sb.core.register;

import org.apache.log4j.Logger;

import com.sb.core.inst.Instruction;

public class Register {
	private RegState state;
	private String Name;
	private int hashOfReservingInst = 0;
	private String opcodeOfReservingInst;
	private int orderOfReservingInst = 0;
	
	private int hashOfReadingInst = 0;
	private String opcodeOfReadingInst;
	private int orderOfReadingInst = 0;
	
	Logger log = Logger.getLogger(this.getClass().getName());
	
	public Register(String name) {
		state = RegState.IDLE;
		this.Name = name;
	}

	public RegState getState() {
		return state;
	}
	
	public void setIdle() {
		state = RegState.IDLE;
	}

	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

	public void setBusy() {
		state = RegState.BUSY;
	}
	
	public void setBusy(Instruction inst) {
		state = RegState.BUSY;
		hashOfReservingInst = inst.hashCode();
		opcodeOfReservingInst = inst.getOpcode().name();
		orderOfReservingInst = inst.getOrder();
		log.debug("Register " + Name + " reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
	}
	
	public void setReading() {
		state = RegState.READING;
	}
	
	public void setReading(Instruction inst) {
		state = RegState.READING;
		hashOfReadingInst = inst.hashCode();
		opcodeOfReadingInst = inst.getOpcode().name();
		orderOfReadingInst = inst.getOrder();
		log.debug("Register " + Name + " Reading set by: " + opcodeOfReadingInst + "[" + orderOfReadingInst + "]");
	}
	
	public boolean isIdle() {
		return (state == RegState.IDLE);
	}
	
	public boolean isBusy() {
		if (state == RegState.BUSY) {
			log.debug("Register " + Name + " reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
			return true;
		}
		
		return false;
	}
	
	public boolean isBusy(int hash) {
		if (hashOfReservingInst == hash) {
			return false;
		} else {
			log.debug("Register " + Name + " reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
			return (state == RegState.BUSY);
		}
	}
	
	public boolean isBusy(Instruction inst) {
		if (hashOfReservingInst == inst.hashCode() ||
			orderOfReservingInst > inst.getOrder()) {
			return false;
		} else {
			log.debug("Register " + Name + " reserved by: " + opcodeOfReservingInst + "[" + orderOfReservingInst + "]");
			return (state == RegState.BUSY);
		}
	}
	
	public boolean isReading() {
		if (state == RegState.READING) {
			log.debug("Register " + Name + " Reading set by: " + opcodeOfReadingInst + "[" + orderOfReadingInst + "]");
			return true;
		}
		
		return false;
	}
	
	public boolean isReading(int hash) {
		if (hashOfReadingInst == hash) {
			return false;
		} else {
			log.debug("Register " + Name + " Reading set by: " + opcodeOfReadingInst + "[" + orderOfReadingInst + "]");
			return (state == RegState.READING);
		}
	}
	
	public boolean isReading(Instruction inst) {
		if (hashOfReadingInst == inst.hashCode() ||
			orderOfReservingInst > inst.getOrder()) {
			return false;
		} else {
			log.debug("Register " + Name + " Reading set by: " + opcodeOfReadingInst + "[" + orderOfReadingInst + "]");
			return (state == RegState.READING);
		}
	}

	public boolean isWAWHazard(Instruction inst) {
		
		boolean result = false;
		
		if (state.equals(RegState.BUSY)) {
			if (hashOfReservingInst == inst.hashCode() ||
				orderOfReservingInst > inst.getOrder()) {
				result = false;
			} else {
				result = true;
			}
		} else if (state.equals(RegState.READING)) {
			result = false;
		}
		
		return result;
	}
	
	public boolean isRAWHazard(Instruction inst) {
		boolean result = false;
		
		if (state.equals(RegState.BUSY)) {
			if (hashOfReservingInst == inst.hashCode()) {
				result = false;
			} else {
				result = true;
			}
		} else if (state.equals(RegState.READING)) {
			result = true;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return "";
	}
}
