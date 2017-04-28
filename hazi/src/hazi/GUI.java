package hazi;

public class GUI implements IGameState{

	public void onNewGameState(GameState gs)
	{
		System.out.println("Game state recieved: " + gs.state);
	}
	
}
