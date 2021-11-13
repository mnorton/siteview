package com.nolaria.sv;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A very simple logger that writes out to a file.
 * 
 * @author Mark Norton
 *
 */
public class LogHandler {
	public static String logFileName ="C:\\dev\\sv-logs\\sv.log";
    public static Logger logger = Logger.getLogger(LogHandler.class.getName());
    private static FileHandler fileHandler;
	//private Logger logger = LogManager.getLogManager().getLogger("sv");

    public LogHandler() {
    	LogHandler.createFileHandler();

		/*
		 * try { FileHandler fileHandler = new FileHandler(logFileName, true);
		 * SimpleFormatter logFormatter = new SimpleFormatter();
		 * //logFormatter.formatMessage(new LogRecord())
		 * 
		 * fileHandler.setFormatter(logFormatter);
		 * 
		 * logger.addHandler(fileHandler); if (logger.isLoggable(Level.INFO)) {
		 * logger.info("Information message"); }
		 * 
		 * } catch (IOException e) { e.printStackTrace(); }
		 */
    }
    
    public static Logger createFileHandler() {
        {
            try {
                fileHandler = new FileHandler(logFileName, true);
                
                logger.addHandler(fileHandler);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Information message");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return logger;
        }
    }
}

