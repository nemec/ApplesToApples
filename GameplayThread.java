import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class GameplayThread implements Runnable{
	private Deck noun;
	private Deck adj;
	private ArrayList<Player> users;
	private Player dealer;
	private final int MAX_HAND_SIZE = 7;
	private boolean gameover;
	
	public GameplayThread(String nfile, String afile, 
			ArrayList<Player>users) throws java.io.FileNotFoundException {

		this.users=users;
		dealer = null;
		gameover = false;
		
		noun = new Deck(nfile);
		adj = new Deck(afile); 
	}
	
	private void playRound(){
		if(pruneConnections() < 3){
			//TODO wait for three players
		}
		
		ArrayList<Player> players;
		synchronized(users){
			players = new ArrayList<Player>(users);
		}
		
		int ix = players.indexOf(dealer); 
		if(dealer == null || ix < 0 || ix >= players.size()){
			dealer = players.remove(0);
		}
		else{
			dealer = players.remove((ix+1)%players.size());
		}
				
		Iterator<Player> i = users.iterator();
		while(i.hasNext()){
			try{
				refillHand(i.next());
			}
			catch(IOException e){
				i.remove();
			}
		}
		
		// Notify all of new adjective card
		Card greenCard = adj.remove();
		if(greenCard==null){
			for(Player p : users){
				p.sendGameOver();
			}
			this.gameover = true;
			return;
		}
		
		if(!dealer.changeAdjective(greenCard)){
			System.err.println("WHOA THE DEALER DISCONNECTED.");
			return;
		}
		
		i = users.iterator();
		while(i.hasNext()){
			if(!i.next().changeAdjective(greenCard)){
				i.remove();
			}
		}
		
		dealer.setDealer();
		Map<Card, Player> submitted = collectNounCards(players);
		for(Player p : players){
			p.sendPreview(submitted.keySet());
		}
		Card winner = dealer.chooseCard(submitted.keySet());
		System.out.println(submitted.containsKey(winner));
		submitted.remove(winner).sendWinner(winner);
		for(Player p : submitted.values()){
			p.sendLoser();
		}
		noun.add(winner);
		for(Card c : submitted.keySet()){
			noun.add(c);
		}
		
	}
	
	// Sends "ping" to every connection. If a non-null response is given,
	// keeps the connection in the list, otherwise removes it.
	private int pruneConnections(){
		int connectionCount = 0;
		synchronized(users){
			Iterator<Player> i = users.iterator();
			while(i.hasNext()){
				if(!i.next().connectionIsAlive()){
					i.remove();
				}
			}
			connectionCount = users.size();
		}
		return connectionCount;
	}
	
	private void refillHand(Player p) throws IOException{
		int cards = MAX_HAND_SIZE-p.getHandCount();
		for(int x=0;x<cards;x++){
			Card c = noun.remove();
			p.sendCard(c);
		}
		
	}
	
	private Map<Card, Player> collectNounCards(ArrayList<Player> players){
		// Execute tasks
		ExecutorService executor = Executors.newFixedThreadPool(players.size());
		ArrayList<FutureTask<Card>> waitingTasks = new ArrayList<FutureTask<Card>>();
		for(final Player p: players){
			FutureTask<Card> f = new FutureTask<Card>(new Callable<Card>(){
				public Card call(){
					return p.requestCard();
				}
			});
			waitingTasks.add(f);
			executor.execute(f);
		}
		
		// Collect sent cards
		Map<Card, Player> submitted = new HashMap<Card, Player>();
		for(int x=0;x<players.size();x++){
			try {
				Card c = waitingTasks.get(x).get(2, TimeUnit.MINUTES);
				submitted.put(c, players.get(x));
			} catch (InterruptedException e) {
				System.err.println("Card transmit interrupted.");
			} catch (ExecutionException e) {
				System.err.println("Card tranmit execution error.");
			} catch (TimeoutException e) {
				System.err.println("Card transmit timeout.");
			}
		}
		
		return submitted;
	}
	
	protected boolean isGameOver(){
		return gameover;
	}

	public void run() {
		try {
			System.out.println("sleeping");
			Thread.sleep(5000);
			System.out.println("done sleeping");
		} catch (InterruptedException e) {}
		while(!isGameOver()){
			playRound();
		}
	}
}
