package application;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MediaControl{
	private Media media;
	private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaBar;
    private Button playButton;
    private Label timeLabel,volumeLabel;
    
    public MediaControl(String url){
    	//this.media = new Media(getClass().getResource(url).toString());
    	this.media = new Media(url);
    	this.mediaPlayer = new MediaPlayer(media);
    	this.mediaView = new MediaView(mediaPlayer);
    	
    	mediaBar = new HBox();
    }
 
    //public void createLinkMediaControl(String urlMedia){
    public void createLinkMediaControl() {
       
        mediaPlayer.setAutoPlay(true);
      
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setMaxHeight(10);
        mediaBar.setSpacing(10);
        //mediaBar.setVisible(false);
        mediaBar.setStyle("-fx-background-color: transparent;");
        
       // BorderPane.setAlignment(mediaBar, Pos.CENTER);
 
        //PLAYBUTTON
        ImageView imgPlayButton = new ImageView(new Image(getClass().getResourceAsStream("../Images/play_Icon.png")));
        imgPlayButton.setFitWidth(26);
        imgPlayButton.setFitHeight(26);
        imgPlayButton.setPreserveRatio(true);
        playButton  = new Button();
        playButton.setGraphic(imgPlayButton);
        playButton.getStyleClass().add("btnSettings");
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Status status = mediaPlayer.getStatus();
         
                if (status == Status.UNKNOWN  || status == Status.HALTED)
                {
                   // don't do anything in these states
                   return;
                }
         
                  if ( status == Status.PAUSED
                     || status == Status.READY
                     || status == Status.STOPPED)
                  {
                     // rewind the movie if we're sitting at the end
                     if (atEndOfMedia) {
                    	 mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                     }
                     mediaPlayer.play();
                     } else {
                    	 mediaPlayer.pause();
                     }
                 }
           });
        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() 
        {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });
 
        mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                	mediaPlayer.pause();
                    stopRequested = false;
                } else {
                	ImageView imgPlayButton = new ImageView(new Image(getClass().getResourceAsStream("../Images/pause_Icon.png")));
                	imgPlayButton.setFitWidth(24);
    	            imgPlayButton.setFitHeight(26);
                    playButton.setGraphic(imgPlayButton);
                }
            }
        });
 
        mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
            	ImageView imgPlayButton = new ImageView(new Image(getClass().getResourceAsStream("../Images/play_Icon.png")));
	            imgPlayButton.setFitWidth(26);
	            imgPlayButton.setFitHeight(26);
	            imgPlayButton.setPreserveRatio(true);
                playButton.setGraphic(imgPlayButton);
            }
        });
 
        mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateValues();
            }
        });
 
        mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
       });
        mediaBar.getChildren().add(playButton);
           
         
        // Add Time label
        timeLabel = new Label("   Time: ");
        timeLabel.getStyleClass().add("labelMediaControl");
        mediaBar.getChildren().add(timeLabel);
         
        // Add time slider
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider,Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
               if (timeSlider.isValueChanging()) {
               // multiply duration by percentage calculated by slider position
            	   mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
               }
            }
        });
      
        mediaBar.getChildren().add(timeSlider);
         
        // Add Play label
        playTime = new Label();
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);
        playTime.getStyleClass().add("labelMediaControl");
        mediaBar.getChildren().add(playTime);
         
        // Add the volume label
        volumeLabel = new Label("Vol: ");
        volumeLabel.getStyleClass().add("labelMediaControl");
        mediaBar.getChildren().add(volumeLabel);
         
        // Add Volume slider
        volumeSlider = new Slider();        
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
               if (volumeSlider.isValueChanging()) {
            	   mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
               }
            }
        });
        volumeSlider.setMinWidth(30);
         
        mediaBar.getChildren().add(volumeSlider);
        
        hideComponents();
     }
    
    protected void updateValues() {
    	  if (playTime != null && timeSlider != null && volumeSlider != null) {
    	     Platform.runLater(new Runnable() {
    	        public void run() {
    	          Duration currentTime = mediaPlayer.getCurrentTime();
    	          playTime.setText(formatTime(currentTime, duration));
    	          timeSlider.setDisable(duration.isUnknown());
    	          if (!timeSlider.isDisabled() 
    	            && duration.greaterThan(Duration.ZERO) 
    	            && !timeSlider.isValueChanging()) {
    	              timeSlider.setValue(currentTime.divide(duration).toMillis()
    	                  * 100.0);
    	          }
    	          if (!volumeSlider.isValueChanging()) {
    	            volumeSlider.setValue((int)Math.round(mediaPlayer.getVolume() 
    	                  * 100));
    	          }
    	        }
    	     });
    	  }
    	}
    
    private static String formatTime(Duration elapsed, Duration duration) {
    	   int intElapsed = (int)Math.floor(elapsed.toSeconds());
    	   int elapsedHours = intElapsed / (60 * 60);
    	   if (elapsedHours > 0) {
    	       intElapsed -= elapsedHours * 60 * 60;
    	   }
    	   int elapsedMinutes = intElapsed / 60;
    	   int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 
    	                           - elapsedMinutes * 60;
    	 
    	   if (duration.greaterThan(Duration.ZERO)) {
    	      int intDuration = (int)Math.floor(duration.toSeconds());
    	      int durationHours = intDuration / (60 * 60);
    	      if (durationHours > 0) {
    	         intDuration -= durationHours * 60 * 60;
    	      }
    	      int durationMinutes = intDuration / 60;
    	      int durationSeconds = intDuration - durationHours * 60 * 60 - 
    	          durationMinutes * 60;
    	      if (durationHours > 0) {
    	         return String.format("%d:%02d:%02d/%d:%02d:%02d", 
    	            elapsedHours, elapsedMinutes, elapsedSeconds,
    	            durationHours, durationMinutes, durationSeconds);
    	      } else {
    	          return String.format("%02d:%02d/%02d:%02d",
    	            elapsedMinutes, elapsedSeconds,durationMinutes, 
    	                durationSeconds);
    	      }
    	      } else {
    	          if (elapsedHours > 0) {
    	             return String.format("%d:%02d:%02d", elapsedHours, 
    	                    elapsedMinutes, elapsedSeconds);
    	            } else {
    	                return String.format("%02d:%02d",elapsedMinutes, 
    	                    elapsedSeconds);
    	            }
    	        }
    	    }
    
    public MediaView getMediaView(){
    	//this.mediaView.setVisible(false);
    	return this.mediaView;
    }
    
    public HBox getMediaBar(){
    	showComponents();
    	return this.mediaBar;
    }
    
    public void hideComponents(){
    	playButton.setVisible(false);
    	timeSlider.setVisible(false);
    	playTime.setVisible(false);
    	volumeSlider.setVisible(false);
    	volumeLabel.setVisible(false);
    	timeLabel.setVisible(false);
    }
    
    public void showComponents(){
    	playButton.setVisible(true);
    	timeSlider.setVisible(true);
    	playTime.setVisible(true);
    	volumeSlider.setVisible(true);
    	volumeLabel.setVisible(true);
    	timeLabel.setVisible(true);
    }
}
