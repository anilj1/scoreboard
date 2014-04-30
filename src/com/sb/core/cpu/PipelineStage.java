package com.sb.core.cpu;

public abstract class PipelineStage {
	public abstract int execute();

	public abstract float getClock();

	public abstract void incrementClock();
	
	public abstract boolean isEmpty();
}
