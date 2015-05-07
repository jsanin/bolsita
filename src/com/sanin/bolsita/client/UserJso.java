package com.sanin.bolsita.client;

import com.google.gwt.core.client.JavaScriptObject;

public class UserJso extends JavaScriptObject {
	  // Overlay types always have protected, zero-arg constructors
	  protected UserJso() { }

	  public final native String getFirstName() /*-{ return this.first_name; }-*/;
	  public final native String getLastName()  /*-{ return this.last_name;  }-*/;
	  public final native String getId()  /*-{ return this.id;  }-*/;
	  public final native String getGender()  /*-{ return this.gender;  }-*/;
	  public final native String getLocale()  /*-{ return this.locale;  }-*/;
	  public final native String getName()  /*-{ return this.name;  }-*/;
	  public final native int getTimezone()  /*-{ return this.timezone;  }-*/;
	  public final native String getLink()  /*-{ return this.link;  }-*/;
  
  
}
