package com.zavedil.apilator;

public class Config {
	//private static final String DocumentRoot = "/Users/assen/Desktop/L";
	private static final String DocumentRoot = "/root/Desktop/_Pics/L";
	private static final int TcpPort = 8080;
	private static final int LogLevel = 6; // LOG_DEBUG
	private static final String SystemName = "Apilator";
	
	public static int getTcpPort() {
		return TcpPort;
	}
	
	public static String getDocumentRoot() {
		return DocumentRoot;
	}
	
	public static int getLogLevel() {
		return LogLevel;
	}
	
	public static String getSystemName() {
		return SystemName;
	}
}
