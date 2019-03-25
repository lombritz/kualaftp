package org.kualaftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import javax.swing.JTextArea;

import org.kualaftp.config.Config;
import org.kualaftp.dtp.ListDTP;
import org.kualaftp.dtp.ListFileWrapper;
import org.kualaftp.dtp.PassiveDTP;
import org.kualaftp.dtp.RetrieveFileDTP;
import org.kualaftp.dtp.SendFileDTP;

/**
 * Protocol Interpreter process.
 * 
 * @author jaime
 */
public class PI {

	private Socket controlSocket = null; 
	private Config config;
	private InetSocketAddress serverAddress = null;
	private InetSocketAddress bindAddress = null;
	private BufferedReader controlReader = null;
	private BufferedWriter controlWriter = null;
	private String serverReply = null;
	private boolean loggedIn = false;
	private JTextArea console = null;
	
	private File localDir = null;
	
	public PI(Config config) throws UnknownHostException {
		this.config = config;
		localDir = new File("/home/jaime/Desktop/");
		System.out.println(localDir.getAbsolutePath());
		serverAddress = new InetSocketAddress(config.getServerHost(), config.getServerPort());
		bindAddress = new InetSocketAddress(1024 + new Random().nextInt(65535-1024));
	}
	
	public void setConsole(JTextArea console) {
		this.console = console;
	}
	
	private void println(String str) {
		System.out.println(str);
		console.append(str+"\n");
	}
	
	/**
	 * Establishes a connection to the FTP server. 
	 */
	public void connect()
				throws Exception {
		System.out.println("Connecting to " + config.getServerHost() + " FTP server...");
		controlSocket = new Socket();
		bindAddress = new InetSocketAddress(1024 + new Random().nextInt(65535-1024));
		controlSocket.bind(bindAddress);
		controlSocket.connect(serverAddress);
		controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		controlWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
		serverReply = controlReader.readLine();
		println(serverReply);
	}

	public void login(String username, String password)
			throws Exception {
		if(isConnected()) {
			controlWriter.write("USER " + username + "\n");
			println("USER " + username);
			controlWriter.flush();
			serverReply = controlReader.readLine();
			println(serverReply);
			controlWriter.write("PASS " + password + "\n");
			println("PASS ****");
			controlWriter.flush();
			serverReply = controlReader.readLine();
			println(serverReply);
			if(serverReply.startsWith("230")) {
				loggedIn = true;
			} else
				throw new IOException(serverReply);
		} else throw new IOException("Not connected!");
	}
	
	public void logout() throws IOException {
		if(isConnected()) {
			controlWriter.write("QUIT\n");
			controlWriter.flush();
			serverReply = controlReader.readLine();
			println(serverReply);
			if(serverReply.startsWith("221")) {
				loggedIn = false;
			} else
				throw new IOException(serverReply);
		} else throw new IOException("Not connected!");
	}

	public List<ListFileWrapper> localList() {
		List<ListFileWrapper> list = new ArrayList<ListFileWrapper>();
		if(localDir!=null) {
			System.out.println(localDir.isDirectory() + " " + Arrays.toString(localDir.list()));
			for(File f : localDir.listFiles()) {
				ListFileWrapper wrapper = new ListFileWrapper();
				wrapper.setName(f.getName());
				wrapper.setDate(new Date(f.lastModified()));
				wrapper.setDirectory(f.isDirectory());
				// TODO: get file permission.
				wrapper.setPermissions("----------");
				wrapper.setSize(f.length());
				list.add(wrapper);
			}
		}
		return list;
	}
	
