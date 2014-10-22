package com.sanin.bolsita.shared;

@SuppressWarnings("serial")
public class BolsitaException extends RuntimeException {
	
	public BolsitaException() {
		super();
	}
	
	public BolsitaException(String msg){
		super(msg);
		
	}
	
	public BolsitaException(String msg, Throwable t){
		super(msg, t);
		
	}
	
	public BolsitaException(Throwable t){
		super(t);
		
	}


}
