package com.sanin.bolsita.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.googlecode.objectify.Query;
import com.sanin.bolsita.shared.BolsitaException;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.MensajeReporte;
import com.sanin.bolsita.shared.ReporteBolsita;

public class MailHandlerModel {
	
	private static final Logger log = Logger.getLogger(MailHandlerModel.class.getName());
	
	private static final String VLR_PERSONAL_REGEX = "\\s\\d[\\d\\,\\.]*\\s*";
	private static final String BOLSITA_EMAIL_REGEX = "[\\w\\.]*@reportebolsita.appspotmail.com";
	
	private static Pattern vlrPersonalPattern = Pattern.compile(VLR_PERSONAL_REGEX);
	private static Pattern bolsitaEmailPattern = Pattern.compile(BOLSITA_EMAIL_REGEX);
	
	private static final int MAX_LOC_LENGTH = 40;
	
	private PresupuestoMes presupuestoMes = null;
	
	public MailHandlerModel() {
		// TODO: Que este presupuesto quede en la BD por usuario
		presupuestoMes = new PresupuestoMes();
		presupuestoMes.setPresupuestoTotal(9_000f);
		HashMap<String, Float> presuCat = new HashMap<>();
		presuCat.put("belleza", 300f);
		presuCat.put("carro", 570f);
		presuCat.put("diversion", 910f);
		presuCat.put("gastosisa", 520f);
		presuCat.put("gastosjuan", 375f);
		presuCat.put("gladys", 150f);
		presuCat.put("hogar", 3_850f);
		presuCat.put("imprevistos", 300f);
		presuCat.put("mercado", 1_240f);
		presuCat.put("princess", 660f);
		presuCat.put("salud", 950f);
		presupuestoMes.setPresupuestoCategoria(presuCat);
		presupuestoMes.setConsumoPresupuestoParaReporte(0.75f);
		
	}
	
