package com.sanin.bolsita.shared;

import java.util.Date;

import javax.persistence.Id;

@SuppressWarnings("serial")
public class MensajeReporte implements java.io.Serializable {
	@Id Long id;
	Date fechaRecibido;
	String contenido;
	String fromAddress;
	String toAddress;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getFechaRecibido() {
		return fechaRecibido;
	}
	public void setFechaRecibido(Date fechaRecibido) {
		this.fechaRecibido = fechaRecibido;
	}
	public String getContenido() {
		return contenido;
	}
	public void setContenido(String contenido) {
		this.contenido = contenido;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	
}
