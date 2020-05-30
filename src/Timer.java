import java.awt.Desktop;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Provides an application to keep track of your score in membean and includes a timer that keeps you from getting counted as in active
 */
public class Timer extends Application {
//	Fonts
	private final Font fntButton = Font.font("Georgia", 20);
	private final Font fntAddSub = Font.font("Georgia", 50);
	private final Font fntText = Font.font("Arial", 20);
	private final Font fntTimer = Font.font("Georgia", 45);

//	Radius of the timer circle
	private double circR = 75;

//	Scene graph
	private AnchorPane rootApn = new AnchorPane();
		private AnchorPane ancpnButtons = new AnchorPane();
			private Button btn3Mins = new Button();
			private StackPane stkpnAddSub = new StackPane();
			    private HBox hbxAddSub = new HBox();
			        private Button btnAdd = new Button("I");
			        private Button btnSub = new Button("X");
			private Button btnMultMins = new Button();
		private StackPane stpnPlaying = new StackPane();
			private Canvas canvas = new Canvas();
				private GraphicsContext gc = canvas.getGraphicsContext2D(); 
			private Label lbPlaying = new Label("00:00");
		private HBox hbxText = new HBox();
			private VBox vbxInitials = new VBox();
				private Button btnT = new Button("T");
				private Button btnJ = new Button("J");
				private Button btnBack = new Button("<");
			private VBox vbxText = new VBox();
				private FlowPane flpnTextX = new FlowPane();
				    private Text txtCountX = new Text();  
				private FlowPane flpnTextI = new FlowPane();
				    private Text txtCountI = new Text();
	private BorderPane bpnProgressBar = new BorderPane();
		private Rectangle rectProgressBar = new Rectangle();
	private Scene scene = new Scene(rootApn);

	/**
	 * States of last action, either add I or X
	 */
	private enum Was{AddI, AddX};
	/**
	 * The last action
	 */
	private Was lastMove = Was.AddI;

	/**
	 * Media player to play the audio at the end of the timer
	 */
	private MediaPlayer mdplayer;
	//	private WebView wv = new WebView();
	//	private WebEngine web = wv.getEngine();

	/**
	 * The screen of the computer
	 */
	private Rectangle2D screen;

	/**
	 * The timelines that control the timers
	 */
	private Timeline timer3Mins, timerMultMins;

	/**
	 * Thread that runs the sounds
	 */
	private Thread t;

	/**
	 * Formats the number for the timer
	 */
	private NumberFormat formatter = new DecimalFormat("00");

	/**
	 * Images for the Icons for the button and window Icon
	 */
	private Image icon, img3MinsPlay, imgMultMins1;

	/**
	 * String representation for the number of Correct in the session
	 */
	private String countI = "";
	/**
	 * String representation for the number Incorrect in the session
	 */
	private String countX = "";

	/**
	 * The number right, wrong, the length of elapsed time and the sound length
	 */
	private int numRight = 0, numWrong = 0, mins = 0, secs = 0, soundLength = 2500;
	private double percent = 0.5;
	private long startMillis = 0;

