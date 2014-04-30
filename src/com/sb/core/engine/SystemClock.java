package com.sb.core.engine;

public final class SystemClock {

	private static int Clock = 1;

	public static int getClock() {
		return Clock;
	}

	public static void incrementClock() {
		Clock += 1;
	}
}
