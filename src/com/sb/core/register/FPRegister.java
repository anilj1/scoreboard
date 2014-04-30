package com.sb.core.register;

public class FPRegister extends Register {
	
	private float Value;
	FPRegC Id;

	public FPRegister(String value, String name) {
		super(name);
		this.Value = Float.parseFloat(value);
		this.Id = FPRegC.valueOf(name);
	}

	public float getValue() {
		return Value;
	}
	
	public void setValue(float value) {
		Value = value;
	}
	
	public FPRegC getId() {
		return Id;
	}
	
	@Override
	public String toString() {
		String str = new String("");
		System.out.println(Value);
		return str;
	}
}
