package synapticloop.hedera.client.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import synapticloop.hedera.client.util.HederaUtils;
import synapticloop.hedera.client.util.SimpleLogger;
import synapticloop.hedera.client.util.SimpleLogger.LoggerType;

public class Repository {
	private String url = null;
	private String name = null;
	private List<String> tokens = new ArrayList<String>();
	private Set<String> tokenSet = new HashSet<String>();

	private static final Set<String> ALLOWABLE_TOKENS = new HashSet<String>();
	static {
		ALLOWABLE_TOKENS.add("group");
		ALLOWABLE_TOKENS.add("name");
		ALLOWABLE_TOKENS.add("version");
		ALLOWABLE_TOKENS.add("type");
	}

	public Repository(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		this.url = HederaUtils.getNodeValue(attributes, "url");

		this.name = HederaUtils.getNodeValue(attributes, "name");

		// now get the tokens - if they exist
		if(null == url || null == name) {
			SimpleLogger.logFatal(LoggerType.REPOSITORY, "Repository node __MUST__ have both 'url' and 'name' attributes: " + this.toString());
		}

		tokens = HederaUtils.tokenize(url,"{","}");
		tokenSet.addAll(tokens);
		tokens.clear();
		tokens.addAll(tokenSet);


		if(!tokens.isEmpty()) {
			SimpleLogger.logInfo(LoggerType.REPOSITORY, "Repository '" + name + "' is tokenised:");
			for (String token : tokens) {
				if(ALLOWABLE_TOKENS.contains(token)) {
					SimpleLogger.logInfo(LoggerType.REPOSITORY, "    [   VALID ] '" + token + "'.");
				} else {
					SimpleLogger.logInfo(LoggerType.REPOSITORY, "    [ INVALID ] '" + token + "'.");
				}
			}
		}
	}

	public String getUrl() { return url; }

	/**
	 * Clear all of the allowable tokens for a repository
	 */
	public static void clearTokens() {
		ALLOWABLE_TOKENS.clear();
	}

	/**
	 * Add a token to the allowable tokens map
	 * 
	 * @param token the token to be added
	 */
	public static void addToken(String token) {
		SimpleLogger.logInfo(LoggerType.TOKEN, "Adding in repository token of '" + token + "'.");
		ALLOWABLE_TOKENS.add(token);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"url\":\"" + url + "\", \"name\":\"" + name + "\" }");
		return (stringBuilder.toString());
	}
}
