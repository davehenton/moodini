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
package ch.fihlon.moodini.server.exception;

import javax.validation.constraints.NotNull;

/**
 * This is the base class of all custom exceptions used in this project and
 * allows the custom exception to specify a HTTP status code which is used by
 * the Vert.x error handler to create a more meaningful HTTP error response.
 */
public abstract class AbstractStatusCodeException extends RuntimeException {

    private static final int DEF_STATUS_CODE = 500;

    private Integer statusCode = DEF_STATUS_CODE;

    AbstractStatusCodeException(@NotNull final Integer statusCode) {
        this(statusCode, null);
    }

    AbstractStatusCodeException(@NotNull final Integer statusCode, @NotNull final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code of this exception.
     *
     * @return the HTTP status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

}
