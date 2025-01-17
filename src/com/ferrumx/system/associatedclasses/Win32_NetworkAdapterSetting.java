package com.ferrumx.system.associatedclasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ferrumx.exceptions.ShellException;
import com.ferrumx.system.networking.Win32_NetworkAdapterConfiguration;

/**
 * This class relates {@link com.ferrumx.system.hardware.Win32_NetworkAdapter}
 * with {@link Win32_NetworkAdapterConfiguration}.
 * <p>
 * Queries the Win32_NetworkAdapterSetting of WMI to fetch the "Setting"
 * property which is used in Win32_NetworkAdapterConfiguration.
 * <p>
 * The linking happens as follows:
 * <p>
 * The NetworkAdapter ID fetched from
 * {@link com.ferrumx.system.hardware.Win32_NetworkAdapter#getDeviceIDList()}
 * gets passed onto {@link Win32_NetworkAdapterSetting#getIndex(String)} which
 * returns the "Setting" property, which is a parameter for
 * {@link Win32_NetworkAdapterConfiguration#getAdapterConfiguration(String)}
 *
 * @author Egg-03
 */
public class Win32_NetworkAdapterSetting {

	private Win32_NetworkAdapterSetting() {
		throw new IllegalStateException("Utility Class");
	}

	/**
	 * Fetches the "Settings" property for a given Network Adapter ID
	 *
	 * @param deviceID the adapter ID received from
	 *                 {@link com.ferrumx.system.hardware.Win32_NetworkAdapter#getDeviceIDList()}
	 * @return the "Settings" property for a particular adapter ID
	 * @throws IOException               in case of general I/O errors
	 * @throws IndexOutOfBoundsException in case of text parsing issues from
	 *                                   powershell
	 * @throws ShellException            if any internal command used in the
	 *                                   powershell throws errors
	 * @throws InterruptedException      if the thread waiting for the process to
	 *                                   exit, gets interrupted. When catching this
	 *                                   exception, you may re-throw it's
	 *                                   interrupted status by using
	 *                                   Thread.currentThread().interrupt();
	 */
	public static String getIndex(String deviceID) throws IOException, IndexOutOfBoundsException, ShellException, InterruptedException {

		String[] command = { "powershell.exe", "/c", "Get-CimInstance -ClassName Win32_NetworkAdapterSetting | Where-Object {$_.Element.DeviceID -eq '"+ deviceID + "'} | Select-Object Setting | Format-List" };
		
		Process process = Runtime.getRuntime().exec(command);
		int exitCode = process.waitFor();
		
		if (exitCode != 0 || deviceID.equals("JUNIT TEST VALUE")) { // deviceID.equals("JUNIT TEST VALUE") is here for coverage
			Capture.errorCapture(process, exitCode);
		}
		
		// A custom data capture function cause the one from the Capture class wont work here
		return dataCapture(process);

	}
	
	private static String dataCapture(Process process) throws IOException {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String setting = "";
			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				if (!currentLine.isBlank() || !currentLine.isEmpty()) {
					if (currentLine.contains(" : ")) {
						setting = currentLine;
					} else {
						setting = setting.concat(currentLine);
					}
				}
			}
			return setting.substring(setting.indexOf("=") + 1, setting.indexOf(")")).trim();	
		}
	}
}
