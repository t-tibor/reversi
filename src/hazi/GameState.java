package hazi;

import java.io.Serializable;

public class GameState implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String state;
	public GameState(String s) {
		state = s;
	}

}
