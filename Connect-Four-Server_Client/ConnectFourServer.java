
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class ConnectFourServer extends JFrame
implements ConnectFourConstants {
	public static void main(String[] args) {
		ConnectFourServer frame = new ConnectFourServer();
	}

	public ConnectFourServer() {
		JTextArea jtaLog = new JTextArea();

		// Create a scroll pane to hold text area
		JScrollPane scrollPane = new JScrollPane(jtaLog);

		// Add the scroll pane to the frame
		add(scrollPane, BorderLayout.CENTER);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 300);
		setTitle("TicTacToeServer");
		setVisible(true);

		try {
			// Create a server socket
			ServerSocket serverSocket = new ServerSocket(8000);
			jtaLog.append(new Date() +": Server started at socket 8000\n");

			// Number a session
			int sessionNo = 1;

			// Ready to create a session for every two players
			while (true) {
				jtaLog.append(new Date() +": Wait for players to join session " + sessionNo + '\n');

				// Connect to player 1
				Socket player1 = serverSocket.accept();

				jtaLog.append(new Date() + ": Player 1 joined session " +sessionNo + '\n');
				jtaLog.append("Player 1's IP address" +player1.getInetAddress().getHostAddress() + '\n');

				// Notify that the player is Player 1
				new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

				// Connect to player 2
				Socket player2 = serverSocket.accept();

				jtaLog.append(new Date() +": Player 2 joined session " + sessionNo + '\n');
				jtaLog.append("Player 2's IP address" +player2.getInetAddress().getHostAddress() + '\n');

				// Notify that the player is Player 2
				new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

				// Display this session and increment session number
				jtaLog.append(new Date() + ": Start a thread for session " +sessionNo++ + '\n');

				// Create a new thread for this session of two players
				HandleASession task = new HandleASession(player1, player2);

				// Start the new thread
				new Thread(task).start();
			}
		}
		catch(IOException ex) {
			System.err.println(ex);
		}
	}
}

// Define the thread class for handling a new session for two players
class HandleASession implements Runnable, ConnectFourConstants {
	private Socket player1;
	private Socket player2;

	private GameBoard game;

	private DataInputStream fromPlayer1;
	private DataOutputStream toPlayer1;
	private DataInputStream fromPlayer2;
	private DataOutputStream toPlayer2;

	// Continue to play
	private boolean continueToPlay = true;

	/** Construct a thread */
	public HandleASession(Socket player1, Socket player2) {
		this.player1 = player1;
		this.player2 = player2;

		game = new GameBoard();
	}

	/** Implement the run() method for the thread */
	public void run() {
		try {
			// Create data input and output streams
			DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
			DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
			DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
			DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

			// Write anything to notify player 1 to start
			// This is just to let player 1 know to start
			toPlayer1.writeInt(1);

			// Continuously serve the players and determine and report
			// the game status to the players
			while (true) {
				// Receive a move from player 1
				int row = fromPlayer1.readInt();
				int column = fromPlayer1.readInt();
				int yCoordinate = game.insert("[X]", column);

				// Check if Player 1 wins
				if(game.isWinner(column, yCoordinate, "[X]")){//if a  winner is found
					toPlayer1.writeInt(PLAYER1_WON);
					toPlayer2.writeInt(PLAYER1_WON);
					sendMove(toPlayer2, row, column);
					break; // Break the loop
				}
				else{
					toPlayer2.writeInt(CONTINUE);
					sendMove(toPlayer2, row, column);
				}
				// Receive a move from Player 2
				row = fromPlayer2.readInt();
				column = fromPlayer2.readInt();
				yCoordinate = game.insert("[O]", column);
				// Check if Player 2 wins
				if(game.isWinner(column, yCoordinate, "[O]")){//if a  winner is found
					toPlayer1.writeInt(PLAYER2_WON);
					toPlayer2.writeInt(PLAYER2_WON);
					sendMove(toPlayer1, row, column);
					break; // Break the loop
				}
				else if (game.boardIsFull()) { // Check if all cells are filled
					toPlayer1.writeInt(DRAW);
					toPlayer2.writeInt(DRAW);
					sendMove(toPlayer1, row, column);
					break;
				}
				else {
					// Notify player 2 to take the turn
					toPlayer1.writeInt(CONTINUE);
					// Send player 1's selected row and column to player 2
					sendMove(toPlayer1, row, column);
				}
			}
		}
		catch(IOException ex) {
			System.err.println(ex);
		}
	}

	/** Send the move to other player */
	private void sendMove(DataOutputStream out, int row, int column)throws IOException {
		out.writeInt(row); // Send row index
		out.writeInt(column); // Send column index
	}
}
