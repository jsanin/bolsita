package com.sanin.bolsita.shared;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.NotSaved;

@SuppressWarnings("serial")
public class ReporteBolsita implements Serializable{
	
	@Id Long id;
	Date fecha;
	@NotSaved String strFecha;
	String establecimiento;
	float valor;
	@NotSaved String strValor;
	String responsable;
	String tcEfectivo;
	String numeroTC;
	@NotSaved boolean complete;
	String reportadoPor;
	Long userId;
	String[] categoria;
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getEstablecimiento() {
		return establecimiento;
	}
	public void setEstablecimiento(String establecimiento) {
		this.establecimiento = establecimiento;
	}
	public float getValor() {
		return valor;
	}
	public void setValor(float valor) {
		this.valor = valor;
	}
	public String getResponsable() {
		return responsable;
	}
	public void setResponsable(String responsable) {
		this.responsable = responsable;
	}
	public String getTcEfectivo() {
		return tcEfectivo;
	}
	public void setTcEfectivo(String tcEfectivo) {
		this.tcEfectivo = tcEfectivo;
	}
	
	public String toString() {
		String c = "";
		if(this.getCategoria() != null && this.getCategoria().length > 0) {
			c = this.getCategoria()[0];
		}

		return "Fecha: " + fecha + ", establecimiento: " + establecimiento + ", valor: " + valor +
				", responsable: " + responsable + ", tcEfectivo: " + tcEfectivo + ", num TC: " + numeroTC +
				", categorias: " + c +
				", complete: " + complete;
	}
	public String getNumeroTC() {
		return numeroTC;
	}
	public void setNumeroTC(String numeroTC) {
		this.numeroTC = numeroTC;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public String getReportadoPor() {
		return reportadoPor;
	}
	public void setReportadoPor(String reportadoPor) {
		this.reportadoPor = reportadoPor;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String[] getCategoria() {
		return categoria;
	}
	public void setCategoria(String[] categoria) {
		this.categoria = categoria;
	}
	public String getStrFecha() {
		return strFecha;
	}
	public void setStrFecha(String strFecha) {
		this.strFecha = strFecha;
	}
	public String getStrValor() {
		return strValor;
	}
	public void setStrValor(String strValor) {
		this.strValor = strValor;
	}

}
