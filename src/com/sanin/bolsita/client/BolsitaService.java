package com.sanin.bolsita.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sanin.bolsita.shared.BolsitaException;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.ReporteBolsita;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("bolsitaserv")
public interface BolsitaService extends RemoteService {
	List<ReporteBolsita> getReports(Long userId, int month) throws BolsitaException;
	BolsitaUser updateUser(BolsitaUser user) throws BolsitaException;
	ReporteBolsita updateReporte(Long userId, ReporteBolsita rb) throws BolsitaException;
	ReporteBolsita deleteReporte(Long userId, ReporteBolsita rb) throws BolsitaException;
	Map<Integer, Map<String, Float>> getReportsByMonthAndCat(Long userId) throws BolsitaException;

}
