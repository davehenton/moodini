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

import io.dropwizard.Configuration;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
public class MoodiniConfiguration extends Configuration {

    @NotEmpty
    private String tokenSecret;

    @Valid
    private SmtpConfiguration smtp;

    @Getter
    public class SmtpConfiguration {

        @NotEmpty
        private String hostname;

        @NotNull
        @Min(1)
        private Integer port;

        @NotEmpty
        private String user;

        @NotEmpty
        private String password;

        @NotNull
        private Boolean ssl;

        @NotEmpty
        private String from;

    }
}
