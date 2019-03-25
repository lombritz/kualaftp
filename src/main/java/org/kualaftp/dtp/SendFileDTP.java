package org.kualaftp.dtp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kualaftp.PI;

public class SendFileDTP extends PassiveDTP {
	DataInputStream is = null;
	DataOutputStream os = null;
	String filename = null;
	
	public SendFileDTP(PI pi, String host, int port, String filename) throws IOException {
		super(pi, host, port);
		this.filename = filename;
	}
	
	@Override
	public void handleTransfer() throws IOException {
		try {
			int read = -1;
			int writen = 0;
			byte [] buffer = new byte[8];
			// TODO: use a current directory variable to create and save the file.
			// File file = new File(new File(currDir), filename);
			File file = new File(pi.getLocalDir(), filename);
			is = new DataInputStream(new FileInputStream(file));
			os = new DataOutputStream(dataSocket.getOutputStream());
			while((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
				writen += read;
				os.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dataSocket.close();
		}
	}
}
