package hazi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;


public class Client extends Network implements ICommand{
	
	private Socket socket = null;
	private boolean exit_flag = false;
	private ObjectOutputStream out = null;
	private ReentrantLock lock = null;
	
	private ObjectInputStream in = null;
	
	private String ip;
	private IGameState gsInterface;

	private ListenerWorker worker;
	private Thread thread;
	
	Client(IGameState g)
	{
		gsInterface = g;
		worker = new ListenerWorker();
		lock = new ReentrantLock();
	}

	private void cleanup()
	{
		lock.lock();
		try{
			if (out != null){
				out.close();
				out = null;
			}
		} catch (IOException ex){
			System.err.println("Error while closing out.");
		}
		try{
			if (in != null){
				in.close();
				in = null;
			}
		} catch (IOException ex){
			System.err.println("Error while closing in.");
		}
		try{
			if (socket != null){
				socket.close();
				socket = null;
			}
		} catch (IOException ex){
			System.err.println("Error while closing socket.");
		}
		lock.unlock();
	}
	public void start(String ip)
	{
		stop();
		exit_flag=false;
		thread = new Thread(worker);
		thread.start();
	}
	public void stop()
	{
		lock.lock();
		try{
			exit_flag = true;
			if(socket != null)
				socket.close();
		} catch(IOException ex){
			System.out.println("Cannot close socket");
		}finally{
			lock.unlock();
		}
		try{
		if(thread!=null)
		{
			thread.join();
			thread=null;
		}
		} catch(InterruptedException ie){
			System.out.println("Join interrupted");
		}

	}
	
	private class ListenerWorker implements Runnable {
		public void run() {
			while(true){
				boolean f= true;
				Socket s = null;
				ObjectOutputStream o=null;
				// Connecting
				try {
					thread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(s == null)
				{
					try{
						lock.lock();
						f = exit_flag;
						lock.unlock();
						if(f) return;
						
						s = new Socket(ip,10007);
						try{
							o = new ObjectOutputStream(s.getOutputStream());
							o.flush();
							in = new ObjectInputStream(s.getInputStream());
						} catch(IOException ex){
							s.close();
							s = null;
						}
					} catch (UnknownHostException he){
						System.out.println("Cannot reach host");
					} catch (IOException ie) {
						System.out.println("Connection error");
					}
				}
				// share objects
				lock.lock();
				try{
					f = exit_flag;
					if(f){
						o.close();
						in.close();
						s.close();
					} else {
						socket = s;
						out = o;
					}
				}catch (IOException ex){
					System.out.println("Close error");
				} finally{
				lock.unlock();
				}
				if(f) return;
	
				// CONNECTION ESTABLISHED
				System.out.println("Connected to server.");
				// TODO send comman for the logic
				GameState g = new GameState("Connected");
				gsInterface.onNewGameState(g);
					
				try {
					while (true) {
						GameState gs = (GameState) in.readObject();
						gsInterface.onNewGameState(gs);
					}
				} catch (Exception ex) {
					System.out.println("Disconnected");
				} finally {
					cleanup();
					// TODO create game state to signal to he gsInterface
					GameState gs = new GameState("Connection lost");
					gsInterface.onNewGameState(gs);
				}
			}// while
		} // run
	} //worker
	
	public void onNewCommand(Command c)
	{
		lock.lock();
		try {
			if(out != null){
				out.writeObject(c);
				out.flush();
			}
		} catch (IOException ex) {
			System.out.println("Send error");
		} finally {
			lock.unlock();
		}
		// if command == close, stop client
	}

}
