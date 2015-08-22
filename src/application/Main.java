package application;
	

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;


public class Main extends Application {
	
	private ObservableList<String> listTimeZones = FXCollections.observableArrayList("UTC-Coordinated Universal Time","BRT-Brasília Time","AMT-Amazon Time");
	private String urlDBSessionConnection = "jdbc:mysql://localhost/synctvsession";
	private String userDBSessionConnection = "root";
	private String passDBSessionConnection = "";
	private Double widthMainStage = 1280.0, heightMainStage = 720.0;
	private Double widthLoginStage = 340.0, heightLoginStage = 300.0;
	private Double widthSignStage = 480.0, 	heightSignStage = 480.0;
	private Double widthVoDStage = 1190.0, heightVoDStage = 660.0;
	private Double widthPreConfigStage = 500.0, heightPreConfigStage = 200.0;
	private Double widthNotificationStage = 350.0, heightNotificationStage = 150.0;
	private Double widthChoiceStage = 500.0, heigthChoiceStage = 250.0;
	private Double widthBtnSettings = 150.0, heightBtnSettings = 50.0;
	
	private Synchronizer synchronizer;
	private String pathFolderUpload = "http://localhost/SyncTV/syncfiles/uploads/" ;
    private String pathMainXml = "http://localhost/SyncTV/syncfiles/synctv.xml" ;
	//private String QRCodeValue = null;
	private String QRCodeValue = "globo", choice = null;
	private StackPane root, spVoD;
	private FlowPane fpVoD;
	private Stage mainStage, loginStage, signStage, vodStage, progStage, preConfigStage, notificationStage, choiceStage;
	private Scene sceneMain;
	private MediaControl mediaControl;
	private ImageView imageViewBackground;
	private WebView webView;
	private Button btnSettings,btnLogout, btnLinearTV, btnVoD, btnManual;
	private boolean hasLeft, hasRight;
	private int beginNode, endNode;
	private HBox hbBottom, hb_PreConfig;
	private VBox vbRight, vbLogin,vb_PreConfig;
	private SwingNode swingNode = new SwingNode();
	private TextField tfLogin, tfLoginSign;
	private PasswordField pfPassword, pfPasswordSign2, pfPasswordSign; 
	private ChoiceBox<String> cbTimeZones,choicebox;
	private Label lblNotification_Login, lblTitle_PreConfig;
	
	private int amountProg = 51;
	private ArrayList<Programming> listProgVoD;
	
	
	public void start(final Stage primaryStage) {
		createPreConfigStage();		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	//PRÉCONFIGSTAGE
	public void createPreConfigStage(){
		
		lblTitle_PreConfig = new Label("Escolha o método de sincronização:");
		lblTitle_PreConfig.getStyleClass().add("labelTitle2");
		
		swingNode = new SwingNode();
        createSwingContent(swingNode);
        swingNode.setBlendMode(BlendMode.SRC_ATOP);
        swingNode.prefWidth(widthBtnSettings);
        swingNode.prefHeight(heightBtnSettings);
        
        //swingNode.getStyleClass().add("buttonsSettings");
		
		btnManual = new Button("Manual");
		btnManual.getStyleClass().add("buttonsSettings");
		btnManual.setPrefSize(widthBtnSettings, heightBtnSettings);
		btnManual.setOnAction(onClickBtnManual);
		
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER);
		hb.setSpacing(25);
		hb.getChildren().addAll(swingNode, btnManual);
		
		vb_PreConfig = new VBox();
		vb_PreConfig.setAlignment(Pos.CENTER);
		vb_PreConfig.setSpacing(40);
		vb_PreConfig.getChildren().addAll(lblTitle_PreConfig,hb);
		vb_PreConfig.getStyleClass().add("backgroundPreConfigStage");
		
		Scene sceneConfig = new Scene(vb_PreConfig, widthPreConfigStage, heightPreConfigStage, Color.TRANSPARENT);
		sceneConfig.getStylesheets().add("/CSS/application.css");
		
		preConfigStage = new Stage();
		preConfigStage.initStyle(StageStyle.TRANSPARENT);
		preConfigStage.setScene(sceneConfig);
		preConfigStage.show();
		preConfigStage.toFront();
		preConfigStage.requestFocus();
		
	}
	
