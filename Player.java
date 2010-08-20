import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;


public class Player {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;
	private final ApplesCommands ops;
	
	public Player(Socket s) throws IOException{
		ops = new ApplesCommands();
		connection = s;
		out = new PrintWriter(connection.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	}
	
	public boolean equals(Object p){
		if(!(p instanceof Player)){
			return false;
		}
		return connection.equals(((Player)p).connection);
	}
	
	public int hashCode(){
		return connection.hashCode();
	}
	
	private boolean sendData(String toSend, String expectedResponse){
		String response = sendData(toSend);
		
		if((response == null) || (!response.equals(expectedResponse))){
			return false;
		}
		return true;
	}
	
	private String sendData(String toSend){
		out.println(toSend);
		try {
			return in.readLine();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public boolean connectionIsAlive(){
		return sendData(ops.ping, ops.ack_ping);
	}
	
	public boolean changeAdjective(Card c){
		if(!sendData(ops.cmd_adj, ops.ack_adj)){
			return false;
		}
		return sendData(c.toString(), ops.ack_data);
	}
	
	public boolean sendCard(Card c){
		if(!sendData(ops.cmd_card, ops.ack_card)){
			return false;
		}
		return sendData(c.toString(), ops.ack_data);
	}
	
	public void sendGameOver(){
		sendData(ops.cmd_done, ops.ack_done);
	}
	
	public int getHandCount(){
		String count = sendData(ops.cmd_hand);
		if(count == null){
			return -1;
		}
		try {
			return Integer.parseInt(count);
		} 
		catch (NumberFormatException e) {
			return -1;
		}
	}
	
	public int dataAvailable(){
		try {
			return connection.getInputStream().available();
		}
		catch (IOException e) {
			return -1;
		}
	}
	
	public Card requestCard(){
		return new Card(sendData(ops.cmd_req));
	}
	
	// setDealer and chooseCard are tied together
	// setDealer is sent before querying the players for cards
	// to alert the dealer to his dealer status.
	public boolean setDealer(){
		return sendData(ops.cmd_chs, ops.ack_chs);
	}
	
	// chooseCard is run after all the cards are collected and
	// it sends the cards to the dealer and waits for his choice.
	public Card chooseCard(Set<Card> cards){
		sendData(""+cards.size(), ops.ack_data);
		for(Card c : cards){
			sendData(c.toString(), ops.ack_data);
		}
		return new Card(sendData(ops.cmd_chs).split(":"));
	}
	
	public void sendPreview(Set<Card> cards){
		sendData(ops.cmd_prev, ops.ack_prev);
		sendData(""+cards.size(), ops.ack_data);
		for(Card c : cards){
			sendData(c.toString(), ops.ack_data);
		}
	}
	
	public boolean sendWinner(Card c){
		sendData(ops.cmd_win, ops.ack_win);
		return sendData(c.toString(), ops.ack_data);
	}
	
	public boolean sendLoser(){
		return sendData(ops.cmd_lose, ops.ack_lose);
	}
}