    public void processEmail(Message message) {
    	
    	MailHandlerDAO mhdao = new MailHandlerDAO();
    	try {
            String toAdd = message.getRecipients(RecipientType.TO)[0].toString();
            String username = getUsernameFromAdd(toAdd);
            BolsitaUser buser = getUser(username);
            if(buser == null) {
            	log.warning("Usuario no existe en la BD: " + toAdd);
            	// TODO: Adicionar email de respuesta indicándole los pasos a seguir para registrarse
            	return;
            }
            Object content = message.getContent();
            int count = 0;
            Object c = null;
            
            
            if (content instanceof Multipart) {
                Multipart mp = (Multipart)content;
                 
                count = mp.getCount();
                if(count > 0) {
                    c = mp.getBodyPart(0).getContent();
                } else {
                	throw new BolsitaException("El mensaje tiene 0 partes");
                }
            } else if (content instanceof String) {
            	count = 1;
            	c = content;
            } else {
	            String strTipoContent = content.getClass().getName();
	            
	        	String errorMsg = "El contenido del mensaje no es Multipart, es: " + strTipoContent;
	        	log.severe(errorMsg);
	        	throw new BolsitaException(errorMsg);
	        }

            
            Address[] addrs = message.getFrom();
            StringBuffer sb = new StringBuffer();
            for (Address address : addrs) {
            	sb.append(address.toString());
			}
            log.info("Direcciones from: " + sb.toString());
            
            addrs = message.getRecipients(RecipientType.TO);
            sb.delete(0, sb.length());
            for (Address address : addrs) {
            	sb.append(address.toString());
			}
            log.info("Direcciones to: " + sb.toString());
            
            log.info("Mensaje from: " + message.getFrom()[0] +
            		"to: " + message.getRecipients(RecipientType.TO)[0] +
            		", fecha envío: " + message.getSentDate() + 
            		", subject: " + message.getSubject() +
            		", partes: " + count);
            String fromAdd = ((javax.mail.internet.InternetAddress)message.getFrom()[0]).getAddress();
            if (c instanceof String) {
            	log.info("mensaje parte1: \n" + c);
            	ReporteBolsita rb = null;
            	BolsitaMessage bm = recognizeMessage((String)c);
            	// TODO: reconocer el usuario al que le envían el email. sino existe ignorar. si existe asignar reporte bolsita
            	if(bm != null) {
            		rb = getReporteBolsita(bm);
            		rb.setReportadoPor(fromAdd);
					rb.setUserId(buser.getId());
                	log.info(rb.toString());
                	if(rb.isComplete()) {
                		// se almacena en la BD si se pudo recolectar toda la info del mensaje
                		mhdao.saveReporteBolsita(rb);
                		notificarIngresoExitoso(message, rb);
                		notificarEstadoGastos(message, buser);
                	} else {
                		log.warning("El objeto ReporteBolsita no está completo");
                		notificarIngresoFallido(message, rb);
                		saveMessageForReview(message, (String)c);
                		// TODO: poner una pantalla de revisión de los que no fueron ingresados
                	}
            	} else {
            		boolean exitoso = true;
            		String strMensajeFallido = null;
            		List<ReporteBolsita> rbs = null;
            		try {
                		rbs = getPersonInfoContent((String)c, fromAdd, buser);
                		if(rbs != null && rbs.size() > 0) {
                			// tiene al menos un reporte bolsita
                    		mhdao.saveReporteBolsita(rbs);
                    		notificarIngresoExitoso(message, rbs);
                    		notificarEstadoGastos(message, buser);
                		} else {
                			exitoso = false;
                			strMensajeFallido = "No se encontró ningún reporte bolsita en el mensaje, " +
                					"recuerde que el formato de número debe ser con '.'";
                		}
            		} catch(Exception e) {
            			log.log(Level.SEVERE, "Error en registro múltiple de reporte bolsita");
            			exitoso = false;
            			strMensajeFallido = "Ocurrió un error en el registro del reporte: " + e.getMessage();
            		}
            		if(!exitoso) {
            			// si el registro no fue exitoso
                		log.warning("El registro múltiple de reportes bolsita no fue exitoso: " + 
                				strMensajeFallido);
                		notificarIngresoFallido(message, rbs, strMensajeFallido);
                		saveMessageForReview(message, (String)c);
            			
            		}
            	} // TODO: poner mensaje de email no soportado
//            	else {
//            		throw new BolsitaException("Email no soportado: " + toAdd);
//            	}
            	
            	
            	
            } else {
            	throw new BolsitaException("La parte 1 del mensaje no es String");
            }/*
        	if(count > 1) {
                BodyPart bp2 = mp.getBodyPart(1);
                Object c2 = bp2.getContent();
                if (c2 instanceof String) {
                	log.info("mensaje parte2: \n" + c2);
                }

        	}*/
                 
    		
    	} catch(BolsitaException e) {
    		log.log(Level.WARNING, e.getMessage());
    		throw e;
    	} catch(Exception e) {
    		log.log(Level.SEVERE, "Error inesperado: " + e.getMessage());
    		throw new BolsitaException(e);
    	}
        
        
    }

