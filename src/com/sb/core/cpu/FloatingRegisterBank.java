package com.sb.core.cpu;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sb.core.memory.RegisterMemory;
import com.sb.core.register.FPRegC;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntRegC;
import com.sb.core.register.IntegerRegister;

public class FloatingRegisterBank {
	// Single instance.
	private static FloatingRegisterBank instance = null;
	private List<FPRegister> floatRegBank;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected FloatingRegisterBank() {
		floatRegBank = new ArrayList<FPRegister>();
		
		// Initialize the bank.
		initialize();
	}

	public static FloatingRegisterBank getInstance() {
		if (instance == null) {
			instance = new FloatingRegisterBank();
		}
		return instance;
	}
	
	private int initialize() {
		int noOfFpReg = FPRegC.values().length;
		
		for (int index = 0; index < noOfFpReg; index++) {
			
			// Create IntegerRegister.
			FPRegister fpReg = new FPRegister("0", FPRegC.values()[index].toString());
			
			// Populate the register bank.
			floatRegBank.add(fpReg);
		}
		
		return 0;
	}
	
	public FPRegister getFpRegister(String regName) {
		
		// local variable.
		FPRegister fpReg = null;
		int size = floatRegBank.size();
				
		try {
			for (int index = 0; index < size; index++) {
				// Check if index is correct, and return null if wrong one.
				try {
					fpReg = floatRegBank.get(index);
				} catch (IndexOutOfBoundsException e1) {
					log.error("Invalid index specified: " + index);
					return null;
				}
				
				// Return the requested register.
				if (fpReg != null) {
					if (fpReg.getId().equals(FPRegC.valueOf(regName))) {
						return fpReg;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("Invalid Float Register requested: " + regName);
			fpReg = null;
		}
		
		// Return register.
		return fpReg;
	}
}
