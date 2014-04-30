package com.sb.test;

import org.apache.log4j.PropertyConfigurator;

import com.sb.core.cpu.DataHazard;
import com.sb.core.inst.ADDD;
import com.sb.core.inst.DADD;
import com.sb.core.inst.Instruction;
import com.sb.core.inst.SD;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntegerRegister;

public class DHTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(args[0]);
		DataHazard dh = DataHazard.getInstance();
		
		FPRegister src = new FPRegister("5", "F1");
		int offset = 10;
		IntegerRegister baseAddr = new IntegerRegister("10", "R1");
		Instruction sd = new SD(src, offset, baseAddr);
		
		FPRegister dest11 = new FPRegister("5", "F2");
		FPRegister src12 = new FPRegister("5", "F3");
		FPRegister src13 = new FPRegister("5", "F4");
		Instruction addd = new ADDD(dest11, src12, src13);

		IntegerRegister dest21 = new IntegerRegister("10", "R2");
		IntegerRegister src22 = new IntegerRegister("10", "R3");
		IntegerRegister src23 = new IntegerRegister("10", "R4");
		Instruction dadd = new DADD(dest21, src22, src23);

		dh.addToWawBlock(src, sd);
		dh.addToWawBlock(src, addd);
		dh.addToWawBlock(dest11, addd);
		
		dh.addToRawBlock(src22, dadd);
		dh.addToRawBlock(src22, sd);
		
		dh.checkWAWHazard(src, sd);
		dh.checkWAWHazard(src, addd);

		dh.checkRAWHazard(src, sd);
		dh.checkRAWHazard(src, addd);
	}

}
