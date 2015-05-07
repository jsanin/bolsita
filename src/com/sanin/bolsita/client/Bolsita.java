package com.sanin.bolsita.client;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gwt.advanced.client.ui.widget.AdvancedFlexTable;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import com.sanin.bolsita.shared.BolsitaUser;
import com.sanin.bolsita.shared.ReporteBolsita;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * <pre>
      appId      : '413223732092807', // App ID
      appId      : '493814644010167', // App ID dev
</pre>
 */
public class Bolsita implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 *
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 *
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);
*/
	private final BolsitaServiceAsync bolsitaService = GWT
			.create(BolsitaService.class);

	
	final AdvancedFlexTable flxt = new AdvancedFlexTable();
	final FlexTable sumFlxt = new FlexTable();
	AdvancedFlexTable puntoDecimalPanel = new AdvancedFlexTable();
	SimplePanel chartCatBarPanel = new SimplePanel();
	SimplePanel chartTotalYearPanel = new SimplePanel();


//	final HTML fbProfile = new HTML();
	
	final Label loginMsg = new Label();
	final HTML fcbkLoginButton = new HTML();

//	final HorizontalPanel userInfoPanel = new HorizontalPanel();

	final Anchor[] linksMonth = new Anchor[12];
	final Label welcomeMsg = new Label();
	final Anchor mailto = new Anchor();
	String strPuntoDecimal = ".";
	int indexSelectedMonth = 0;

	
	UnorderedListWidget ulInsideMenu = new UnorderedListWidget();
	
	String username = null;
	Long userId = new Long(0l);
	
	String[] months = LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().monthsFull();
	
	Label cargando = new Label("Cargando...");
	
	String delId = null;

	private String emailDomain = "@reportebolsita.appspotmail.com";
	
	String[] barColors = {"#FF0F00", "#FF6600", "#FF9E01", "#FCD202", "#F8FF01", "#B0DE09", "#04D215", "#0D8ECF", "#0D52D1", "#2A0CD0", "#8A0CCF", "#CD0D74"};
	String[] catColors = {"#C72C95", "#D8E0BD", "#B3DBD4", "#69A55C", "#B5B8D3", "#F4E23B"};

	String jsonMonthCat = null;
	Set<String> categorias = null;
	
	Anchor linkFbLogout = null;
	Anchor topUserName = null;
	Anchor nameApp = null;
	
	UnorderedListWidget ulMenu = new UnorderedListWidget();
	
	ListItemWidget liMesLink = new ListItemWidget();
	ListItemWidget liAnnoLink = new ListItemWidget();


	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		exportMethods(this);
		
		loginMsg.setText(getLoginMsg());
		// loginMsg.setStyleName("brand");
	    fcbkLoginButton.setHTML(getFcbkButton());
	    
	    nameApp = new Anchor("Bolsita", "./Bolsita.html");
	    nameApp.setStyleName("brand");
	    
	    
//		FINAL Anchor linkFbLogout = new Anchor("Salir");
//		linkFbLogout = Anchor.wrap(DOM.getElementById("signOutLink"));
	    linkFbLogout = new Anchor("Salir");
	    linkFbLogout.getElement().setId("signOutLink");
	    linkFbLogout.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				fbLogout();
				showInicio();
			}

		});
	    
	    topUserName = new Anchor();
	    topUserName.setHref("javascript:;");
	    topUserName.getElement().setId("topUserName");
	    
	    RootPanel mainMenu = RootPanel.get("mainMenu");
