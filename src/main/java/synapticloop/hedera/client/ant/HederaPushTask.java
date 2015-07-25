package synapticloop.hedera.client.ant;


import java.util.ArrayList;

import org.apache.tools.ant.BuildException;

import synapticloop.hedera.client.bean.Repository;
import synapticloop.hedera.client.exception.HederaException;

public class HederaPushTask extends HederaBaseTask {
	private String file = null;
	private String name = null;
	private String version = null;
	private String type = null;

	public void execute() throws BuildException {
		super.execute();

		// get the master repositories
		ArrayList<Repository> masterRepositories = hedera.getMasterRepositories();
		if(masterRepositories.size() == 0) {
			throw new BuildException("Could not find any master repositories.  Exiting...");
		}

		try {
			hedera.executePush(masterRepositories, file, getRepositoryPath());
		} catch (HederaException hex) {
			throw new BuildException(hex.getMessage());
		}
	}

	public String getRepositoryPath() {
		StringBuilder repositoryPathBuilder = new StringBuilder();
		StringBuilder artifactNameBuilder = new StringBuilder();
		if(null != name) { 
			repositoryPathBuilder.append(name.replaceAll("-", "/") + "/");
			artifactNameBuilder.append(name);
		}
		if(null != version) { 
			artifactNameBuilder.append("-");
			artifactNameBuilder.append(version);
		}
		if(null != type) { 
			repositoryPathBuilder.append(type + "/");
			artifactNameBuilder.append(".");
			artifactNameBuilder.append(type);
		}

		return(repositoryPathBuilder.toString() + artifactNameBuilder.toString());
	}

	public void setHederaFile(String hederaFile) { this.hederaFile = hederaFile; }
	public void setFile(String file) { this.file = file; }
	public void setName(String name) { this.name = name; }
	public void setVersion(String version) { this.version = version; }
	public void setType(String type) { this.type = type; }
}
