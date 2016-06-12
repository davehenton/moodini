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
package ch.fihlon.moodini.business.user.boundary;

import ch.fihlon.moodini.business.user.control.UserService;
import ch.fihlon.moodini.business.user.entity.User;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.util.List;

@Immutable
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Timed(name = "Timed: UsersResource")
@Metered(name = "Metered: UsersResource")
public class UsersResource {

    private UserService userService;

    @Inject
    public UsersResource(@NotNull final UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     *
     * @param newUser the user to create
     * @param uriInfo information about the URI of the request
     * @return a <code>201 CREATED</code> and the location of the created user
     * @successResponse 201 The user was successfully created
     * @errorResponse 400 The user data in the request was invalid
     */
    @POST
    public Response create(@Valid final User newUser,
                           @Context UriInfo uriInfo) {
        final User savedUser = userService.create(newUser);
        final Long userId = savedUser.getUserId();
        final URI uri = uriInfo.getAbsolutePathBuilder().path(File.separator + userId).build();
        return Response.created(uri).build();
    }

    /**
     * Get a list of all users
     *
     * @return a list of all users
     * @successResponse 200 Successful request
     */
    @GET
    public List<User> readAll() {
        return userService.readAll();
    }

    /**
     * Get the user with the specified id
     *
     * @return the user with the specified id or a <code>404 NOT FOUND</code> if there is no user available
     * @successResponse 200 Successful request
     * @errorResponse 404 The user does not exist
     */
    @Path("{userId}")
    @GET
    public User read(@PathParam("userId") final Long userId) {
        return userService.read(userId).orElseThrow(NotFoundException::new);
    }

    /**
     * Update an existing user
     *
     * @param user the user to update
     * @return a <code>200 OK</code> and the updated user
     * @successResponse 200 The user was successfully updated
     * @errorResponse 400 The user data in the request was invalid
     */
    @PUT
    public Response update(@Valid final User user) {
        final User savedUser = userService.update(user);
        return Response.ok(savedUser).build();
    }

    /**
     * Delete the user with the specified id
     *
     * @return a <code>204 NO CONTENT</code> on success or a <code>404 NOT FOUND</code> if there is no user available
     * @successResponse 204 The user was successfully deleted
     * @errorResponse 404 The user does not exist
     */
    @Path("{userId}")
    @DELETE
    public Response delete(@PathParam("userId") final Long userId) {
        userService.delete(userId);
        return Response.noContent().build();
    }

}