//		 int wcount = mainMenu.getWidgetCount();
		 UnorderedListWidget ulMainMenu = new UnorderedListWidget();
		 
		 mainMenu.add(nameApp);
		 mainMenu.add(ulMainMenu);
		 ListItemWidget liw = new ListItemWidget();
		 liw.add(topUserName);
		 ulMainMenu.add(liw);
		 liw = new ListItemWidget();
		 liw.add(linkFbLogout);
		 ulMainMenu.add(liw);
		 ulMainMenu.addStyleName("nav pull-right");

	    
	    setDecimalSeparator();
	    
		RootPanel.get("loginInfo").add(loginMsg);
		RootPanel.get("loginInfo").add(fcbkLoginButton);


	}

	private void setDecimalSeparator() {
	    String decSep = LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator();
	    if(decSep != null && decSep.length() == 1) {
	    	strPuntoDecimal = decSep;
	    }		
	}

	private void showInicio() {
		RootPanel.get("loginInfo").add(loginMsg);
		RootPanel.get("loginInfo").add(fcbkLoginButton);

		RootPanel.get("loginInfo").remove(welcomeMsg);
		RootPanel.get("loginInfo").remove(mailto);
		
		clearInsideMenu(ulInsideMenu);
		RootPanel.get("loginInfo").remove(ulInsideMenu);
		
		clearFlexTable(flxt, 1);
		clearFlexTable(sumFlxt, -1);
		RootPanel.get("reportTable").remove(flxt);
		RootPanel.get("reportTable").remove(puntoDecimalPanel);
		RootPanel.get("barChartCat").remove(chartCatBarPanel);
		chartCatBarPanel.removeFromParent();

//		RootPanel.get("sumTable").remove(sumFlxt);
		
		topUserName.setText("");
		ulMenu.getElement().setInnerHTML("");
		ulMenu.removeFromParent();
		
		// creo que cada vez que se invoca algún fbmx hay que llamar a este método
//		parseDomTree();


	}

	private void clearInsideMenu(UnorderedListWidget ulIM) {
		int wcount = ulInsideMenu.getWidgetCount();
		for (int i = wcount-1; i >= 0; i--) {
			ListItemWidget liw = (ListItemWidget)ulInsideMenu.getWidget(i);
			Anchor anch = (Anchor)liw.getWidget();
			anch.setHTML("");
			liw.clear();
			ulInsideMenu.remove(i);
		}

	}
	
	private void showAnno() {
		
		// se remueven los elementos de la pestaña Mes
		clearInsideMenu(ulInsideMenu);
		RootPanel.get("loginInfo").remove(ulInsideMenu);
		
		clearFlexTable(flxt, 1);
		clearFlexTable(sumFlxt, -1);
		RootPanel.get("reportTable").remove(flxt);
		RootPanel.get("reportTable").remove(puntoDecimalPanel);
		RootPanel.get("barChartCat").remove(chartCatBarPanel);
		chartCatBarPanel.removeFromParent();

		RootPanel.get("barChartCat").remove(chartTotalYearPanel);
		chartTotalYearPanel.removeFromParent();


		// fin remoción de elementos de pestaña Mes
		
		chartTotalYearPanel.getElement().setInnerHTML("");
		chartTotalYearPanel.getElement().setId("charttotaldiv");
		chartTotalYearPanel.getElement().setAttribute("style", "width:1000px; height:450px;");
		
		RootPanel.get("barChartCat").add(chartTotalYearPanel);
		
		clearCatInfo();
		int colIndex = 0;
		for (String cat : categorias) {
			addNewCatInfo(cat, cat, catColors[colIndex++]);
			if(colIndex == catColors.length) {
				colIndex = 0;
			}
		}
		
		createTotalYear(jsonMonthCat);


		
	}

	private String getFcbkButton() {
		//return "<fb:login-button/>";
		return "<input type='button' class='gwt-Button' value='Login con Facebook' onclick='login();'>";
	}

	private String getLoginMsg() {
		return "Ingresa a la Bolsita con tu usuario de Facebook. No compartiremos tu información personal con nadie, sólo utilizaremos Facebook como medio de autenticación.";
	}

	private String getWelcomeMsg() {
		return "Para hacer reportes escribe al correo electrónico: ";
	}
	
	
	private native void exportMethods(Bolsita instance) /*-{
		$wnd.onLogin = function(response) {
		return instance.@com.sanin.bolsita.client.Bolsita::onLogin(Lcom/google/gwt/core/client/JavaScriptObject;)(response);
		}
		
	}-*/;

	public void onLogin(JavaScriptObject response) {
		// just to show that xfbml works here, as well
		
//		fbProfile.setHTML("<span>"
//						+ "<fb:profile-pic uid=loggedinuser facebook-logo=true></fb:profile-pic>"
//						+ "&nbsp;Bienvenido, <fb:name uid=loggedinuser useyou=false></fb:name>.</span>");
//		userInfoPanel.add(fbProfile);
//		userInfoPanel.add(linkFbLogout);
		UserJso userJso = (UserJso)response;
//		Hidden fcbkName = Hidden.wrap(DOM.getElementById("fcbkName"));
//		System.out.println("fcbkName: " + fcbkName.getValue());
		// 6may2015 el campo username del objeto user no está disponible
		// tuve que cambiarlo por el campo id
		// this.username = userJso.getUsername();
		this.username = userJso.getId();
//		System.out.println("username from javacript: " + this.username);
		topUserName.setText(userJso.getName());
		
		BolsitaUser bolsitaUser = getBolsitaUser(userJso);

		RootPanel.get("loginInfo").remove(loginMsg);
		RootPanel.get("loginInfo").remove(fcbkLoginButton);
		RootPanel.get("loginInfo").add(cargando);

		
		bolsitaService.updateUser(bolsitaUser, new AsyncCallback<BolsitaUser>() {
			@Override
			public void onFailure(Throwable t) {
				RootPanel.get("loginInfo").add(loginMsg);
				RootPanel.get("loginInfo").add(fcbkLoginButton);
				RootPanel.get("loginInfo").remove(cargando);
				Window.alert("Error guardando el usuario en la base de datos");
			}

			@Override
			public void onSuccess(BolsitaUser bu) {
				userId = bu.getId();
				parseDomTree();
				generateTopMenu();
				showContent();
			}
		});

		// make sure the xfbml is rendered
	}

	private BolsitaUser getBolsitaUser(UserJso uj) {
		BolsitaUser bu = new BolsitaUser();
		bu.setAuthType("FB");
		bu.setFirstName(uj.getFirstName());
		bu.setGender(uj.getGender());
		bu.setLastName(uj.getLastName());
		bu.setLocale(uj.getLocale());
		bu.setName(uj.getName());
		bu.setTimezone(uj.getTimezone());
		bu.setUsername(uj.getId());
		return bu;
	}

	private void showContent() {
		welcomeMsg.setText(getWelcomeMsg());
		String toemail = this.username + this.emailDomain;

		mailto.setHref("mailto:" + toemail + "?subject='Reporte de gastos'");
		mailto.setText(toemail);

		RootPanel.get("loginInfo").remove(loginMsg);
		RootPanel.get("loginInfo").remove(fcbkLoginButton);
		RootPanel.get("loginInfo").remove(cargando);
		
//		final Label msgDeleted = new Label("Reporte eliminado", false);
//		msgDeleted.setStyleName("alert alert-success");

//		RootPanel.get("loginInfo").add(userInfoPanel);
//		HTMLPanel mainContainer = new HTMLPanel(getInsideMenu());
		
		int idxCMonth = getIndexCurrentMonth();
		indexSelectedMonth = 0;
		ulInsideMenu.setStyleName("nav nav-pills");
		ListItemWidget[] liMonths = new ListItemWidget[12];
		
		for (int i = 0; i < 12; i++) {
			liMonths[i] = new ListItemWidget();
			linksMonth[i] = new Anchor(months[i]);
			final int currentMonth = i;
			linksMonth[i].addClickHandler(new ClickHandler() {
				int month;
				@Override
				public void onClick(ClickEvent event) {
					this.month = currentMonth;
					changeSelectedMonth(this.month);
					showDataTable(this.month);
					indexSelectedMonth = this.month;
				}
			});
			if(i == indexSelectedMonth) {
				liMonths[i].setStyleName("active");
			}
			liMonths[i].add(linksMonth[i]);
			ulInsideMenu.add(liMonths[i]);
		}
		
//		ListItemWidget li1 = new ListItemWidget();
//		li1.setStyleName("active");
//		li1.add(linksMonth);
//		ulInsideMenu.add(li1);
		
		RootPanel.get("loginInfo").add(welcomeMsg);
		RootPanel.get("loginInfo").add(mailto);
		RootPanel.get("loginInfo").add(ulInsideMenu);
		flxt.getElement().setId("reportTableResult1");
		flxt.setStylePrimaryName("table table-condensed");
//		RootPanel.get("sumTable").add(sumFlxt);
		RootPanel.get("pieDiv").add(sumFlxt);
//		DOM.setElementAttribute(sumFlxt.getElement(), "id", "totalCategoria");
		sumFlxt.setStylePrimaryName("table table-condensed");
		
		chartCatBarPanel.getElement().setInnerHTML("");
		chartCatBarPanel.getElement().setId("divcatbar");
		chartCatBarPanel.getElement().setAttribute("style", "width:600px; height:250px;");

		RootPanel.get("barChartCat").add(chartCatBarPanel);

		// se elimina la gráfica de año en caso de que exista
		RootPanel.get("barChartCat").remove(chartTotalYearPanel);
		chartTotalYearPanel.removeFromParent();



		// configurar punto decimal para los valores de la tabla
		final ListBox puntoDecimal = new ListBox();
		puntoDecimal.getElement().setId("puntoDecimal");
		puntoDecimal.addItem(".", "punto");
		puntoDecimal.addItem(",", "coma");
		puntoDecimal.setStyleName("input-small");
		if(!strPuntoDecimal.equals(".")) {
			puntoDecimal.setSelectedIndex(1);
		}
		puntoDecimal.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				int pdIndex = puntoDecimal.getSelectedIndex();
				strPuntoDecimal = puntoDecimal.getItemText(pdIndex);
				changeSelectedMonth(indexSelectedMonth);
				showDataTable(indexSelectedMonth);
				
			}
		});

		puntoDecimalPanel.setWidth("100%");
		Label lPuntoDecimal = new Label("Cambiar punto decimal:  ");
		puntoDecimalPanel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_MIDDLE);
		puntoDecimalPanel.setWidget(0, 0, lPuntoDecimal);
		puntoDecimalPanel.getFlexCellFormatter().setWidth(0, 0, "10px");
		puntoDecimalPanel.getFlexCellFormatter().setWordWrap(0, 0, false);
		puntoDecimalPanel.setWidget(0, 1, puntoDecimal);
		// fin punto decinal
		
		//Exportar a CSV
		Button exportButton = new Button("Exportar");
		exportButton.setStyleName("btn btn-small btn-primary");
		puntoDecimalPanel.setWidget(0, 2, exportButton);
		puntoDecimalPanel.getFlexCellFormatter().setColSpan(0, 2, 3);
		puntoDecimalPanel.getFlexCellFormatter().setStyleName(0, 2, "pull-right");
		exportButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int rows = flxt.getRowCount();
				String retcsv = "";
				for(int i = 1; i < rows; i++) {
					int cells = flxt.getCellCount(i);
					for(int j = 0; j < cells; j++) {
						retcsv += "\"";
						retcsv += flxt.getText(i, j);
						retcsv += "\";";
					}
					retcsv += "\n";
				}
				Window.Location.assign("data:text/csv;base64,"+
						b64encode(retcsv));
			}
		});

		//FIN Exportar a CSV

