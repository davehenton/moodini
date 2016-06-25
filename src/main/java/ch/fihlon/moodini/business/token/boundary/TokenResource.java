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
package ch.fihlon.moodini.business.token.boundary;

import ch.fihlon.moodini.business.token.control.TokenService;
import ch.fihlon.moodini.business.token.entity.AuthenticationData;
import ch.fihlon.moodini.business.user.entity.User;
import io.dropwizard.auth.Auth;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("token")
@Produces(APPLICATION_JSON)
public class TokenResource {

    private final TokenService tokenService;

    @Inject
    public TokenResource(@NotNull final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Request a challenge for authorization
     *
     * @return a <code>200 OK</code> on success
     */
    @GET
    public Response requestChallenge(@QueryParam("email") final String email) {
        final Response.ResponseBuilder responseBuilder;

        if (email == null || email.trim().isEmpty()) {
            return Response.status(BAD_REQUEST)
                    .build();
        }
        tokenService.requestChallenge(email);
        return Response.ok()
                .build();
    }

    /**
     * Authorize and request a new valid token
     *
     * @param authenticationData the data to authenticate with
     * @return a <code>201 CREATED</code> with a new valid token on success
     */
    @POST
    public Response authorizeAndRequestToken(@NotNull final AuthenticationData authenticationData) {
        final Optional<String> token = tokenService.authorize(
                authenticationData.getEmail(), authenticationData.getChallenge());
        if (token.isPresent()) {
            return Response.status(CREATED)
                    .entity(singletonMap("token", token.get()))
                    .header("Authorization", "Bearer ".concat(token.get()))
                    .build();
        }
        return Response.status(UNAUTHORIZED)
                .build();
    }

    /**
     * Check if the provided token is still valid
     *
     * @return a <code>200 OK</code> if the provided token is still valid
     */
    @GET
    @Path("check")
    public Response validateToken(@Auth Principal principal) {
        final User user = (User) principal;
        return Response.ok(user)
                .build();
    }

}