    private void notificarEstadoGastos(Message message, BolsitaUser buser) {
		try {

			List<ReporteBolsita> reports = getReports(buser.getId(), getIndexCurrentMonth());
			
			DecimalFormat df = new DecimalFormat(getFormatNumberPattern());
			DecimalFormat dfPercent = new DecimalFormat(getPercentagePattern());
			
			HashMap<String, Float> catsSum = new HashMap<String, Float>();
			double total = 0l;
			boolean superaLimite = false;
			
			for (ReporteBolsita rb : reports) {
			    String cat = "";
			    if(rb.getCategoria() != null && rb.getCategoria().length > 0) {
			    	cat = rb.getCategoria()[0];
			    }
			    float vlr = rb.getValor();
	   
			    Float catAcum = catsSum.get(cat);
			    if(catAcum == null) {
			    	catAcum = new Float(0);
			    }
			    catAcum+=vlr;
			    catsSum.put(cat, catAcum);
			    
			    total+=vlr;
	
			    if(superaLimite == false) {
				    Float presuCat = presupuestoMes.getPresupuestoCategoria().get(cat);
				    if(presuCat != null && presuCat > 0) {
				    	if(catAcum/presuCat > presupuestoMes.getConsumoPresupuestoParaReporte()) {
				    		superaLimite = true;
				    	}
				    }
				    
				    if(total/presupuestoMes.getPresupuestoTotal() > presupuestoMes.getConsumoPresupuestoParaReporte()) {
			    		superaLimite = true;
			    	}
			    }
			    
			}
			
			if(superaLimite) {
				// si se supera el límite para reporte se envía la notificación por email del estado de los gastos
				StringBuilder sb = new StringBuilder();
				setMessageHeader(sb);
				
	//			sb.append("Hola ").append(toAddName).append("<br><br>");
				sb.append("Estas recibiendo este mensaje porque superaste al menos uno de los presupuestos en más del <b>").
					append(dfPercent.format(presupuestoMes.getConsumoPresupuestoParaReporte())).append("</b>. A continuación el estado de tus gastos:<br>");
				
				sb.append("<table class=\"gridtable\">");
				sb.append("<tr>");
				sb.append("<th>Categoría</th><th>Presupuesto</th><th>Gasto real</th><th>% de ejecución</th>");
				sb.append("</tr>");
				
				Set<Entry<String,Float>> keys = catsSum.entrySet();
				for (Iterator<Entry<String, Float>> iter = keys.iterator(); iter.hasNext();) {
					Entry<String, Float> cat = iter.next();
					
					Float presuCat = presupuestoMes.getPresupuestoCategoria().get(cat.getKey());
					
					sb.append("<tr>");
					sb.append("<td>").append(cat.getKey()).append("</td>");
					sb.append("<td>").append(presuCat==null?"":df.format(presuCat)).append("</td>");
					sb.append("<td>").append(cat.getValue()==null?"":df.format(cat.getValue())).append("</td>");
					sb.append("<td>").append(presuCat==null||cat.getValue()==null?"":dfPercent.format(cat.getValue()/presuCat)).append("</td>");
					sb.append("</tr>");
				}
				
				sb.append("<tr>");
				sb.append("<td>").append("Total").append("</td>");
				sb.append("<td>").append(
						presupuestoMes.getPresupuestoTotal()==null?
								"":df.format(presupuestoMes.getPresupuestoTotal())).append("</td>");
				sb.append("<td>").append(df.format(total)).append("</td>");
				sb.append("<td>").append(presupuestoMes.getPresupuestoTotal()==null?"":dfPercent.format(total/presupuestoMes.getPresupuestoTotal())).append("</td>");
				sb.append("</tr>");
	
				sb.append("</table>");
				sb.append("<br><br>");
				sb.append("Continúa con más registros!!!<br><br>").append("Att: Reporte Bolsita Staff");
				setMessageFooter(sb);
				sendEmail(message, getSubjectLimitePresupuesto(), sb.toString());
			}
			
		} catch(Exception e){
			throw new BolsitaException("Error en el envío de email de superación límite", e);
		}

		
	}
    
	private String getSubjectLimitePresupuesto() {
		return "ALERTA: Presupuesto próximo a superarse";
	}

	protected String getDateFormatPattern() {
		return "dd/MM/yyyy";
	}

	protected String getFormatNumberPattern() {
		return "#,##0";
	}

	private String getPercentagePattern() {
		return "0.00%";
	}

    
	private int getIndexCurrentMonth() {
		SimpleDateFormat dtf = new SimpleDateFormat("M");
		String m = dtf.format(new Date());
		return Integer.valueOf(m) - 1;
	}


	private String getUsernameFromAdd(String toAdd) {
    	String username = null;
		Matcher m3 = bolsitaEmailPattern.matcher(toAdd);
		if (m3.find()) {
			String email = toAdd.substring(m3.start(), m3.end());
			int indexArroba = email.indexOf('@');
			 username = email.substring(0, indexArroba);

		} else {
			log.log(Level.WARNING, "Username no encontrado en dirección: " + toAdd);
		}
		return username;
	}

/*
    public void handleMultipart(Multipart mp) throws Exception
    {
        int count = mp.getCount();
        for (int i = 0; i < count; i++)
        {
            BodyPart bp = mp.getBodyPart(i);
            Object content = bp.getContent();
            if (content instanceof String)
            {
            	//log.info("** parte " + i + " es STRING: \n" + content);
            	log.info("** parte " + i + " es STRING mime/type:" + mp.getContentType());
            	
            	getInfoContent((String)content);
            	
            }
            else if (content instanceof InputStream)
            {
            	log.info("** es un InputStream");
            }
            else if (content instanceof Message)
            {
                Message message = (Message)content;
                handleMessage(message);
            }
            else if (content instanceof Multipart)
            {
                Multipart mp2 = (Multipart)content;
                handleMultipart(mp2);
            }
        }
    }
*/

