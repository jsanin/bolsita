package com.sanin.bolsita.server;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reconocedor del mensaje que envía el Banco de Occidente al hacer transacciones con la 
 * tarjeta de crédito Credencial
 * 
 * Ejemplos de mensajes:
 * <pre>
 ejemplo1:
Ud. realizo una compra en DELI THE PASTRY SHOP           por $27.800.Tarjeta Credencial *1452, 2013/01/23, 14:50. Informes 031-3278647 Banco de Occidente

ejemplo2:
Ud. realizo una compra en 0009800200966995           por $1.Tarjeta Credencial *1452, 2013/01/31, 19:58. Informes 031-3278647 Banco de Occidente

ejemplo3:
Ud. realizo una compra en 000980020029990           por $50.026.Tarjeta Credencial *1452, 2013/01/31, 19:58. Informes 031-3278647 Banco de Occidente

 </pre>
 * @author juan
 *
 */
public class BolsitaMessageBOCredencial extends
		BolsitaMessageBase {

	private static final Logger log = Logger.getLogger(BolsitaMessageBOCredencial.class.getName());

	
	private static final String DATE_REGEX = "\\d{4}/\\d{2}/\\d{2}";
	private static final String LOC_REGEX = "compra en[\\w\\W\\s]*por\\s\\$";
	private static final String VLR_REGEX = "\\$[\\d\\.]*\\.T";
	private static final String TCNUM_REGEX = "\\*\\d{4}";
	private static final String MATCHES_REGEX = "[\\w\\W\\s]*Ud\\. realizo una compra en[\\w\\W\\s]*por\\s\\$[\\w\\W\\s]*Tarjeta Credencial \\*[\\w\\W\\s]*Banco\\sde Occidente[\\w\\W\\s]*";
	
	private static Pattern datePattern = Pattern.compile(DATE_REGEX);
	private static Pattern locPattern = Pattern.compile(LOC_REGEX);
	private static Pattern vlrPattern = Pattern.compile(VLR_REGEX);
	private static Pattern tcnumPattern = Pattern.compile(TCNUM_REGEX);

	private String msg = null;

	public BolsitaMessageBOCredencial(String msg) {
		super(msg);
		this.msg = msg;
	}

	@Override
	public String getEstablecimiento() {
		String ret = null;
		Matcher m2 = locPattern.matcher(this.msg);
		if (m2.find()) {
			// System.out.print(" Lugar: "	+ content.substring(m2.start(), m2.end()));
			ret = this.msg.substring(m2.start() + 9, m2.end() - 5);
			if(ret != null) {
				ret = ret.trim();
			}

		} else {
			log.log(Level.WARNING, " Establecimiento: sin establecimiento");
		}
		return ret;
	}

	@Override
	public float getValor() {
		float ret = -1;
		Matcher m3 = vlrPattern.matcher(this.msg);
		if (m3.find()) {
			String strVlr = this.msg.substring(m3.start(), m3.end());
			//System.out.print(" Vlr: " + strVlr);
			float vlr = getValor(strVlr);
			ret = vlr/1000;

		} else {
			log.log(Level.WARNING, " Vlr: sin valor");
			// no encontré valor
		}
		return ret;
	}
	
	@Override
	protected float getValor(String strVlr) {
		char[] charVlr = strVlr.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : charVlr) {
			if(Character.isDigit(c)) {
				sb.append(c);
			}
		}
		
		return Float.parseFloat(sb.toString());
	}


	@Override
	public Date getFecha() {
		Matcher m = datePattern.matcher(this.msg);
		Date ret = null;
		// int count = 0;
		if (m.find()) {
			String strDate = this.msg.substring(m.start(), m.end());
			ret = getFecha(strDate);

		} else {
			log.log(Level.WARNING, "Fecha: sin fecha ");
			// no encontré fecha
		}
		return ret;
	}

	@Override
	public String getNumeroTC() {
		String ret = null;
		Matcher m = tcnumPattern.matcher(this.msg);
		if (m.find()) {
			String strTcNum = this.msg.substring(m.start(), m.end()); 
			//System.out.println(" tcnum: " + strTcNum);
			ret = strTcNum.substring(1);

		} else {
			log.log(Level.WARNING, " tcnum: sin valor");
			// no encontré el número de la TC
		}
		return ret;
	}

	@Override
	public String getTcDebito() {
		return "TC";
	}

	public static boolean matches(String m) {
		return m != null?m.matches(MATCHES_REGEX):false;
	}

}
