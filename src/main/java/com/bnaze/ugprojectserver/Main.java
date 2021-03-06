package com.bnaze.ugprojectserver;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	
	static double a1x = 0;
	static double a1y = 0;
	
	static double v1x = 0;
	static double p1x = 0;
	
	static double v2x = 0;
	static double p2y = 0;
	
	private static Robot robot;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, AWTException {
		ServerSocket server = new ServerSocket(1024);
		robot = new Robot();

		Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
		while (ni.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) ni.nextElement();
			Enumeration<InetAddress> en = n.getInetAddresses();
			while (en.hasMoreElements()) {
				InetAddress i = (InetAddress) en.nextElement();
				String address = i.getHostAddress();
				System.out.println(String.format("Server started at %s:%s", address, server.getLocalPort()));
			}
		}
		
		while (true) {
			Socket socket = server.accept();
			System.out.println("Connection establised");
			InputStream inputStream = socket.getInputStream();
			handleSocketConnection(inputStream);
		}
	}
	
	private static void handleSocketConnection(InputStream inputStream) throws IOException {
		new Thread(() -> {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String text = "";
			try {
				while ((text = br.readLine()) != null) {
					try {
						JSONObject json = new JSONObject(text);
						process(json);
					}
					catch(JSONException e) {
					}
				}
			} catch (IOException e) {
			}
		}).start();
	}

	private static void process(JSONObject json) throws JSONException {	
		new Thread(() -> {
			try {
			String type = json.getString("type");
			switch (type) {
			case "MOUSE_SCROLL": {
				JSONObject value = json.getJSONObject("value");
				float y = value.getInt("y");
				robot.mouseWheel((int) y);
			}
				break;
			case "MOUSE_LEFT_CLICK": {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
				break;
			case "MOUSE_DOUBLE_CLICK": {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
				break;
			case "MOUSE_RIGHT_CLICK": {
				robot.mousePress(InputEvent.BUTTON3_MASK);
				robot.mouseRelease(InputEvent.BUTTON3_MASK);
			}
				break;
			case "MOUSE_MOVE": {
				JSONObject value = json.getJSONObject("value");

				float dx = value.getInt("x");
				float dy = value.getInt("y");

				Point location = MouseInfo.getPointerInfo()
					.getLocation();

				double x = location.getX() - dx;
				double y = location.getY() - dy;

				robot.mouseMove((int) x, (int) y);
			}
				break;
			case "ACC_MOVE": {
				//Implement here
				JSONObject value = json.getJSONObject("value");

				float dx = value.getInt("x");
				float dy = value.getInt("y");

				Point location = MouseInfo.getPointerInfo()
					.getLocation();
				
				double x = location.getX() - dx;
				double y = location.getY() - dy;

				robot.mouseMove((int) x, (int) y);

			}
			break;
			case "AIR_MOUSE": {
				JSONObject value = json.getJSONObject("value");

				float dx = value.getInt("x");
				float dy = value.getInt("y");
				
				Point location = MouseInfo.getPointerInfo()
						.getLocation();
				
				double x = location.getX() - dx;
				double y = location.getY() - dy;

				robot.mouseMove((int) x, (int) y);

			}
				break;
			case "KEYBOARD_INPUT": {
				JSONObject value = json.getJSONObject("value");
				int keyCode = value.getInt("keyCode");
				if(keyCode == 0) {
					robot.keyPress(KeyEvent.VK_BACK_SPACE);
					break;
				}
				boolean isShiftPressed = value.getBoolean("shift");
				int event = KeyEvent.getExtendedKeyCodeForChar(keyCode);

				if(isShiftPressed) {
					robot.keyPress(KeyEvent.VK_SHIFT);
				}
				robot.keyPress(event);
				robot.keyRelease(event);
				if(isShiftPressed) {
					robot.keyRelease(KeyEvent.VK_SHIFT);
				}
			}
				break;
			}
			}
			catch(Exception e) {
				
			}
		}).start();
	}
}