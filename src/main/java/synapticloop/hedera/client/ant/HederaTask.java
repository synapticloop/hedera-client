package synapticloop.hedera.client.ant;


import org.apache.tools.ant.BuildException;

import synapticloop.hedera.client.exception.HederaException;

public class HederaTask extends HederaBaseTask {

	public void execute() throws BuildException {
		super.execute();

		System.out.println(hedera);

		try {
			hedera.execute();
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}

	}

}
