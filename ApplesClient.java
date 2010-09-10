import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class ApplesClient{
	private Socket connection;
	private PrintWriter out;
	private BufferedReader in;
	private final ApplesCommands ops;
	private ArrayList<Card> hand;
	private Card adjective;
	private ArrayList<Card> spoils;
	
	public ApplesClient(){
		this("localhost", 8886);
	}
	
	public ApplesClient(String server, int port){
		ops = new ApplesCommands();
		hand = new ArrayList<Card>();
		spoils = new ArrayList<Card>();
		adjective = null;
		try{
			connection = new Socket(server, port);
			out = new PrintWriter(connection.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		}
		catch(UnknownHostException e){
			System.err.println("Don't know about host: "+server+":"+port);
			System.exit(1);
		}
		catch(IOException e){
			System.err.println("Couldn't get I/O for the connection to: "+server+":"+port);
			System.exit(1);
		}
	}
	
	public void handleCommand(){
		try {
			String command = in.readLine();
			if(command==null){
				System.out.println("Null command");
				System.exit(1);
			}
			if(command.equals(ops.ping)){
				out.println(ops.ack_ping);
			}
			// New adjective from server
			else if(command.equals(ops.cmd_adj)){
				out.println(ops.ack_adj);
				Card c = new Card(in.readLine().split(":"));
				out.println(ops.ack_data);
				adjective = c;
			}
			// Dealer chooses card
			else if(command.equals(ops.cmd_chs)){
				out.println(ops.ack_chs);
				System.out.println("You're the dealer. Waiting for input from players.");
				// chooseCard will block while waiting for the server to collect
				// the players' cards.
				chooseCard();
			}
			// Player sends card to server
			else if(command.equals(ops.cmd_req)){
				System.out.println(adjective);
				sendCard();
			}
			// New noun being send from the server (refill hand)
			else if(command.equals(ops.cmd_card)){
				out.println(ops.ack_card);
				Card c = new Card(in.readLine().split(":"));
				out.println(ops.ack_data);
				hand.add(c);
			}
			else if(command.equals(ops.cmd_prev)){
				out.println(ops.ack_prev);
				ArrayList<Card> cards = receiveCards();
				System.out.println("These are the submitted cards:");
				for(Card c : cards){
					System.out.println(c.getWord());
				}
			}
			// Server requesting hand size
			else if(command.equals(ops.cmd_hand)){
				out.println(""+hand.size());
			}
			else if(command.equals(ops.cmd_win)){
				out.println(ops.ack_win);
				Card win = new Card(in.readLine().split(":"));
				out.println(ops.ack_data);
				spoils.add(win);
				System.out.println("You won this round!");
			}
			else if(command.equals(ops.cmd_lose)){
				out.println(ops.ack_lose);
				System.out.println("You lost this round.");
			}
			else if(command.equals(ops.cmd_done)){
				out.println(ops.ack_done);
				System.out.println("Game over!");
				System.exit(0);
			}
			else{
				out.println(ops.err);
				System.out.println("unknown command");
			}
			
		}
		catch (IOException e) {
			System.exit(1);
		}
		
	}
	
	private void sendCard(){
		System.out.println("Adjective: "+adjective);
		// time to send a new card
		int size = hand.size();
		System.out.println("Choices:");
		for(int x=0;x<size;x++){
			System.out.println(x+". "+hand.get(x));
		}
		System.out.print(">");
		Scanner sc = new Scanner(System.in);
		int choice = sc.nextInt();
		out.println(hand.remove(choice));
	}
	
	private ArrayList<Card> receiveCards(){
		ArrayList<Card> cards = new ArrayList<Card>();
		int count = -1;
		try {
			count = Integer.parseInt(in.readLine());
		} 
		catch (NumberFormatException e) {
			System.err.println("Error parsing transmitted card");
		}
		catch (IOException e) {
		}
		if(count < 0){
			System.err.println("Error: Number of cards less than zero.");
		}
		
		out.println(ops.ack_data);
		
		for(int x=0;x<count;x++){
			try{
				cards.add(new Card(in.readLine().split(":")));
			}
			catch (IOException e) {
				System.err.println("Error receiving card.");
			}
			out.println(ops.ack_data);
		}
		return cards;
	}
	
	// Dealer chooses a card here
	private void chooseCard(){
		ArrayList<Card> cards = receiveCards();
		System.out.println("Adjective: "+adjective);
		System.out.println("Choices:");
		for(int x=0;x<cards.size();x++){
			System.out.println(x+". "+cards.get(x));
		}
		try {
			if(!in.readLine().equals(ops.cmd_chs)){
				System.err.println("Error with card choice.");
			}
		}
		catch (IOException e) {
		}
		catch (NullPointerException e){
			out.println(ops.err);
		}
		System.out.print(">");
		Scanner sc = new Scanner(System.in);
		out.println(cards.get(sc.nextInt()));
	}

	public void run(){
		System.out.println("Waiting for a game to open up...");
		while(true){
			handleCommand();
		}
	}
	
	public static void main(String[]args){
		ApplesClient c = new ApplesClient("localhost", 8886);
		c.run();
	}
}
