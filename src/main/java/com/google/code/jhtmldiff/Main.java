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
		String results = new Diff(oldText, newText).Build();
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
