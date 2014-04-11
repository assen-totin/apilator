package com.zavedil.apilator;

public class Config {
	private static final String DocumentRoot = "/Users/assen/Desktop/L";
	private static final int TcpPort = 8080;
	
	public static int getTcpPort() {
		return TcpPort;
	}
	
	public static String getDocumentRoot() {
		return DocumentRoot;
	}
	
}
