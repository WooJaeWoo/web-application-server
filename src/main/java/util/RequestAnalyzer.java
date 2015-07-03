package util;

import java.io.BufferedReader;
import java.io.IOException;

public class RequestAnalyzer {
	
	private int contentLength;
	private String url;
	private String method;
	private boolean isStyleSheet = false;
	private boolean isLogined = false;
	
	public void analysisRequest(BufferedReader br) throws IOException {
		String line = br.readLine();
		while (!"".equals(line)) {
			extractURL(line);
			extractContentLength(line);
			extractAccept(line);
			line = br.readLine();
		}
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getURL() {
		return url;
	}
	
	public int getContentLength() {
		return contentLength;
	}
	
	public boolean getIsStyleSheet() {
		return isStyleSheet;
	}
	
	public boolean getIsLogined() {
		return isLogined;
	}
	
	public void setIsLogined(boolean login) {
		isLogined = login;
	}
	
	private void extractURL(String line) {
		String[] tokens = line.split(" "); 
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("GET") || tokens[i].equals("POST")) {
				method = tokens[i];
				url = tokens[i+1];				
			}
		}
	}
	
	private void extractContentLength(String line) {
		String[] tokens = line.split(" "); 
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("Content-Length:")) {
				contentLength = Integer.parseInt(tokens[i+1]);
			}
		}
	}
	
	private void extractAccept(String line) {
		String[] tokens = line.split(" "); 
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("Accept:") && tokens[i+1].contains("text/css")) {
				isStyleSheet = true;		
			}
		}
	}
	
	/*private void extractCookie(String line) {
		String[] tokens = line.split(" "); 
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("Cookie:") && tokens[i+1].equals("logined=true")) {
				isLogined = true;	
			}
		}
	}*/
}
