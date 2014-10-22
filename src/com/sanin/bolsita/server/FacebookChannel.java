package com.sanin.bolsita.server;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FacebookChannel extends HttpServlet {
    public void doPost(HttpServletRequest req, 
            HttpServletResponse resp) throws IOException {
    	doGet(req, resp);
    }

    public void doGet(HttpServletRequest req, 
            HttpServletResponse resp) throws IOException {
    	long cacheExpiresSeconds = 60*60*24*365;
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(System.currentTimeMillis() + cacheExpiresSeconds*1000);
    	resp.setHeader("Pragma", "public");
    	resp.setHeader("Cache-Control", "max-age=" + cacheExpiresSeconds);
    	resp.setHeader("Expires", htmlExpiresDateFormat().format(cal.getTime()));
    	ServletOutputStream out = resp.getOutputStream();
    	out.print("<script src=\"//connect.facebook.net/en_US/all.js\"></script>");
    	out.close();
    }

    public DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }
}