	public List<ListFileWrapper> list() throws IOException, InterruptedException {
		if(isConnected()) {
			if(config.isPassive()) {
				controlWriter.write("PASV\n");
				controlWriter.flush();
				serverReply = controlReader.readLine();
				println(serverReply);
				
				String [] splited = serverReply
					.substring(serverReply.indexOf('(')+1, serverReply.indexOf(')'))
					.split(",");
				
				// DTP parameters
				String serverHost = splited[0]+"."+splited[1]+"."+splited[2]+"."+splited[3];
				int serverPort = 256 * Integer.valueOf(splited[4]) + Integer.valueOf(splited[5]);
				
				// Initializing DTP
				PassiveDTP dtp = new ListDTP(this, serverHost ,serverPort);
				
				// Starts DTP on a dedicated thread.
				Thread dtpThread = new Thread(dtp);
				dtpThread.start();

				Lock listLock = dtp.getLock();
				
				controlWriter.write("LIST\n");
				controlWriter.flush();

				serverReply = controlReader.readLine();
				println(serverReply);
				
				// Wait until list is ready to be read.
				listLock.lock();
				listLock.unlock();
				
				serverReply = controlReader.readLine();
				println(serverReply);
				return ((ListDTP)dtp).getList();
			} else {
				// TODO: implements active mode logic.
				throw new UnsupportedOperationException("Active mode not supported!");
			}
		} else throw new IOException("Not connected!");
	}

//	public void nlst() throws IOException, InterruptedException {
//		if(isConnected()) {
//			if(config.isPassive()) {
//				controlWriter.write("PASV\n");
//				controlWriter.flush();
//				serverReply = controlReader.readLine();
//				println(serverReply);
//				
//				String [] splited = serverReply
//					.substring(serverReply.indexOf('(')+1, serverReply.indexOf(')'))
//					.split(",");
//				
//				// DTP parameters.
//				String serverHost = splited[0]+"."+splited[1]+"."+splited[2]+"."+splited[3];
//				int serverPort = 256 * Integer.valueOf(splited[4]) + Integer.valueOf(splited[5]);
//				
//				// Initializing DTP.
//				PassiveDTP dtp = new ListDTP(this, serverHost ,serverPort);
//				
//				// Starts DTP on a dedicated thread.
//				Thread dtpThread = new Thread(dtp);
//				dtpThread.start();
//
//				Lock listLock = dtp.getLock();
//				
//				controlWriter.write("LIST\n");
//				controlWriter.flush();
//
//				serverReply = controlReader.readLine();
//				println(serverReply);
//				
//				listLock.lock();
//				// Means that the File list is ready to be read.
//				
//				serverReply = controlReader.readLine();
//				println(serverReply);
//
//				listLock.unlock();
//			} else {
//				// TODO: implements active mode logic.
//			}
//		} else throw new IOException("Not connected!");
//	}
	
	/**
	 * Retrieves a file from the remote FTP file system. The
	 * file is saved on the local file system in the current
	 * directory, using the same name.
	 * 
	 * @param the remote file name.
	 */
	public void retr(String filename) throws IOException {
		if(isConnected()) {
			if(config.isPassive()) {
				// Setting up DTP parameters
				String socket = pasv();
				String serverHost = socket.split(":")[0];
				int serverPort = Integer.valueOf(socket.split(":")[1]);
				
				// Initializing DTP.
				PassiveDTP dtp = new RetrieveFileDTP(this, serverHost, serverPort, filename);

				// Starts DTP on a dedicated thread.
				Thread dtpThread = new Thread(dtp);
				dtpThread.start();

				Lock fileTransferLock = dtp.getLock();
				
				controlWriter.write("RETR " + filename + "\n");
				controlWriter.flush();

				serverReply = controlReader.readLine();
				println(serverReply);

				fileTransferLock.lock();
				fileTransferLock.unlock();

				serverReply = controlReader.readLine();
				println(serverReply);
			} else {
				// TODO: implements active mode logic.
			}
		}
	}

	/**
	 * Stores a file from the local file system. The
	 * file is saved on the remote FTP file system in
	 * the current directory, using the same name.
	 * 
	 * @param the remote file name.
	 */
	public void stor(String filename) throws IOException {
		if(isConnected()) {
			if(config.isPassive()) {
				// Setting up DTP parameters
				String socket = pasv();
				String serverHost = socket.split(":")[0];
				int serverPort = Integer.valueOf(socket.split(":")[1]);
				
				// Initializing DTP.
				PassiveDTP dtp = new SendFileDTP(this, serverHost, serverPort, filename);

				// Starts DTP on a dedicated thread.
				Thread dtpThread = new Thread(dtp);
				dtpThread.start();

				Lock fileTransferLock = dtp.getLock();
				
				controlWriter.write("STOR " + filename + "\n");
				controlWriter.flush();

				serverReply = controlReader.readLine();
				println(serverReply);

				fileTransferLock.lock();
				fileTransferLock.unlock();

				serverReply = controlReader.readLine();
				println(serverReply);
			} else {
				// TODO: implements active mode logic.
			}
		}
	}
	
