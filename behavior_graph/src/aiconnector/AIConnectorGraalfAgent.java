package aiconnector;

import controlClasses.Configurations;
import java.net.*;

import org.junit.experimental.theories.Theories;

import java.io.*;

public class AIConnectorGraalfAgent implements Runnable {

	public AIConnectorGraalfAgent() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
//		TODO : implement the sending procedure
		String ip = Configurations.getInstance().getSetting(Configurations.AI_CONNECTOR_IP);
		int port = Integer.parseInt(Configurations.getInstance().getSetting(Configurations.AI_CONNECTOR_PORT));
		System.out.printf("conecting to : %s - %d\n", ip, port);
		try {
			Socket the_socket = new Socket(ip, port);

			PrintWriter the_printer = new PrintWriter(the_socket.getOutputStream(), true);
			Thread.sleep(12000);
			String last_str = "";
			while (true) {
				String str_to_Send = AIConnectorMemory.getInstance().getObservationString();
				if (str_to_Send.equals(last_str))
					continue;

				the_printer.println(str_to_Send);
				last_str = str_to_Send;
				Thread.sleep(2000);
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();

		}

	}

}