//		RootPanel.get("reportTable").add(puntoDecimalPanel);
		RootPanel.get("reportTable").add(flxt);
		
		
		flxt.addClickHandler(new ClickHandler() {
			int selectedRow = -1;
			String fecha;
			String establec;
			String categoria;
			String vlr;
			String responsable;
			String tipoPago;
			Hidden hidId;

			@Override
			public void onClick(ClickEvent event) {
				Cell src = flxt.getCellForEvent(event);
				if(src != null) {
					int rowIndex = src.getRowIndex();
					int totalrows = flxt.getRowCount();
//					System.out.println("click rowIndex: "+ rowIndex + ", totalrows: " + totalrows);
					if(delId != null) {
						delId = null;
						fecha = flxt.getText(rowIndex, 0);
						establec = flxt.getText(rowIndex, 1);
						vlr = flxt.getText(rowIndex, 3);
						hidId = (Hidden)flxt.getWidget(rowIndex, 6);
						boolean confDel = Window.confirm(
								"Quieres eliminar " + fecha + " " + establec + " por valor: " + vlr + "?");
						if(confDel) {
							ReporteBolsita rb = new ReporteBolsita();
							rb.setId(Long.valueOf(hidId.getValue()));
							bolsitaService.deleteReporte(userId, rb, new AsyncCallback<ReporteBolsita>() {
								@Override
								public void onFailure(Throwable t) {
									flxt.setText(1, 0, "Error: " + t.getMessage());						
								}

								@Override
								public void onSuccess(ReporteBolsita rbDeleted) {
//									flxt.removeRow(rowIndex);
									showDataTable(indexSelectedMonth);
									final Label msgDeleted = new Label("Reporte eliminado", false);
									msgDeleted.setStyleName("alert alert-success");

									final RootPanel root = RootPanel.get();
									root.add(msgDeleted);

									int left = flxt.getAbsoluteLeft();
									int top = flxt.getAbsoluteTop();
									root.setWidgetPosition(msgDeleted, left + 70, top + 10);
									
									Timer t = new Timer() {
										
										@Override
										public void run() {
											msgDeleted.setVisible(false);
											root.remove(msgDeleted);
										}
									};
									t.schedule(2000);
								}
							});
						}
					} else
					if(rowIndex > 0 && rowIndex < totalrows-1 && selectedRow != rowIndex) {
						if(selectedRow != -1) {
							//TODO: cuando se cambia de mes termina utilizando el último indice 
							// del mes anterior
							flxt.setText(selectedRow, 0, fecha);
							flxt.setText(selectedRow, 1, establec);
							flxt.setText(selectedRow, 2, categoria);
							flxt.setText(selectedRow, 3, vlr);
							flxt.setText(selectedRow, 4, responsable);
							flxt.setText(selectedRow, 5, tipoPago);
						}
						
						selectedRow = rowIndex;
						fecha = flxt.getText(rowIndex, 0);
						establec = flxt.getText(rowIndex, 1);
						categoria = flxt.getText(rowIndex, 2);
						vlr = flxt.getText(rowIndex, 3);
						responsable = flxt.getText(rowIndex, 4);
						tipoPago = flxt.getText(rowIndex, 5);
						hidId = (Hidden)flxt.getWidget(rowIndex, 6);
						
						final ReporteBolsita rbInicial = new ReporteBolsita();
						rbInicial.setStrFecha(fecha);
						rbInicial.setEstablecimiento(establec);
						rbInicial.setCategoria(new String[]{categoria});
						rbInicial.setStrValor(vlr);
						rbInicial.setResponsable(responsable);
						rbInicial.setTcEfectivo(tipoPago);
						
						final TextBox tbFecha = new TextBox();
						tbFecha.getElement().setId("fecha");
						tbFecha.setStyleName("input-small");
						final TextBox tbEstablecimiento = new TextBox();
						tbEstablecimiento.setStyleName("input-medium");
						final TextBox tbCateg = new TextBox();
						tbCateg.getElement().setId("categoria");
						tbCateg.setStyleName("input-small");
						final TextBox tbValor = new TextBox();
						tbValor.getElement().setId("valor");
						tbValor.setStyleName("input-small");
						tbValor.setAlignment(TextAlignment.RIGHT);
						final TextBox tbResponsable = new TextBox();
						tbResponsable.getElement().setId("responsable");
						tbResponsable.setStyleName("input-small");
						final ListBox tbTipoPago = new ListBox();
						tbTipoPago.getElement().setId("tipoPago");
						tbTipoPago.addItem("tc");
						tbTipoPago.addItem("débito", "debito");
						tbTipoPago.addItem("efectivo");
						tbTipoPago.setStyleName("input-small");
						

//							tbFecha.setMaxLength(10);
						tbFecha.setText(fecha);
//							tbFecha.setVisibleLength(10);
						flxt.setWidget(rowIndex, 0, tbFecha);
						flxt.getCellFormatter().setWidth(rowIndex, 0, "1");
						
						tbEstablecimiento.setText(establec);
						flxt.setWidget(rowIndex, 1, tbEstablecimiento);
						
						tbCateg.setText(categoria);
						flxt.setWidget(rowIndex, 2, tbCateg);
						
						tbValor.setText(vlr);
						flxt.setWidget(rowIndex, 3, tbValor);
						
						tbResponsable.setText(responsable);						
						flxt.setWidget(rowIndex, 4, tbResponsable);
						
						if(tipoPago != null) {
							if(tipoPago.equalsIgnoreCase("tc")) {
								tbTipoPago.setSelectedIndex(0);
							} else if(tipoPago.equalsIgnoreCase("debito") || tipoPago.equalsIgnoreCase("débito")) {
								tbTipoPago.setSelectedIndex(1);
							} else {
								tbTipoPago.setSelectedIndex(2);
							}
						}
						flxt.setWidget(rowIndex, 5, tbTipoPago);
						
						final KeyDownHandler erbHandler = new KeyDownHandler() {
							
							@Override
							public void onKeyDown(KeyDownEvent event) {
							      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							    	  boolean valid = true;
							    	  String strErrors = "";
							    	  if(!tbFecha.getText().matches("\\d{2}/\\d{2}/\\d{4}")) {
							    		  strErrors += "Fecha debe tener formato dd/MM/yyyy\n";
							    		  valid = false;
							    	  }
							    	  String establecimiento1 = tbEstablecimiento.getText();
							    	  if(establecimiento1.trim().length() <= 0) {
							    		  strErrors += "Establecimiento no puede estar vacío\n";
							    		  valid = false;
							    	  }
							    	  if(!tbValor.getText().matches("\\d*(\\.\\d+)?")) {
							    		  strErrors += "Valor debe tener formato 2477.98\n";
							    		  valid = false;
							    	  }
							    	  String responsable1 = tbResponsable.getText();
							    	  if(responsable1.trim().length() <= 0) {
							    		  strErrors += "Responsable no puede estar vacío\n";
							    		  valid = false;
							    	  }
							    	  if(!valid) {
							    		  Window.alert(strErrors);
							    		  return;
							    	  }
							    	  
							    	  final ReporteBolsita rb = new ReporteBolsita();
							    	  rb.setStrFecha(tbFecha.getText());
							    	  rb.setEstablecimiento(establecimiento1);
							    	  rb.setCategoria(new String[]{tbCateg.getText()});
							    	  rb.setStrValor(tbValor.getText());
							    	  rb.setResponsable(responsable1);
							    	  rb.setTcEfectivo(tbTipoPago.getValue(tbTipoPago.getSelectedIndex()));
							    	  rb.setId(new Long(hidId.getValue()));

							    	  boolean change = false;
									if(!rbInicial.getStrFecha().equals(rb.getStrFecha())) {
										change = true;
									}
//										tbFecha.addStyleName("errorTextBox");
									if(!rbInicial.getEstablecimiento().equals(rb.getEstablecimiento())) {
										change = true;
									}
									String cat1 = rbInicial.getCategoria()[0];
									String cat2 = rb.getCategoria()[0];
									if(!cat1.equals(cat2)) {
										change = true;
									}
									if(!rbInicial.getStrValor().equals(rb.getStrValor())) {
										change = true;
									}
									if(!rbInicial.getResponsable().equals(rb.getResponsable())) {
										change = true;
									}
									if(!rbInicial.getTcEfectivo().equals(rb.getTcEfectivo())) {
										change = true;
									}
									
									if(change) {
								    	  bolsitaService.updateReporte(userId, rb, 
										    		new AsyncCallback<ReporteBolsita>() {
										  			@Override
													public void onFailure(Throwable t) {
										  				Window.alert("Error: " + t.getMessage());
													}
			
													@Override
													public void onSuccess(ReporteBolsita result) {
														fecha = rb.getStrFecha();
														establec = rb.getEstablecimiento();
														categoria = rb.getCategoria()[0];
														vlr = rb.getStrValor();
														responsable = rb.getResponsable();
														tipoPago = rb.getTcEfectivo();

														flxt.setText(selectedRow, 0, fecha);
														flxt.setText(selectedRow, 1, establec);
														flxt.setText(selectedRow, 2, categoria);
														flxt.setText(selectedRow, 3, vlr);
														flxt.setText(selectedRow, 4, responsable);
														flxt.setText(selectedRow, 5, tipoPago);
														selectedRow = -1;
										  				Window.alert("Reporte actualizado");
														
													}

									    	  	});
									} else {
										flxt.setText(selectedRow, 0, fecha);
										flxt.setText(selectedRow, 1, establec);
										flxt.setText(selectedRow, 2, categoria);
										flxt.setText(selectedRow, 3, vlr);
										flxt.setText(selectedRow, 4, responsable);
										flxt.setText(selectedRow, 5, tipoPago);
										selectedRow = -1;

									}

							      }			
							}

						};

						tbFecha.addKeyDownHandler(erbHandler);
						tbEstablecimiento.addKeyDownHandler(erbHandler);
						tbCateg.addKeyDownHandler(erbHandler);
						tbValor.addKeyDownHandler(erbHandler);
						tbResponsable.addKeyDownHandler(erbHandler);
						tbTipoPago.addKeyDownHandler(erbHandler);
							
						

					
					} else {
						
					}
				}
		      
			}
		});

