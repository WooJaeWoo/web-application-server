package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import model.UserDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;
import util.RequestAnalyzer;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			
			//Request
			RequestAnalyzer ra = new RequestAnalyzer();
			ra.analysisRequest(br);
			String url = ra.getURL();
			String method = ra.getMethod();
			
			//Response
			DataOutputStream dos = new DataOutputStream(out);
			if (method.equals("GET")) {
				byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
				response200Header(dos, body.length, ra.getIsStyleSheet(), ra.getIsLogined());	
				responseBody(dos, body);
			}
			else if (method.equals("POST")) {
				if (url.equals("/create")) {
					createUser(br, ra.getContentLength());
					response302Header(dos);					
				}
				else if (url.equals("/login")) {
					boolean login = loginUser(br, ra.getContentLength());
					if (login) ra.setIsLogined(true);
					byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
					response200Header(dos, body.length, ra.getIsStyleSheet(), ra.getIsLogined());	
					responseBody(dos, body);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, boolean isStyleSheet, boolean isLogined) {
		try {
			dos.writeBytes("HTTP/1.1 200 Document Follows \r\n");
			if (isStyleSheet)
				dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
			else
				dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			if (isLogined)
				dos.writeBytes("Set-Cookie: logined=true\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	//post방식 createUser
	private void createUser(BufferedReader br, int contentLength) throws IOException {
		if (contentLength == 0)
			return;
		String params = IOUtils.readData(br, contentLength);
		Map<String,String> info = HttpRequestUtils.parseQueryString(params);
		User user = new User(info.get("userId"), info.get("password"), info.get("name"), info.get("email"));
		UserDB.addUser(user);
	}
	
	private boolean loginUser(BufferedReader br, int contentLength) throws IOException {
		if (contentLength == 0)
			return false;
		String params = IOUtils.readData(br, contentLength);
		Map<String,String> info = HttpRequestUtils.parseQueryString(params);
		if (info.get("password").equals(UserDB.getUser(info.get("userId")).getPassword())) {
			return true;
		}
		return false;
	}
	
	//get방식 createUser
	/*int index = url.indexOf("?");
			if (index > -1) {
				createUser(url, index);
	}
	private void createUser(String url, int index) {
		String requestPath = url.substring(0, index);
		String params = url.substring(index+1);
		Map<String,String> info = HttpRequestUtils.parseQueryString(params);
		User user = new User(info.get("userId"), info.get("password"), info.get("name"), info.get("email"));
		UserDB.addUser(user);
	}*/
}
