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
package ch.fihlon.moodini;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotNull;

@UtilityClass
public class Injector {

    private static Module module = createModule();

    public static void injectMembers(@NotNull final Object instance) {
        Guice.createInjector(module).injectMembers(instance);
    }

    private static Module createModule() {
        return new InjectorModule();
    }

    public static void setModule(@NotNull final Module newModule) {
        module = newModule;
    }

    public static void resetModule() {
        module = createModule();
    }

    private static class InjectorModule extends AbstractModule {
        @Override
        protected void configure() {

        }
    }

}
