package hazi;

public class Logic implements ICommand{

	public void onNewCommand(Command c)
	{
		System.out.println("Command received:" + c.commandCode.toString());
	}
	

}
