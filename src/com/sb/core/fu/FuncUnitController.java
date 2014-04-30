package com.sb.core.fu;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.sb.core.config.SBConfig;
import com.sb.core.inst.Instruction;
import com.sb.core.inst.State;

public class FuncUnitController {
		
	private int noOfFPAdder;
	private int noOfFPMultiplier;
	private int noOfFPDivider;
	private int noOfIntegerUnit;
	
	private List<FPAdder> fpAdderList;
	private List<FPMultiplier> fpMultiplierList;
	private List<FPDivider> fpDividerList;
	private List<IntAdder> intAdderList;
	
	// Single instance.
	private static FuncUnitController instance = null;

	protected FuncUnitController() {
		noOfFPAdder = 0;
		noOfFPMultiplier = 0;
		noOfFPDivider = 0;
		noOfIntegerUnit = 0;
		
		fpAdderList = new ArrayList<FPAdder>();
		fpMultiplierList = new ArrayList<FPMultiplier>();
		fpDividerList = new ArrayList<FPDivider>();
		intAdderList = new ArrayList<IntAdder>();
	}

	public static FuncUnitController getInstance() {
		if (instance == null) {
			instance = new FuncUnitController();
		}
		return instance;
	}

	public void populateController() {

		// Get the config object.
		SBConfig config = SBConfig.getInstance();

		// Create the functional unit for the scoreboard.
		int index = 0;
		noOfFPAdder = config.getNoOfFPAdder();
		noOfFPMultiplier = config.getNoOfFPMultiplier();
		noOfFPDivider = config.getNoOfFPDivider();
		noOfIntegerUnit = config.getNoOfIntegerUnit();
		
		// Populate the FU lists.
		for (index = 0; index < noOfFPAdder; index++) {
			fpAdderList.add(new FPAdder(index, FuState.AVAILABLE));
		}
		
		for (index = 0; index < noOfFPMultiplier; index++) {
			fpMultiplierList.add(new FPMultiplier(index, FuState.AVAILABLE));
		}
		
		for (index = 0; index < noOfFPDivider; index++) {
			fpDividerList.add(new FPDivider(index, FuState.AVAILABLE));
		}
		
		for (index = 0; index < noOfIntegerUnit; index++) {
			intAdderList.add(new IntAdder(index, FuState.AVAILABLE));
		}
	}
	
	public FPAdder getFPAdder() {
		
		// Return the FP adder.
		ListIterator<FPAdder> itr = fpAdderList.listIterator();
		for (; itr.hasNext(); ) {
			FPAdder fpAdr = itr.next();
			if (fpAdr.isAvailable()) {
				return fpAdr;
			}
		}
				
		return null;
	}
	
	public FPAdder getFPAdder(int index) {
		
		// Return the FP adder.
		ListIterator<FPAdder> itr = fpAdderList.listIterator();
		for (; itr.hasNext(); ) {
			FPAdder fpAdr = itr.next();
			if (fpAdr.getIndex() == index) {
				return fpAdr;
			}
		}
		return null;
	}
	
	public void returnFPAdder(FPAdder fpAdder) {
		// Return the FP adder.
		fpAdderList.add(fpAdder);
	}
	
	public FPMultiplier getFPMultiplier() {
		
		// Return the FP adder.
		ListIterator<FPMultiplier> itr = fpMultiplierList.listIterator();
		for (; itr.hasNext(); ) {
			FPMultiplier fpMul = itr.next();
			if (fpMul.isAvailable()) {
				return fpMul;
			}
		}
		return null;
	}
	
	public FPMultiplier getFPMultiplier(int index) {
		
		// Return the FP multiplier.
		ListIterator<FPMultiplier> itr = fpMultiplierList.listIterator();
		for (; itr.hasNext(); ) {
			FPMultiplier fpMul = itr.next();
			if (fpMul.getIndex() == index) {
				return fpMul;
			}
		}
		return null;
	}
	
