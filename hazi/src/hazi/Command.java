package hazi;

import java.io.Serializable;

public class Command implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public Integer commandCode;
	Command(int c)
	{
		commandCode = c;
	}
	
}
