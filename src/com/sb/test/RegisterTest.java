package com.sb.test;

import com.sb.core.memory.RegisterMemory;
import com.sb.core.register.IntRegC;
import com.sb.parser.registers.RegMemParser;

public class RegisterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		RegMemParser regParser = RegMemParser.getInstance();

		RegisterMemory regState = regParser
				.parseRegister("E:\\aSpring-2013\\eclipse-ws\\scoreboard\\inputs\\reg1.txt");

		System.out.println("Register 240 is: "
				+ regState.getRegValue(240));
		System.out.println("Register 5 is: "
				+ regState.getRegValue(5));
		System.out.println("Register 31 is: "
				+ regState.getRegValue(31));
		System.out.println("Register -1 is: "
				+ regState.getRegValue(-1));
		System.out.println("Register 20 is: "
				+ regState.getRegValue(20));
		
		System.out.println("");
		
		IntRegC r0 = IntRegC.R0;
		System.out.println("Register R0 is: " + regState.getRegValue(r0));
		
		IntRegC r1 = IntRegC.R1;
		System.out.println("Register R1 is: " + regState.getRegValue(r1));
		
		IntRegC r2 = IntRegC.R2;
		System.out.println("Register R2 is: " + regState.getRegValue(r2));
		
		IntRegC r3 = IntRegC.R3;
		System.out.println("Register R3 is: " + regState.getRegValue(r3));
		
		IntRegC r4 = IntRegC.R4;
		System.out.println("Register R4 is: " + regState.getRegValue(r4));
		
		IntRegC r5 = IntRegC.R5;
		System.out.println("Register R5 is: " + regState.getRegValue(r5));
		
		IntRegC r6 = IntRegC.R6;
		System.out.println("Register R6 is: " + regState.getRegValue(r6));
		
		IntRegC r30 = IntRegC.R30;
		System.out.println("Register R30 is: " + regState.getRegValue(r30));
		
		IntRegC r31 = IntRegC.R31;
		System.out.println("Register R31 is: " + regState.getRegValue(r31));
		
		regState.toString();
	}
}