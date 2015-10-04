
public class GameBoard {

	private static String [][] Connect_4 = new String [7][8];
	public GameBoard(){
		Connect_4 = initializeBoard(Connect_4);//make a 2x2 array for board
	}
	public String PrintGameBoard() {//method to print board
		String x = "";
		for(int i = 0; i < Connect_4.length; i++){
			for(int j=0; j<Connect_4[i].length; j++){
				System.out.print(Connect_4[i][j]);
				x+=Connect_4[i][j];
			}
			System.out.println();
			x+="\r\n";
		}
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("\n\n******************************************");
		return x;
	}
	//initialize board to emptyness
	private static String[][] initializeBoard(String[][] connect_4) {
		for(int i = 0; i < connect_4.length; i++){
			for(int j=0; j<connect_4[i].length; j++)
				connect_4[i][j]="[ ]";
		}
		return connect_4;
	}
	//method to check if board is full by only checking the top row
	public boolean boardIsFull() {
		for(int i = 0 ; i< Connect_4[0].length; i++)
			if(Connect_4[0][i] == "[ ]")
				return false;			
		return true;
	}
	//method to insert the token
	public int insert(String playerToken, int dropPosition) {
		int counter = Connect_4.length;
		for(int i = Connect_4.length-1 ; i >=0; i--){
			if(Connect_4[i][dropPosition] == "[ ]"){
				Connect_4[i][dropPosition] = playerToken;	
				i = -1;
			}
			counter--;
		}
		return counter;
	}
	//check if move is valid by first looking if it is within the size of board game
	public boolean isValidMove(int dropPosition) {
		if( dropPosition  < 0  || dropPosition >= 8 )
			return false;
		if(Connect_4[0][dropPosition] == "[ ]")
			return true;
		return false;
	}
	/**This method checks for a winner by counting the number of times the same token is counted in a row.
	 * It checks all but up, we check left-up, left, left-down, down, right-down, right, up-right
	 * when we count the left parts we keep track of the same tokens found in a row and add it to the right side
	 * when we check down-left first we add the count to up-right
	 * and when we do down-right we add the token count to up-left
	 */
	public boolean isWinner(int xCoordinate, int yCoordinate, String token) {
		int xCoord = xCoordinate;
		int yCoord = yCoordinate;
		int counter=0;

		//check down
		for(int i = yCoord; i < Connect_4.length ; i++ ){			
			if(Connect_4[i][xCoord] == token){
				counter++;
				//System.out.println("counter:" + counter);
				if(counter == 4){
					System.out.println("won at down");
					return true;
				}
			}
			else 
				i=99;
		}		
		counter = 0;
		////////////check left first//////////////////
		for(int i = xCoord; i >= 0; i-- ){			
			if(Connect_4[yCoord][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at left");
					return true;
				}	
			}
			else 
				i=-99;
		}		
		//do not reset counter to zero to add to right count
		///////////after check left add to right counter///////////////////////////
		for(int i = xCoord+1; i < Connect_4[0].length; i++ ){			
			if(Connect_4[yCoord][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at right ");
					return true;
				}
			}
			else 
				i=99;
		}
		counter = 0;
		///////////first check left down diagonal///////////////////////////
		for(int i = xCoord, j = yCoord; i >= 0 && j<Connect_4.length;  i--, j++ ){			
			if(Connect_4[j][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at left down diag");
					return true;
				}
			}
			else 
				j=99;
		}
		//do not reset counter to zero to add to right-up count
		///////////then add to right up diagonal///////////////////////////
		for(int i = xCoord+1, j = yCoord-1; i <Connect_4[0].length  && j>=0;  i++, j-- ){			
			if(Connect_4[j][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at right up diag");
					return true;
				}
			}
			else 
				j=-1;
		}	
		counter = 0;
		///////////first check down right diagonal///////////////////////////
		for(int i = xCoord, j = yCoord; i <Connect_4[0].length && j<Connect_4.length;  i++, j++ ){			
			if(Connect_4[j][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at right diag");
					return true;
				}
			}		
			else 
				i=99;
		}
		//do not reset counter to zero to add to left-up count
		///////////then add to up left up diagonal///////////////////////////
		for(int i = xCoord-1, j = yCoord-1; i >= 0 && j>=0;  i--, j-- ){			
			if(Connect_4[j][i] == token){
				counter++;
				if(counter == 4){
					System.out.println("won at up left diag");
					return true;
				}
			}		
			else 
				i=-99;
		}
		counter = 0;
		//////////////////////////////////////////////////////////////////////////////
		return false;

	}
}

