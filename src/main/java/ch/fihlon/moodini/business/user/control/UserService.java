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
package ch.fihlon.moodini.business.user.control;

import ch.fihlon.moodini.AbstractLifecycleListener;
import ch.fihlon.moodini.PersistenceManager;
import ch.fihlon.moodini.business.user.entity.User;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.jetty.util.component.LifeCycle;
import pl.setblack.airomem.core.SimpleController;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Singleton
@Timed(name = "Timed: UserService")
@Metered(name = "Metered: UserService")
public class UserService {

    private SimpleController<UserRepository> controller;

    @Inject
    public UserService(@NotNull final LifecycleEnvironment lifecycleEnvironment) {
        controller = PersistenceManager.createSimpleController(User.class, UserRepository::new);
        lifecycleEnvironment.addLifeCycleListener(new AbstractLifecycleListener() {
            @Override
            public void lifeCycleStopping(@NotNull final LifeCycle event) {
                controller.close();
            }
        });
    }

    public User create(@NotNull final User user) {
        return controller.executeAndQuery((ctrl) -> ctrl.create(user));
    }

    public Optional<User> read(@NotNull final Long userId) {
        return controller.readOnly().read(userId);
    }

    public List<User> readAll() {
        return controller.readOnly().readAll();
    }

    public User update(@NotNull final User user) {
        return controller.executeAndQuery((ctrl) -> ctrl.update(user));
    }

    public void delete(@NotNull final Long userId) {
        controller.execute((ctrl) -> ctrl.delete(userId));
    }

}
