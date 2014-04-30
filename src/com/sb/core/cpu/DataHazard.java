package com.sb.core.cpu;

import java.util.Hashtable;
import org.apache.log4j.Logger;

import com.sb.core.inst.Instruction;
import com.sb.core.register.Register;

public class DataHazard {

	// Single instance.
	private static DataHazard instance = null;

	private static Hashtable<Integer, ReserveBlock> WawBlock = null;
	private static Hashtable<Integer, ReserveBlock> RawBlock = null;
	private static Hashtable<Integer, ReserveBlock> WarBlock = null;
	Logger log = Logger.getLogger(this.getClass().getName());

	public DataHazard() {
		WawBlock = new Hashtable<Integer, ReserveBlock>();
		RawBlock = new Hashtable<Integer, ReserveBlock>();
		WarBlock = new Hashtable<Integer, ReserveBlock>();
	}

	public static DataHazard getInstance() {
		if (instance == null) {
			instance = new DataHazard();
		}
		return instance;
	}

	// Data population functions.
	public boolean addToWawBlock(Register reg, Instruction inst) {

		ReserveBlock rblk = null;
		boolean result = false;
		
		if (!WawBlock.containsKey(reg.hashCode())) {
			rblk = new ReserveBlock(reg.getName(), inst);
			WawBlock.put(reg.hashCode(), rblk);
			log.debug("Register: " + reg.getName() + " added in WAW Block by "
					+ inst.getOpcode() + "[" + inst.getOrder() + "]");
			result = true;
		} else {
			if ((rblk = WawBlock.get(reg.hashCode())) != null) {
				String opCode = rblk.getReservedBy().getOpcode().name();
				int order = rblk.getReservedBy().getOrder();
				
				log.debug("Register: " + reg.getName()
						+ " already in WAW Block; added by " + opCode + "["
						+ order + "]");
				result = false;
			}
		}
		
		return result;
	}

	public boolean removeFromWawBlock(Register reg) {
		WawBlock.remove(reg.hashCode());
		return true;
	}

	public boolean addToRawBlock(Register reg, Instruction inst) {

		ReserveBlock rblk = null;
		boolean result = false;
		
		if (!RawBlock.containsKey(reg.hashCode())) {
			rblk = new ReserveBlock(reg.getName(), inst);
			RawBlock.put(reg.hashCode(), rblk);
			log.debug("Register: " + reg.getName() + " added in RAW Block by "
					+ inst.getOpcode() + "[" + inst.getOrder() + "]");
			result = true;
		} else {
			if ((rblk = RawBlock.get(reg.hashCode())) != null) {
				String opCode = rblk.getReservedBy().getOpcode().name();
				int order = rblk.getReservedBy().getOrder();
				
				log.debug("Register: " + reg.getName()
						+ " already in RAW Block; added by " + opCode + "["
						+ order + "]");
				result = false;
			}
		}
		
		return result;
	}

	public boolean removeFromRawBlock(Register reg) {
		RawBlock.remove(reg.hashCode());
		return true;
	}

	public boolean addToWarBlock(Register reg, Instruction inst) {

		ReserveBlock rblk = null;
		boolean result = false;
		
		if (!WarBlock.containsKey(reg.hashCode())) {
			rblk = new ReserveBlock(reg.getName(), inst);
			WarBlock.put(reg.hashCode(), rblk);
			log.debug("Register: " + reg.getName() + " added in WAR Block by "
					+ inst.getOpcode() + "[" + inst.getOrder() + "]");
			result = true;
		} else {
			if ((rblk = WarBlock.get(reg.hashCode())) != null) {
				String opCode = rblk.getReservedBy().getOpcode().name();
				int order = rblk.getReservedBy().getOrder();
				
				log.debug("Register: " + reg.getName()
						+ " already in WAR Block; added by " + opCode + "["
						+ order + "]");
				result = false;
			}
		}
		
		return result;
	}

	public boolean removeFromWarBlock(Register reg) {
		WarBlock.remove(reg.hashCode());
		return true;
	}

	// Check for data hazards.
	public boolean checkWAWHazard(Register reg, Instruction inst) {

		ReserveBlock rblk = null;

		if (WawBlock.containsKey(reg.hashCode())) {
			if ((rblk = WawBlock.get(reg.hashCode())) != null) {
				if (rblk.getRegName().equals(reg.getName())
						&& rblk.getReservedBy().hashCode() != inst.hashCode()) {

					log.debug("WAW Hazard found - Register "
							+ rblk.getRegName() + " reserved by "
							+ rblk.getReservedBy().getOpcode() + "["
							+ rblk.getReservedBy().getOrder() + "]");

					return true;
				}
			}
		}

		log.debug("WAW Hazard not found - reserved by requesting instruction");
		return false;
	}

	public boolean checkRAWHazard(Register reg, Instruction inst) {

		ReserveBlock rblk = null;

		if (RawBlock.containsKey(reg.hashCode())) {
			if ((rblk = RawBlock.get(reg.hashCode())) != null) {
				if (rblk.getRegName().equals(reg.getName())
						&& rblk.getReservedBy().hashCode() != inst.hashCode() 
						&& rblk.getReservedBy().getOrder() < inst.getOrder()) {

					log.debug("RAW Hazard found - Register "
							+ rblk.getRegName() + " reserved by "
							+ rblk.getReservedBy().getOpcode() + "["
							+ rblk.getReservedBy().getOrder() + "]");

					return true;
				}
			}
		}

		log.debug("RAW Hazard not found - reserved by requesting instruction");
		return false;
	}

	public boolean checkWARHazard(Register reg, Instruction inst) {

		ReserveBlock rblk = null;

		if (WarBlock.containsKey(reg.hashCode())) {
			if ((rblk = WarBlock.get(reg.hashCode())) != null) {
				if (rblk.getRegName().equals(reg.getName())
						&& rblk.getReservedBy().hashCode() != inst.hashCode()) {

					log.debug("WAR Hazard found - Register "
							+ rblk.getRegName() + " reserved by "
							+ rblk.getReservedBy().getOpcode() + "["
							+ rblk.getReservedBy().getOrder() + "]");

					return true;
				}
			}
		}

		log.debug("WAR Hazard not found - reserved by requesting instruction");
		return false;
	}
}
