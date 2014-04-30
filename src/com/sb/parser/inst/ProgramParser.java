	package com.sb.parser.inst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sb.core.inst.ADDD;
import com.sb.core.inst.AND;
import com.sb.core.inst.ANDI;
import com.sb.core.inst.BEQ;
import com.sb.core.inst.BNE;
import com.sb.core.inst.DADD;
import com.sb.core.inst.DADDI;
import com.sb.core.inst.DIVD;
import com.sb.core.inst.DSUB;
import com.sb.core.inst.DSUBI;
import com.sb.core.inst.HLT;
import com.sb.core.inst.Instruction;
import com.sb.core.inst.JUMP;
import com.sb.core.inst.LD;
import com.sb.core.inst.LW;
import com.sb.core.inst.MULD;
import com.sb.core.inst.OR;
import com.sb.core.inst.ORI;
import com.sb.core.inst.SD;
import com.sb.core.inst.SUBD;
import com.sb.core.inst.SW;
import com.sb.core.prog.Program;
import com.sb.core.register.FPRegC;
import com.sb.core.register.FPRegister;
import com.sb.core.register.IntRegC;
import com.sb.core.register.IntegerRegister;
import com.sb.core.register.Register;

public class ProgramParser {
	
	// Single instance.
	private static ProgramParser instance = null;
	private InstructionParser instParser = null;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected ProgramParser() {
		instParser = InstructionParser.getInstance();
	}

	public static ProgramParser getInstance() {
		if (instance == null) {
			instance = new ProgramParser();
		}
		return instance;
	}
	
	public Program parseProgram(String path) {
		Program prog = null;
		BufferedReader inStream = null;
		
        try {
        	// Initialize the program object.
        	prog = new Program();
        	int order = 1;
        	
            inStream = new BufferedReader(new FileReader(path));
            String str;
            while ((str = inStream.readLine()) != null) {
                Instruction inst = instParser.parseInst(str.trim());
           
                System.out.println();
                
                if (inst == null) {
                	log.error("Could not parse the instruction: " + str);
                } else {             	
                	inst.setOrder(order);
                	inst.setRawInst(str);
                	prog.setInst(inst);
                }
                
                // Increase the order. The 'order' can be used 
                // to pre process the program, and check if its valid.
                // If some instruction parsing fails, instructions would
                // be out of order.
                order++;
            }
        } catch (IOException e) {
        	log.error("Program file does not exist. Please check the path: " + path);
		} finally {
            if (inStream != null) {
                try {
					inStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        
        return prog;
	}
}
