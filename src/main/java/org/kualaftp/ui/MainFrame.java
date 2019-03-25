/*
 * MainFrame.java
 *
 * Created on 16 de julio de 2009, 10:23 PM
 */
package org.kualaftp.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.kualaftp.PI;
import org.kualaftp.config.Config;
import org.kualaftp.dtp.ListFileWrapper;

/**
 * Main Frame for FTP client interface.
 * 
 * @author  Roxanna
 */
public class MainFrame extends javax.swing.JFrame {
	PI pi = null;

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        serverTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        portTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        usernameTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        passwordTF = new javax.swing.JPasswordField();
        connectBtn = new javax.swing.JButton();
        disconnectBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        localFileTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        remoteFileTable = new javax.swing.JTable();

        localFileTable.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if(e.getClickCount()==2) {
        			int selected = localFileTable.getSelectedRow() - 1;
        			if(selected >= 0){
        				ListFileWrapper wrapper = localFileSystemTableData.get(selected);
	            		if(wrapper.isDirectory()) {
				        	File dir = new File(pi.getLocalDir(), wrapper.getName());
							pi.setLocalDir(dir);
							refreshLocalExplorerTable();
	            		}
        			} else if(selected == -1){
        				pi.setLocalDir(pi.getLocalDir().getParentFile());
						refreshLocalExplorerTable();
        			}
        		}
        	}
        });
        remoteFileTable.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if(e.getClickCount()==2) {
        			int selected = remoteFileTable.getSelectedRow() - 1;
        			if(selected >= 0){
        				ListFileWrapper wrapper = remoteFileSystemTableData.get(selected);
        				try {
    	            		if(wrapper.isDirectory()) {
    				        	String base = pi.pwd();
    				        	String dirname = wrapper.getName();
    							pi.cwd(base + "/" + dirname);
    							refreshRemoteExplorerTable();
    	            		}
    					} catch (IOException e1) {
    						JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
    					}
        			} else if(selected == -1){
        				try {
							pi.cdup();
	        				refreshRemoteExplorerTable();
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
						}
        			}
        		}
        	}
        });
        
        initPopupMenus();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        logTextArea.setColumns(20);
        logTextArea.setRows(5);
        jScrollPane3.setViewportView(logTextArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Server:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Port:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Username:");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Password:");

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new ActionListener(){
        	@SuppressWarnings("deprecation")
			@Override
        	public void actionPerformed(ActionEvent e) {
        		String username = usernameTF.getText();
        		String password = passwordTF.getText();
        		if("".equals(username) || "".equals(password)) {
        			JOptionPane.showMessageDialog(MainFrame.this, "Invalid username/password.");
        			return;
        		}
        		try {
        	    	pi = new PI(
        	    			new Config(serverTF.getText(),
        	    					Integer.valueOf(portTF.getText()),
        	    					true)
        	    			);
        	    	pi.setConsole(logTextArea);
					if(!pi.isConnected()) pi.connect();
	        		if(!pi.isLoggedIn()) pi.login(username, password);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
					return;
				}
				refreshLocalExplorerTable();
				refreshRemoteExplorerTable();
        	}
        });

        disconnectBtn.setText("Disconnect");
        disconnectBtn.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		try {
					pi.quit();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(MainFrame.this, e2.getMessage());
					return;
				}
        	}
        });

        serverTF.setText("127.0.0.1");
        portTF.setText("21");
        usernameTF.setText("jaime");
        passwordTF.setPreferredSize(new Dimension(80, passwordTF.getPreferredSize().height));
        passwordTF.setText("DBc1314");
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(serverTF, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(usernameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(passwordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(connectBtn)
                .addGap(18, 18, 18)
                .addComponent(disconnectBtn)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(serverTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(usernameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(passwordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectBtn)
                    .addComponent(disconnectBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        localFileTable.setModel(new LocalFileSystemTableModel());
        jScrollPane1.setViewportView(localFileTable);

        remoteFileTable.setModel(new RemoteFileSystemTableModel());
        jScrollPane2.setViewportView(remoteFileTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

	private void initPopupMenus() {

        localTablePopupMenu = new JPopupMenu();
        JMenuItem uploadFileItem = new JMenuItem("Upload");
        uploadFileItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = localFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = localFileSystemTableData.get(selected);
		        	try {
	            		if(wrapper.isDirectory()) {
	            			// TODO: implements directory recursive uploading.
	            			JOptionPane.showMessageDialog(MainFrame.this, "Folder upload not supported!");
			        	} else {
			        		String filename = wrapper.getName();
							pi.stor(filename);
							refreshRemoteExplorerTable();
	            		}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
					}
        		}
	        }
        });
        JMenuItem deleteFileItem = new JMenuItem("Delete");
        deleteFileItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = localFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = localFileSystemTableData.get(selected);
	        		File file = new File(pi.getLocalDir(), wrapper.getName());
	        		file.delete();
	        		refreshLocalExplorerTable();
        		}
	        }
        });
        JMenuItem openDirectoryItem = new JMenuItem("Open");
        openDirectoryItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = localFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = localFileSystemTableData.get(selected);
            		if(wrapper.isDirectory()) {
			        	File dir = new File(pi.getLocalDir(), wrapper.getName());
						pi.setLocalDir(dir);
						refreshLocalExplorerTable();
            		} else {
            			// TODO: implements directory recursive uploading.
            		}
        		}
	        }
        });
        JMenuItem createDirectoryItem = new JMenuItem("Create folder");
        createDirectoryItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		String name = JOptionPane.showInputDialog("Enter folder name: ");
        		File dir = new File(pi.getLocalDir(), name);
        		if(!dir.exists()) {
        			dir.mkdir();
        			refreshLocalExplorerTable();
        		} else {
        			JOptionPane.showMessageDialog(MainFrame.this, "Folder/File "+name+" already exists.");
        		}
	        }
        });

        localTablePopupMenu.add(openDirectoryItem);
        localTablePopupMenu.add(createDirectoryItem);
        localTablePopupMenu.add(new JSeparator());
        localTablePopupMenu.add(uploadFileItem);
        localTablePopupMenu.add(deleteFileItem);
        
        localFileTable.addMouseListener(new PopupListener(localTablePopupMenu, localFileTable));
        localFileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        remoteTablePopupMenu = new JPopupMenu();
        JMenuItem remoteDownloadFileItem = new JMenuItem("Download");
        remoteDownloadFileItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = remoteFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = remoteFileSystemTableData.get(selected);
		        	try {
	            		if(wrapper.isDirectory()) {
	            			// TODO: implements directory recursive uploading.
	            			JOptionPane.showMessageDialog(MainFrame.this, "Folder download not supported!");
			        	} else {
			        		String filename = wrapper.getName();
							pi.retr(filename);
							refreshLocalExplorerTable();
	            		}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
					}
        		}
	        }
        });
        JMenuItem remoteDeleteFileItem = new JMenuItem("Delete");
        remoteDeleteFileItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = remoteFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = remoteFileSystemTableData.get(selected);
		        	try {
	            		if(!wrapper.isDirectory()) {
				        	String filename = wrapper.getName();
							pi.dele(filename);
							refreshRemoteExplorerTable();
	            		} else {
	            			// TODO: implements directory recursive uploading.
	            		}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
					}
        		}
	        }
        });
        JMenuItem remoteOpenDirectoryItem = new JMenuItem("Open");
        remoteOpenDirectoryItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		int selected = remoteFileTable.getSelectedRow()-1;
        		if(selected>=0) {
            		ListFileWrapper wrapper = remoteFileSystemTableData.get(selected);
		        	try {
	            		if(wrapper.isDirectory()) {
				        	String base = pi.pwd();
				        	String dirname = wrapper.getName();
							pi.cwd(base + "/" + dirname);
							refreshRemoteExplorerTable();
	            		}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
					}
        		}
	        }
        });
        JMenuItem remoteCreateDirectoryItem = new JMenuItem("Create folder");
        remoteCreateDirectoryItem.addActionListener(new ActionListener() {
        	@Override
	        public void actionPerformed(ActionEvent e) {
        		String name = JOptionPane.showInputDialog("Enter folder name: ");
        		try {
					pi.mkdir(name);
					refreshRemoteExplorerTable();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
				}
	        }
        });
        
        remoteTablePopupMenu.add(remoteOpenDirectoryItem);
        remoteTablePopupMenu.add(remoteCreateDirectoryItem);
        remoteTablePopupMenu.add(new JSeparator());
        remoteTablePopupMenu.add(remoteDownloadFileItem);
        remoteTablePopupMenu.add(remoteDeleteFileItem);
        
        remoteFileTable.addMouseListener(new PopupListener(remoteTablePopupMenu, remoteFileTable));
        remoteFileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
	}

	private void refreshLocalExplorerTable() {
		try {
			localFileSystemTableData = pi.localList();
			localFileTable.revalidate();
			localFileTable.repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
		}
	}

	private void refreshRemoteExplorerTable() {
		try {
			remoteFileSystemTableData = pi.list();
			remoteFileTable.revalidate();
			remoteFileTable.repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainFrame.this, e.getMessage());
		}
	}

	class LocalFileSystemTableModel extends DefaultTableModel {
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 0) return JLabel.class;
			else return String.class;
		}
		
		@Override
		public int getColumnCount() {
			return 4;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
				case 0:
					return "File Name";
				case 1:
					return "Size (bytes)";
				case 2:
					return "Creation Date";
				case 3:
					return "Permissions";
				default:
					return "";
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if(row == 0) {
				switch(column) {
					case 0:
						return "..";
					default:
						return "";
				}
			} else {//if(row > 0) {
				ListFileWrapper wrapper = localFileSystemTableData.get(row-1);
				switch(column) {
					case 0:
						return wrapper.getName();
					case 1:
						return wrapper.getSize();
					case 2:
						return sdf.format(wrapper.getDate());
					case 3:
						return wrapper.getPermissions();
					default:
						return "";
				}
			}
		}
	
		@Override
		public int getRowCount() {
			return localFileSystemTableData.size()+1;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

	class RemoteFileSystemTableModel extends DefaultTableModel {
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == 0) return JLabel.class;
			else return String.class;
		}
		
		@Override
		public int getColumnCount() {
			return 4;
		}
		
		@Override
		public String getColumnName(int column) {
			switch(column) {
				case 0:
					return "File Name";
				case 1:
					return "Size (bytes)";
				case 2:
					return "Creation Date";
				case 3:
					return "Permissions";
				default:
					return "";
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if(row == 0) {
				switch(column) {
					case 0:
						return "..";
					default:
						return "";
				}
			} else {//if(row > 0) {
				ListFileWrapper wrapper = remoteFileSystemTableData.get(row-1);
				switch(column) {
					case 0:
						return wrapper.getName();
					case 1:
						return wrapper.getSize();
					case 2:
						return sdf.format(wrapper.getDate());
					case 3:
						return wrapper.getPermissions();
					default:
						return "";
				}
			}
		}
	
		@Override
		public int getRowCount() {
			return remoteFileSystemTableData.size()+1;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}
	
	/**
	 * This class show dynamically popups on popup trigger events.
	 */
	class PopupListener extends MouseAdapter {
		JPopupMenu popup = null;
		JTable table = null;
		
		public PopupListener(JPopupMenu popup, JTable table) {
			this.popup = popup;
			this.table = table;
		}
		
	    public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				Point p = e.getPoint();
				int rowNumber = table.rowAtPoint(p);
				ListSelectionModel model = table.getSelectionModel();
				model.setSelectionInterval(rowNumber, rowNumber);
			}
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	            MainFrame.this.pack();
	        }
	    }
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectBtn;
    private javax.swing.JButton disconnectBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable localFileTable;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JPasswordField passwordTF;
    private javax.swing.JTextField portTF;
    private javax.swing.JTable remoteFileTable;
    private javax.swing.JTextField serverTF;
    private javax.swing.JTextField usernameTF;
    private JPopupMenu localTablePopupMenu;
    private JPopupMenu remoteTablePopupMenu;
    
    private ImageIcon dir = null, file = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private List<ListFileWrapper> localFileSystemTableData = new ArrayList<ListFileWrapper>();
    private List<ListFileWrapper> remoteFileSystemTableData = new ArrayList<ListFileWrapper>();;
}