	/**
	 * Sets up the scene graph and initializes the fields
	 * @param stage
	 * @throws Exception
	 */
	@Override
	public void start(Stage stage) throws Exception {
//		threadTimer.start();
		icon = new Image(this.getClass().getResource("DracoIncLogoIcon.png").toURI().toURL().toString());
		img3MinsPlay = new Image(this.getClass().getResource("3Mins.png").toURI().toURL().toString());
		imgMultMins1 = new Image(this.getClass().getResource("MultMins1.png").toURI().toURL().toString());
		
		mdplayer = new MediaPlayer(new Media(this.getClass().getResource("harp.wav").toURI().toURL().toString()));
		
		screen = Screen.getPrimary().getVisualBounds();
		
		stage.setScene(scene);
		stage.getIcons().add(icon);
		stage.setTitle("Membean Timer");
		stage.setX(screen.getWidth() / 2);
		stage.setY(screen.getHeight() * .6 - 5);
		stage.setHeight(screen.getHeight() * .4 + 5);
		stage.setWidth(screen.getWidth() / 2);
		stage.setMinHeight(400);
		stage.setMinWidth(700);
		stage.setOnCloseRequest((ae) -> {
			System.out.println("CLosing");
			System.exit(0);
		});
		stage.show();

		/**
		 * sets up 3 minute timer to update the visuals according to the amount of time remaining
		 */
		timer3Mins = new Timeline(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (secs != 0 || mins != 0) {
					secs--;
					if (secs < 0)	{
						mins--;
						secs = 59;
					}
					lbPlaying.setText(formatter.format(mins) + ":" + formatter.format(secs));
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					gc.setStroke(Color.FORESTGREEN);
					circR = ((stage.getHeight() / 3) - 50);
					canvas.setHeight(circR * 2 + gc.getLineWidth());
					canvas.setWidth(circR * 2 + gc.getLineWidth());
					gc.strokeArc((canvas.getWidth() / 2) - (circR), gc.getLineWidth() / 2, circR * 2, circR * 2, 90, ((((double)mins * 60d) + (double)secs) / 180d) * -360, ArcType.OPEN);
				}
				if (secs == 0 && mins == 0) {
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					rootApn.setBackground(new Background(new BackgroundFill(Color.LAWNGREEN, null, null)));
					t = new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stud
							mdplayer.setStopTime(Duration.millis(soundLength));
							mdplayer.play();
						}
					});
					t.start(); 
				}
			}			
		}));
		timer3Mins.setCycleCount(181);
		timer3Mins.setOnFinished((final ActionEvent a) -> {
			ancpnButtons.setVisible(true);
			stpnPlaying.setVisible(false);
			updateBKGD();
			mins = secs = 0;
			gc.strokeArc((canvas.getWidth() / 2) - (circR), gc.getLineWidth() / 2, circR * 2, circR * 2, 90, 360, ArcType.OPEN);
//			rootApn.setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
		});

		/**
		 * Sets up the 1 minute timer to update the visuals according to the amount of time left
		 */
		timerMultMins = new Timeline(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (secs != 0 || mins != 0) {
					secs--;
					if (secs < 0)	{
						mins--;
						secs = 59;
					}
					lbPlaying.setText(formatter.format(mins) + ":" + formatter.format(secs));
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					gc.setStroke(Color.FORESTGREEN);
					gc.strokeArc((canvas.getWidth() / 2) - (circR), gc.getLineWidth() / 2, circR * 2, circR * 2, 90, ((((double)mins * 60d) + (double)secs) / 60d) * -360, ArcType.OPEN);
				}
				if (secs == 0 && mins == 0) {
					gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
					rootApn.setBackground(new Background(new BackgroundFill(Color.LAWNGREEN, null, null)));
					t = new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stud
							mdplayer.setStopTime(Duration.millis(soundLength));
							mdplayer.play();
						}
					});
					t.start();
				}
			}			
		}));
		timerMultMins.setCycleCount(61);
		timerMultMins.setOnFinished((final ActionEvent a) -> {
			ancpnButtons.setVisible(true);
			stpnPlaying.setVisible(false);
			mins = secs = 0;
			gc.strokeArc((canvas.getWidth() / 2) - (circR), gc.getLineWidth() / 2, circR * 2, circR * 2, 90, 360, ArcType.OPEN);
			rootApn.setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
		});

		/**
		 * Controls the ratio of the Stage
		 */
		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			// TODO Auto-generated method stub
			hbxText.setMinSize(stage.getWidth() - 40, stage.getHeight()/3);
			hbxText.setMaxSize(stage.getWidth() - 40, stage.getHeight()/3);
			flpnTextI.setMinHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextI.setMaxHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextX.setMinHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextX.setMaxHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
		});
		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				hbxText.setMinSize(stage.getWidth() - 40, stage.getHeight()/3);
				hbxText.setMaxSize(stage.getWidth() - 40, stage.getHeight()/3);
				txtCountX.setWrappingWidth(hbxText.getMaxWidth() - vbxInitials.getMaxWidth() - hbxText.getPadding().getLeft() * 3 - hbxText.getSpacing());
				txtCountI.setWrappingWidth(hbxText.getMaxWidth() - vbxInitials.getMaxWidth() - hbxText.getPadding().getLeft() * 3 - hbxText.getSpacing());
				flpnTextI.setMinHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
				flpnTextI.setMaxHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
				flpnTextX.setMinHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
				flpnTextX.setMaxHeight(hbxText.getMaxHeight() / 2 - hbxText.getPadding().getBottom());
			} 
		});
		
			rectProgressBar.widthProperty().bind(stage.widthProperty().multiply(percent));
			rectProgressBar.setHeight(15);
			rectProgressBar.setFill(Color.LAWNGREEN);
