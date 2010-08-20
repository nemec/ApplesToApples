public class Card{
	private String word;
	private String description;
	
	public Card(String[]args){
		word = null;
		description = "";
		
		if(args.length > 0){
			word = args[0];
		}
		if(args.length > 1){
			StringBuilder s = new StringBuilder();
			s.append(args[1]);
			for(int x=2;x<args.length;x++){
				s.append(":").append(args[x]);
			}
			description = s.toString();
		}
	}
	
	public String getWord(){
		return word;
	}
	
	public String getDescription(){
		return description;
	}
	
	public boolean equals(Object c){
		if(!(c instanceof Card)){
			return false;
		}
		return word.equals(((Card)c).word);
	}
	
	public int hashCode(){
		return word.hashCode();
	}

	public String toString(){
		if(description.equals("")){
			return word;
		}
		else{
			return word+":"+description;
		}
	}
}
