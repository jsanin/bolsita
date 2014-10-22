package com.sanin.bolsita.server;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.MensajeReporte;
import com.sanin.bolsita.shared.ReporteBolsita;

public class MailHandlerDAO {
	private static final Logger log = Logger.getLogger(MailHandlerDAO.class.getName());
	
    static {
        ObjectifyService.register(ReporteBolsita.class);
        ObjectifyService.register(MensajeReporte.class);
        ObjectifyService.register(BolsitaUser.class);
    }

	public ReporteBolsita saveReporteBolsita(ReporteBolsita rb) {
    	Objectify ofy = getOfy();
    	ofy.put(rb);
    	return rb;
	}
   
	public Query<ReporteBolsita> getReports() {
		Objectify ofy = getOfy();
		//Map<Object, ReporteBolsita> reports = ofy.get(ReporteBolsita.class);
		Query<ReporteBolsita> reports = ofy.query(ReporteBolsita.class).order("-fecha");
		//log.info("Query sobre ReporteBolsita ordenado por desc por fecha");
		return reports;
	}
    
	protected Objectify getOfyTxn() {
		return ObjectifyService.beginTransaction();
	}

	protected Objectify getOfy() {
		return ObjectifyService.begin();
	}

	public MensajeReporte saveMensajeReporte(MensajeReporte mr) {
    	Objectify ofy = getOfy();
    	ofy.put(mr);
    	return mr;
		
	}

	public List<ReporteBolsita> saveReporteBolsita(List<ReporteBolsita> rbs) {
    	Objectify ofy = getOfy();
    	ofy.put(rbs);
    	return rbs;
		
	}

	public Query<ReporteBolsita> getReports(Long userId, Date fechaInicial,
			Date fechaFinal) {
		Objectify ofy = getOfy();
		//Map<Object, ReporteBolsita> reports = ofy.get(ReporteBolsita.class);
		Query<ReporteBolsita> reports = ofy.query(ReporteBolsita.class).
				filter("fecha >=", fechaInicial).
				filter("fecha <", fechaFinal).
				filter("userId", userId).
				order("fecha");
		//log.info("Query sobre ReporteBolsita ordenado por desc por fecha");
		return reports;
	}

	public BolsitaUser findUser(String username) {
		Objectify ofy = getOfy();
		Iterable<BolsitaUser> users = ofy.query(BolsitaUser.class).filter("username", username);
		BolsitaUser ret = null;
		if(users.iterator().hasNext()) {
			ret = users.iterator().next();
		}
		return ret;
	}

	public BolsitaUser updateUser(BolsitaUser user) {
    	Objectify ofy = getOfy();
    	ofy.put(user);
    	return user;

	}
	
	public ReporteBolsita getReporte(Long userId, Long reporteId) {
    	Objectify ofy = getOfy();
		Iterable<ReporteBolsita> reports = ofy.query(ReporteBolsita.class).
				filter("id", reporteId).
				filter("userId", userId);
		ReporteBolsita ret = null;
		if(reports.iterator().hasNext()) {
			ret = reports.iterator().next();
		}
		return ret;
	}

	public ReporteBolsita updateReporte(ReporteBolsita daoRb) {
    	Objectify ofy = getOfy();
    	ofy.put(daoRb);
		return daoRb;
	}
	
	public void deleteReporte(ReporteBolsita rb) {
    	Objectify ofy = getOfy();
    	ofy.delete(rb);
	}

}
