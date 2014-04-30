package com.sb.core.prog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import com.sb.core.inst.Instruction;

public class Program {
	
	private String progName = "";

	public String getProgName() {
		return progName;
	}

	public void setProgName(String progName) {
		this.progName = progName;
	}

	private ArrayList <Instruction> InstList = null;
	
	public Program() {
		InstList = new ArrayList<Instruction>();
	}
	
	public Instruction getInst(int pos) {
		try {
			return InstList.get(pos);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	public int setInst(Instruction inst) {
		InstList.add(inst);
		return 0;
	}

	public ArrayList<Instruction> getProg() {
		return InstList;
	}

	public void setProg(ArrayList<Instruction> prog) {
		this.InstList = prog;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "";
		Iterator<Instruction> itr = InstList.iterator();
		while (itr.hasNext()) {
			Instruction inst = itr.next();
			str.concat(inst.toString());
		}
		
		return str;
	}
}
