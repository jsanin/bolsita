package com.sanin.bolsita.server;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BolsitaMessageBBVA extends BolsitaMessageBase {

	private static final Logger log = Logger.getLogger(BolsitaMessageBBVA.class.getName());

	
	private static final String LOC_PREFIX = "establecimiento";
	private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
	private static final String LOC_REGEX = LOC_PREFIX + "\\(([\\w\\W\\s]*)\\)";
	private static final String VLR_REGEX = "\\$\\s[\\d\\,\\.]*\\s*";
	private static final String TCNUM_REGEX = "No\\.\\s\\d{4}";
	private static final String MATCHES_REGEX = "[\\w\\W\\s]*BBVA@bbvanet\\.com\\.co[\\w\\W\\s]*Estimado \\(a\\) Cliente:[\\w\\W\\s]*Te informamos que el[\\w\\W\\s]*";
	
	private static Pattern datePattern = Pattern.compile(DATE_REGEX);
	private static Pattern locPattern = Pattern.compile(LOC_REGEX);
	private static Pattern vlrPattern = Pattern.compile(VLR_REGEX);
	private static Pattern tcnumPattern = Pattern.compile(TCNUM_REGEX);

	private String msg = null;
	
	public BolsitaMessageBBVA(String msg) {
		super(msg);
		this.msg = msg;
	}

	@Override
	public String getEstablecimiento() {
		String ret = null;
		Matcher m2 = locPattern.matcher(this.msg);
		if (m2.find()) {
			int estLength = LOC_PREFIX.length();
			ret = this.msg.substring(estLength + m2.start() + 1, m2.end() - 1);
			//System.out.print(" Lugar: "	+ ret);

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
		Matcher m4 = tcnumPattern.matcher(this.msg);
		if (m4.find()) {
			String strTcNum = this.msg.substring(m4.start(), m4.end()); 
			//System.out.println(" tcnum: " + strTcNum);
			ret = strTcNum.substring(4);

		} else {
			log.log(Level.WARNING, " tcnum: sin valor");
			// no encontré el número de la TC
		}
		return ret;
	}

	@Override
	public String getResponsable() {
		String superResp = super.getResponsable();
		String ret = null;
		if(superResp == null) {
			String numeroTC = getNumeroTC();
			if(numeroTC != null && numeroTC.equals("1692")) {
				ret = getResponsableJuan();
			} else if(numeroTC != null && numeroTC.equals("3518")) {
				ret = getResponsableIsa();
			}
		} else {
			ret = superResp;
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
	
	private String getResponsableIsa() {
		return "isa";
	}

	private String getResponsableJuan() {
		return "juan";
	}


}
