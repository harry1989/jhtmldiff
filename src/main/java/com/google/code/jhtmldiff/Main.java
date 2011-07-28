/**
 * Copyright (c) 2011 Nathan Herald, Rohland de Charmoy, Adam Gent
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */

package com.google.code.jhtmldiff;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class Main {
	
	public static void main(String[] args) {
		String oldUri = args[0];
		String newUri = args[1];
		String oldText;
		String newText;
		try {
			oldText = getString(oldUri);
			newText = getString(newUri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		String results = new Diff(oldText, newText).build();
		System.out.print(results);
	}
	
	public static String getString(String path) throws IOException, URISyntaxException {
		return getString(new URI(path));
	}
	
	public static String getString(URI uri) throws IOException {
		InputStream is = null;
		try {
			
			if (uri.getScheme() != null) {
				URL url = uri.toURL();
				is = url.openStream();
			}
			else {
				is = new FileInputStream(uri.toString());
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine + "\n");
			return sb.toString();
		}
		finally {
			try {
				if (is != null) is.close();
			} catch (Exception e) {
			}
		}
	}

	

}
