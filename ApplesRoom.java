import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ApplesRoom{
	private ServerSocket listener;
	private final int MAX_PLAYERS = 10;
	private ArrayList<Player> users;
	private Thread gameplay;
	
	public ApplesRoom(){
		this("noun.txt", "adj.txt", 8886);	
	}

	public ApplesRoom(String nfile, String afile, int port){
		users = new ArrayList<Player>();
		try{
			gameplay = new Thread(new GameplayThread(nfile, afile, users));
			gameplay.start();
			
		}
		catch(java.io.FileNotFoundException e){
			System.out.println("Could not open file.");
			System.exit(1);
		}
		try{
			listener = new ServerSocket(port, MAX_PLAYERS);
		}
		catch(IOException e){
			System.out.println("Could not listen on port: "+port);
			System.exit(1);
		}
		
	}

	public void run(){
		boolean start = false;
		while(!start){
			try{
				Socket server;
				server = listener.accept();
				synchronized(users){
					users.add(new Player(server));
					/*if(users.size() == 3){
					 *	notify_gamethread();
					 *}
					 */
				}
			}
			catch(IOException e){
				System.out.println("fail");
				return;
			}
		}
	}

	public static void main(String[]args){
		ApplesRoom s = new ApplesRoom();
		s.run();
	}
}
