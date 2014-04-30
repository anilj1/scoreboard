package com.sb.test;

import com.sb.core.prog.Program;
import com.sb.parser.inst.ProgramParser;

public class ParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Parse the program file.
		ProgramParser parser = ProgramParser.getInstance();
	
		Program prog = parser.parseProgram("E:\\aSpring-2013\\eclipse-ws\\scoreboard\\inputs\\prog2.txt");
		
        // Print the program before returning.
		prog.toString();
	}
}
