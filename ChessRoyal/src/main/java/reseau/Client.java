package main.java.reseau;

import static main.java.common.Log.COMM;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import main.java.messages.Message;
import main.java.messages.NouveauJoueur;
import main.java.messages.ProtocoleMessageType;


/**
 * Classe représent un client du jeu connecté au serveur (et donc un joueur).
 * 
 * Permet l'envoi et la réception de messages sérialisés.
 */
public class Client implements AutoCloseable {
	/**
	 * Nom de la machine par défaut, lors de l'usage du constructeur
	 * sans paramètre.
	 */
	public static final String HOST_PAR_DEFAUT = "localhost";
	/**
	 * numéro du port par défaut, lors de l'usage du constructeur
	 * sans paramètre.
	 */
	public static final int PORT_PAR_DEFAUT = 4545;
	
	/**
	 * worker gérant l'échange TCP d'objets sérialisés.
	 */
	private FullDuplexMsgWorker worker;
	

	
	/**
	 * Constructeur qui établit une connexion synchrone avec le
	 * serveur hébergé sur la machine host sur le port portNum
	 * La connexion est un appel bloquant. Il faut donc placer ce
	 * constructeur dans un service lors de son usage dans
	 * l'interface graphiqué géré par JavaFX.
	 * @param host
	 *         hostname de la machine hébergeant le serveur.
	 * @param portNum
	 *         port du serveur.
	 * @throws IOException
	 *         toutes les exceptions qui peuvent se produire lors de la connexion.
	 */
	public Client(final String host, final int portNum) throws IOException {
		COMM.trace("Client::Client: " + host + ":" + portNum);
		InetSocketAddress serveurAdresse = new InetSocketAddress(host, portNum);
		SocketChannel socketChannel = SocketChannel.open(serveurAdresse);
		worker = new FullDuplexMsgWorker(socketChannel);
	}
	
	/**
	 * * Constructeur qui établit une connexion synchrone avec le
	 * serveur hébergé sur la machine {@link Client#HOST_PAR_DEFAUT}
	 * sur le port {@link Client#PORT_PAR_DEFAUT}.
	 * @throws IOException
	 *         toutes les exceptions qui peuvent se produire lors de la connexion.
	 */
	public Client() throws IOException {
		this(HOST_PAR_DEFAUT, PORT_PAR_DEFAUT);
	}

	/**
	 * Lecture complète d'un message arrivant du réseau.
	 * Cette méthode est bloquante. Il faut donc la placer dans
	 * un service lors de son usage dans l'interface graphique
	 * géré par JavaFX.
	 * @return
	 *       null si le distant s'est déconnecté avant la réception
	 *       complète, le message reçu sinon.  
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       la réception et désérialisation du message.
	 */
	public Message lireMessage() throws IOException {
		ReadMessageStatus status;
		do {
			status = worker.readMessage();
		} while ((status != ReadMessageStatus.ReadDataCompleted) 
				&& (status != ReadMessageStatus.ChannelClosed));
		if (status == ReadMessageStatus.ChannelClosed) {
			return null;
		}
		return (Message) worker.getData();

	}

	/**
	 * Envoi d'un message sur le réseau.
	 * @param message
	 *       Le message à envoyer.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerMessage(final Message message) throws IOException {
		worker.sendMsg(message);
	}

	
	@Override
	public void close() throws Exception {
		COMM.trace("Client::close: fermeture de la connexion.");
		worker.close();
	}

	
	//################### Début de la section gérant la partie 
	//################### spécifique du protocole de cette application.


	/**
	 * Envoi des informations (le pseudonyme) concernant le nouveau joueur connecté.
	 * @param pseudo
	 *           le pseudonyme du nouveau joueur.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.
	 */
	public void envoyerInfoNouveauJoueur(final String pseudo) throws IOException  {
		COMM.trace("Client::envoyerInfoNouveauJoueur(" + pseudo + ")");	
		NouveauJoueur nouveauJoueur = new NouveauJoueur(pseudo);
		Message message = new Message(nouveauJoueur, ProtocoleMessageType.INFO_NOUVEAU_JOUEUR);
		envoyerMessage(message);
	}

	/**
	 * Envoi du message protocolaire du type {@link ProtocoleMessageType#DEBUT_PARTIE}.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.		
	 */
	public void envoyerDebutDePartie() throws IOException {
		COMM.trace("Client::envoyerDebutDePartie()");		
		Message message = new Message(null, ProtocoleMessageType.DEBUT_PARTIE);
		envoyerMessage(message);
	}
	
	/**
	 * Envoi du message protocolaire du type {@link ProtocoleMessageType#DEMANDE_LANCEMENT_DES}.
	 * @throws IOException
	 *       toutes les exceptions pouvant se produire durant
	 *       l'émission du message.		
	 */
	
	/**
	 * Envoi du message protocolaire du type {@link ProtocoleMessageType#COUP_ECHECS}.
	 * @param coup le coup joué par le client.
	 * @throws IOException toutes les exceptions pouvant se produire durant l'émission du message.
	 */
	public void envoyerCoup(final messages.CoupEchecs coup) throws IOException {
		COMM.trace("Client::envoyerCoup(" + coup + ")");
		Message message = new Message(coup, ProtocoleMessageType.COUP_ECHECS);
		envoyerMessage(message);
	}



}