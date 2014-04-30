package com.sb.core.engine;

import org.apache.log4j.Logger;

import com.sb.core.cpu.ExecStage;
import com.sb.core.cpu.FetchStage;
import com.sb.core.cpu.IssueStage;
import com.sb.core.cpu.ReadStage;
import com.sb.core.cpu.WriteStage;
import com.sb.core.prog.Program;

public class PipeLine extends Thread {

	private FetchStage fetchStage = null;
	private IssueStage issueStage = null;
	private ReadStage readStage = null;
	private ExecStage execStage = null;
	private WriteStage writeStage = null;

	private boolean stopRequested = false;
	private boolean isRunning = false;
	static int cnt = 0;
	private static int clock = 0;

	Logger log = Logger.getLogger(this.getName());
	
	// Single instance.
	private static PipeLine instance = null;

	protected PipeLine() {
		fetchStage = FetchStage.getInstance();
		issueStage = IssueStage.getInstance();
		readStage = ReadStage.getInstance();
		execStage = ExecStage.getInstance();
		writeStage = WriteStage.getInstance();
		
		isRunning = true;
	}

	public static PipeLine getInstance() {
		if (instance == null) {
			instance = new PipeLine();
		}
		return instance;
	}
	
	public void loadProgram(Program prog) {
		// Execute the program.
		fetchStage.setProgram(prog);
	}
	
	public void stopPipeline() {
		stopRequested = true;
	}

	public boolean isEmpty() {
		return (clock != 0) && issueStage.isEmpty()
				&& readStage.isEmpty() && execStage.isEmpty()
				&& writeStage.isEmpty();
	}

	public static int getClock() {
		return clock;
	}
	
	public void run() {
		
		// Launch the pipeline, and execute each stage in sequence.
		while (this.isRunning) {
			
			// Start FETCG stage.
			if (fetchStage.execute() != 0) {
				// FETCH stage failed.
				log.error("FETCH stage failed. Please check");
			}
			
			// Start ISSUE stage
			if (issueStage.execute() != 0) {
				// ISSUE stage failed.
				log.error("ISSUE stage failed. Please check");
				
				// Stop the pipeline (for now)
				this.isRunning = false;
				break;
			}
			
			// Start READ stage.
			if (readStage.execute() != 0) {
				// READ stage failed.
				log.error("READ stage failed. Please check");
				
				// Stop the pipeline (for now)
				this.isRunning = false;
				break;
			}
			
			// Start EXE stage.
			if (execStage.execute() != 0) {
				// EXEC stage failed.
				log.error("EXEC stage failed. Please check");
				
				// Stop the pipeline (for now)
				this.isRunning = false;
				break;
			}
			
			// Start WRITE stage.
			if (writeStage.execute() != 0) {
				// WRITE stage failed.
				log.error("WRITE stage failed. Please check");
				
				// Stop the pipeline (for now)
				this.isRunning = false;
				break;
			}
			
			// Check pipeline is still running. 
			if (this.isEmpty()) {
				// Stop the pipeline (for now)
				log.debug("Pipeline is now empty, stopping program execution.");
				this.isRunning = false;
				break;
			} else {
				log.debug("Pipeline still running.");
			}
			
			// Sleep for 250ms;
			try {
				if (++cnt == 4) {
					cnt = 0;
					log.info("Ticking at 1sec");
				}
				PipeLine.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			log.debug("This is clock tick: " + clock++);
			System.out.println("");
		} // end of while.
	}
}
