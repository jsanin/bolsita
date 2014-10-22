package com.sanin.bolsita.server;


import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class MailHandlerServlet extends HttpServlet { 
	
	private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());
		
	MailHandlerModel mhm = new MailHandlerModel();
	
    public void doPost(HttpServletRequest req, 
                       HttpServletResponse resp) 
            throws IOException { 
        Properties props = new Properties(); 
        Session session = Session.getDefaultInstance(props, null); 
        try {
			MimeMessage message = new MimeMessage(session, req.getInputStream());
			mhm.processEmail(message);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.WARNING, "", e);
		}
        
        
        
    }
    
}