	private ReporteBolsita getReporteBolsita(BolsitaMessage bm) {
		ReporteBolsita rb = new ReporteBolsita();
		boolean complete = true;
		rb.setEstablecimiento(bm.getEstablecimiento());
		if(rb.getEstablecimiento() == null) complete = false;
		
		rb.setFecha(bm.getFecha());
		if(rb.getFecha() == null) complete = false;

		rb.setValor(bm.getValor());
		if(rb.getValor() < 0) complete = false;
		
		rb.setNumeroTC(bm.getNumeroTC());
		rb.setResponsable(bm.getResponsable());
		
		rb.setTcEfectivo(bm.getTcDebito());
		if(rb.getTcEfectivo() == null) {
			rb.setTcEfectivo(getEfectivo());
		}
		if(bm.getCategoria() != null) {
			rb.setCategoria(new String[]{bm.getCategoria()});
		}
		
		rb.setComplete(complete);
		
		return rb;
	}

	private BolsitaMessage recognizeMessage(String c) {
		BolsitaMessage bm = null;
		if(BolsitaMessageBBVA.matches(c)) {
			// es un mensaje del bbva
			bm = new BolsitaMessageBBVA(c);
		} else if(BolsitaMessageBOCredencial.matches(c)) {
			bm = new BolsitaMessageBOCredencial(c);
		} else if(BolsitaMessageStandard.matches(c)) {
			// es un mensaje estándar
			bm = new BolsitaMessageStandard(c);
		}
		
		return bm;
	}

	private void notificarIngresoExitoso(Message message, String bodyMessage) {
		try {
	        sendEmail(message, getSubjectExitoso(), bodyMessage);
			
		} catch(Exception e){
			throw new BolsitaException("Error en el envío de email de reporte exitoso", e);
		}
		
	}

	private void sendEmail(Message message, String subject, String bodyMessage)
			throws MessagingException, UnsupportedEncodingException,
			AddressException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String from = getFrom();
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from, getFromName()));
		msg.setReplyTo(new Address[]{new InternetAddress(from)});
		msg.addRecipient(Message.RecipientType.TO,
				 message.getFrom()[0]);
		msg.setSubject(subject, "utf16");
		
		
		Multipart mp = new MimeMultipart();

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(bodyMessage, "text/html");
		mp.addBodyPart(htmlPart);

		//log.info("Mensaje de email: " + message);
