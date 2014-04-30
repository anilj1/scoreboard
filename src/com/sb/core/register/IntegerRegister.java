package com.sb.core.register;

public class IntegerRegister extends Register{

	private int Value;
	private IntRegC Id;
	
	public IntegerRegister(String value, String name) {
		super(name);
		this.Value = Integer.parseInt(value);
		this.Id = IntRegC.valueOf(name);
	}
	
	public int getValue() {
		return Value;
	}
	
	public void setValue(int value) {
		Value = value;
	}

	public IntRegC getId() {
		return Id;
	}
	
	@Override
	public String toString() {
		String str = "";
		System.out.println(Value);
		return str;
	}
}
