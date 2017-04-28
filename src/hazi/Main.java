package hazi;

import java.io.IOException;

public class Main {

	
	public static void main(String[] args) throws Exception {
		
		Server s;
		Client c;
		GUI g;
		Logic l;
		
		
		byte input[] = new byte[5];
		System.out.println("Choose server (s) /client (c)");
		System.in.read(input,0,5);
		
		switch(input[0])
		{
		case 'c': System.out.println("Creating client");
		g = new GUI();
		c = new Client(g);
		while(input[0]!='q')
		{
			System.out.println("c - connect, d - disconnect");
			System.in.read(input,0,5);
			switch(input[0])
			{
			case 'c': c.start(""); break;
			case 'd':c.stop(); break;
			}
		}
		break;
		
		case 's': System.out.println("Creating server");
		l = new Logic();
		s = new Server(l);
		while(input[0]!='q')
		{
			System.out.println("c - connect, d - disconnect");
			System.in.read(input,0,5);
			switch(input[0])
			{
			case 'c': s.start(""); break;
			case 'd':s.stop(); break;
			}
		}
		
		break;
		}

	}

}
