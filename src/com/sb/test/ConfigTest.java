package com.sb.test;

import com.sb.core.config.SBConfig;
import com.sb.parser.config.ConfigParser;

public class ConfigTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Parse the program file.
		ConfigParser config = ConfigParser.getInstance();
	
		SBConfig conf = config.parseConfig("E:\\aSpring-2013\\eclipse-ws\\scoreboard\\inputs\\config1.txt");
		
		System.out.println("No of FP Adders: " + conf.getNoOfFPAdder());
		System.out.println("No of FP Dividers: " + conf.getNoOfFPDivider());
		System.out.println("No of FP Multipliers: " + conf.getNoOfFPMultiplier());
		System.out.println("No of Integder Units: " + conf.getNoOfIntegerUnit());
	}
}
