package synapticloop.hedera.client.util;

public class SimpleLogger {
	public enum LoggerType {
		HEDERA,
		SCOPE,
		ARTIFACT,
		REPOSITORY,
		TOKEN,
		ARTEFACT_DOWNLOAD,
		ARTEFACT_URL
	}

	private static int maxLength = 0;
	static {
		LoggerType[] values = LoggerType.values();
		for (LoggerType loggerType : values) {
			int length = loggerType.name().length();
			if(length > maxLength) {
				maxLength = length;
			}
		}
	}

	public static final String INFO = " INFO";
	public static final String WARN = " WARN";
	public static final String ERROR = "ERROR";
	public static final String FATAL = "FATAL";

	private SimpleLogger() {}

	public static void logInfo(LoggerType loggerType, String message) { log(INFO, loggerType, message); }
	@SuppressWarnings("rawtypes")
	public static void logInfo(LoggerType loggerType, Class clazz, String message) { log(INFO, loggerType, clazz, message); }

	public static void logWarn(LoggerType loggerType, String message) { log(WARN, loggerType, message); }
	@SuppressWarnings("rawtypes")
	public static void logWarn(LoggerType loggerType, Class clazz, String message) { log(WARN, loggerType, clazz, message); }
	
	public static void logError(LoggerType loggerType, String message) { log(ERROR, loggerType, message); }
	@SuppressWarnings("rawtypes")
	public static void logError(LoggerType loggerType, Class clazz, String message) { log(ERROR, loggerType, clazz, message); }
	
	public static void logFatal(LoggerType loggerType, String message) { log(FATAL, loggerType, message); }
	@SuppressWarnings("rawtypes")
	public static void logFatal(LoggerType loggerType, Class clazz, String message) { log(FATAL, loggerType, clazz, message); }

	private static void log(String type, LoggerType loggerType, String message) {
		System.out.println(String.format("[ %" + maxLength + "s ] [ %s ] %s", loggerType.name(), type, message));
	}
	@SuppressWarnings("rawtypes")
	private static void log(String type, LoggerType loggerType, Class clazz, String message) {
		System.out.println(String.format("[ %" + maxLength + "s ] [ %s ] [ %s ] %s", loggerType.name(), type, clazz.getSimpleName(), message));
	}

}
