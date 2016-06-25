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
package ch.fihlon.moodini.business.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import ch.fihlon.moodini.business.user.entity.User.UserBuilder;

import java.io.Serializable;
import java.security.Principal;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@JsonDeserialize(builder = UserBuilder.class)
public class User implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long version;

    @NotEmpty
    @Length(max=50)
    private String firstname;

    @NotEmpty
    @Length(max=50)
    private String lastname;

    @Email
    @NotEmpty
    @Length(max=100)
    private String email;

    @Override
    @JsonIgnore
    public String getName() {
        return String.format("%s %s", getFirstname(), getLastname());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class UserBuilder {
    }

}
