package com.egg.system.hardware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.egg.system.logger.ErrorLog;

public class Win32_Printer {
	private static String classname = "Win32_Printer";
	private Win32_Printer() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static List<String> getDeviceIDList() throws IOException, IndexOutOfBoundsException {
		String methodName = "getDeviceIDList()";
		List<String> deviceIDList = new ArrayList<>();
		
		String[] command = {"powershell.exe", "/c", "Get-CimInstance -ClassName Win32_Printer | Select-Object DeviceID | Format-List"};
		Process process = Runtime.getRuntime().exec(command);
		try {
			int exitCode = process.waitFor();
			if(exitCode!=0) {
				BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String errorLine;
     			List<String> errorList = new ArrayList<>();
				
				while((errorLine=error.readLine())!=null)
					if(!errorLine.isBlank() || !errorLine.isEmpty())
						errorList.add(errorLine);
				
				error.close();
				ErrorLog errorLog = new ErrorLog();
				
				errorLog.log("\n"+classname+"-"+methodName+"\n"+errorList.toString()+"\nProcess Exited with code:"+exitCode+"\n");
				return Collections.emptyList();
			}
		}catch (InterruptedException e) {
			ErrorLog errorLog = new ErrorLog();
			errorLog.log("\n"+classname+"-"+methodName+"\n"+e.getMessage()+"\n\n");
			Thread.currentThread().interrupt();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
		String currentLine;
		int lastIndex;
		String value = "";
		while((currentLine=br.readLine())!=null)
			if(!currentLine.isBlank() || !currentLine.isEmpty()) {
				if(currentLine.contains(" : "))
					deviceIDList.add(value =currentLine);
				else {
					lastIndex = deviceIDList.size()-1;
					deviceIDList.set(lastIndex, deviceIDList.get(lastIndex).concat(value));
				}
			}
				
		br.close();
		
		//strip the property_name and keep only the property value
		for(int i=0 ; i<deviceIDList.size(); i++) {
			deviceIDList.set(i, deviceIDList.get(i).substring(deviceIDList.get(i).indexOf(":")+1).strip());
		}
		
		return deviceIDList;
	}
	
	public static Map<String, String> getCurrentPrinter(String deviceID) throws IOException, IndexOutOfBoundsException {
		String methodName = "getCurrentPrinter(String deviceID)";
		String[] command = {"powershell.exe", "/c", "Get-CimInstance -ClassName Win32_Printer | Where-Object {$_.DeviceID -eq '"+deviceID+"'} | Select-Object Name, HorizontalResolution, VerticalResolution, Capabilites, CapabilityDescriptions, Default, DriverName, Hidden, Local, Network, PortName, PrintProcessor, Shared, ShareName, SpoolEnabled, WorkOffline  | Format-List"};
		
		Process process = Runtime.getRuntime().exec(command);
		try {
			int exitCode = process.waitFor();
			if(exitCode!=0) {
				BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String errorLine;
     			List<String> errorList = new ArrayList<>();
				
				while((errorLine=error.readLine())!=null)
					if(!errorLine.isBlank() || !errorLine.isEmpty())
						errorList.add(errorLine);
				
				error.close();
				ErrorLog errorLog = new ErrorLog();
				
				errorLog.log("\n"+classname+"-"+methodName+"\n"+errorList.toString()+"\nProcess Exited with code:"+exitCode+"\n");
				return Collections.emptyMap();
			}
		}catch (InterruptedException e) {
			ErrorLog errorLog = new ErrorLog();
			errorLog.log("\n"+classname+"-"+methodName+"\n"+e.getMessage()+"\n\n");
			Thread.currentThread().interrupt();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		String currentLine;
		Map<String, String> propertyValues = new LinkedHashMap<>();
		
		while((currentLine=br.readLine())!=null)
			if(!currentLine.isBlank() || !currentLine.isEmpty()) {
				String key = "";
				String value = "";
				if(currentLine.contains(" : "))
					propertyValues.put(key=currentLine.substring(0, currentLine.indexOf(":")).strip(), value =currentLine.substring(currentLine.indexOf(":")+1).strip());
				else
					propertyValues.replace(key, value.concat(currentLine.strip()));
			}
		br.close();
		
		return propertyValues;
	}
}
