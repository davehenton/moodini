/*
 * Moodini
 * Copyright (C) 2016 Marcus Fihlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fihlon.moodini.server;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pl.setblack.airomem.core.SimpleController;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

/**
 * This utility class is responsible for creating the controllers to connect to
 * the persistence store.
 */
@Slf4j
@UtilityClass
public class PersistenceManager {

    private static final String MOODINI_DIRECTORY_NAME = ".moodini";
    private static final String PREVAYLER_DIRECTORY_NAME = "prevayler";

    private static final Path DATA_DIRECTORY;

    static {
        DATA_DIRECTORY = Paths.get(MOODINI_DIRECTORY_NAME, PREVAYLER_DIRECTORY_NAME);
    }

    /**
     * Create a {@link SimpleController} for the specified entity using the specified repository constructor.
     *
     * @param clazz the entity class
     * @param constructor the constructor of the repository class
     * @param <T> the type of the entity, must extend {@link Serializable}
     * @return a {@link SimpleController} for the entity repository
     */
    public static <T extends Serializable> SimpleController<T> createSimpleController(
            final Class<? extends Serializable> clazz, final Supplier<T> constructor) {
        final String dir = DATA_DIRECTORY.resolve(clazz.getName()).toString();
        log.info("Using persistence store '{}' for entity '{}'.",
                Paths.get(System.getProperty("user.home"), dir).toAbsolutePath(),
                clazz.getName());
        return SimpleController.loadOptional(dir, constructor);
    }

}