	//MAIN STAGE
	public void createMainStage(){
        
		imageViewBackground = new ImageView(new Image(getClass().getResourceAsStream("../Images/background.jpg")));
		
	    //Media media = new Media(getClass().getResource("../Medias/box.mp4").toString());
		//media = new Media(getClass().getResource("").toString());
	    //MediaPlayer mediaPlayer = new MediaPlayer(media);
	    mediaControl = new MediaControl("http://localhost/synctv/syncfiles/uploads/prog1/video1/240p.mp4");
	    //MediaView mediaView = new MediaView(mediaPlayer);
	    //mediaView.setVisible(false); 
	    
	    //BUTTON SETTINGS
	    btnSettings = new Button();
	    ImageView imgVSettings = new ImageView(new Image(getClass().getResourceAsStream("../Images/setting_Icon.png")));
	    imgVSettings.setFitWidth(26);
	    imgVSettings.setFitHeight(26);
	    imgVSettings.setPreserveRatio(true);   
	    btnSettings.setGraphic(imgVSettings);
	    //btnSettings.setVisible(false);
	    btnSettings.getStyleClass().add("btnSettings");
	    Tooltip tooltipSettings = new Tooltip();
	    tooltipSettings.setText("Settings");
	    btnSettings.setTooltip(tooltipSettings);
	    btnSettings.setOnAction(onClickBtnSettings);
	    
	    Button btnExit = new Button("Sair");
	    btnExit.getStyleClass().add("buttonsSettings");
	    btnExit.setPrefSize(widthBtnSettings, heightBtnSettings);
	    btnExit.setOnAction(onClickBtnExit);
	    
	    vbRight = new VBox();
	    vbRight.getChildren().addAll(btnExit);
	    vbRight.setMaxWidth(widthBtnSettings);
	    //vBoxRight.setAlignment(Pos.BOTTOM_RIGHT);
	    vbRight.setMaxHeight(heightBtnSettings * vbRight.getChildren().size());
	    vbRight.setVisible(false);
	    vbRight.setOnMouseExited(onMouseExitedVBoxSettings);
	    vbRight.setTranslateY(-37);
	    
	    root = new StackPane();
	    root.getStyleClass().add("background");
	    root.setAlignment(Pos.BOTTOM_RIGHT);
	    
	    root.getChildren().addAll(btnSettings, vbRight);
	    sceneMain = new Scene(root, widthMainStage, heightMainStage, Color.BLACK);
	 
	    //mediaControl.getMediaView().setOnMouseMoved(onMouseMovedMediaView);
	    //mediaControl.getMediaView().setOnMouseReleased(onMouseReleasedMediaView);
	  
	    sceneMain.getStylesheets().add("/CSS/application.css");
	    mainStage = new Stage();
	    mainStage.setScene(sceneMain);
	    //mainStage.setFullScreen(true);
	    mainStage.show();
	  
	    //MANTENDO PROPORÇÃO ENTRE MEDIAVIEW E IMAGEVIEW COM SCENE
	    /*final DoubleProperty widthMV = mediaControl.getMediaView().fitWidthProperty();
	    final DoubleProperty heightMV = mediaControl.getMediaView().fitHeightProperty();
	    widthMV.bind(Bindings.selectDouble(mediaControl.getMediaView().sceneProperty(), "width"));
	    heightMV.bind(Bindings.selectDouble(mediaControl.getMediaView().sceneProperty(), "height"));
	    mediaControl.getMediaView().setPreserveRatio(true);
	    
	    final DoubleProperty widthIV = imageViewBackground.fitWidthProperty();
	    final DoubleProperty heightIV = imageViewBackground.fitHeightProperty();
	    widthIV.bind(Bindings.selectDouble(imageViewBackground.sceneProperty(), "width"));
	    heightIV.bind(Bindings.selectDouble(imageViewBackground.sceneProperty(), "height"));
	    imageViewBackground.setPreserveRatio(true);*/
	    
	    
	    synchronizer.start();
	    mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	    	@Override
	    	public void handle(WindowEvent event) {
	    		synchronizer.stopIt();
	    	}
		});
	    
	}
	
	//SWINGNODE AND QRCODE READER
	private void createSwingContent(final SwingNode swingNode) {
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	
	            	final JPanel jPanel = new JPanel();
                    jPanel.setBackground(new java.awt.Color(45,45,45));
                    jPanel.setSize( 150, 50);
                    
	            	final JButton jbtn = new JButton("QRCode");
	            	jbtn.setFont(new Font( "lucida",Font.PLAIN,20));
	            	jbtn.setOpaque(false);
	            	jbtn.setContentAreaFilled(false);
	            	jbtn.setBorderPainted(false);
	            	//jbtn.setBackground( new java.awt.Color( 0, 0, 0, 200 ) );
	            	jbtn.setForeground( new java.awt.Color( 255, 255, 255 ) );
	            	//jbtn.setBackground(new java.awt.Color(5,5,5,1));
	                
	            	jbtn.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(java.awt.event.ActionEvent e) {
							new WebcamQRCode(Main.this);
						}
					});
	            	jbtn.addMouseListener(new MouseAdapter() {
	            		@Override
	            		public void mouseEntered(MouseEvent e) {
	            			jbtn.setForeground(new java.awt.Color(0,0,0));
	            			jPanel.setBackground(new java.awt.Color(163,41,0));
	            			super.mouseEntered(e);
	            		}
	            		@Override
	            		public void mouseExited(MouseEvent e) {
	            			jbtn.setForeground(new java.awt.Color(255,255,255));
	            			jPanel.setBackground(new java.awt.Color(45,45,45));
	            			super.mouseExited(e);
	            		}
					});
	    
	            	
	            	jPanel.add(jbtn);
	                swingNode.setContent(jPanel);
	            }
	        });
	    }
		
	//SETTING QRCODE VALUE
	public void setValueQRcode(String str){
		QRCodeValue = str;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				boolean isValid = false;
				synchronizer = new Synchronizer(pathMainXml,pathFolderUpload, Main.this);
				if(synchronizer.loadSynctv()){
					Set<String> keys = synchronizer.getSyncTV().getListChannel().keySet();
					for (String string : keys) {
						if(string.equals(QRCodeValue))
							isValid = true;
					}
					
					if(isValid){
						Button button = new Button("ok");
						button.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								preConfigStage.close();
								notificationStage.close();
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										synchronizer.setOptChannel(QRCodeValue);
										createMainStage();
									}
								});
								
							}
						});
						createNotificationStage("Sincronização efetuada com sucesso!", button);
					}else{
						Button button = new Button("Tentar Novamente!");
						button.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								notificationStage.close();
							}
						});
						createNotificationStage("QRCode Inválido,\n Não foi possível sincronizar!", button);
					}
				}else{
					Button button = new Button("Ok");
					button.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							notificationStage.close();
						}
					});
					createNotificationStage("Não foi possível Conectar com o Servidor!\n Verifique sua conexão!", button);
				}
			}
		});
		
	}
	
	//NOTIFICATIONSTAGE
	public void createNotificationStage(String notification, Button button){

		Label lblNotification = new Label(notification);
		lblNotification.getStyleClass().add("labelNotification");
		lblNotification.setTextAlignment(TextAlignment.CENTER);
		
		button.getStyleClass().add("buttonsSettings");
		button.setPrefSize(widthBtnSettings, heightBtnSettings);
		
		VBox vb = new VBox();
		vb.getChildren().addAll(lblNotification, button);
		vb.setAlignment(Pos.CENTER);
		vb.setSpacing(30);
		vb.getStyleClass().add("backgroundPreConfigStage");
		
		Scene sceneNotification = new Scene(vb, widthNotificationStage, heightNotificationStage, Color.TRANSPARENT);
		sceneNotification.getStylesheets().add("/CSS/application.css");
		
		notificationStage = new Stage();
		notificationStage.initStyle(StageStyle.TRANSPARENT);
		notificationStage.setScene(sceneNotification);
		notificationStage.show();
		notificationStage.toFront();
		notificationStage.requestFocus();
	}
	
	public void createChoiceStage(){
		Label lblTitle = new Label("Selecione o canal ao qual deseja sincronizar!");
		lblTitle.getStyleClass().add("labelTitle2");
		
		synchronizer = new Synchronizer(pathMainXml,pathFolderUpload,this);
		
		choicebox = new ChoiceBox();
		choicebox.setMinWidth(250);
		if(synchronizer.loadSynctv()){
			Set<String> keys = synchronizer.getSyncTV().getListChannel().keySet();
			for (String string : keys) {
				choicebox.getItems().add(string);
			}
		}else{
			Button button = new Button("Tentar Novamente!");
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					notificationStage.close();
				}
			});
			createNotificationStage("Não foi possível conectar com o servidor,\n Verifique sua conexão!", button);
		}
		
		Button btnVoltar = new Button("Voltar");
		btnVoltar.getStyleClass().add("buttonsSettings");
		btnVoltar.setPrefSize(widthBtnSettings, heightBtnSettings);
		btnVoltar.setOnAction(onClickBtnVoltar);
		
		Button btnOk = new Button("Ok");
		btnOk.getStyleClass().add("buttonsSettings");
		btnOk.setPrefSize(widthBtnSettings, heightBtnSettings);
		btnOk.setOnAction(onClickBtnOkChoice);
		
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER);
		hb.setSpacing(20);
		hb.getChildren().addAll(btnVoltar, btnOk);
		
		VBox vb = new VBox();
		vb.getChildren().addAll(lblTitle, choicebox, hb);
		vb.setAlignment(Pos.CENTER);
		vb.setSpacing(30);
		vb.getStyleClass().add("backgroundChoiceStage");
		
		Scene scene = new Scene(vb, widthChoiceStage, heigthChoiceStage, Color.TRANSPARENT);
		scene.getStylesheets().add("/CSS/application.css");
		
		choiceStage = new Stage();
		choiceStage.initStyle(StageStyle.TRANSPARENT);
		choiceStage.setScene(scene);
		choiceStage.show();
		choiceStage.toFront();
		choiceStage.requestFocus();
	}
	
	public void showMedia(final Midia midia){
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				switch (midia.getType()) {
				case "imagem":
					ImageView imageView = new ImageView(new Image(midia.getUrl()));
					root.getChildren().add(0, imageView);
					
					//mantem proporçao imageview com scene
					final DoubleProperty widthIV = imageView.fitWidthProperty();
				    final DoubleProperty heightIV = imageView.fitHeightProperty();
				    widthIV.bind(Bindings.selectDouble(imageView.sceneProperty(), "width"));
				    heightIV.bind(Bindings.selectDouble(imageView.sceneProperty(), "height"));
				    imageView.setPreserveRatio(true);
					
				    (new Thread(new Runnable() {	
						ImageView img;
						public Runnable setImage(ImageView i){
							img = i;
							return this;
						}	
						public void run() {
							try { Thread.sleep(midia.getDur() * 1000); } catch (InterruptedException e) { e.printStackTrace();}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									root.getChildren().remove(img);
								}
							});
						}
					}.setImage(imageView))).start();
					break;
				case "audio":
					AudioClip audioClip = new AudioClip(midia.getUrl());
					audioClip.play();
					(new Thread(new Runnable() {	
						AudioClip ac;
						public Runnable setImage(AudioClip audioClip){
							ac = audioClip;
							return this;
						}	
						public void run() {
							try { Thread.sleep(midia.getDur() * 1000); } catch (InterruptedException e) { e.printStackTrace();}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									ac.stop();
								}
							});
						}
					}.setImage(audioClip))).start();
					break;
				case "video":
					MediaView mediaView = new MediaView(new MediaPlayer(new Media(midia.getUrlMidia("720p"))));
					mediaView.getMediaPlayer().setAutoPlay(true);
				    mediaControl.getMediaView().setPreserveRatio(true);
					root.getChildren().add(mediaView);
					
					final DoubleProperty widthMV = mediaView.fitWidthProperty();
				    final DoubleProperty heightMV = mediaView.fitHeightProperty();
				    widthMV.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
				    heightMV.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
				    mediaView.setPreserveRatio(true);
				    
					(new Thread(new Runnable() {	
						MediaView mv;
						public Runnable setVideo(MediaView m){
							mv = m;
							return this;
						}	
						public void run() {
							try { Thread.sleep(midia.getDur() * 1000); } catch (InterruptedException e) { e.printStackTrace();}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									mv.getMediaPlayer().stop();
									root.getChildren().remove(mv);
								}
							});
						}
					}.setVideo(mediaView))).start();
					break;
				case "webpage":
					webView = new WebView();
					WebEngine webEngine = webView.getEngine();
					webEngine.load(midia.getUrl());
					root.getChildren().add(0, webView);
					(new Thread(new Runnable() {	
						WebView wv;
						public Runnable setWebView(WebView wView){
							wv = wView;
							return this;
						}	
						public void run() {
							try { Thread.sleep(midia.getDur() * 1000); } catch (InterruptedException e) { e.printStackTrace();}
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									root.getChildren().remove(wv);
								}
							});
						}
					}.setWebView(webView))).start();
					break;
				default:
					break;
				}
			}
		});
		
	}
	
	
	/*public void createMainStage(){
		//mainStage.initStyle(StageStyle.UNDECORATED);

		swingNode = new SwingNode();
        createSwingContent(swingNode);
        swingNode.setBlendMode(BlendMode.SRC_ATOP);
        swingNode.setVisible(false);
        
		imageViewBackground = new ImageView(new Image(getClass().getResourceAsStream("../Images/background.jpg")));
		
	    //Media media = new Media(getClass().getResource("../Medias/box.mp4").toString());
		//media = new Media(getClass().getResource("").toString());
	    //MediaPlayer mediaPlayer = new MediaPlayer(media);
	    //mediaControl = new MediaControl();
	    //MediaView mediaView = new MediaView(mediaPlayer);
	    //mediaView.setVisible(false); 
	    
	    //BUTTON SETTINGS
	    btnSettings = new Button();
	    ImageView imgVSettings = new ImageView(new Image(getClass().getResourceAsStream("../Images/setting_Icon.png")));
	    imgVSettings.setFitWidth(26);
	    imgVSettings.setFitHeight(26);
	    imgVSettings.setPreserveRatio(true);   
	    btnSettings.setGraphic(imgVSettings);
	    btnSettings.setVisible(false);
	    btnSettings.getStyleClass().add("btnSettings");
	    Tooltip tooltipSettings = new Tooltip();
	    tooltipSettings.setText("Settings");
	    btnSettings.setTooltip(tooltipSettings);
	    btnSettings.setOnAction(onClickBtnSettings);
	    
	    hbTop = new HBox();
	    hbTop.getChildren().addAll(btnSettings);
	    hbTop.setAlignment(Pos.TOP_RIGHT);
	   
	    btnLogout = new Button("Logout");
	    btnLogout.getStyleClass().add("buttonsSettings");
	    btnLogout.setPrefSize(widthBtnSettings, heightBtnSettings);
	    btnLogout.setVisible(false);
	    btnLogout.setOnAction(onClickBtnLogout);
	    
	    btnLinearTV = new Button("LinearTV");
	    btnLinearTV.getStyleClass().add("buttonsSettings");
	    btnLinearTV.setPrefSize(widthBtnSettings, heightBtnSettings);
	    btnLinearTV.setOnAction(onClickBtnLinearTV);
	    btnLinearTV.setVisible(false);
	    
	    btnVoD = new Button("VoD");
	    btnVoD.getStyleClass().add("buttonsSettings");
	    btnVoD.setPrefSize(widthBtnSettings, heightBtnSettings);
	    btnVoD.setOnAction(onClickBtnVoD);
	    btnVoD.setVisible(false);
	    
	    vbRight = new VBox();
	    vbRight.getChildren().addAll(btnLinearTV, btnVoD,swingNode, btnLogout);
	    vbRight.setMaxWidth(widthBtnSettings);
	    //vBoxRight.setAlignment(Pos.BOTTOM_RIGHT);
	    vbRight.setMaxHeight(heightBtnSettings * vbRight.getChildren().size());
	    vbRight.setVisible(false);
	    vbRight.setOnMouseExited(onMouseExitedVBoxSettings);
	    vbRight.setTranslateY(-30);
	    
	    //mediaControl = new MediaControl();
	    //mediaControl.createLinkMediaControl(); 
	    mediaControl.getMediaBar().getChildren().add(btnSettings);
	    
	    root = new StackPane();
	    root.getStyleClass().add("background");
	    root.setAlignment(Pos.BOTTOM_RIGHT);
	    
	    root.getChildren().addAll(imageViewBackground, mediaControl.getMediaView(), mediaControl.getMediaBar(),vbRight);
	    sceneMain = new Scene(root, widthMainStage, heightMainStage, Color.BLACK);
	 
	    mediaControl.getMediaView().setOnMouseMoved(onMouseMovedMediaView);
	    mediaControl.getMediaView().setOnMouseReleased(onMouseReleasedMediaView);
	  
	    sceneMain.getStylesheets().add("/CSS/application.css");
	    mainStage = new Stage();
	    mainStage.setScene(sceneMain);
	    //mainStage.setFullScreen(true);
	    mainStage.show();
	  
	    //MANTENDO PROPORÇÃO ENTRE MEDIAVIEW E IMAGEVIEW COM SCENE
	    final DoubleProperty widthMV = mediaControl.getMediaView().fitWidthProperty();
	    final DoubleProperty heightMV = mediaControl.getMediaView().fitHeightProperty();
	    widthMV.bind(Bindings.selectDouble(mediaControl.getMediaView().sceneProperty(), "width"));
	    heightMV.bind(Bindings.selectDouble(mediaControl.getMediaView().sceneProperty(), "height"));
	    mediaControl.getMediaView().setPreserveRatio(true);
	    
	    final DoubleProperty widthIV = imageViewBackground.fitWidthProperty();
	    final DoubleProperty heightIV = imageViewBackground.fitHeightProperty();
	    widthIV.bind(Bindings.selectDouble(imageViewBackground.sceneProperty(), "width"));
	    heightIV.bind(Bindings.selectDouble(imageViewBackground.sceneProperty(), "height"));
	    imageViewBackground.setPreserveRatio(true);

	}*/
	
	//LOGIN STAGE
	public void createLoginStage(){
		
		Label lblTitle = new Label("Start Session");
		lblTitle.getStyleClass().add("labelTitle");
		lblTitle.setAlignment(Pos.TOP_CENTER);
		lblTitle.setTranslateY(-10);
		Label lblLogin = new Label("Login:");
		lblLogin.getStyleClass().add("label");
		Label lblPassword = new Label("Password:");
		lblPassword.getStyleClass().add("label");
		tfLogin = new TextField();
		tfLogin.setText("samoht");
		pfPassword = new PasswordField();
		pfPassword.setText("123456");
		Button btnLogin = new Button("Login");
		btnLogin.setOnAction(onClickBtnLogin);
		btnLogin.getStyleClass().add("buttonsSettings");
		Button btnClose = new Button("Close");
		btnClose.setOnAction(onClickBtnClose);
		btnClose.getStyleClass().add("buttonsSettings");
		
		HBox hBox1 = new HBox();
		hBox1.getChildren().addAll(lblLogin,tfLogin);
		hBox1.setSpacing(30);
		hBox1.setAlignment(Pos.CENTER);
		HBox hBox2 = new HBox();
		hBox2.getChildren().addAll(lblPassword,pfPassword);
		hBox2.setSpacing(5);
		hBox2.setAlignment(Pos.CENTER);
		HBox hBox3 = new HBox();
		hBox3.getChildren().addAll(btnLogin,btnClose);
		hBox3.setSpacing(20);
		hBox3.setTranslateY(10);
		hBox3.setAlignment(Pos.CENTER);
		lblNotification_Login = new Label("\"Não foi possível conectar!\"");
		lblNotification_Login.getStyleClass().add("labelNotification");
		lblNotification_Login.setTranslateY(10);
		Label lblSign = new Label("Sign In");
		lblSign.getStyleClass().add("label2");
		lblSign.setOnMouseClicked(onClickLabelSign);
		lblSign.setTranslateX(100);
		
		vbLogin = new VBox();
		vbLogin.getChildren().addAll(lblTitle, hBox1 ,hBox2 ,hBox3 ,lblSign);
		vbLogin.setSpacing(20);
		vbLogin.setAlignment(Pos.CENTER);
		
		StackPane spLogin = new StackPane();
		spLogin.getChildren().addAll(vbLogin);
		spLogin.getStyleClass().add("backgroundStageLogin");
		Scene sceneLogin = new Scene(spLogin, widthLoginStage, heightLoginStage, Color.TRANSPARENT);
		sceneLogin.getStylesheets().add("/CSS/application.css");
		
		loginStage = new Stage();
		loginStage.initStyle(StageStyle.TRANSPARENT);
		loginStage.setScene(sceneLogin);
		loginStage.show();
		loginStage.toFront();
		loginStage.requestFocus();		
		
	}
	
	//SIGN STAGE
	public void createSignStage(){
		
		Label lblTitle = new Label("Registering new user");
		lblTitle.getStyleClass().add("labelTitle");
		lblTitle.setAlignment(Pos.TOP_CENTER);
		lblTitle.setTranslateY(-30);
		Label lblLogin = new Label("Login: ");
		lblLogin.getStyleClass().add("label");
		Label lblPassword = new Label("Password: ");
		lblPassword.getStyleClass().add("label");
		Label lblPassword2 = new Label("Repeat: ");
		Label lblTimeZone = new Label("TimeZone: ");
		lblTimeZone.getStyleClass().add("label");
		lblPassword2.getStyleClass().add("label");
		cbTimeZones = new ChoiceBox<>(); 
		cbTimeZones.getItems().addAll(listTimeZones);
		cbTimeZones.getSelectionModel().selectFirst();
		pfPasswordSign = new PasswordField();
		pfPasswordSign.getStyleClass().add("textFieldSignStage");
		pfPasswordSign2 = new PasswordField();
		pfPasswordSign2.getStyleClass().add("textFieldSignStage");
		tfLoginSign = new TextField();
		tfLoginSign.getStyleClass().add("textFieldSignStage");
		Button btnRegister = new Button("Register");
		btnRegister.setOnAction(onClickBtnRegister);
		btnRegister.getStyleClass().add("buttonsSettings");
		Button btnCancel = new Button("Cancel");
		btnCancel.setOnAction(onClickBtnCancelSignStage);
		btnCancel.getStyleClass().add("buttonsSettings");
		final Label lblNotificationSignIn = new Label("");
		lblNotificationSignIn.getStyleClass().add("labelNotification");
		
		HBox hBox1 = new HBox();
		hBox1.getChildren().addAll(lblLogin,tfLoginSign);
		hBox1.setSpacing(10);
		hBox1.setTranslateX(-48);
		hBox1.setAlignment(Pos.CENTER);
		HBox hBox2 = new HBox();
		hBox2.getChildren().addAll(lblPassword,pfPasswordSign);
		hBox2.setSpacing(10);
		hBox2.setTranslateX(-62);
		hBox2.setAlignment(Pos.CENTER);
		HBox hBox3 = new HBox();
		hBox3.getChildren().addAll(lblPassword2,pfPasswordSign2);
		hBox3.setSpacing(10);
		hBox3.setTranslateX(-55);
		hBox3.setAlignment(Pos.CENTER);
		HBox hBox4 = new HBox();
		hBox4.getChildren().addAll(lblTimeZone,cbTimeZones);
		hBox4.setSpacing(10);
		hBox4.setAlignment(Pos.CENTER);
		HBox hBox5 = new HBox();
		hBox5.getChildren().addAll(btnRegister,btnCancel);
		hBox5.setSpacing(20);
		hBox5.setTranslateY(10);
		hBox5.setAlignment(Pos.CENTER);
		
		VBox vBox = new VBox();
		vBox.getChildren().addAll(lblTitle,hBox1,hBox2,hBox3,hBox4,hBox5);
		vBox.setSpacing(20);
		vBox.setAlignment(Pos.CENTER);
		
		StackPane spSign = new StackPane();
		spSign.getChildren().add(vBox);
		spSign.getStyleClass().add("backgroundStageSign");
		Scene sceneSign = new Scene(spSign, widthSignStage, heightSignStage, Color.TRANSPARENT);
		sceneSign.getStylesheets().add("/CSS/application.css");
		
		signStage = new Stage();
		signStage.initStyle(StageStyle.TRANSPARENT);
		signStage.setScene(sceneSign);
		signStage.show();
		signStage.toFront();
		signStage.requestFocus();	
	}
	
	
	public void createVoDStage(){
		
		SyncTV syncTV = synchronizer.getSyncTV();
		
		listProgVoD = syncTV.getChannel(QRCodeValue).getListProgVoD();
		amountProg = listProgVoD.size();
		if(amountProg != 0){//se existe programações
			if(amountProg <= 20){
				hasRight=false;
				endNode = amountProg;
			}else{
				hasRight=true;
				endNode= 20;
			}		
			hasLeft = false;
			beginNode = 0;
		}
		
		ImageView imgPrevButton = new ImageView(new Image(getClass().getResourceAsStream("../Images/left_Arrow.png")));
		imgPrevButton.setFitWidth(26);
		imgPrevButton.setFitHeight(26);
		Button btnPrev = new Button();
		btnPrev.setGraphic(imgPrevButton);
		btnPrev.setTranslateX(-(widthVoDStage/2)+27);
		btnPrev.setTranslateY(-0.5);
		btnPrev.setPrefSize(50, heightVoDStage-1);
		btnPrev.getStyleClass().add("btnPrev");
		if(hasLeft == false)
			btnPrev.setVisible(false);
		else
			btnPrev.setVisible(true);
		btnPrev.setOnAction(onClickPrevButton);
		
		ImageView imgNextButton = new ImageView(new Image(getClass().getResourceAsStream("../Images/right_Arrow.png")));
		imgNextButton.setFitWidth(26);
		imgNextButton.setFitHeight(26);
		Button btnNext = new Button();
		btnNext.setGraphic(imgNextButton);
		btnNext.setTranslateX((widthVoDStage/2)-27);
		btnNext.setTranslateY(-0.5);
		btnNext.setPrefSize(50, heightVoDStage-1);
		btnNext.getStyleClass().add("btnNext");
		if(hasRight == false)
			btnNext.setVisible(false);
		else
			btnNext.setVisible(true);
		btnNext.setOnAction(onClickNextButton);
		
		Button btnCloseVoDStage = new Button("Close");
		btnCloseVoDStage.setTranslateY((heightVoDStage/2)-23);
		btnCloseVoDStage.setOnAction(onClickCloseVoDStage);
		btnCloseVoDStage.getStyleClass().add("btnClose");
		
		
		spVoD = new StackPane();
		spVoD.getChildren().addAll(btnPrev,btnNext,btnCloseVoDStage);
		spVoD.getStyleClass().add("backgroundStageVoD");
		
		Scene sceneVoD = new Scene(spVoD, widthVoDStage, heightVoDStage, Color.TRANSPARENT);
		sceneVoD.getStylesheets().add("/CSS/application.css");
		
		vodStage = new Stage();
		vodStage.initStyle(StageStyle.TRANSPARENT);
		vodStage.setScene(sceneVoD);
		vodStage.show();
		vodStage.toFront();
		vodStage.requestFocus();
		
		createFlowPaneVoD();
	}
	
	public void createFlowPaneVoD(){
		
		fpVoD = new FlowPane();
		fpVoD.setAlignment(Pos.TOP_LEFT);
		fpVoD.setHgap(20);
		fpVoD.setVgap(10);
		fpVoD.setMaxHeight(heightVoDStage - 50);
		fpVoD.setTranslateX(20);
		fpVoD.setTranslateY(-10);
		fpVoD.setOrientation(Orientation.VERTICAL);
		
		for(int i=beginNode; i < endNode; i++){
			Button btnProg = new Button(listProgVoD.get(i).getName());
			//Button btnProg = new Button("prog"+i);
			btnProg.setPrefSize((widthVoDStage/2)-(heightBtnSettings/2+fpVoD.getTranslateX()/2), heightBtnSettings);//(1195/2)-((50/2)*(20/2))
			btnProg.getStyleClass().add("progVoD");
			fpVoD.getChildren().addAll(btnProg);
		}
		
		
		//System.out.println("beginNode: "+beginNode);
		//System.out.println("endNode: "+endNode);
		//System.out.println("aux: "+amountProg);
		
		spVoD.getChildren().add(0,fpVoD);
		
		
	}
	
	public void createProgStage(){
		
		
		progStage = new Stage();
		progStage.initStyle(StageStyle.TRANSPARENT);
		//progStage.setScene();
		progStage.toFront();
		progStage.requestFocus();
		
	}
	
	//EVENTS
	public EventHandler onClickNextButton = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			spVoD.getChildren().remove(0);
			spVoD.getChildren().get(0).setVisible(true);//btnPrev
			
			beginNode += 20;
			if(amountProg - beginNode <= 20){
				spVoD.getChildren().get(1).setVisible(false);//btnNext
				endNode = amountProg;	
			}else{
				endNode += 20;
			}
			createFlowPaneVoD();
		}
	};
	
	public EventHandler onClickPrevButton = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			spVoD.getChildren().remove(0);
			spVoD.getChildren().get(1).setVisible(true);//btnNext
			
			if(amountProg == endNode){//ultima página
				endNode = endNode -(amountProg - beginNode);
				beginNode -=20;
			}else{//demais páginas
				endNode -= 20;
				beginNode -= 20;
			}
			
			if(beginNode <= 0)//primeira página
				spVoD.getChildren().get(0).setVisible(false);//btnPrev
				
			createFlowPaneVoD();
		}
	};
	
	public EventHandler onClickCloseVoDStage = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			vodStage.close();
		}
	};
	
	public EventHandler onClickBtnManual = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			createChoiceStage();
		}
	}; 
	
	public EventHandler onClickBtnClose = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			loginStage.close();
			mainStage.close();
		}
	};
		
	public EventHandler onClickBtnVoltar = new EventHandler<Event>() {
		public void handle(Event event) {
			choiceStage.close();
		};
	};
	
	public EventHandler onClickBtnExit = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			mainStage.close();
			createPreConfigStage();
		}
	};
	
	public EventHandler onClickBtnLogin = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			try {
				ConnectionFactory cf = new ConnectionFactory(urlDBSessionConnection, userDBSessionConnection, passDBSessionConnection);
			
				if (cf.userExist(tfLogin.getText().toString(), pfPassword.getText().toString())){
					btnSettings.setVisible(true);
					loginStage.close();
				}
		
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	};	
	
	public EventHandler onClickBtnLogout = new EventHandler<ActionEvent>() {
    	public void handle(ActionEvent event) {
    		vbRight.setVisible(false);
    		btnSettings.setVisible(false);
    		btnLogout.setVisible(false);
    		swingNode.setVisible(false);   		
    		createLoginStage();
    	}
	};
	
	public EventHandler onClickBtnSettings = new EventHandler<ActionEvent>() {
    	public void handle(ActionEvent event) {
    		if(vbRight.isVisible()){
    			vbRight.setVisible(false);      		
    		}else{
    			vbRight.setVisible(true);    		
    		}
    	};
	};
		
	public EventHandler onMouseExitedVBoxSettings = new EventHandler<Event>() {
    	public void handle(Event event) {
    		vbRight.setVisible(false);
    	};
	};
	
	public EventHandler onClickLabelSign = new EventHandler<Event>() {
		public void handle(Event event) {
			createSignStage();
		};
	};
	
	public EventHandler onClickBtnRegister = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			try {
				ConnectionFactory cf = new ConnectionFactory(urlDBSessionConnection, userDBSessionConnection, passDBSessionConnection);
			
				if ((!cf.userExist(tfLoginSign.getText().toString(), pfPasswordSign.getText().toString()))//se nao existe user/pass igual cadastrado
						&&(!tfLoginSign.getText().toString().equals(""))//se login nao nulo
						&&(!pfPasswordSign.getText().toString().equals(""))//se password não nulo
						&&(pfPasswordSign.getText().toString().equals(pfPasswordSign2.getText().toString())))//se pass1 é igual ao pass2
						{
						cf.signUp(tfLoginSign.getText().toString(), pfPassword.getText().toString(), cbTimeZones.getSelectionModel().getSelectedIndex());
						signStage.close();
						System.out.println("ok");
				}else{
					System.out.println("not ok");
				}
		
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	};
	
	public EventHandler onClickBtnCancelSignStage = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			signStage.close();
			
		}
	};
	
	public EventHandler onClickBtnOkChoice = new EventHandler<Event>() {
		@Override
		public void handle(Event event) {
			if(!choicebox.getSelectionModel().isEmpty()){
				choiceStage.close();
				setValueQRcode(choicebox.getSelectionModel().getSelectedItem().toString());
			}else{
				Button button = new Button("Ok");
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						notificationStage.close();
					}
				});
				createNotificationStage("Por favor, selecione algum item!", button);
			}
		}
	};
	
	
}
