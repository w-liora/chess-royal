package main.java.reseau;



import static main.java.common.Log.COMM;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import main.java.messages.Message;
import main.java.messages.MessageSigne;
import main.java.messages.ProtocoleMessageType;

/**
 * Classe représentant le serveur du jeu, utilisant JavaNIO pour gérer les connexions clients de manière asynchrone
 * 
 * Le serveur 
 *      écoute sur le port passé en argument du constructeur, 
 *      accepte les connexions entrantes,
 *      gère les échanges de messages avec chaque client connecté
 */
public class Serveur implements AutoCloseable {
	/**
	 * selector pour gérer en asynchrone tous les clients connectés.
	 */
	private Selector selector;
	/**
	 * Canal serveur pour accepter les connexions des clients (géré en asynchrone).
	 */
	private ServerSocketChannel serverSocketChannel;
	
	/**
	 * Clef enregistrée par le selector pour le serveurChannel.
	 */
	private SelectionKey serveurChannelKey;;
	/**
	 * Table d'association pour retrouver le worker en fonction de la
	 * clef retournée par le selector lors de l'arrivée d'un message 
	 * (ou de la deconnexion) d'un client.
	 */
	private Map<SelectionKey, FullDuplexMsgWorker> workersFromKey;
	

	/**
	 * Constructeur qui ouvre le serveur en mode asynchrone et initialise
	 * le selector pour la future gestion des flux entrants; ce constructeur
	 * initialise aussi la tables d'association workersFromKey
	 * qui permettra de retrouver le Worker associé à chaque client connecté.
	 * @throws IOException
	 *          toutes les exceptions qui peuvent se produire lors
	 *          de l'ouverture du selector ou du canal de connexion.
	 */
	public Serveur(int port) throws IOException {
		COMM.trace("Serveur::Serveur(" + port + ")");
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		serveurChannelKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		workersFromKey = new HashMap<>();
	}


	/**
	 * Retourne le premiere message, reçu par le serveur.
	 * Ce message peut être:
	 *   - le message de détection de la deconnexion d'un client;
	 *   - un message reçu d'un client enrichi de la signature de 
	 *     ce client pour authentifier sa provenance.
	 * Cette méthode continue sans return lors d'une connexion.
	 * La connexion est gérée, et les structures mise à jour,
	 * mais elle n'est pas signalée à la classe appelant.
	 * Il faudra attendre un premier message du client connecté
	 * pour qu'elle soit au courant de cette connexion.
	 *   
	 * @return
	 *    Un message signé contenant le message reçu du
	 *    client, et la clef authentifiant le client
	 *    émetteur.
	 * @throws IOException
	 *    Toutes les exceptions d'entrées-sorties qui peuvent se 
	 *    produire lors de cette réception.
	 */
	public MessageSigne lireMessage() throws IOException {
		FullDuplexMsgWorker worker;
		SocketChannel rwChan;

		while (true) {
			int nbClefs = selector.select();
			
			if (nbClefs == 0) {
				// gestion d'éventuels selector.wakeup(), et d'interuptions
				// dans le ou ce serveur serait placé dans une application
				// multi-thread.
				continue;
			}
			
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			
			// gestion de clefs par Iterator, pour pouvoir faire
			// de remove() au fur et et mesure.
			// Dés qu'un message est complet le méthode fait un return
			// alors que certaines clefs n'ont pas été gérées. Il
			// faut donc pouvoir faire des remove() au fil de l'eau.
			Iterator<SelectionKey> keyIterator = readyKeys.iterator();
			
			while (keyIterator.hasNext()) {
		        SelectionKey curKey = keyIterator.next();
		        keyIterator.remove();
		        
				if (curKey.isAcceptable()) {
					// connexion d'un nouveau joueur.
					SelectionKey newKey;
					rwChan = serverSocketChannel.accept();
					
					if (rwChan != null) {
						rwChan.configureBlocking(false);

						newKey = rwChan.register(selector, SelectionKey.OP_READ);
						worker = new FullDuplexMsgWorker(rwChan);
						workersFromKey.put(newKey, worker); // ajouts de cette nouvelle connexion à la Map
						// repère connexion
					}
				} else { // cas des keys readable
					worker = workersFromKey.get(curKey);
					ReadMessageStatus status;
					status = worker.readMessage();
					if (status == ReadMessageStatus.ChannelClosed) { // closed channel
						// déconnexion d'un joueur
						COMM.info("Server: closed channel detected");

						workersFromKey.remove(curKey);
						curKey.cancel();
						worker.close();
		
						Message message = new Message(null, ProtocoleMessageType.DECONNEXION);
						MessageSigne messageSigne = new MessageSigne(message, curKey);
						return messageSigne;
					}
					if (status == ReadMessageStatus.ReadDataCompleted) {
						// réception d'une réponse d'un client.
						COMM.info("Server: reponse reçue du client");
						Message message = (Message) worker.getData();
						COMM.info("Serveur::getMessage: type du message reçu: " + message.getProtocoleMessage());
						
						MessageSigne messageSigne = new MessageSigne(message, curKey);
						return messageSigne;
					}
				}
			}
		}
	}	
	
	/**
	 * Fermeture du ServerSocketChannel pour interdire toute nouvelle connexion.
	 * @throws IOException 
	 *     Toutes les exceptions pouvant se produire lors de la clôture 
	 *     du canal.
	 *        
	 */
	public void cloreServerSocketChannel() throws IOException {
		// arrêt de la surveillance par le selector.
		this.serveurChannelKey.cancel();
		
		// fermeture un canal.
		serverSocketChannel.close();
		
	}

	/**
	 * Envoi d'un message au client identifié par key.
	 * @param key
	 *        SelectionKey associé au client destinataire du message.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message, ou suite à l'usage d'une
	 *       clef (paramètre key) ne correspondant à aucun client connecté.
	 */
	public void envoyerMessage(SelectionKey key, Message message) throws IOException {
		COMM.info("Serveur::envoyerMessage: Envoi du message " + message + " à " + key);
		FullDuplexMsgWorker worker = workersFromKey.get(key);
		if (worker == null) {
			throw new IOException("Le client identifié par " + key + "n'existe pas ou n'est plus connecté");
		}
		worker.sendMsg(message);
	}
	
	/**
	 * Envoi un message à tous les clients connectés.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerATous(Message message) throws IOException {
		COMM.info("Serveur::envoyerATous(" + message + ")");
		for (SelectionKey key: workersFromKey.keySet()) {
			envoyerMessage(key, message);
		}
	}


	@Override
	public void close() throws Exception {
		cloreServerSocketChannel();
	}
}
