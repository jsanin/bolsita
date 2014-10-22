package com.sanin.bolsita.server;

import java.util.Date;

public class BolsitaMessageStandard extends BolsitaMessageBase {

	public BolsitaMessageStandard(String c) {
		super(c);
	}

	@Override
	public String getEstablecimiento() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getValor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getFecha() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNumeroTC() {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean matches(String m) {
		// TODO Auto-generated method stub
		return false;
	}

}
