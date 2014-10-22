package com.sanin.bolsita.server;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BolsitaMessageBase implements BolsitaMessage {

	static final String TAG_REGEX = "#[\\w\\.]{2,}\\s*";
	static Pattern tagPattern = Pattern.compile(TAG_REGEX);

	private String categoria;
	private String responsable;
	private String tipoPago;
	
	public BolsitaMessageBase(String msg) {
		String[] tags = getTags(msg);
		this.categoria = tags[0];
		this.responsable = tags[1];
		this.tipoPago = tags[2];
	}

	protected String[] getTags(String msg) {
		Matcher mtag = tagPattern.matcher(msg);
		String categ = null;
		String responsable = null;
		String tcEfectivo = null;
		if(mtag.find()) {
			// la primera que encuentra debe ser la categoria
			categ = msg.substring(mtag.start() + 1, mtag.end()).trim();
			if(mtag.find()) {
				// la segunda puede ser el responsable o tcEfectivo: tc, debito o efectivo
				String str = msg.substring(mtag.start() + 1, mtag.end()).trim();
				tcEfectivo = getTcEfectivo(str);
				if(tcEfectivo == null) {
					// si no era alguna de las palabras tc, debito o efectivo entonces se asume
					// que es el responsable y se intenta buscar un siguiente tag
					responsable = str;
					if(mtag.find()) {
						// si encuentra otro tag, este debe ser tc, debito o efectivo
						String str2 = msg.substring(mtag.start() + 1, mtag.end()).trim();
						tcEfectivo = getTcEfectivo(str2);
						if(tcEfectivo == null) {
							// si no es tc, debito o efectivo se asume que es efectivo
							tcEfectivo = getEfectivo();
						}
					} else {
						// si no encuentra otro tag se asume que es en efectivo
						tcEfectivo = getEfectivo();
					}
				} else {
					// en caso de que si haya sido alguna de las palabras tc, debito o efectivo
					// se busca si hay otro tag y este se asumirá como el responsable
					if(mtag.find()) {
						responsable = msg.substring(mtag.start() + 1, mtag.end()).trim();
					}
					
				}
			}
		}
		return new String[]{categ, responsable, tcEfectivo};
	
	}


	@Override
	public String getResponsable() {
		return this.responsable;
	}

	@Override
	public String getCategoria() {
		return this.categoria;
	}

	@Override
	public String getTcDebito() {
		return this.tipoPago;
	}

	protected String getTcEfectivo(String str) {
		String tcefe = null;
		if(str != null && (
				str.equalsIgnoreCase(getTC()) || 
				str.equalsIgnoreCase(getEfectivo()) || 
				str.equalsIgnoreCase(getDebito()) )) {
			tcefe = str.toLowerCase();
		}
		return tcefe;
		
	}

	protected float getValor(String strVlr) {
		char[] charVlr = strVlr.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : charVlr) {
			if(Character.isDigit(c)) {
				sb.append(c);
			}
			if(c == '.') {
				break;
			}
		}
		
		return Float.parseFloat(sb.toString());
	}

	/**
	 * Obtiene la fecha a partir de un texto en formato 
	 * yyyy[separador]MM[separador]dd, donde separador puede ser cualquier separador de 1 caracter.
	 * Puede ser 2013-01-24 o 2103/01/24 o cualquier otro separador de 1 caracter.
	 */
	protected Date getFecha(String strDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT-5"));
		cal.set(Integer.parseInt(strDate.substring(0, 4)),
				Integer.parseInt(strDate.substring(5, 7)) - 1,
				Integer.parseInt(strDate.substring(8, 10)), 0, 0, 0);

		return cal.getTime();

	}

	protected String getTC() {
		return "TC";
	}

	protected String getEfectivo() {
		return "efectivo";
	}
	
	protected String getDebito() {
		return "debito";
	}

}