//	        msg.setText(message, "text/html");
		msg.setContent(mp);
		Transport.send(msg);
	}

    
    private void notificarIngresoExitoso(Message message,
			List<ReporteBolsita> rbs) {
    	try {
			notificarIngresoExitoso(message, getMessageExitosoMultiple(message.getFrom()[0].toString(), rbs));
		} catch (MessagingException e) {
			throw new BolsitaException("Error en el envío de email de reporte exitoso", e);
		} catch(BolsitaException e) {
			throw e;
		}

	}

	private String getMessageExitosoMultiple(String toAddName, List<ReporteBolsita> rbs) {
		StringBuilder sb = new StringBuilder();
		setMessageHeader(sb);

		
		sb.append("Hola ").append(toAddName).append("<br><br>");
		sb.append("Se ingresaron satisfactoriamente <b>").append(rbs.size()).append("</b> reportes:<br>");
		
		sb.append("<table class=\"gridtable\">");
		sb.append("<tr>");
		sb.append("<th>Fecha</th><th>Establecimiento</th><th>Valor</th><th>Categoría</th><th>Responsable</th><th>TC/Efectivo</th>");
		sb.append("</tr>");
		for (ReporteBolsita rb : rbs) {
			sb.append("<tr>");
			sb.append("<td>").append(rb.getFecha()).append("</td>");
			sb.append("<td>").append(rb.getEstablecimiento()).append("</td>");
			sb.append("<td>").append(rb.getValor()).append("</td>");
			String c = "";
			if(rb.getCategoria() != null && rb.getCategoria().length > 0) {
				c = rb.getCategoria()[0];
			}
			sb.append("<td>").append(c).append("</td>");
			sb.append("<td>").append(rb.getResponsable()).append("</td>");
			sb.append("<td>").append(rb.getTcEfectivo()).append("</td>");
			sb.append("</tr>");
		}

		sb.append("</table>");
		sb.append("<br><br>");
		sb.append("Continúa con más registros!!!<br><br>").append("Att: Reporte Bolsita Staff");
		setMessageFooter(sb);
		return sb.toString();
	}

	private void setMessageFooter(StringBuilder sb) {
		sb.append("</body></html>");
	}

	private void setMessageHeader(StringBuilder sb) {
		sb.append("<html><head>");
		sb.append("<style type=\"text/css\">");
		sb.append("table.gridtable {");
		sb.append("	font-family: verdana,arial,sans-serif;");
		sb.append("	font-size:11px;");
		sb.append("	color:#333333;");
		sb.append("	border-width: 1px;");
		sb.append("		border-color: #666666;");
		sb.append("	border-collapse: collapse;");
		sb.append("	}");
		sb.append("	table.gridtable th {");
		sb.append("	border-width: 1px;");
		sb.append("	padding: 8px;");
		sb.append("	border-style: solid;");
		sb.append("	border-color: #666666;");
		sb.append("	background-color: #dedede;");
		sb.append("	}");
		sb.append("	table.gridtable td {");
		sb.append("		border-width: 1px;");
		sb.append("				padding: 8px;");
		sb.append("	border-style: solid;");
		sb.append("	border-color: #666666;");
		sb.append("	background-color: #ffffff;");
		sb.append("	}");
		sb.append("	</style></head><body style=\"font-family: verdana,arial,sans-serif;\">");
	}

	private List<ReporteBolsita> getPersonInfoContent(String c, String fromAdd, BolsitaUser buser) throws IOException {
		BufferedReader bf = new BufferedReader(new StringReader(c));
		String line = null;
		List<ReporteBolsita> rbs = new ArrayList<ReporteBolsita>();
		int i = 0;
		while((line = bf.readLine()) != null) {
			Matcher m = vlrPersonalPattern.matcher(line);
			if (m.find()) {
				String strVlr = null;
				try {
					strVlr = line.substring(m.start(), m.end());
					float vlr = Float.parseFloat(strVlr);
					String establecimiento = line.substring(0, m.start());
					if(establecimiento == null || establecimiento.length() < 3) {
						log.warning("La línea número " + i + " del mensaje enviado a " + buser.getUsername() + 
								" tiene un nombre de establecimiento muy corto (mínimo 3 caracteres): " + establecimiento);
						i++;
						continue;
					}
					if(establecimiento.length() > MAX_LOC_LENGTH) {
						establecimiento = establecimiento.substring(0, MAX_LOC_LENGTH);
					}

					BolsitaMessageStandard bmStandard = new BolsitaMessageStandard(line);

					String categ = bmStandard.getCategoria();
					String responsable = bmStandard.getResponsable();
					String tcEfectivo = bmStandard.getTcDebito();
					
					ReporteBolsita rb = new ReporteBolsita();
					rb.setEstablecimiento(establecimiento);
					rb.setFecha(Calendar.getInstance(TimeZone.getTimeZone("GMT-5")).getTime());
					if(responsable != null) {
						rb.setResponsable(responsable);
					} else {
						//rb.setResponsable(buser.getUsername());
						rb.setResponsable(fromAdd);
					}
					rb.setTcEfectivo(tcEfectivo == null?getEfectivo():tcEfectivo);
					rb.setValor(vlr);
					rb.setComplete(true);
					rb.setReportadoPor(fromAdd);
					rb.setUserId(buser.getId());
					rb.setCategoria(new String[]{categ});
					rbs.add(rb);
				} catch(NumberFormatException nfe) {
					log.warning("La línea número " + i + " del mensaje enviado a " + buser.getUsername() + 
							" no tiene un número válido: " + strVlr);
				}
			} else {
				//log.info("");
			}
			i++;
		}
		return rbs;
		
	}

	private Long getUserId(String username) {
		BolsitaUser udao = getUser(username);
		return udao != null?udao.getId():null;
	}
	
	private BolsitaUser getUser(String username) {
		MailHandlerDAO dao = new MailHandlerDAO();
		return dao.findUser(username);
		
	}

	private String getEfectivo() {
		return "efectivo";
	}

	private void saveMessageForReview(Message message, String c) throws MessagingException {
		MensajeReporte mr = new MensajeReporte();
		mr.setContenido(c);
		mr.setFechaRecibido(Calendar.getInstance(TimeZone.getTimeZone("GMT-5")).getTime());
		mr.setFromAddress(((javax.mail.internet.InternetAddress)message.getFrom()[0]).getAddress());
		mr.setToAddress(((javax.mail.internet.InternetAddress)message.getRecipients(RecipientType.TO)[0]).getAddress());
		
		MailHandlerDAO mhdao = new MailHandlerDAO();
		mhdao.saveMensajeReporte(mr);
		
		
	}

	private void notificarIngresoFallido(Message message, ReporteBolsita rb) {
    	try {
    		notificarIngresoFallido(message, getMessageFallido(message.getFrom()[0].toString(), rb));
		} catch (MessagingException e) {
			throw new BolsitaException("Error en el envío de email de reporte fallido", e);
		} catch(BolsitaException e) {
			throw e;
		}
		
	}

	private void notificarIngresoFallido(Message message, List<ReporteBolsita> rbs, 
			String strMensajeFallido) {
    	try {
    		notificarIngresoFallido(message, 
    				getMessageFallidoMultiple(message.getFrom()[0].toString(), rbs, strMensajeFallido));
		} catch (MessagingException e) {
			throw new BolsitaException("Error en el envío de email de reporte fallido", e);
		} catch(BolsitaException e) {
			throw e;
		}
		
	}

	
	private String getMessageFallidoMultiple(String toAddName,
			List<ReporteBolsita> rbs, String strMensajeFallido) {
	
		StringBuilder sb = new StringBuilder();
		sb.append("Hola ").append(toAddName).append("<br><br>");
		sb.append("Ha ocurrido un error en el ingreso de alguno de los siguientes reporte bolsita:<br>");
		sb.append("<font color='#FF0000'>").append(strMensajeFallido).append("</font><br><br>");
		if(rbs != null) {
			for (ReporteBolsita rb : rbs) {
				sb.append(rb).append("<br>");
			}
			
		} else {
			sb.append("null<br>");
		}
		sb.append("<br><br>");
		sb.append("Att: Reporte Bolsita Staff");
		return sb.toString();
	}

	private void notificarIngresoFallido(Message message, String bodyMessage) {
		try {
			sendEmail(message, getSubjectFallido(), bodyMessage);			
		} catch(Exception e){
			throw new BolsitaException("Error en el envío de email de reporte fallido", e);
		}
		
	}

	
	private String getMessageFallido(String toAddName, ReporteBolsita rb) {
		return "Hola " + toAddName + "<br><br>" +
				"Ha ocurrido un <b>error</b> en el ingreso del siguiente reporte bolsita<br>" +
				rb + "<br><br>" +
				"Att: Reporte Bolsita Staff";
	}

	private String getSubjectFallido() {
		return "Reporte no ingresado";
	}

	private void notificarIngresoExitoso(Message message, ReporteBolsita rb) {
    	try {
    		notificarIngresoExitoso(message, getMessageExitoso(message.getFrom()[0].toString(), rb));
		} catch (MessagingException e) {
			throw new BolsitaException("Error en el envío de email de reporte exitoso", e);
		} catch(BolsitaException e) {
			throw e;
		}

	}

	private String getMessageExitoso(String toAddName, ReporteBolsita rb) {
		List<ReporteBolsita> l = new ArrayList<ReporteBolsita>();
		l.add(rb);
		return getMessageExitosoMultiple(toAddName, l);
//		return "Hola " + toAddName + "<br><br>" +
//				"El siguiente reporte de bolsita ha sido ingresado <b>satisfactoriamente</b><br>" +
//				rb + "<br><br>" +
//				"Continúa con más registros!!!<br><br>" +
//				"Att: Reporte Bolsita Staff";
	}

	private String getSubjectExitoso() {
		return "Reporte ingresado";
	}

	private String getFromName() {
		return "Reporte Bolsita staff";
	}

	private String getFrom() {
		return "jsanin@gmail.com";
	}

	
	/**
	 * Retorna los reportes del usuario userId del año actual
	 * @param userId
	 * @return
	 */
	public List<ReporteBolsita> getUserReports(Long userId) {
		return getUserReports(userId, -1, -1);
	}
	
	/**
	 * Retorna los reportes del usuario userId en los años especificados en los parámetros. Los parámetros
	 * annoInicio y annoFin se setean sólo si son mayores a 0 en caso contrario no se setean, el sistema
	 * asume el año actual
	 * @param userId
	 * @param annoInicio
	 * @param annoFin
	 * @return
	 */
	public List<ReporteBolsita> getUserReports(Long userId, int annoInicio, int annoFin) {
		MailHandlerDAO dao = new MailHandlerDAO();

		Calendar calIni = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		calIni.set(Calendar.MONTH, 0);
		calIni.set(Calendar.DAY_OF_MONTH, 1);
		calIni.set(Calendar.HOUR_OF_DAY, 0);
		calIni.set(Calendar.MINUTE, 0);
		calIni.set(Calendar.SECOND, 0);
		calIni.set(Calendar.MILLISECOND, 0);

		Calendar calFin = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		calFin.set(Calendar.MONTH, 12);
		calFin.set(Calendar.DAY_OF_MONTH, 1);
		calFin.set(Calendar.HOUR_OF_DAY, 0);
		calFin.set(Calendar.MINUTE, 0);
		calFin.set(Calendar.SECOND, 0);
		calFin.set(Calendar.MILLISECOND, 0);

		if(annoInicio > 0) {
			calIni.set(Calendar.YEAR, annoInicio);
		}
		if(annoFin > 0) {
			calFin.set(Calendar.YEAR, annoFin);
		}
		
		Query<ReporteBolsita> reports = dao.getReports(userId, calIni.getTime(), calFin.getTime());
		return reports.list();
	}

	public List<ReporteBolsita> getReports(Long userId, int month) {
		MailHandlerDAO dao = new MailHandlerDAO();
		Calendar calIni = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		calIni.set(Calendar.MONTH, month);
		calIni.set(Calendar.DAY_OF_MONTH, 1);
		calIni.set(Calendar.HOUR_OF_DAY, 0);
		calIni.set(Calendar.MINUTE, 0);
		calIni.set(Calendar.SECOND, 0);
		calIni.set(Calendar.MILLISECOND, 0);

		Calendar calFin = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
		calFin.set(Calendar.MONTH, month + 1);
		calFin.set(Calendar.DAY_OF_MONTH, 1);
		calFin.set(Calendar.HOUR_OF_DAY, 0);
		calFin.set(Calendar.MINUTE, 0);
		calFin.set(Calendar.SECOND, 0);
		calFin.set(Calendar.MILLISECOND, 0);
	
		// para que la consulta se haga sobre un año en particular
//		calIni.set(Calendar.YEAR, 2000);
//		calFin.set(Calendar.YEAR, 2020);
		
		Query<ReporteBolsita> reports = dao.getReports(userId, calIni.getTime(), calFin.getTime());
		return reports.list();
	}

	public BolsitaUser updateUser(BolsitaUser user) {
		MailHandlerDAO dao = new MailHandlerDAO();
		BolsitaUser udao = dao.findUser(user.getUsername());
		if(udao != null) {
			// usuario existe en la bd
			user = udao;
		}
		
		user.setLastLogin(Calendar.getInstance(TimeZone.getTimeZone("GMT-5")).getTime());
		user = dao.updateUser(user);
		return user;
	}

	public ReporteBolsita updateReporte(Long userId, ReporteBolsita rb) {
		MailHandlerDAO dao = new MailHandlerDAO();
		ReporteBolsita daoRb = dao.getReporte(userId, rb.getId());
		daoRb.setFecha(getFecha(rb.getStrFecha()));
		daoRb.setEstablecimiento(rb.getEstablecimiento());
		daoRb.setCategoria(rb.getCategoria());
		daoRb.setResponsable(rb.getResponsable());
		daoRb.setValor(Float.parseFloat(rb.getStrValor()));
		daoRb.setTcEfectivo(rb.getTcEfectivo());
		daoRb = dao.updateReporte(daoRb);
		return daoRb;
	}

