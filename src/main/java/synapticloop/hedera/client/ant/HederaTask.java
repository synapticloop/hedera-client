package synapticloop.hedera.client.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import synapticloop.hedera.client.bean.Hedera;
import synapticloop.hedera.client.exception.HederaException;
import synapticloop.hedera.client.util.SimpleLogger;
import synapticloop.hedera.client.util.SimpleLogger.LoggerType;

public class HederaTask extends Task {
	protected String hederaFile = "hedera.xml";
	protected Hedera hedera = null;

	public void execute() throws BuildException {
		SimpleLogger.logInfo(LoggerType.HEDERA, "Hedera executing with '" + hederaFile + "'.");

		try {
			hedera = new Hedera(hederaFile);
			hedera.execute();
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}

	}


	public void setHederaFile(String hederaFile) { this.hederaFile = hederaFile; }

}
