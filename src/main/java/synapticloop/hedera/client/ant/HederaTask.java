package synapticloop.hedera.client.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import synapticloop.hedera.client.bean.Hedera;
import synapticloop.hedera.client.exception.HederaException;

public class HederaTask extends Task {
	private String hederaFile = "hedera.xml";

	public void execute() throws BuildException {
		System.out.println("Hedera executing with '" + hederaFile + "'.");

		Hedera hedera;
		try {
			hedera = new Hedera(hederaFile);
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}

		System.out.println(hedera);

		try {
			hedera.execute();
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}

	}

	public void setHederaFile(String hederaFile) { this.hederaFile = hederaFile; }

}
