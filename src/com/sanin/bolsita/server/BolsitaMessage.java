package com.sanin.bolsita.server;

import java.util.Date;


public interface BolsitaMessage {

	public String getEstablecimiento();
	public float getValor();
	public Date getFecha();
	public String getNumeroTC();
	public String getResponsable();
	public String getCategoria();
	public String getTcDebito();

}
