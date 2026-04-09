package course;

import static common.Log.COMM;
import static common.Log.GEN;

import java.io.IOException;

import org.apache.logging.log4j.Level;

import common.Log;

public class MainReseau {
	/**
	 * number of expected arguments of main method.
	 */
	private static final int NB_ARGS = 2;

	/**
	 * position of the port number in args[].
	 */
	private static  final int PORT_ARG = 0;

	/**
	 * position of the hostname in args[].
	 */
	private static  final int NB_CASES_ARG = 1;
	
	
	/**
	 * constructeur privé pour rendre la contruction de cette classe impossible.
	 */
	private MainReseau() {
		
	}

	
	public static void main(String[] args) throws Exception {
		if (args.length != NB_ARGS) {
			System.out.println("usage: serveur port nb_cases");
			return;
		}
		int port = Integer.parseInt(args[PORT_ARG]);
		int nb_cases =  Integer.parseInt(args[NB_CASES_ARG]);
		
		Log.setLevel(COMM, Level.TRACE); 
		Log.setLevel(GEN, Level.TRACE); 
		
		CourseReseau course = new CourseReseau(nb_cases, port);
		course.jouer();
		
	}
}
