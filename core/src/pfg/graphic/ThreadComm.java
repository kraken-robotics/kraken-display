/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */

package pfg.graphic;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import pfg.config.Config;
import pfg.log.Log;

/**
 * Thread du serveur d'affichage
 * 
 * @author pf
 *
 */

public class ThreadComm extends Thread
{

	/**
	 * Thread qui envoie les données au socket donné en paramètre du
	 * constructeur
	 * 
	 * @author pf
	 *
	 */
	private class ThreadSocket implements Runnable
	{
		protected Log log;
		private PrintBuffer buffer;
		private Socket socket;
		private int nb;

		public ThreadSocket(Log log, PrintBuffer buffer, Socket socket, int nb)
		{
			this.log = log;
			this.buffer = buffer;
			this.socket = socket;
			this.nb = nb;
		}

		@Override
		public void run()
		{
			Thread.currentThread().setName(getClass().getSimpleName() + "-" + nb);
//			log.write("Connexion d'un client au serveur d'affichage", Subject.DUMMY);
			try
			{
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				while(true)
				{
					buffer.send(out);
					Thread.sleep(200); // on met à jour toutes les 200ms
				}
			}
			catch(InterruptedException | IOException e)
			{
//				log.write("Arrêt de " + Thread.currentThread().getName(), Subject.DUMMY);
				Thread.currentThread().interrupt();
			}
		}

	}

	protected Log log;
	private PrintBuffer buffer;
	private int port;
	private ServerSocket ssocket = null;
	private List<Thread> threads = new ArrayList<Thread>();

	public ThreadComm(Log log, PrintBuffer buffer, Config config)
	{
		this.log = log;
		this.buffer = buffer;
		port = config.getInt(ConfigInfoGraphic.GRAPHIC_SERVER_PORT_NUMBER);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
//		log.write("Démarrage de " + Thread.currentThread().getName(), Subject.DUMMY);
		try
		{
			ssocket = new ServerSocket(port);
			int nbConnexions = 0;

			while(true)
			{
				try
				{
					Thread t = new Thread(new ThreadSocket(log, buffer, ssocket.accept(), nbConnexions++));
					t.start();
					threads.add(t);
				}
				catch(SocketTimeoutException e)
				{}
			}
		}
		catch(IOException e)
		{
			/*
			 * On arrête tous les threads de socket en cours
			 */
			for(Thread t : threads)
				t.interrupt();

			if(ssocket != null && !ssocket.isClosed())
				try
				{
					ssocket.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
					e1.printStackTrace(log.getPrintWriter());
				}

//			log.write("Arrêt de " + Thread.currentThread().getName(), Subject.DUMMY);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Surcharge d'interrupt car accept() y est insensible
	 */
	@Override
	public void interrupt()
	{
		try
		{
			if(ssocket != null && !ssocket.isClosed())
				ssocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		super.interrupt();
	}

}
