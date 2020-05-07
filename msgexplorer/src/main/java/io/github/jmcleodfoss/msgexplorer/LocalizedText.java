package io.github.jmcleodfoss.msgexplorer;

import java.util.Locale;
import java.util.ResourceBundle;

/** Wrapper for ResourceBundle to allow easy localization */
class LocalizedText
{
	/** The name of the resource bundle to load. */
	private final String RESOURCE_SOURCE = "io.github.jmcleodfoss.msgexplorer.text";

	/** The locale we are running it. */
	private Locale locale;

	/** The resource bundle with localized text. There is an English version of this showing all the property names and their English values. */
	private ResourceBundle resources;

	/** Create a localization object for the default locale */
	LocalizedText()
	{
		this(Locale.getDefault());
	}

	/** Create a localization object for the given locale
	*	@param	locale	The locale to localize for
	*/
	LocalizedText(java.util.Locale locale)
	{
		this.locale = locale;
		resources = ResourceBundle.getBundle(RESOURCE_SOURCE, locale, this.getClass().getClassLoader());
	}

	/** Get the localized text for the stored locale for the given property name
	*	@param	property	The property name to retrive the localized text for.
	*	@return	The value for property in the resource bundle
	*/
	String getText(String property)
	{
		if (resources.keySet().contains(property))
			return resources.getString(property);
		return property;
	}
}
