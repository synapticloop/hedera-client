package synapticloop.hedera.client.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import synapticloop.hedera.client.bean.Hedera;
import synapticloop.hedera.client.exception.HederaException;

public abstract class HederaBaseTask extends Task {
	protected String hederaFile = "hedera";
	protected Hedera hedera = null;
	
	public void execute() throws BuildException {
		System.out.println("Hedera executing with '" + hederaFile + "'.");

		try {
			hedera = new Hedera(hederaFile);
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}
	}

	public void setHederaFile(String hederaFile) { this.hederaFile = hederaFile; }
}
