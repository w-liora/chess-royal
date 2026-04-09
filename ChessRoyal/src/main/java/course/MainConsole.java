package course;

import static common.Log.COMM;
import static common.Log.GEN;

import java.io.IOException;

import org.apache.logging.log4j.Level;

import common.Log;

public class MainConsole {
	/**
	 * number of expected arguments of main method.
	 */
	private static final int NB_ARGS = 1;


	/**
	 * position of the hostname in args[].
	 */
	private static  final int NB_CASES_ARG = 0;
	
	
	/**
	 * constructeur privé pour rendre la contruction de cette classe impossible.
	 */
	private MainConsole() {
		
	}

	
	public static void main(String[] args) throws Exception {
		if (args.length != NB_ARGS) {
			System.out.println("usage: serveur nb_cases");
			return;
		}
		int nb_cases =  Integer.parseInt(args[NB_CASES_ARG]);
		
		Log.setLevel(COMM, Level.WARN); 
		Log.setLevel(GEN, Level.WARN); 
		
		CourseConsole course = new CourseConsole(nb_cases);
		course.jouer();
		
	}
}
