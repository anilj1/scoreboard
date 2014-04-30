package com.sb.test;

import org.apache.log4j.PropertyConfigurator;

import com.sb.core.cpu.FloatingRegisterBank;
import com.sb.core.cpu.IntegerRegisterBank;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntegerRegister;

public class RegisterBankTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(args[0]);

		IntegerRegisterBank intRegBank = IntegerRegisterBank.getInstance();
		FloatingRegisterBank fpRegBank = FloatingRegisterBank.getInstance();
		IntegerRegister intReg = null;
		FPRegister fpReg = null;
		
		intReg = intRegBank.getIntRegister("R1");
		if (intReg != null) {
			System.out.println("Integer Register R1 is: " + intReg.getName());
		} else {
			System.out.println("Invalid register requested: " + "R1");
		}
		
		intReg = intRegBank.getIntRegister("R31");
		if (intReg != null) {
			System.out.println("Integer Register R31 is: " + intReg.getName());
		}  else {
			System.out.println("Invalid register requested: " + "R31");
		}
		
		intReg = intRegBank.getIntRegister("R32");
		if (intReg != null) {
			System.out.println("Integer Register R32 is: " + intReg.getName());
		} else {
			System.out.println("Invalid register requested: " + "R32");
		}
		
		fpReg = fpRegBank.getFpRegister("F1");
		if (fpReg != null) {
			System.out.println("Floating Register F1 is: " + fpReg.getName());
		} else {
			System.out.println("Invalid register requested: " + "F1");
		}
		
		fpReg = fpRegBank.getFpRegister("F31");
		if (fpReg != null) {
			System.out.println("Floating Register F31 is: " + fpReg.getName());
		} else {
			System.out.println("Invalid register requested: " + "F31");
		}
		
		fpReg = fpRegBank.getFpRegister("F32");
		if (fpReg != null) {
			System.out.println("Floating Register F32 is: " + fpReg.getName());
		} else {
			System.out.println("Invalid register requested: " + "F32");
		}
	}

}
