package synapticloop.hedera.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) {
		System.out.println("Output of herea-build.cml file: ");
		InputStream inputStream = Main.class.getResourceAsStream("/hedera-build.xml");
		BufferedReader bufferedReader = null;
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		try {
			while((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			if(null != bufferedReader) {
				try {
					bufferedReader.close();
				} catch (IOException ignored) {
				}
			}
		}
		
	}

}
