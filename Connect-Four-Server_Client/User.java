

public class User {
	private String PlayerName; 
	private String PlayerToken;
	public User(String name, String token){
		PlayerName = name;
		PlayerToken = token;
	}
	public String getPlayerName(){
		return PlayerName;
	}	
	public String getPlayerToken(){
		return PlayerToken;
	}
	public void setPlayerName(String PlayerName){
		this.PlayerName = PlayerName;
	}	
	public void setPlayerToken(String PlayerToken){
		this.PlayerToken = PlayerToken;
	}	
}

