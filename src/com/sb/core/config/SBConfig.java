package com.sb.core.config;

public class SBConfig {

	private int NoOfFPAdder;
	private int NoOfFPMultiplier;
	private int NoOfFPDivider;
	private int NoOfIntegerUnit;
	
	// Single instance.
	private static SBConfig instance = null;

	protected SBConfig() {
		NoOfFPAdder = 0;
		NoOfFPMultiplier = 0;
		NoOfFPDivider = 0;
		NoOfIntegerUnit = 0;
	}

	public static SBConfig getInstance() {
		if (instance == null) {
			instance = new SBConfig();
		}
		return instance;
	}

	public int getNoOfFPAdder() {
		return NoOfFPAdder;
	}


	public void setNoOfFPAdder(int noOfFPAdder) {
		NoOfFPAdder = noOfFPAdder;
	}


	public int getNoOfFPMultiplier() {
		return NoOfFPMultiplier;
	}


	public void setNoOfFPMultiplier(int noOfFPMultiplier) {
		NoOfFPMultiplier = noOfFPMultiplier;
	}


	public int getNoOfFPDivider() {
		return NoOfFPDivider;
	}


	public void setNoOfFPDivider(int noOfFPDivider) {
		NoOfFPDivider = noOfFPDivider;
	}


	public int getNoOfIntegerUnit() {
		return NoOfIntegerUnit;
	}


	public void setNoOfIntegerUnit(int noOfIntegerUnit) {
		NoOfIntegerUnit = noOfIntegerUnit;
	}
}
