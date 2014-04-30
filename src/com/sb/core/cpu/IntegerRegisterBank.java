package com.sb.core.cpu;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sb.core.memory.RegisterMemory;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntRegC;
import com.sb.core.register.IntegerRegister;

public class IntegerRegisterBank {
	// Single instance.
	private static IntegerRegisterBank instance = null;
	private List<IntegerRegister> intRegBank;
	Logger log = Logger.getLogger(this.getClass().getName());
	
	protected IntegerRegisterBank() {
		intRegBank = new ArrayList<IntegerRegister>();
		
		// Initialize the bank.
		initialize();
	}

	public static IntegerRegisterBank getInstance() {
		if (instance == null) {
			instance = new IntegerRegisterBank();
		}
		return instance;
	}
	
	private int initialize() {
		int noOfIntReg = IntRegC.values().length;
		
		for (int index = 0; index < noOfIntReg; index++) {
			
			// Create IntegerRegister.
			IntegerRegister intReg = new IntegerRegister("0", IntRegC.values()[index].toString());
			
			// Populate the register bank.
			intRegBank.add(intReg);
		}
		
		return 0;
	}
	
	public IntegerRegister getIntRegister(String regName) {
		
		// local variable.
		IntegerRegister intReg = null;
		int size = intRegBank.size();
		
		try {
			for (int index = 0; index < size; index++) {
				
				// Check if index is correct, and return null if wrong one.
				try {
					intReg = intRegBank.get(index);
				} catch (IndexOutOfBoundsException e1) {
					log.error("Invalid index specified: " + index);
					return null;
				}
				
				// Return the requested register.
				if (intReg != null) {
					if (intReg.getId().equals(IntRegC.valueOf(regName))) {
						return intReg;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("Invalid Int Register name requested: " + regName);
			intReg = null;
		}
		
		// Return register.
		return intReg;
	}
}
