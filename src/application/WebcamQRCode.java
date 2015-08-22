package application;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


public class WebcamQRCode extends JFrame implements Runnable, ThreadFactory {

	private static final long serialVersionUID = 6441489157408381878L;

	private Executor executor = Executors.newSingleThreadExecutor(this);

	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private Main mainApp= null;
	private boolean terminated = false;
	
	//private JTextArea textarea = null;

	public WebcamQRCode(Main main) {
		super();
		mainApp = main;
		
		setLayout(new FlowLayout());
		setTitle("Read QR / Bar Code With Webcam");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt){
                stopCam();            
           }
        });
		

		Dimension size = WebcamResolution.VGA.getSize();

		webcam = Webcam.getWebcams().get(0);
		webcam.setViewSize(size);

		panel = new WebcamPanel(webcam);
		panel.setPreferredSize(size);

		//textarea = new JTextArea();
		//textarea.setEditable(false);
		//textarea.setPreferredSize(size);

		add(panel);
		//add(textarea);

		pack();
		setVisible(true);

		executor.execute(this);
	}

	
	public void run() {

		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Result result = null;
			BufferedImage image = null;

			if (webcam.isOpen()) {

				if ((image = webcam.getImage()) == null) {
					continue;
				}

				LuminanceSource source = new BufferedImageLuminanceSource(image);
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

				try {
					result = new MultiFormatReader().decode(bitmap);
				} catch (NotFoundException e) {
					// fall thru, it means there is no QR code in image
				}
			}else{
				terminated = true;
				processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}

			if (result != null) {
				mainApp.setValueQRcode(result.getText());
				//System.out.println(result.getText());
				processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				
				break;
			}

		} while (!terminated);
		
		if(webcam.isOpen()){
			webcam.close();
		}
		
	}

	
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, "example-runner");
		t.setDaemon(true);
		return t;
	}
	
	public void stopCam(){
		terminated = true;
	}

}