//		bolsitaService.getReportsByMonthAndCat(userId, new AsyncCallback<Map<Integer,Map<String,Float>>>() {
//			public void onFailure(Throwable t) {
//				Window.alert("Error: " + t.getMessage());						
//			}
//
//			public void onSuccess(Map<Integer,Map<String,Float>> result) {
//				Object[] rets = getMonthCatJson(result);
//				jsonMonthCat = (String)rets[0];
//				categorias = (Set<String>)rets[1];
//			}
//
//		});

		
//		final Hidden fcbkLoginStatus = Hidden.wrap(DOM.getElementById("fcbkLoginStatus"));
//		System.out.println("fcbkLoginStatus: " + fcbkLoginStatus.getValue());
		flxt.setVisible(false);
		
				
			    // Let's put a button in the middle...
				//flxt.setWidget(1, 0, new Button("Wide Button"));

			    // ...and set it's column span so that it takes up the whole row.
				//flxt.getFlexCellFormatter().setColSpan(1, 0, 3);
				
				
	}

	/**
	 * Genera los links del menú principal de la aplicación. Hasta ahora los links de Mes y de Año
	 */
	private void generateTopMenu() {
		// links de mes y de año
		
		RootPanel mainMenu = RootPanel.get("mainMenu");

		ulMenu.setStyleName("nav");
 		liMesLink.setStyleName("active");
 		Anchor linkMes = new Anchor("Mes");
 		linkMes.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				showContent();
				liMesLink.setStyleName("active");
				liAnnoLink.setStyleName("");
				
			}
		});
 		
 		liMesLink.add(linkMes);
 		ulMenu.add(liMesLink);

 		Anchor annoMes = new Anchor("Año");

		annoMes.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
