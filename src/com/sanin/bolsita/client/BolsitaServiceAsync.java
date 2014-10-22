package com.sanin.bolsita.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.ReporteBolsita;

public interface BolsitaServiceAsync {
	
	void getReports(Long userId, int month,
			AsyncCallback<List<ReporteBolsita>> callback);

	void updateUser(BolsitaUser user, AsyncCallback<BolsitaUser> callback);

	void updateReporte(Long userId, ReporteBolsita rb,
			AsyncCallback<ReporteBolsita> callback);

	void deleteReporte(Long userId, ReporteBolsita rb,
			AsyncCallback<ReporteBolsita> callback);

	void getReportsByMonthAndCat(Long userId,
			AsyncCallback<Map<Integer, Map<String, Float>>> callback);

}
