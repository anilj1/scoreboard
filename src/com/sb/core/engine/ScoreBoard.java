package com.sb.core.engine;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sb.core.config.SBConfig;
import com.sb.core.fu.FuncUnitController;
import com.sb.core.memory.DataMemory;
import com.sb.core.memory.RegisterMemory;
import com.sb.core.prog.Program;
import com.sb.parser.config.ConfigParser;
import com.sb.parser.data.DataMemParser;
import com.sb.parser.inst.ProgramParser;
import com.sb.parser.registers.RegMemParser;
import com.sb.writer.output.ResultMgr;

public class ScoreBoard {
	  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String projRoot = System.getProperty("user.dir");
		String pathToLog = projRoot;
		String logConfig = projRoot + File.separator + "inputs" + File.separator + "log4j.properties";
		
		String configfile = projRoot + File.separator + "inputs" + File.separator + "config.txt";
		String dataMemFile = projRoot + File.separator + "inputs" + File.separator + "memory.txt";
		String regMemFile = projRoot + File.separator + "inputs" + File.separator + "reg.txt";
		String progFile = projRoot + File.separator + "inputs" + File.separator + "prog3.txt";
		String resultFile = "";
		
		// Initialize the logger.
		System.setProperty("log4j.logpath", pathToLog);
		PropertyConfigurator.configure(logConfig);
		Logger log = Logger.getLogger(ScoreBoard.class.getName());
		
		// Check the arguments of the program.
		if (args.length == 5) {
			progFile = args[0];
			dataMemFile = args[1];
			regMemFile = args[2];
			configfile = args[3];
			resultFile = args[4];
			
			log.debug("Config File is: " + configfile);
			log.debug("Data Memory File is: " + dataMemFile);
			log.debug("Reg Memory File is: " + regMemFile);
			log.debug("Prog File is: " + progFile);
			log.debug("Result File is: " + resultFile);
			
			// Set the result output.
			ResultMgr.getInstance().setResultFile(resultFile);
		} else {
			System.out.println("No of arguments provided are: " + args.length);
			printUsage();
			System.exit(0);
		}
		
		// Initialize Config Parser.
		ConfigParser configParser = ConfigParser.getInstance();
		SBConfig conf = configParser.parseConfig(configfile);
		
		// Initialize Data Parser.
		DataMemParser dataMemParser = DataMemParser.getInstance();
		DataMemory mem = dataMemParser.parseMemory(dataMemFile);
		
		// Initialize Register Memory Parser.
		RegMemParser regMemParser = RegMemParser.getInstance();
		RegisterMemory regState = regMemParser.parseRegister(regMemFile);
		
		// Initialize Program Parser.
		ProgramParser progParser = ProgramParser.getInstance();
		Program prog = progParser.parseProgram(progFile);
		prog.setProgName(progFile);
		
		// Initialize functional units.
		FuncUnitController funUnitCntl = FuncUnitController.getInstance();
		funUnitCntl.populateController();
		
		// Create the pipeline and load it.
		PipeLine pipeLine = PipeLine.getInstance();
		pipeLine.loadProgram(prog);
		
		// Start the pipeline.
		pipeLine.start();

		try {
			pipeLine.join();
			log.debug("Program executed successfully.");
			
			// Print the Result.
			ResultMgr.getInstance().PrintResult(prog);
			ResultMgr.getInstance().PrintResultToFile(prog);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void printUsage() {
		System.out.println("No enough arguments");
		System.out.println("Correct usage of tool is...");
		System.out.println("\tjava scoreboard inst.txt data.txt reg.txt config.txt result.txt");
		System.out.println();
		System.out.println("Please provide absolute path to the files...");
	}
}
