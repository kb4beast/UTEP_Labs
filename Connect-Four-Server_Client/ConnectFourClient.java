
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class ConnectFourClient extends JFrame implements Runnable, ConnectFourConstants {
	////////////////////////////////////////////////////////
	private JButton jbtReset,jbtForfiet, jbtDrop[], jbtPlayAgain, jbtQuit; //= new JButton("Restart");
	private JPanel gameGrid ,drops, newGameGrid, Buttons, players, totalGUI ,timerInfo, info;//player1Info,  player2Info  
	private JLabel jlbPositionInGrid[], currentPlayerName, clockTimer, currentTurn;//, p1Name ,p2Name,,  p1Token, p2Token, 
	private static JFrame frame, frame2;
	private static User currentUser ,otherUser;//, player1, player2,
	private static GameBoard Game;
	private static java.util.Date startTimer, currentTimer;
	private static java.io.File autoSaveDirectory;
	private static String fileDate;
	//////////////////////////////////////////////////////
	private boolean myTurn = false;	// Indicate whether the player has the turn
	private String myToken = " ";// Indicate the token for the player
	private String otherToken = " ";//otherUser.getPlayerToken();;	// Indicate the token for the other player
	//	private Cell [] cell = new Cell[56];
	private int rowSelected;
	private int columnSelected;
	// Input and output streams from/to server
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	// Continue to play?
	private boolean continueToPlay = true;
	// Wait for the player to mark a cell
	private boolean waiting = true;
	// Indicate if it runs as application
	private boolean isStandAlone = true;
	// Host name or IP
	private String host = "localhost";

	/** Initialize UI */
	public  ConnectFourClient() {
		Border lineBorder = new LineBorder(Color.BLACK, 2);		
		//shows the 7*8 game board
		gameGrid = new JPanel();
		gameGrid.setLayout(new GridLayout(7,8));
		jlbPositionInGrid = new JLabel[56];
		for(int i = 0; i < 56; i++){////////////////////////game is 56 pieces 7*8=56 
			jlbPositionInGrid[i]=new JLabel(" " );
			jlbPositionInGrid[i].setBorder(lineBorder);
			gameGrid.add(jlbPositionInGrid[i]);
		}
		//shows drop buttons on top of each column\
		//adds array of buttons to top panel
		drops = new JPanel();
		drops.setLayout(new GridLayout(1,8));
		jbtDrop = new JButton[8];
		for(int i = 0; i<=7; i++){
			jbtDrop[i] = new JButton("Drop");
			drops.add(jbtDrop[i]);
			jbtDrop[i].addActionListener(new ButtonListener());/////////////////////////////////////////////////
		}
		//adds drop buttons to grid in border
		newGameGrid = new JPanel(new BorderLayout());
		newGameGrid.add(drops, BorderLayout.NORTH);//button array
		newGameGrid.add(gameGrid, BorderLayout.CENTER);//7*8 grid
		//panel for reset and forfeit buttons
		Buttons = new JPanel(new BorderLayout());//forfiet and reset buttons
		jbtReset = new JButton("Reset");
		jbtForfiet = new JButton("Forfeit");
		Buttons.add(jbtReset, BorderLayout.NORTH);
		Buttons.add(jbtForfiet, BorderLayout.SOUTH);
		jbtReset.addActionListener(new ButtonListener());///////////////////////////////////////////////////////////////
		jbtForfiet.addActionListener(new ButtonListener());/////////////////////////////////////////////////////////////

		//panel for player1 information** name and token
		//player1Info = new JPanel(new GridLayout(2,1));
		//p1Name = new JLabel("Player 1 Name:   "+player1.getPlayerName());
		//p1Token = new JLabel("Player 1 Token:   "+player1.getPlayerToken());
		//player1Info.add(p1Name);
		//player1Info.add(p1Token);
		//panel for player2 information** name and token
		//player2Info = new JPanel(new GridLayout(2,1));
		//p2Name = new JLabel("Player 2 Name:   "+ player2.getPlayerName());
		//p2Token = new JLabel("Player 2 Token:   "+player2.getPlayerToken());
		//player2Info.add(p2Name);
		//player2Info.add(p2Token);
		/**player1Info.add(firstPlayerName);//firstPlayerName is global variable*/

		//j panel for clock timer
		timerInfo = new JPanel(new BorderLayout());
		startTimer = new java.util.Date();
		currentTimer= new java.util.Date();
		long millis = (currentTimer.getTime() - startTimer.getTime());//start time-end time
		String time = String.format("%02d:%02d\n", 
				TimeUnit.MILLISECONDS.toMinutes(millis) - 
				TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		clockTimer = new JLabel("        Time:"+time);
		timerInfo.add( clockTimer , BorderLayout.CENTER);
		//Timer timer = new Timer(1000, new TimerListener1());//every 1000 milliseconds (1 second)
		//timer.start();

		/**************new file***************************************************/
		java.util.Date date = new java.util.Date();
		fileDate = (date.toString()).substring(0,11);	

		//System.out.println(autoSaveDirectory.getAbsolutePath());  /*used to debug*/
		//autoSaveDirectory = new java.io.File("c:\\GameLogs\\"+fileDate);
		//autoSaveDirectory.mkdirs();
		//Timer autoSaveTimer = new Timer(2000, new TimerListener2());//every 10000 milliseconds (10 second)
		//autoSaveTimer.start();
		//Timer timer = new Timer(1000, new TimerListener1());//every 1000 milliseconds (1 second)
		//timer.start();
		//panel for both players info and clock timer
		players = new JPanel(new BorderLayout());
		//players.add(player1Info, BorderLayout.NORTH);
		players.add(timerInfo, BorderLayout.CENTER);		
		//players.add(player2Info, BorderLayout.SOUTH);

		//label for current player name
		info = new JPanel(new GridLayout(2,1));
		currentPlayerName = new JLabel("Current Player Turn: " + currentUser.getPlayerName() + "   Token:" +currentUser.getPlayerToken(), JLabel.CENTER);
		currentTurn = new JLabel("it isnt your turn", JLabel.CENTER);
		info.add(currentPlayerName);
		info.add(currentTurn);

		// Panel p1 to hold labels and text fields
		totalGUI= new JPanel();
		totalGUI.setLayout(new BorderLayout(10,15));
		totalGUI.add(newGameGrid, BorderLayout.CENTER);
		totalGUI.add(info, BorderLayout.NORTH);
		totalGUI.add(players, BorderLayout.WEST);
		totalGUI.add(Buttons,BorderLayout.EAST);

		add(totalGUI);
		connectToServer();
	}

	/**listener triggered by 10 SEC for autosave**/
	private class TimerListener2 implements ActionListener{
		@Override /**Handle 1 second exception*/
		public void actionPerformed(ActionEvent e){
			autoSaveDirectory = new java.io.File("c:\\GameLogs\\"+fileDate);
			autoSaveDirectory.mkdirs();
			//String fileDate = (date.toString()).substring(0,11);
			//fileDate = fileDate + ".txt";
			File file = new File(autoSaveDirectory.getAbsolutePath()+ "LOG.txt");
			try {
				PrintWriter output = new PrintWriter(file);
				output.println("Auto Save:" + new java.util.Date(file.lastModified()));
				output.println(Game.PrintGameBoard());
				output.print("It was " + currentUser.getPlayerName()  +  "'s turn to make next move");
				output.close();

			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			}


			//System.out.println(fileDate);
			//autoSaveFile.
		}
	}
	//listener triggered by 1 SEC for display time
	private class TimerListener1 implements ActionListener{
		@Override /**Handle 1 second exception*/
		public void actionPerformed(ActionEvent e){

			currentTimer= new java.util.Date();
			long millis = (currentTimer.getTime() - startTimer.getTime());//start time-end time
			String time = String.format("%02d:%02d\n", 
					TimeUnit.MILLISECONDS.toMinutes(millis) - 
					TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
					TimeUnit.MILLISECONDS.toSeconds(millis) - 
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
			clockTimer.setText("        Time:"+time);
		}
	}


	//button listener for all buttons, drops, forfiet, reset
	private class ButtonListener implements ActionListener {
		public ButtonListener(){
		}

		public void actionPerformed(ActionEvent e){

			if(!Game.boardIsFull()){
				if(myTurn){
					//used to convert index of double array from lab1 to
					//index of single array in JLabel of this lab
					int yCoordinate;
					int arrayIndex;
					boolean isValid;

					if(e.getSource() == jbtDrop[0]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(0);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 0);
							arrayIndex = 0+(8*yCoordinate);
							jlbPositionInGrid[arrayIndex].setText((myToken));
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=0;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else {
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
						}
					}

					else if(e.getSource() == jbtDrop[1]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(1);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 1);
							arrayIndex = 1+(8*yCoordinate);
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=1;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[2]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(2);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 2);
							arrayIndex = 2+(8*yCoordinate);
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=2;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[3]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(3);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 3);
							arrayIndex = 3+(8*yCoordinate);
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=3;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[4]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(4);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 4);
							arrayIndex = (8*yCoordinate)+4;
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=4;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[5]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(5);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 5);
							arrayIndex = (8*yCoordinate)+5;
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=5;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;
						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[6]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(6);
						if(isValid){					
							yCoordinate = Game.insert(myToken,6 );
							arrayIndex = (8*yCoordinate)+6;
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=6;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;

						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtDrop[7]){//////////////////////////////////////////////////////////////////////////////////////////////////
						isValid = Game.isValidMove(7);
						if(isValid){					
							yCoordinate = Game.insert(myToken, 7);
							arrayIndex = (8*yCoordinate)+7;
							jlbPositionInGrid[arrayIndex].setText(myToken);
							myTurn=false;
							rowSelected =yCoordinate;
							columnSelected=7;
							currentTurn.setText("Waiting fro the other player to move");
							waiting = false;

						}
						else 
							JOptionPane.showMessageDialog(null, "Invalid move, already full");
					}
					else if(e.getSource() == jbtReset){//////////////////////////////////////////////////////////////////////////////////////////////////
						frame.dispose();
						//startnewGame();
						System.out.println("pressed reset");
					}
					else if(e.getSource() == jbtForfiet){//////////////////////////////////////////////////////////////////////////////////////////////////
						System.out.println("pressed forfiet");
						//switchPlayer();
						//	printWinnerMessage(currentUser);
						frame.dispose();
					}
					else if(e.getSource() == jbtPlayAgain){//////////////////////////////////////////////////////////////////////////////////////////////////
						System.out.println("pressed play again");
						frame2.dispose();
						frame.dispose();
						//		startnewGame();
					}
					else if(e.getSource() == jbtQuit){///////////////////////////////////////////////////////////////////////////////////////////////////////
						System.out.println("pressed Quit");
						frame2.dispose();
						frame.dispose();
					}			
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Board is full, new game will start.");
				frame.dispose();
				//startnewGame();
			}
			Game.PrintGameBoard();
			//switchPlayer();
		}
		
	}



	@SuppressWarnings("resource")
	private void connectToServer() {
		try {
			// Create a socket to connect to the server
			Socket socket;
			if (isStandAlone)
				socket = new Socket(host, 8000);
			else
				socket = new Socket(host, 8000);
			//socket = new Socket(getCodeBase().getHost(), 8000);
			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());
			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch (Exception ex) {
			System.err.println(ex);
		}
		// Control the game on a separate thread
		Thread thread = new Thread(this);
		thread.start();
	}
	@Override
	public void run() {
		try {
			// Get notification from the server
			int player = fromServer.readInt();

			// Am I player 1 or 2?
			if (player == PLAYER1) {
				myToken =  currentUser.getPlayerToken();
				otherToken =  otherUser.getPlayerToken();
				currentPlayerName.setText("Current Player Turn: " + currentUser.getPlayerName() +
						"   Token:" +currentUser.getPlayerToken());
				currentTurn.setText("Waiting for player 2 to join");
				//	jlblStatus.setText("Waiting for player 2 to join");

				// Receive startup notification from the server
				fromServer.readInt(); // Whatever read is ignored
				// The other player has joined  
				//currentPlayerName.setText("Current Player Turn: " + currentUser.getPlayerName() +"   Token:" +currentUser.getPlayerToken());
				currentTurn.setText("Player 2 has joined. I start first");
				// It is my turn
				myTurn = true;
			}
			else if (player == PLAYER2) {
				myToken =  otherUser.getPlayerToken();
				otherToken =  currentUser.getPlayerToken();
				currentPlayerName.setText("Current Player Turn: " + otherUser.getPlayerName() +"   Token:" +otherUser.getPlayerToken());
				currentTurn.setText("Waiting for player 1 to move");
			}
			Timer autoSaveTimer = new Timer(2000, new TimerListener2());//every 10000 milliseconds (10 second)
			autoSaveTimer.start();
			Timer timer = new Timer(1000, new TimerListener1());//every 1000 milliseconds (1 second)
			timer.start();

			// Continue to play
			while (continueToPlay) {
				if (player == PLAYER1) {
					waitForPlayerAction(); // Wait for player 1 to move
					sendMove(); // Send the move to the server
					receiveInfoFromServer(); // Receive info from the server
				}
				else if (player == PLAYER2) {
					receiveInfoFromServer(); // Receive info from the server
					waitForPlayerAction(); // Wait for player 2 to move
					sendMove(); // Send player 2's move to the server
				}
			}
		}
		catch (Exception ex) {
		}
	}

	/** Wait for the player to mark a cell */
	private void waitForPlayerAction() throws InterruptedException {
		while (waiting) {
			Thread.sleep(100);
		}

		waiting = true;
	}

	/** Send this player's move to the server */
	private void sendMove() throws IOException {
		toServer.writeInt(rowSelected); // Send the selected row
		toServer.writeInt(columnSelected); // Send the selected column
	}

	/** Receive info from the server */
	private void receiveInfoFromServer() throws IOException {
		// Receive game status
		int status = fromServer.readInt();

		if (status == PLAYER1_WON) {
			// Player 1 won, stop playing
			continueToPlay = false;
			if (myToken == "[X]") {
				currentTurn.setText("I won! (X)");
			}
			else if (myToken == "[O]") {
				currentTurn.setText("Player 1 (X) has won!");
				receiveMove();
			}
		}
		else if (status == PLAYER2_WON) {
			// Player 2 won, stop playing
			continueToPlay = false;
			if (myToken == "[O]") {
				currentTurn.setText("I won! (O)");
			}
			else if (myToken == "[X]") {
				currentTurn.setText("Player 2 (O) has won!");
				receiveMove();
			}
		}
		else if (status == DRAW) {
			// No winner, game is over
			continueToPlay = false;
			currentTurn.setText("Game is over, no winner!");

			if (myToken =="[O]") {
				receiveMove();
			}
		}
		else {
			receiveMove();
			currentTurn.setText("My turn");
			myTurn = true; // It is my turn
		}
	}

	private void receiveMove() throws IOException {
		// Get the other player's move
		int row = fromServer.readInt();
		int column = fromServer.readInt();
		int arINDEX = ((row*8)+(column));
		Game.insert(otherToken,column );
		jlbPositionInGrid[arINDEX].setText(otherToken);
		//jlbPositionInGrid[arINDEX].setToken(otherToken);
	}


	// This main method enables the applet to run as an application 
	public static void main(String[] args) {
		currentUser= new User(JOptionPane.showInputDialog("Enter name:"), "[X]");
		otherUser= new User("Other Player ", "[O]");
		// Create a frame
		frame = new ConnectFourClient();//("Tic Tac Toe Client");
		Game = new GameBoard();
		// Display the frame
		frame.setSize(850,700);
		frame.setTitle("Connect Four");
		frame.setLocationRelativeTo(null); // Center the frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);		
	}
}
