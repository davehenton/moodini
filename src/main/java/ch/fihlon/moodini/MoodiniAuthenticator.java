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

import ch.fihlon.moodini.business.user.control.UserService;
import ch.fihlon.moodini.business.user.entity.User;
import com.github.toastshaman.dropwizard.auth.jwt.JsonWebTokenValidator;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.github.toastshaman.dropwizard.auth.jwt.validator.ExpiryValidator;
import io.dropwizard.auth.Authenticator;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class MoodiniAuthenticator implements Authenticator<JsonWebToken, User> {

    private final UserService userService;

    @Inject
    public MoodiniAuthenticator(@NotNull final UserService userService) {
        this.userService = userService;
    }

    @Override
    @SuppressWarnings({"Guava", "OptionalGetWithoutIsPresent"})
    public com.google.common.base.Optional<User> authenticate(@NotNull final JsonWebToken token) {
        final JsonWebTokenValidator expiryValidator = new ExpiryValidator();

        // Provide your own implementation to lookup users based on the principal attribute in the
        // JWT Token. E.g.: lookup users from a database etc.
        // This method will be called once the token's signature has been verified

        // In case you want to verify different parts of the token you can do that here.
        // E.g.: Verifying that the provided token has not expired.

        // All JsonWebTokenExceptions will result in a 401 Unauthorized response.

        expiryValidator.validate(token);

        final String subject = token.claim().subject();
        final Long userId = Long.parseLong(subject);
        final Optional<User> userOptional = userService.readById(userId);

        return com.google.common.base.Optional.fromNullable(userOptional.get());
    }

}
