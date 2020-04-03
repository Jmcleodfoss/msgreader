package io.github.jmcleodfoss.msgexplorer;

import java.util.Locale;
import java.util.ResourceBundle;

class LocalizedText
{
	private final String RESOURCE_SOURCE = "io.github.jmcleodfoss.msgexplorer.text";
	private Locale locale;
	private ResourceBundle resources;

	LocalizedText()
	{
		this(Locale.getDefault());
	}

	LocalizedText(java.util.Locale locale)
	{
		this.locale = locale;
		resources = ResourceBundle.getBundle(RESOURCE_SOURCE, locale, this.getClass().getClassLoader());
	}

	String getText(String property)
	{
		if (resources.keySet().contains(property))
			return resources.getString(property);
		return property;
	}
}
