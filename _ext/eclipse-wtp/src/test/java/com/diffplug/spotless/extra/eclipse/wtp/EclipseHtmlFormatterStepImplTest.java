/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.extra.eclipse.wtp;

import static org.eclipse.wst.html.core.internal.preferences.HTMLCorePreferenceNames.*;
import static org.eclipse.wst.jsdt.core.formatter.DefaultCodeFormatterConstants.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.eclipse.wst.html.core.internal.preferences.HTMLCorePreferenceNames;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.junit.Before;
import org.junit.Test;

//@RunWith(QuarantiningRunner.class)
//@Quarantine({"org.eclipse", "org.osgi", "com.diffplug"})
public class EclipseHtmlFormatterStepImplTest {

	private TestData testData = null;
	private EclipseHtmlFormatterStepImpl formatter;

	@Before
	public void initialize() throws Exception {
		testData = TestData.getTestDataOnFileSystem("html");
		/*
		 * The instantiation can be repeated for each step, but only with the same configuration
		 * All formatter configuration is stored in
		 * org.eclipse.core.runtime/.settings/org.eclipse.wst.xml.core.prefs.
		 * So a simple test of one configuration item change is considered sufficient.
		 */
		Properties properties = new Properties();
		properties.put(CLEANUP_TAG_NAME_CASE, Integer.toString(HTMLCorePreferenceNames.UPPER)); //HTML config
		properties.put(FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaScriptCore.INSERT); //JS config
		properties.put(QUOTE_ATTR_VALUES, "TRUE"); //CSS config
		formatter = new EclipseHtmlFormatterStepImpl(properties);
	}

	@Test
	public void formatHtml4() throws Exception {
		String[] input = testData.input("html4.html");
		String output = formatter.format(input[0]);
		assertEquals("Unexpected HTML4 formatting.",
				testData.expected("html4.html"), output);
	}

	@Test
	public void formatHtml5() throws Exception {
		String[] input = testData.input("html5.html");
		String output = formatter.format(input[0]);
		assertEquals("Unexpected HTML5 formatting.",
				testData.expected("html5.html"), output);
	}

	@Test
	public void invalidSyntax() throws Exception {
		String[] input = testData.input("invalid_syntax.html");
		String output = formatter.format(input[0]);
		assertEquals("Unexpected HTML formatting in case syntax is not valid.",
				testData.expected("invalid_syntax.html"), output);
	}

	@Test
	public void formatJavaScript() throws Exception {
		String[] input = testData.input("javascript.html");
		String output = formatter.format(input[0]);
		assertEquals("Unexpected JS formatting.",
				testData.expected("javascript.html"), output);
	}

	@Test
	public void formatCSS() throws Exception {
		String[] input = testData.input("css.html");
		String output = formatter.format(input[0]);
		assertEquals("Unexpected CSS formatting.",
				testData.expected("css.html"), output);
	}

	@Test
	public void checkNoDoubleEndoding() throws Exception {
		String osEncoding = System.getProperty("file.encoding");
		//Assure that file.encoding is not used during the clean-up.
		System.setProperty("file.encoding", "ISO-8859-1");
		//Check that WTP does not try to do UTF-8 conversion again (since done by Spotless framework)
		String[] input = testData.input("utf-8.html");
		String output = formatter.format(input[0]);
		System.setProperty("file.encoding", osEncoding);
		assertEquals("Unexpected formatting of UTF-8", testData.expected("utf-8.html"), output);
	}

	@Test
	public void checkBOMisStripped() throws Exception {
		String[] input = testData.input("bom.html");
		String[] inputWithoutBom = testData.input("utf-8.html");
		//The UTF-8 BOM is interpreted as on UTF-16 character.
		assertEquals("BOM input invalid", input[0].length() - 1, inputWithoutBom[0].length());
		String output = formatter.format(input[0]);
		assertEquals("BOM is not stripped", testData.expected("utf-8.html"), output);
	}

	@Test(expected = IllegalArgumentException.class)
	public void configurationChange() throws Exception {
		new EclipseHtmlFormatterStepImpl(new Properties());
	}
}
