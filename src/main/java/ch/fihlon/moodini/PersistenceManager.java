package ch.fihlon.moodini;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.setblack.airomem.core.SimpleController;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

@UtilityClass
public class PersistenceManager {

    private static final String MOODINI_DIRECTORY_NAME = ".moodini";
    private static final String PREVAYLER_DIRECTORY_NAME = "prevayler";

    private static final Path DATA_DIRECTORY;

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManager.class);

    static {
        DATA_DIRECTORY = Paths.get(MOODINI_DIRECTORY_NAME, PREVAYLER_DIRECTORY_NAME);
    }

    public static <T extends Serializable> SimpleController<T> createSimpleController(
            final Class<? extends Serializable> clazz, final Supplier<T> constructor) {
        final String dir = DATA_DIRECTORY.resolve(clazz.getName()).toString();
        LOGGER.info("Using persistence store '{}' for entity '{}'.",
                Paths.get(System.getProperty("user.home"), dir).toAbsolutePath(),
                clazz.getName());
        return SimpleController.loadOptional(dir, constructor);
    }

}
