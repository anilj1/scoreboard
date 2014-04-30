package com.sb.test;

public class floatTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double fl = (double) 0.25;
		double rem = fl % 1;
		double one = 1;
		
		System.out.println("Float is: " + fl);
		System.out.println("Remin is: " + rem);
		
		if (rem < one) {
			System.out.println("reminder is less than ONE " + fl);
		} else {
			System.out.println("reminder is NOT less than ONE" + fl);
		}
	}
}
