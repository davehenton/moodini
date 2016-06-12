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

import ch.fihlon.moodini.business.user.entity.User;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

class UserRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    private final AtomicLong userSeq = new AtomicLong(0);

    User create(@NotNull final User user) {
        final Long userId = userSeq.incrementAndGet();
        final Long version = (long) user.hashCode();
        final User userToCreate = user.toBuilder()
                .userId(userId)
                .version(version)
                .build();
        users.put(userId, userToCreate);
        return userToCreate;
    }

    List<User> readAll() {
        return users.values().stream()
                .sorted(comparingLong(User::getUserId))
                .collect(toList());
    }

    Optional<User> read(@NotNull final Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    User update(@NotNull final User user) {
        final User previousUser = read(user.getUserId()).orElseThrow(NotFoundException::new);
        if (!previousUser.getVersion().equals(user.getVersion())) {
            throw new ConcurrentModificationException("You tried to update a user that was modified concurrently!");
        }
        final Long version = (long) user.hashCode();
        final User userToUpdate = user.toBuilder()
                .version(version)
                .build();
        users.put(userToUpdate.getUserId(), userToUpdate);
        return userToUpdate;
    }

    void delete(@NotNull final Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException();
        }
        // TODO delete only users without questions
        users.remove(userId);
    }
}
