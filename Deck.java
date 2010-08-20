import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// The Deck class holds a list of cards populated with either
// the contents of a file or a List. Those cards are then shuffled.
public class Deck {
	
	private ArrayList<Card> cards;
	private ArrayList<Card> discard;
	
	public Deck(List<Card> l){
		cards = new ArrayList<Card>(l);
		discard = new ArrayList<Card>();
		Collections.shuffle(cards);
	}
	
	public Deck(String file) throws java.io.FileNotFoundException {
		
		cards = new ArrayList<Card>();
		discard = new ArrayList<Card>();
		
		Scanner read = new Scanner(new File(file));
		while(read.hasNext()){
			String text = read.nextLine();
			int ix = text.indexOf(':');
			if(ix > 0)
				cards.add(new Card(text.substring(0,ix), text.substring(ix+1)));
			else
				cards.add(new Card(text));
		}
		
		Collections.shuffle(cards);
	}
	
	// We don't want to disturb the deck itself when adding a card,
	// so place it in the discard pile.
	public void add(Card c){
		discard.add(c);
	}
	
	// Removes a card from the shuffled deck
	// Transparently transfer the discard pile to the deck if the deck runs out of cards.
	public Card remove(){
		if(cards.isEmpty()){
			if(discard.isEmpty()){
				return null;
			}
			cards = discard;
			discard = new ArrayList<Card>();
		}
		Collections.shuffle(cards);
		return cards.remove(0);
	}
	
	public boolean isEmpty(){
		return cards.isEmpty() && discard.isEmpty();
	}
	
	public String toString(){
		return cards.toString();
	}
	
}