//				changeSelectedMonth(this.month);
//				showDataTable(this.month);
				bolsitaService.getReportsByMonthAndCat(userId, new AsyncCallback<Map<Integer,Map<String,Float>>>() {
					public void onFailure(Throwable t) {
						Window.alert("Error: " + t.getMessage());						
					}
		
					public void onSuccess(Map<Integer,Map<String,Float>> result) {
						Object[] rets = getMonthCatJson(result);
						jsonMonthCat = (String)rets[0];
						categorias = (Set<String>)rets[1];
						showAnno();
						liAnnoLink.setStyleName("active");
						liMesLink.setStyleName("");
					}
		
				});

			}
		});
 		
 		liAnnoLink.add(annoMes);
 		ulMenu.add(liAnnoLink);

 		mainMenu.insert(ulMenu, 1);

		
		// Fin links mes y año
	}
	
	private int getIndexCurrentMonth() {
		DateTimeFormat dtf = DateTimeFormat.getFormat("M");
		String m = dtf.format(new Date());
		return Integer.valueOf(m) - 1;
	}

	protected void changeSelectedMonth(int month) {
		int wcount = ulInsideMenu.getWidgetCount();
		for (int i = 0; i < wcount; i++) {
			ListItemWidget liw = (ListItemWidget)ulInsideMenu.getWidget(i);
			if(i == month) {
				liw.setStyleName("active");
			} else {
				liw.setStyleName("");
			}
		}
	}

	protected void showDataTable(int month) {
		int widgetCount = RootPanel.get("reportTable").getWidgetCount();
		if(widgetCount > 0) {
			RootPanel.get("reportTable").insert(puntoDecimalPanel, 0);
		}
		
		flxt.setVisible(true);
//		flxt.setBorderWidth(2);
//		flxt.setStylePrimaryName("simpleTable");
	    // Put some text at the table's extremes.  This forces the table to be
	    // 3 by 3.
//	    flxt.setText(0, 0, "Fecha (dia/mes/año)");
	    flxt.setHeaderWidget(0, new Label("Fecha (d/m/a)"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 0, "reportTableResult-th");
	    flxt.setHeaderWidget(1, new Label("Establecimiento"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 1, "reportTableResult-th");
	    flxt.setHeaderWidget(2, new Label("Categoria"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 2, "reportTableResult-th");
	    flxt.setHeaderWidget(3, new Label("Valor"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 3, "reportTableResult-th");
	    flxt.setHeaderWidget(4, new Label("Responsable"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 4, "reportTableResult-th");
	    flxt.setHeaderWidget(5, new Label("Tipo pago"));
//	    flxt.getCellFormatter().setStylePrimaryName(0, 5, "reportTableResult-th");
		clearFlexTable(flxt, 0);
		clearFlexTable(sumFlxt, -1);
		flxt.setText(1, 0, "Cargando mes: " + (month + 1) + "...");

		bolsitaService.getReports(userId, month, new AsyncCallback<List<ReporteBolsita>>() {
			@Override
			public void onFailure(Throwable t) {
				flxt.setText(1, 0, "Error: " + t.getMessage());						
			}

			@Override
			public void onSuccess(List<ReporteBolsita> result) {
				DateTimeFormat sdf = DateTimeFormat.getFormat(getDateFormatPattern());
				NumberFormat nf = NumberFormat.getFormat(getFormatNumberPattern());
				NumberFormat nfPercent = NumberFormat.getFormat(getPercentagePattern());
				
				HashMap<String, Float> catsSum = new HashMap<String, Float>();
				HashMap<String, Float> respSum = new HashMap<String, Float>();
				double total = 0l;
				
				int i = 1;
				for (ReporteBolsita rb : result) {
				    flxt.setText(i, 0, sdf.format(rb.getFecha()));
//				    flxt.getCellFormatter().setStylePrimaryName(i, 0, "simpleTable-td");
				    flxt.setText(i, 1, rb.getEstablecimiento());
//				    flxt.getCellFormatter().setStylePrimaryName(i, 1, "simpleTable-td");
				    String cat = "";
				    if(rb.getCategoria() != null && rb.getCategoria().length > 0) {
				    	cat = rb.getCategoria()[0];
				    }
				    flxt.setText(i, 2, cat);
//				    flxt.getCellFormatter().setStylePrimaryName(i, 2, "simpleTable-td");
				    float vlr = rb.getValor();
				    String strVlr = formatDecimalString(nf.format(vlr));
				    flxt.setText(i, 3, strVlr);
//				    flxt.getCellFormatter().setStylePrimaryName(i, 3, "simpleTable-td");
				    flxt.getFlexCellFormatter().setStyleName(i, 3, "pull-right");

					String resp = rb.getResponsable();
				    if(resp != null) {
				    	resp = resp.toLowerCase();
				    }
				    flxt.setText(i, 4, resp);
//				    flxt.getCellFormatter().setStylePrimaryName(i, 4, "simpleTable-td");

				    flxt.setText(i, 5, rb.getTcEfectivo());
//				    flxt.getCellFormatter().setStylePrimaryName(i, 5, "simpleTable-td");
				    
				    final Hidden hidId = new Hidden("rbId", rb.getId().toString());
				    flxt.setWidget(i, 6, hidId);
				    
				    Button delButton = new Button("&times;");
				    delButton.setStyleName("close");
				    flxt.setWidget(i, 7, delButton);
				    delButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
//							System.out.println("Click en hidId: " + hidId.getValue());
							delId = hidId.getValue();
						}
					});
				    
				    Float catAcum = catsSum.get(cat);
				    if(catAcum == null) {
				    	catAcum = new Float(0);
				    }
				    catAcum+=vlr;
				    catsSum.put(cat, catAcum);
				    
				    Float repsAcum = respSum.get(resp);
				    if(repsAcum == null) {
				    	repsAcum = new Float(0);
				    }
				    repsAcum+=vlr;
				    respSum.put(resp, repsAcum);
				    
				    total+=vlr;
				    
					i++;
				}
				if(i == 1) {
					// no trajo resultados
					flxt.setText(1, 0, "No hay reportes");
				}
				i++;
				flxt.setText(i, 0, "TOTAL");
				flxt.getFlexCellFormatter().setColSpan(i, 0, 3);
				flxt.getCellFormatter().setHorizontalAlignment(i, 0, 
						HorizontalAlignmentConstant.endOf(Direction.LTR));
				flxt.setText(i, 1, formatDecimalString(nf.format(total)));
			    flxt.getFlexCellFormatter().setStyleName(i, 1, "pull-right");

				flxt.setText(i, 2, "");
				flxt.setText(i, 3, "");
				flxt.setText(i, 4, "");
				flxt.setText(i, 5, "");
				flxt.getRowFormatter().setStyleName(i, "info");

//			    flxt.getCellFormatter().setStylePrimaryName(i, 0, "simpleTable-td");
//			    flxt.getCellFormatter().setStylePrimaryName(i, 1, "simpleTable-td");
				

//				sumFlxt.setWidget(0, 0, new HTML("<h3>Total por categoria</h3>"));
//				sumFlxt.getFlexCellFormatter().setColSpan(0, 0, 2);
//				Anchor animarCategoria = new Anchor("Animar");
//				sumFlxt.setWidget(0, 1, animarCategoria);
//				sumFlxt.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
//				animarCategoria.addClickHandler(new ClickHandler() {
//					@Override
//					public void onClick(ClickEvent event) {
//						animateCats();
//					}
//				});
				
//				sumFlxt.getFlexCellFormatter().setStylePrimaryName(0, 0, "totalCategoria-th");
				Set<Entry<String, Float>> categoriasSum = catsSum.entrySet();
				Iterator<Entry<String, Float>> iter = categoriasSum.iterator();
				float totalcat = 0;
				int j = 0;
				while (iter.hasNext()) {	
					Map.Entry<String, Float> entry = (Map.Entry<String, Float>) iter.next();
//					String cat = entry.getKey();
//					if(cat == null || cat.length() == 0) {
//						cat = "Sin categoria";
//					}
//					sumFlxt.setText(j, 0, cat);
//					sumFlxt.setText(j, 1, formatDecimalString(nf.format(entry.getValue())));
//					sumFlxt.getCellFormatter().setHorizontalAlignment(j, 1, 
//							HorizontalAlignmentConstant.endOf(Direction.LTR));
//					sumFlxt.setText(j, 2, formatDecimalString(nfPercent.format(entry.getValue()/total)));
//					sumFlxt.getCellFormatter().setHorizontalAlignment(j, 2, 
//							HorizontalAlignmentConstant.endOf(Direction.LTR));
					totalcat+=entry.getValue();
//					j++;
				}

//				int widgetCount = RootPanel.get("barChartCat").getWidgetCount();
//				if(widgetCount > 0) {
//					for (int k = 0; k < widgetCount; k++) {
//						RootPanel.get("barChartCat").remove(k);
//					}
//				}
				
				
				createCatBar(getCategoriasSumJson(catsSum, "categoria", true));

				
				SimplePanel chartCatPanel = new SimplePanel();
				chartCatPanel.getElement().setId("chartdivcat");
				chartCatPanel.getElement().setAttribute("style", "width:500px; height:250px;");

				sumFlxt.setWidget(j, 0, chartCatPanel);
				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 3);
				createCatChart(getCategoriasSumJson(catsSum, "categoria"));
//				j++;


//				sumFlxt.setWidget(j, 0, chartCatBarPanel);
//				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 3);
//				createCatBar(getCategoriasSumJson(catsSum, "categoria", true));
//				j++;

//				sumFlxt.setText(j, 0, "TOTAL CATEGORIAS");
//				sumFlxt.setText(j, 1, formatDecimalString(nf.format(totalcat)));
//				sumFlxt.getCellFormatter().setHorizontalAlignment(j, 1, 
//						HorizontalAlignmentConstant.endOf(Direction.LTR));
//				sumFlxt.getFlexCellFormatter().setStylePrimaryName(j, 0, "totalCategoria-th");
//				sumFlxt.setText(j, 2, totalcat == 0?"":formatDecimalString(nfPercent.format(totalcat/total)));
//				sumFlxt.getCellFormatter().setHorizontalAlignment(j, 2, 
//						HorizontalAlignmentConstant.endOf(Direction.LTR));
//				sumFlxt.getRowFormatter().setStyleName(j, "info");
				
				
//				j++;
//				sumFlxt.setWidget(j, 0, new HTML("<h3>Total por responsable</h3>"));
//				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 2);
					
				Set<Entry<String, Float>> responsablesSum = respSum.entrySet();
				iter = responsablesSum.iterator();
				float totalresp = 0;
				j++;
				while (iter.hasNext()) {	
					Map.Entry<String, Float> entry = (Map.Entry<String, Float>) iter.next();
					String responsable = entry.getKey();
					if(responsable == null || responsable.length() == 0) {
						responsable = "Sin responsable";
					}
//					sumFlxt.setText(j, 0, responsable);
//					sumFlxt.setText(j, 1, formatDecimalString(nf.format(entry.getValue())));
//					sumFlxt.getCellFormatter().setHorizontalAlignment(j, 1, 
//							HorizontalAlignmentConstant.endOf(Direction.LTR));
//					sumFlxt.setText(j, 2, formatDecimalString(nfPercent.format(entry.getValue()/total)));
//					sumFlxt.getCellFormatter().setHorizontalAlignment(j, 2, 
//							HorizontalAlignmentConstant.endOf(Direction.LTR));
					totalresp+=entry.getValue();
//					j++;
				}
				SimplePanel chartRespPanel = new SimplePanel();
				chartRespPanel.getElement().setId("chartdivresp");
				chartRespPanel.getElement().setAttribute("style", "width:500px; height:250px;");

				sumFlxt.setWidget(j, 0, chartRespPanel);
				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 2);
				createRespChart(getCategoriasSumJson(respSum, "responsable"));
//				j++;
//				
//				sumFlxt.setText(j, 0, "TOTAL RESPONSABLES");
//				sumFlxt.setText(j, 1, formatDecimalString(nf.format(totalresp)));
//				sumFlxt.getCellFormatter().setHorizontalAlignment(j, 1, 
//						HorizontalAlignmentConstant.endOf(Direction.LTR));
//				sumFlxt.setText(j, 2, total == 0?"":formatDecimalString(nfPercent.format(totalresp/total)));
//				sumFlxt.getCellFormatter().setHorizontalAlignment(j, 2, 
//						HorizontalAlignmentConstant.endOf(Direction.LTR));
//				sumFlxt.getRowFormatter().setStyleName(j, "info");
				
//				j++;
//				SimplePanel chartTotalYearPanel = new SimplePanel();
//				chartTotalYearPanel.getElement().setId("charttotaldiv");
//				chartTotalYearPanel.getElement().setAttribute("style", "width:600px; height:250px;");
//
//				sumFlxt.setWidget(j, 0, chartTotalYearPanel);
//				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 3);
//				
//				clearCatInfo();
//				int colIndex = 0;
//				for (String cat : categorias) {
//					addNewCatInfo(cat, cat, catColors[colIndex++]);
//					if(colIndex == catColors.length) {
//						colIndex = 0;
//					}
//				}
//				
//				createTotalYear(jsonMonthCat);
				
				j++;
				sumFlxt.setText(j, 0, "");
				sumFlxt.getFlexCellFormatter().setColSpan(j, 0, 2);
			}


		});

	}
	
	private String getCategoriasSumJson(HashMap<String, Float> catValueHash, String catName) {
		return getCategoriasSumJson(catValueHash, catName, false);
	}

	private String getCategoriasSumJson(HashMap<String, Float> catValueHash, String catName, boolean withColor) {
		Set<Entry<String, Float>> categoriasSum = catValueHash.entrySet();
		Iterator<Entry<String, Float>> iter = categoriasSum.iterator();
		String json = "[";
		int indexColor = 0;
		while (iter.hasNext()) {	
			Map.Entry<String, Float> entry = (Map.Entry<String, Float>) iter.next();
			String cat = entry.getKey();
			if(cat == null || cat.length() == 0) {
				cat = "Sin categoria";
			}
			json += "{" + catName + ": \"" + cat + "\", valor: " + entry.getValue();
			if(withColor) {
				json += ", color: \"" + barColors[indexColor]+ "\"";
			}
			json += "}, ";  
			indexColor++;
		}
		if(json.length() > 1) {
			// al menos entro una vez al ciclo. Se elimina la ultima coma (,)
			json = json.substring(0, json.length() - 2);
		}
		json += "]";
		
		
		return json;
	}

	private Object[] getMonthCatJson(Map<Integer, Map<String, Float>> monthCat) {
		NumberFormat nf = NumberFormat.getFormat(getFormatNumberPattern());
		
		Set<Integer> keys = monthCat.keySet();
		Set<String> categs = new HashSet<String>();
		String json = "[";
		for (Integer k : keys) {
			json += "{mes: \"" + k.toString().substring(0, 4) + "-" + k.toString().substring(4) + "\", ";
			
			Map<String, Float> categorias = monthCat.get(k);
			
			Set<String> kcat = categorias.keySet();
			for (String kc : kcat) {
				categs.add(kc);
				json += kc + ": " + nf.format(categorias.get(kc)) + ", ";
			}

			if(!kcat.isEmpty()) {
				// al menos entro una vez al ciclo. Se elimina la ultima coma (,)
				json = json.substring(0, json.length() - 2);
			}
			json += "}, ";
		}
		if(json.length() > 1) {
			// al menos entro una vez al ciclo. Se elimina la ultima coma (,)
			json = json.substring(0, json.length() - 2);
		}
		json += "]";
		
		return new Object[]{json, categs};
	}

	
	
	protected String getFormatNumberPattern() {
		return "0.00";
	}

	private String getPercentagePattern() {
		return "%0.00";
	}

	protected String getDateFormatPattern() {
		return "dd/MM/yyyy";
	}

	protected void clearFlexTable(FlexTable flxt2, int fromRow) {
		int rows = flxt2.getRowCount();
		for (int i = rows-1; i > fromRow; i--) {
			flxt2.removeRow(i);
		}
	}

	private native void parseDomTree() /*-{
		$wnd.FB.XFBML.parse();
	}-*/;

	private native void fbLogout() /*-{
		$wnd.FB.logout();
	}-*/;

	private static native String b64encode(String a) /*-{
	  return window.btoa(a);
	}-*/;

	private static native void createCatChart(String data) /*-{
	  $wnd.createCatChart(data);
	}-*/;

	private static native void createRespChart(String data) /*-{
	  $wnd.createRespChart(data);
	}-*/;

	private static native void createCatBar(String data) /*-{
	  $wnd.createCatBar(data);
	}-*/;

	private static native void animateCats() /*-{
	  $wnd.animateCats();
	}-*/;

	private static native void createTotalYear(String data) /*-{
	  $wnd.createTotalYear(data);
	}-*/;

	private static native void addNewCatInfo(String nombreCat, String cat, String color) /*-{
	  $wnd.addNewCatInfo(nombreCat, cat, color);
	}-*/;
	
	private static native void clearCatInfo() /*-{
	  $wnd.clearCatInfo();
	}-*/;
    
	private String formatDecimalString(String vlr) {
		String strRet = vlr;
		if(strPuntoDecimal.equals(",")) {
			strRet = vlr.replace(".", ",");
		}
		return strRet;
	}
	
	
	/**
	 * This is the entry point method.
	 */
	/*
	public void onModuleLoad() {
		final Button sendButton = new Button("Send");
		final TextBox nameField = new TextBox();
		nameField.setText("GWT User");
		final Label errorLabel = new Label();
		final Anchor linkBolsitaReport = new Anchor("Reporte bolsita");

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		RootPanel.get("links").add(linkBolsitaReport);
		RootPanel.get("reportTable").add(flxt);
		
		flxt.setVisible(false);
		
		linkBolsitaReport.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				flxt.setVisible(true);
				flxt.setBorderWidth(2);
			    // Put some text at the table's extremes.  This forces the table to be
			    // 3 by 3.
			    flxt.setText(0, 0, "Fecha");
			    flxt.setText(0, 1, "Establecimiento");
			    flxt.setText(0, 2, "Valor");
			    flxt.setText(0, 3, "Quien lo hizo");
			    flxt.setText(0, 4, "Efectivo/TC");
				//flxt.setText(2, 4, "bottom-right corner");

				bolsitaService.getReports(new AsyncCallback<List<ReporteBolsita>>() {
					@Override
					public void onFailure(Throwable t) {
						flxt.setText(1, 0, "Error: " + t.getMessage());						
					}

					@Override
					public void onSuccess(List<ReporteBolsita> result) {
						int i = 1;
						for (ReporteBolsita rb : result) {
						    flxt.setText(i, 0, rb.getFecha().toString());
						    flxt.setText(i, 1, rb.getEstablecimiento());
						    flxt.setText(i, 2, Float.toString(rb.getValor()));
						    flxt.setText(i, 3, rb.getResponsable());
						    flxt.setText(i, 4, rb.getTcEfectivo());
							i++;
						}
					}
				});

				
				

			    // Let's put a button in the middle...
				//flxt.setWidget(1, 0, new Button("Wide Button"));

			    // ...and set it's column span so that it takes up the whole row.
				//flxt.getFlexCellFormatter().setColSpan(1, 0, 3);
				
			}
		});
		
		
		
		
		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 *
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 *
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a response.
			 *
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
										.setText("Remote Procedure Call - Failure");
								serverResponseLabel
										.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {
								dialogBox.setText("Remote Procedure Call");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
	}
*/

	class EditReporteBolsitaHandler implements KeyDownHandler {
		
		@Override
		public void onKeyDown(KeyDownEvent event) {
		      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		            Window.alert("La actualización aún no funciona");
		        }			
		}
		
	}
}
