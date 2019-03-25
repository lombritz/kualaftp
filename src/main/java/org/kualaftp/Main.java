package org.kualaftp;

import java.awt.EventQueue;
import javax.swing.UIManager;
import org.kualaftp.ui.MainFrame;

public class Main {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				MainFrame mf = new MainFrame();
//				mf.setResizable(false);
				mf.setVisible(true);
			};
		}
		);
	}
	
}