	public void returnFPMultiplier(FPMultiplier fpMultiplier) {
		// Return the FP adder.
		fpMultiplierList.add(fpMultiplier);
	}
	
	public FPDivider getFPDivider() {
		
		// Return the FP adder.
		ListIterator<FPDivider> itr = fpDividerList.listIterator();
		for (; itr.hasNext(); ) {
			FPDivider fpDiv = itr.next();
			if (fpDiv.isAvailable()) {
				return fpDiv;
			}
		}
		return null;
	}
	
	
	public FPDivider getFPDivider(int index) {
		
		// Return the FP divider.
		ListIterator<FPDivider> itr = fpDividerList.listIterator();
		for (; itr.hasNext(); ) {
			FPDivider fpDiv = itr.next();
			if (fpDiv.getIndex() == index) {
				return fpDiv;
			}
		}
		return null;
	}
	
	public void returnFPDivider(FPDivider fpDVDivider) {
		// Return the FP adder.
		fpDividerList.add(fpDVDivider);
	}
	
	public IntAdder getIntAdder() {
		
		// Return the FP adder.
		ListIterator<IntAdder> itr = intAdderList.listIterator();
		for (; itr.hasNext(); ) {
			IntAdder intAdr = itr.next();
			if (intAdr.isAvailable()) {
				return intAdr;
			}
		}
		
		return null;
	}
	
	public IntAdder getIntAdder(int index) {
		
		// Return the FP adder.
		ListIterator<IntAdder> itr = intAdderList.listIterator();
		for (; itr.hasNext(); ) {
			IntAdder intAdr = itr.next();
			if (intAdr.getIndex() == index) {
				return intAdr;
			}
		}
		
		return null;
	}
	
	public void returnIntAdder(IntAdder intAdder) {
		// Return the FP adder.
		intAdderList.add(intAdder);
	}
	
	public Instruction getFPAddrStallingInst() {
		
		Instruction inst = null;
		
		// Return the FP adder.
		ListIterator<FPAdder> itr = fpAdderList.listIterator();
		for (; itr.hasNext();) {
			FPAdder fpAdr = itr.next();
			Instruction in = fpAdr.getReservingInst();
			if (in != null) {
				if (in.getState().equals(State.WRITE_COMP)) {
					return fpAdr.getReservingInst();
				}
			}
		}
		
		return inst;
	}
	
	public Instruction getINTAddrStallingInst() {
		
		Instruction inst = null;
		
		// Return the FP adder.
		ListIterator<IntAdder> itr = intAdderList.listIterator();
		for (; itr.hasNext();) {
			IntAdder intAdr = itr.next();
			Instruction in = intAdr.getReservingInst();
			if (in != null) {
				if (in.getState().equals(State.WRITE_COMP)) {
					return intAdr.getReservingInst();
				}
			}
		}
		
		return inst;
	}
	
	public Instruction getFPMultStallingInst() {
		
		Instruction inst = null;
		
		// Return the FP adder.
		ListIterator<FPMultiplier> itr = fpMultiplierList.listIterator();
		for (; itr.hasNext();) {
			FPMultiplier fpMult = itr.next();
			Instruction in = fpMult.getReservingInst();
			if (in != null) {
				if (in.getState().equals(State.WRITE_COMP)) {
					return fpMult.getReservingInst();
				}
			}
		}
		
		return inst;
	}
	
	public Instruction getFPDivStallingInst() {
		
		Instruction inst = null;
		
		// Return the FP adder.
		ListIterator<FPDivider> itr = fpDividerList.listIterator();
		for (; itr.hasNext();) {
			FPDivider fpDiv = itr.next();
			Instruction in = fpDiv.getReservingInst();
			if (in != null) {
				if (fpDiv.getReservingInst().getState().equals(State.WRITE_COMP)) {
					return fpDiv.getReservingInst();
				}
			}
		}
		
		return inst;
	}
}
