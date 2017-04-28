package hazi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;


public class Server extends Network implements IGameState{
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private boolean exit_flag;
	private ObjectOutputStream out = null;
	private ReentrantLock lock = null;
	
	private ObjectInputStream in = null;
	
	private ICommand commandInterface;
	private ListenerWorker worker;
	private Thread thread;
	
	Server(ICommand ci)
	{
		worker = new ListenerWorker();
		commandInterface = ci;
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
				if (clientSocket != null){
					clientSocket.close();
					clientSocket = null;
				}
			} catch (IOException ex){
				System.err.println("Error while closing socket.");
			}
			lock.unlock();
	}
	
	@Override
	void start(String ip) {
		stop();
		exit_flag=false;
		thread = new Thread(worker);
		thread.start();
	}
	
	@Override
	void stop() {
		lock.lock();
		exit_flag=true;
		try{
			if(serverSocket != null){
				serverSocket.close();
				serverSocket = null;
			}
		} catch (Exception ex){
			System.out.println("Cannot close server socket");
		}
		try{
			if(clientSocket != null){
				clientSocket.close();
				clientSocket = null;
			}
		} catch (Exception ex) {
			System.out.println("Cannot close client socket");
		}
		lock.unlock();
		
		
		try {
			if(thread!=null)
				thread.join();
		} catch (InterruptedException e) {
			System.out.println("Cannot stop the worker thread.");
		}
	}
	
        private class ListenerWorker implements Runnable {
		public void run() {
			// Create server socket
			ServerSocket ss = null;
			while(ss==null)
				try{
					ss =  new ServerSocket(10007);
				}catch(IOException ex){
					System.out.println("Failed to create server socket");
				}
			lock.lock();
			serverSocket = ss;
			lock.unlock();

			// Start listening
			while(true)
			{
				boolean f = true;
				Socket cs=null;
				ObjectOutputStream os=null;
				// Waiting for clients
				while(cs==null)
				{

					lock.lock();
					f = exit_flag;
					lock.unlock();
					if(f==true) return;
					try{
						cs = serverSocket.accept();
						try{
							os = new ObjectOutputStream(cs.getOutputStream());
							in = new ObjectInputStream(cs.getInputStream());
							os.flush();
						} catch(IOException ex){
							cs.close();
							cs = null;
						}
					} catch (IOException ex) {
					}
				}
				// share objects
				lock.lock();
				try{
					f = exit_flag;
					if(f){
						os.close();
						in.close();
						cs.close();
					} else {
						clientSocket = cs;
						out = os;
					}
				}catch (IOException ex){
					System.out.println("Close error");
				} finally{
				lock.unlock();
				}
				if(f) return;
				
				// CONNECTION ESTABLISHED
				System.out.println("Client connected.");

				// TODO send comman for the commandInterface
				Command com = new Command(1);
				commandInterface.onNewCommand(com);
				com = null;
				// COMMUNICATING	
				try {
					while (true) {
						Command c = (Command) in.readObject();
						commandInterface.onNewCommand(c);
					}
				} catch (Exception ex) {
					System.out.println("Disconnected");
				} finally {
					cleanup();
					// TODO create game state to signal to he gui
					Command c = new Command(-1);
					commandInterface.onNewCommand(c);
				}
			} // while
		}//run
	}// worker
	
	public void onNewGameState(GameState gs)
	{
		lock.lock();
		try {
			if(out != null)
			{
				out.writeObject(gs);
				out.flush();
			}
		} catch (IOException ex) {
			System.err.println("Send error.");
		} finally {
			lock.unlock();
		}
	}





}
