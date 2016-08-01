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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotNull;

/**
 * This utility class wraps the Google Guice injection to inject mocks and stubs
 * with less code.
 */
@UtilityClass
public class Injector {

    private static Module module = createModule();

    /**
     * Injects dependencies into the fields and methods of {@code instance}. Ignores the presence or absence of an
     * injectable constructor.
     *
     * @param instance any instance
     */
    @SuppressWarnings("PMD.LawOfDemeter")
    public static void injectMembers(@NotNull final Object instance) {
        Guice.createInjector(module).injectMembers(instance);
    }

    private static Module createModule() {
        return new InjectorModule();
    }

    /**
     * Set the module contributing configuration information, typically interface bindings.
     *
     * @param newModule the module contributes configuration information, typically interface bindings
     */
    public static void setModule(@NotNull final Module newModule) {
        module = newModule;
    }

    /**
     * Reset the module to the default module instance.
     */
    public static void resetModule() {
        module = createModule();
    }

    /**
     * This implementation of the injector module has no specific configuration
     * and is used at runtime. It can be replaced by unit tests to easy inject
     * mocks and stubs into the testee.
     */
    private static class InjectorModule extends AbstractModule {

        /**
         * This is a default implementation providing no additional configuration.
         */
        @Override
        protected void configure() {
            /* default */
        }
    }

}