	public void dele(String filename) throws IOException {
		if(isConnected()) {
			controlWriter.write("DELE " + filename + "\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
		}
	}
	
	public String pasv() throws IOException {
		if(isConnected()) {
			controlWriter.write("PASV\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
			
			String [] splited = serverReply
				.substring(serverReply.indexOf('(')+1, serverReply.indexOf(')'))
				.split(",");
			
			// DTP parameters
			String host = splited[0]+"."+splited[1]+"."+splited[2]+"."+splited[3];
			Integer port = 256 * Integer.valueOf(splited[4]) + Integer.valueOf(splited[5]);
			return host+":"+port;
		}else throw new UnsupportedOperationException("Not connected!");
	}
	
	/**
	 * 
	 * Sets the type of file to be transferred. type-character can be any of:
	 * 		A - ASCII text
	 * 		E - EBCDIC text
	 * 		I - image (binary data)
	 * 		L - local format 
	 * For A and E, the second-type-character specifies how the text should be interpreted. It can be:
	 *  	N - Non-print (not destined for printing). This is the default if second-type-character is omitted.
	 *  	T - Telnet format control (<CR>, <FF>, etc.)
	 *  	C - ASA Carriage Control 
	 * For L, the second-type-character specifies the number of bits per byte on the local system, and may not be omitted.
	 * 
	 * @throws IOException 
	 * */
	public void type(String type) throws IOException {
		if(isConnected()) {
			controlWriter.write("TYPE " + type + "\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
		}
	}
	
	public String syst() throws IOException {
		if(isConnected()) {
			controlWriter.write("SYST\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
		}
		return new String(serverReply);
	}

	public String pwd() throws IOException {
		String dir = null;
		if(isConnected()) {
			controlWriter.write("PWD\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
			
			dir = serverReply.split("\"")[1];
		}
		return dir;
	}

	/**
	 * Change the working directory on the FTP remote file system.
	 * 
	 * @param dirname the name of the directory you want to change on.
	 */
	public void cwd(String dirname)
			throws IOException {
		if(isConnected()) {
			controlWriter.write("CWD " + dirname + "\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
			
		}
	}

	/**
	 * Moves to the parent directory on the FTP remote file system.
	 * 
	 */
	public void cdup()
			throws IOException {
		if(isConnected()) {
			controlWriter.write("CDUP\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
			
		}
	}

	/**
	 * Create a directory on the FTP remote file system.
	 * 
	 * @param name the name of the remote directory to create.
	 */
	public void mkdir(String name) throws IOException {
		if(isConnected()) {
			controlWriter.write("MKD " + name + "\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
		}
	}

	/**
	 * Deletes a directory on the FTP remote file system.
	 * 
	 * @param name the name of the remote directory to delete.
	 */
	public void rmdir(String name) throws IOException {
		if(isConnected()) {
			controlWriter.write("RMD " + name + "\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
		}
	}

	/**
	 * Renames a file on the FTP remote file system.
	 * 
	 * @param from actual name of the file to rename.
	 * @param to desired new name for the file.
	 */
	public void renameFile(String from, String to) throws IOException {
		if(isConnected()) {
			controlWriter.write("RNFR " + from + "\n");
			controlWriter.flush();

			serverReply = controlReader.readLine();
			println(serverReply);
			
			controlWriter.write("RNTO " + to + "\n");
			controlWriter.flush();

			serverReply = controlReader.readLine();
			println(serverReply);
		}
	}
	
	/**
	 * Utility method that generate a random port, in
	 * PORT/PASV notation (e.g.: port = 4,15).
	 * 
	 * @return the two integers of the port in an array.
	 */
	@SuppressWarnings("unused")
	private int[] getRandomPort() {
		int [] port = new int[2];
		int random = 1024 + new Random().nextInt(65536 - 1024);
//		println("Randomly generated port: " + random);
		port[0] = random/256;
		port[1] = random%256;
		return port;
	}

	public void quit() throws IOException {
		if(isConnected()) {
			controlWriter.write("QUIT\n");
			controlWriter.flush();
			
			serverReply = controlReader.readLine();
			println(serverReply);
			
			controlSocket.close();
		}
	}	
	
	public boolean isConnected() {
		return controlSocket!=null && controlSocket.isConnected();
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public File getLocalDir() {
		return localDir;
	}

	public void setLocalDir(File dir) {
		localDir = dir;
	}
	
	public static void main(String[] args) {
		PI pi = null;
		try {
			pi = new PI(new Config("127.0.0.1", 21, true));
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			pi.connect();
			pi.login("jaime", "DBc1314");
//			pi.retr("Test.txt");// OK
			pi.stor("Test.txt");// OK
//			pi.dele("Test.txt");// OK
//			pi.cwd("Booker");// OK
//			pi.cdup();
//			pi.cwd("papi");
//			pi.pwd();// OK
//			System.out.println(Arrays.toString(pi.list().toArray()));// OK
//			pi.rmdir("a1");// OK
//			pi.renameFile("kernel2.c", "kernel.c");// OK
//			pi.syst();// OK
//			pi.type("L8");// OK
//			pi.logout();// OK
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}