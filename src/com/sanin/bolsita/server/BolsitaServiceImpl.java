package com.sanin.bolsita.server;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sanin.bolsita.client.BolsitaService;
import com.sanin.bolsita.shared.BolsitaException;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.ReporteBolsita;

@SuppressWarnings("serial")
public class BolsitaServiceImpl extends RemoteServiceServlet implements
		BolsitaService {

	MailHandlerModel mhm = new MailHandlerModel();
	@Override
	public List<ReporteBolsita> getReports(Long userId, int month) throws BolsitaException {
		return mhm.getReports(userId, month);
	}
	@Override
	public BolsitaUser updateUser(BolsitaUser user) throws BolsitaException {
		return mhm.updateUser(user);
	}
	@Override
	public ReporteBolsita updateReporte(Long userId, ReporteBolsita rb)
			throws BolsitaException {
		return mhm.updateReporte(userId, rb);
	}
	@Override
	public ReporteBolsita deleteReporte(Long userId, ReporteBolsita rb)
			throws BolsitaException {
		return mhm.deleteReporte(userId, rb);
	}
	@Override
	public Map<Integer, Map<String, Float>> getReportsByMonthAndCat(Long userId)
			throws BolsitaException {
		return mhm.getReportsByMonthAndCat(userId);
	}

}
