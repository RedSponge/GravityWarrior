package com.redsponge.upsidedownbb.utils.holders;

public class Triple<A, B, C> {

	public final A a;
	public final B b;
	public final C c;

	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}


        @Override
        public String toString() {
            return "[" + a + ", " + b + ", " + c + "]";
        }
    
}