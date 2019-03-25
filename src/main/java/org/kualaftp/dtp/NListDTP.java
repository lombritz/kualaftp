package org.kualaftp.dtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.kualaftp.PI;

/**
 * Specialized DTP that handles NLST command data transfer.
 * 
 * @author jaime
 */
public class NListDTP extends PassiveDTP {
	BufferedReader reader = null;
	
	public NListDTP(PI pi, String host, int port) throws IOException {
		super(pi, host, port);
	}
	
	@Override
	public void handleTransfer() {
		String line = null;
		try {
			reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
			while((line = reader.readLine()) != null) {
//				System.out.println(line);
				FileWrapper wrapper = new FileWrapper();
				// TODO: parse line.
//				if()
//				wrapper.permissions
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<FileWrapper> getList() {
		List<FileWrapper> list = null;
		
		return list;
	}
	
	class FileWrapper {
		String permissions;
		int size;
		Date date;
		String name;
	}
}
