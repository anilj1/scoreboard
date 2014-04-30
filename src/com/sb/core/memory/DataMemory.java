package com.sb.core.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class DataMemory {

	// Single instance.
	private static DataMemory instance = null;
	private List<Long> dataMem;
	private int startAddr = 256;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected DataMemory () {
		dataMem = new ArrayList<Long>();
	}

	public static DataMemory getInstance() {
		if (instance == null) {
			instance = new DataMemory();
		}
		return instance;
	}
	
	public List<Long> getDataMem() {
		return dataMem;
	}

	public void setDataMem(List<Long> dataMem) {
		this.dataMem = dataMem;
	}
	
	public void append(String data) {
		Long l = Long.parseLong(data, 2);
		dataMem.add(new Long(l));
	}
	
	public Long getDataActual(int loc) {
		
		Long data = null;

		try {
			 data = dataMem.get(loc - 256);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			log.error("Invalid memory location specified");
		}
		
		return data;
	}
	
	public Long getDataRelative(int loc) {
		
		Long data = null;
		
		try {
			data = dataMem.get(loc);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			log.error("Invalid memory location specified");
		}
		
		return data;
	}
	
	public Long setDataRelative(int addr, int val) {
		
		Long data = null;
		
		try {
			data = dataMem.set(addr, new Long(val));
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			log.error("Invalid memory location specified");
		}
		
		return data;
	}
	
	public String toString() {
		
		Iterator<Long> itr = dataMem.iterator(); 
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
		
		return "";
	}
}
