package com.sb.test;

import com.sb.core.memory.InstCache;

public class InstCacheTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		InstCache icache = InstCache.getInstance();
		
		icache.getInstruction(1);
		icache.getInstruction(2);
		icache.getInstruction(3);
		icache.getInstruction(4);
		icache.getInstruction(5);
		icache.getInstruction(6);
		icache.getInstruction(7);
		icache.getInstruction(8);
		icache.getInstruction(9);
		icache.getInstruction(10);
		icache.getInstruction(11);
		icache.getInstruction(12);
		icache.getInstruction(13);
		icache.getInstruction(14);
		icache.getInstruction(15);
		icache.getInstruction(16);
	}

}
