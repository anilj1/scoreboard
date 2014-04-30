package com.sb.core.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;

import com.sb.core.cpu.ReadStage;
import com.sb.core.inst.Instruction;
import com.sb.core.register.IntRegC;

public class RegisterMemory {	
	// Single instance.
	private static RegisterMemory instance = null;
	private List<Long> regMem;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected RegisterMemory() {
		regMem = new ArrayList<Long>();
	}

	public static RegisterMemory getInstance() {
		if (instance == null) {
			instance = new RegisterMemory();
		}
		return instance;
	}

	public List<Long> getDataMem() {
		return regMem;
	}

	public void setDataMem(List<Long> dataMem) {
		this.regMem = dataMem;
	}
	
	public void append(String data) {
		Long l = Long.parseLong(data, 2);
		regMem.add(new Long(l));
	}
	
	public Long getRegValue(int index) {
		
		Long data = null;
		if (index > 31) {
			log.error("Invalid register specified");
			return null;
		}

		try {
			 data = regMem.get(index);
		} catch (IndexOutOfBoundsException e) {
			log.error("Invalid register specified");
		}
		
		return data;
	}
	
	public Long getRegValue(IntRegC regName) {
		
		Long val = regMem.get(regName.ordinal());
	
		return val;
	}
	
	public String toString() {
		
		Iterator<Long> itr = regMem.iterator(); 
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
		
		return "";
	}
}
