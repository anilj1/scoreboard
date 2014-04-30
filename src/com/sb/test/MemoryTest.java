package com.sb.test;

import com.sb.core.memory.DataMemory;
import com.sb.parser.data.DataMemParser;

public class MemoryTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataMemParser memParser = DataMemParser.getInstance();
		
		DataMemory mem = memParser.parseMemory("E:\\aSpring-2013\\eclipse-ws\\scoreboard\\inputs\\memory1.txt");
		
		System.out.println("Data at mem location 240 is: " + mem.getDataActual(240));
		System.out.println("Data at mem location 356 is: " + mem.getDataActual(356));
		System.out.println("Data at mem location 256 is: " + mem.getDataActual(256));
		System.out.println("Data at mem location 0 is: " + mem.getDataRelative(0));
		System.out.println("Data at mem location -1 is: " + mem.getDataRelative(-1));
		System.out.println("Data at mem location 20 is: " + mem.getDataRelative(20));
		System.out.println("Data at mem location 40 is: " + mem.getDataRelative(40));

		mem.toString();
	}
}