//		Sets up the progress bar
		bpnProgressBar.setTop(rectProgressBar);
		
						txtCountI.setText(countI);
						txtCountI.setTextAlignment(TextAlignment.LEFT);
						txtCountI.setFont(fntText);
					
					flpnTextI.getChildren().add(txtCountI);
					flpnTextI.setAlignment(Pos.CENTER);
					flpnTextI.setPadding(new Insets(5));
					flpnTextI.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
			        
						txtCountX.setText(countX);
						txtCountX.setTextAlignment(TextAlignment.LEFT);
						txtCountX.setFont(fntText);
					
					flpnTextX.getChildren().add(txtCountX);
					flpnTextX.setAlignment(Pos.CENTER);
					flpnTextX.setPadding(new Insets(5));
					flpnTextX.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
						
				vbxText.getChildren().addAll(flpnTextX, flpnTextI);
				
		double d = 45;
					btnJ.setFont(fntButton);
					btnJ.setMinSize(d, d);
					btnJ.setMaxSize(d, d);
		
					btnT.setFont(fntButton);
					btnT.setMinSize(d, d);
					btnT.setMaxSize(d, d);
					
				vbxInitials.getChildren().addAll(btnT, btnJ, btnBack);
				vbxInitials.setMaxWidth(btnT.getMaxWidth());
				vbxInitials.setAlignment(Pos.TOP_CENTER);
				vbxInitials.setSpacing(10);
				vbxInitials.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
				
			hbxText.getChildren().addAll(vbxInitials, vbxText);
			hbxText.setMinSize(stage.getWidth() - 40, stage.getHeight()/3);
			hbxText.setMaxSize(stage.getWidth() - 40, stage.getHeight()/3);
			hbxText.setAlignment(Pos.TOP_LEFT);
			hbxText.setSpacing(20);
			hbxText.setPadding(new Insets(10));
			txtCountX.setWrappingWidth(hbxText.getMaxWidth() - vbxInitials.getMaxWidth() - hbxText.getPadding().getLeft() * 3 - hbxText.getSpacing());
			txtCountI.setWrappingWidth(hbxText.getMaxWidth() - vbxInitials.getMaxWidth() - hbxText.getPadding().getLeft() * 3 - hbxText.getSpacing());
			flpnTextI.setMinHeight(hbxText.getMinHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextI.setMaxHeight(hbxText.getMinHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextX.setMinHeight(hbxText.getMinHeight() / 2 - hbxText.getPadding().getBottom());
			flpnTextX.setMaxHeight(hbxText.getMinHeight() / 2 - hbxText.getPadding().getBottom());
		
				lbPlaying.setFont(fntTimer);
				
				//canvas
				gc.setStroke(Color.FORESTGREEN);
				gc.setLineWidth(20);
				canvas.setWidth(stage.getHeight() / 3);
				canvas.setHeight((circR * 2) + gc.getLineWidth());
				canvas.setWidth((circR * 2) + gc.getLineWidth());
				canvas.setOnMouseClicked((final MouseEvent m) ->	{
//					System.out.println("HEllo");
				});
				gc.strokeArc((canvas.getWidth() / 2) - (circR), gc.getLineWidth() / 2, circR * 2, circR * 2, 90, 360, ArcType.OPEN);
			
			stpnPlaying.getChildren().addAll(lbPlaying, canvas);
			stpnPlaying.setVisible(false);
//			stpnPlaying.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
				
				btnMultMins.setGraphic(new ImageView(imgMultMins1));
				
					    btnAdd.setFont(fntAddSub);
					    btnAdd.setMinWidth(85);
					    
					    btnSub.setFont(fntAddSub);GridPane P = new GridPane(); Canvas c = new Canvas(); P.add(c, 1, 1);
					    btnSub.setMinWidth(85);
					
					hbxAddSub.getChildren().addAll(btnAdd, btnSub);
					hbxAddSub.setSpacing(60);
					hbxAddSub.setAlignment(Pos.CENTER);
				
				stkpnAddSub.getChildren().addAll(hbxAddSub);
				
				btn3Mins.setGraphic(new ImageView(img3MinsPlay));
				
			ancpnButtons.getChildren().addAll(stkpnAddSub, btn3Mins, btnMultMins);
			AnchorPane.setLeftAnchor(btn3Mins, 0d);
			AnchorPane.setLeftAnchor(stkpnAddSub, 0d);
			    AnchorPane.setRightAnchor(stkpnAddSub, 0d);
			    AnchorPane.setTopAnchor(stkpnAddSub, 0d);
			    AnchorPane.setBottomAnchor(stkpnAddSub, 0d);
			AnchorPane.setRightAnchor(btnMultMins, 0d);
			
		rootApn.getChildren().addAll(stpnPlaying, ancpnButtons,  hbxText, bpnProgressBar);
		//TOP
		AnchorPane.setTopAnchor(ancpnButtons, 20d); 
			AnchorPane.setRightAnchor(ancpnButtons, 20d);
			AnchorPane.setLeftAnchor(ancpnButtons, 20d);
		AnchorPane.setTopAnchor(stpnPlaying, 20d);
			AnchorPane.setRightAnchor(stpnPlaying, 20d);
			AnchorPane.setLeftAnchor(stpnPlaying, 20d);
		//BOTTOM
		AnchorPane.setBottomAnchor(hbxText, 20d);
			AnchorPane.setRightAnchor(hbxText, 20d);
			AnchorPane.setLeftAnchor(hbxText, 20d);
		rootApn.setMinSize(stage.getWidth(), stage.getHeight());
		rootApn.setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
		
//		Desktop.getDesktop().browse(URI.create("https://membean.com/login"));
//		
//		web.load("https://membean.com/login");
//		StackPane rootStkpnWeb = new StackPane(wv);
//		Scene scnWeb = new Scene(rootStkpnWeb);
//		Stage stgWeb = new Stage();
//		stgWeb.setScene(scnWeb);
//		
//		stgWeb.setX(screen.getWidth() / 2);
//		stgWeb.setY(0);
//		stgWeb.setWidth(screen.getWidth() / 2);
//		stgWeb.setHeight(screen.getHeight() * .6);
//		stgWeb.show();
//		wv.setZoom(1);

		/**
		 * Sets up the stop feature of the timers
		 */
		stpnPlaying.setOnMouseClicked((final MouseEvent a) -> {
			timer3Mins.stop();
			timer3Mins.stop();
			stpnPlaying.setVisible(false);
			ancpnButtons.setVisible(true);
		});
		/**
		 * ends the sound when the sound ends
		 */
		mdplayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mdplayer.seek(Duration.ZERO);
				mdplayer.stop();
//				System.out.println("end");
//				t.interrupt();	
			}
		});
		/**
		 * Adds an "I" and updates the visuals accordingly
		 * <br>Five "I" make an "@" and five "@" make an "^"
		 */
		btnAdd.setOnMouseClicked((final MouseEvent m) -> {
			lastMove = Was.AddI;
			if (countI.length() >= 4 && countI.substring(countI.length() - 4).equals("IIII"))	{
			    countI = countI.substring(0, countI.length() - 4).concat("@");
			   if (countI.length() >= 5 && countI.substring(countI.length() - 5).equals("@@@@@"))	{
				   countI = countI.substring(0, countI.length() - 5).concat("^");
			   }
			} else {
			    countI = countI.concat("I");
			}
			numRight++;
			txtCountI.setText(countI);
			updateBKGD();
		});
		/**
		 * Adds an X and updates the visuals accordingly
		 * <br>Five "X" make a "%"
		 */
		btnSub.setOnMouseClicked((final MouseEvent m) -> {
			lastMove = Was.AddX;
			if (countX.length() >= 4 && countX.substring(countX.length() - 4).equals("XXXX"))	{
				countX = countX.substring(0, countX.length() - 4).concat("%");
			} else {
				countX = countX.concat("X");
			}
			numWrong++;
			txtCountX.setText(countX);
			updateBKGD();
		});
		/**
		 * Updates the session
		 */
		btnT.setOnMouseClicked((final MouseEvent m) -> {
//			Log = Log.concat("#" + countI + "\n" + countX + "\n\n");
			countI = countX = "";
			txtCountI.setText(countI);
			txtCountX.setText(countX);
			numRight = numWrong = 0;
			startMillis = System.currentTimeMillis();
			updateBKGD();
			for (Node b : vbxInitials.getChildren())	{
				((Button)b).setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
				((Button)b).setTextFill(Color.BLACK);
			}
			btnT.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, null, null)));
			btnT.setTextFill(Color.ANTIQUEWHITE);
			ancpnButtons.setVisible(true);
			stpnPlaying.setVisible(false);
			timer3Mins.stop();
			timerMultMins.stop();
		});
		/**
		 * Updates the session
		 */
		btnJ.setOnMouseClicked((final MouseEvent m) -> {
//			Log = Log.concat(countI + "\n" + countX + "\n\n");
			countI = countX = "";
			txtCountI.setText(countI);
			txtCountX.setText(countX);
			numRight = numWrong = 0;
			startMillis = System.currentTimeMillis();
			updateBKGD();
			for (Node b : vbxInitials.getChildren())	{
				((Button)b).setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));
				((Button)b).setTextFill(Color.BLACK);
			}
			btnJ.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, null, null)));
			btnJ.setTextFill(Color.ANTIQUEWHITE);
			ancpnButtons.setVisible(true);
			stpnPlaying.setVisible(false);
			timer3Mins.stop();
			timerMultMins.stop();
		});
		/**
		 * Sets up back button that reverses the last action done
		 */
		btnBack.setOnMouseClicked((final MouseEvent m)-> {
			if (countI.length() > 0 || countX.length() > 0)	{
				if (lastMove == Was.AddI)	{
					String lastChar = countI.substring(countI.length() - 1);
					System.out.println(lastChar);
					switch(lastChar) {
						case "I":
							countI = countI.substring(0, countI.length() - 1);
							System.out.println("1:" + countI);
							break;
						case "@":
							countI = countI.substring(0, countI.length() - 1).concat("IIII");
							System.out.println("2:" + countI);
							break;
						case "^":
							countI = countI.substring(0, countI.length() - 1).concat("@@@@IIII");
							System.out.println("3:" + countI);
							break;
					}
				}
			}
			txtCountI.setText(countI);
			txtCountX.setText(countX);
		});
		/**
		 * Starts the 3 minute timer when clicked
		 */
		btn3Mins.setOnMouseClicked((final MouseEvent m) -> {
			mins = 3;
			secs = 0;
			lbPlaying.setText(formatter.format(mins) + ":" + formatter.format(secs));
			ancpnButtons.setVisible(false);
			stpnPlaying.setVisible(true);
			timer3Mins.playFromStart();
		});
		/**
		 * Starts the 1 minute timer when clicked
		 */
		btnMultMins.setOnMouseClicked((final MouseEvent m) -> {
			mins = 1;
			secs = 0;
			lbPlaying.setText(formatter.format(mins) + ":" + formatter.format(secs));
			ancpnButtons.setVisible(false);
			stpnPlaying.setVisible(true);
			timerMultMins.playFromStart();
		});
	}

	/**
	 * Updates the background according to the current score ration between correct and incorrect
	 */
	private void updateBKGD()	{
		Color BKGDColor = Color.ANTIQUEWHITE;
			double ratio = ((double)numRight / ((double)numRight + (double)numWrong));
			if (numRight <= 0 && numWrong > 0) {
				ratio = 0;
			} else if (numRight > 0 && numWrong <= 0 || (numRight == 0 && numWrong == 0)) {
				ratio = 1;
			}
//			System.out.println(numWrong);
//			System.out.println(numRight);
			System.out.println(ratio);
			if (ratio > .8) {
				BKGDColor = Color.ANTIQUEWHITE;
				vbxInitials.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
				flpnTextI.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
				flpnTextX.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, null)));
			} else if (ratio > .7)	{
				BKGDColor = Color.BURLYWOOD;
//				System.out.println(">.7");
			} else {
				BKGDColor = Color.RED;
//				System.out.println("<.7");
				vbxInitials.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, null)));
				flpnTextI.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, null)));
				flpnTextX.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, null)));
			}
		rootApn.setBackground(new Background(new BackgroundFill(BKGDColor, null, null)));
	}

	/**
	 * Launches the program
	 * @param args
	 */
	public static void main(String[] args)	{
		Timer.launch(args);
	}
	
//	private void test()	{
//		System.out.println("sf s s");
//	}
	
//	private Runnable runTimer = new Runnable() {
//		@Override
//		public void run() {
//			long loog = System.currentTimeMillis();
//			System.out.println("af" + loog);
//			while(true) {
//				if (loog % 5000 == 0)	{
//					System.out.println("af s sasadfssadasd");
//				}
//			}
//		}
//	};
//
//	private Thread threadTimer = new Thread(runTimer);
	
}