//	private float getValor(String strValor) {
//		char[] charVlr = strVlr.toCharArray();
//		StringBuilder sb = new StringBuilder();
//		for (char c : charVlr) {
//			if(Character.isDigit(c)) {
//				sb.append(c);
//			}
//			if(c == '.') {
//				break;
//			}
//		}
//		
//		return Float.parseFloat(sb.toString());
//
//	}

	private Date getFecha(String strDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT-5"));
		cal.set(Integer.parseInt(strDate.substring(6,10)),
				Integer.parseInt(strDate.substring(3, 5)) - 1,
				Integer.parseInt(strDate.substring(0, 2)), 0, 0, 0);

		return cal.getTime();

	}

	public ReporteBolsita deleteReporte(Long userId, ReporteBolsita rb) {
		MailHandlerDAO dao = new MailHandlerDAO();
		ReporteBolsita daoRb = dao.getReporte(userId, rb.getId());
		dao.deleteReporte(daoRb);
		return daoRb;
	}
	
	public Map<Integer, Map<String, Float>> getReportsByMonthAndCat(Long userId) {
		List<ReporteBolsita> reps = getUserReports(userId, 2000, -1);
		final String emptyCat = "SinCategoria";
		Map<Integer, Map<String, Float>> hmMonth = new LinkedHashMap<Integer, Map<String, Float>>();
		for (ReporteBolsita rb : reps) {
			Date date = rb.getFecha();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.YEAR) * 100;
			month += (cal.get(Calendar.MONTH) + 1);
			
			String[] categoria = rb.getCategoria();
			String cat = emptyCat;
			if(categoria != null && categoria.length > 0
					&& categoria[0] != null && categoria[0].trim().length() > 0) {
				cat = categoria[0].trim();
			}
			Map<String, Float> hmCat = hmMonth.get(month);
			if(hmCat == null) {
				hmCat = new HashMap<String, Float>();
				hmMonth.put(month, hmCat);
			}
			
			Float vlrCat = hmCat.get(cat);
			if(vlrCat == null) {
				vlrCat = 0f;
			}
			vlrCat += rb.getValor();
			hmCat.put(cat, vlrCat);
			
		}
		
		return hmMonth;
	}


	class PresupuestoMes {
		Map<String, Float> presupuestoCategoria = new HashMap<>();
		Float presupuestoTotal = 0f;
		/**
		 * Indica que el punto en el cual se empieza a enviar reporte del estado del 
		 * presupuesto. Así, si alguno de los presupuesto (total o categoría) superan el 
		 * % indicado por esta variable se empieza a hacer el reporte
		 */
		float consumoPresupuestoParaReporte = 0.75f;
		
		public Map<String, Float> getPresupuestoCategoria() {
			return presupuestoCategoria;
		}
		public void setPresupuestoCategoria(Map<String, Float> presupuestoCategoria) {
			this.presupuestoCategoria = presupuestoCategoria;
		}
		public Float getPresupuestoTotal() {
			return presupuestoTotal;
		}
		public void setPresupuestoTotal(Float presupuestoTotal) {
			this.presupuestoTotal = presupuestoTotal;
		}
		public float getConsumoPresupuestoParaReporte() {
			return consumoPresupuestoParaReporte;
		}
		public void setConsumoPresupuestoParaReporte(float consumoPresupuestoParaReporte) {
			this.consumoPresupuestoParaReporte = consumoPresupuestoParaReporte;
		}
	}
}
