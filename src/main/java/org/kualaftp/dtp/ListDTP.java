package org.kualaftp.dtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.kualaftp.PI;

/**
 * Specialized DTP that handles LIST command data transfer.
 * 
 * @author jaime
 */
public class ListDTP extends PassiveDTP {
	BufferedReader reader = null;
	List<ListFileWrapper> list = new ArrayList<ListFileWrapper>();
	
	public ListDTP(PI pi, String host, int port) throws IOException {
		super(pi, host, port);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void handleTransfer() {
		String line = null;
		try {
//			String syst = pi.syst();
//			if(syst.contains("UNIX")) {
				reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
				while((line = reader.readLine()) != null) {
					line = line.replaceAll(" +", " ");
//					System.out.println(line);
	
					ListFileWrapper wrapper = new ListFileWrapper();
					String [] splitedLine = line.split(" ");
					
					wrapper.permissions = splitedLine[0];
					String month = splitedLine[5].toLowerCase();
					int numMonth = -1;
					if(month.contains("jan")){
						numMonth = 0;
					}
					if(month.contains("feb")){
						numMonth = 1;
					}
					if(month.contains("mar")){
						numMonth = 2;
					}
					if(month.contains("apr")){
						numMonth = 3;
					}
					if(month.contains("may")){
						numMonth = 4;
					}
					if(month.contains("jun")){
						numMonth = 5;
					}
					if(month.contains("jul")){
						numMonth = 6;
					}
					if(month.contains("aug")){
						numMonth = 7;
					}
					if(month.contains("sep")){
						numMonth = 8;
					}
					if(month.contains("oct")){
						numMonth = 9;
					}
					if(month.contains("nov")){
						numMonth = 10;
					}
					if(month.contains("dec")){
						numMonth = 11;
					}
					if(splitedLine[7].contains(":")) {
						wrapper.date = new Date(
								new Date().getYear(),
								numMonth,
								Integer.valueOf(splitedLine[6])
							);
						wrapper.date.setHours(Integer.valueOf(splitedLine[7].split(":")[0]));
						wrapper.date.setMinutes(Integer.valueOf(splitedLine[7].split(":")[1]));
					} else {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, Integer.valueOf(splitedLine[7]));// Year
						cal.set(Calendar.MONTH, numMonth);// Month
						cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(splitedLine[6]));// Day
						wrapper.date = cal.getTime();
					}

					String filename = "";
					for(int c = 8; c < splitedLine.length; c++) {
						filename += splitedLine[c] + " ";
					}
					filename = filename.substring(0, filename.lastIndexOf(" "));
					wrapper.name = filename;
					
					wrapper.size = Integer.valueOf(splitedLine[4]);
					
					char t = line.charAt(0);
					switch(t) {
					case 'd':// Directory
						wrapper.directory = true;
						list.add(wrapper);
						break;
					case '-':// File
						wrapper.directory = false;
						list.add(wrapper);
						break;
					default:
						// Do nothing.
						break;
					}
					
				}
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<ListFileWrapper> getList() {
		return list;
	}
	
}
