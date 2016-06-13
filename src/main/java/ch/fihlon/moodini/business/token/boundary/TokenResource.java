package ch.fihlon.moodini.business.token.boundary;

import ch.fihlon.moodini.MoodiniConfiguration;
import ch.fihlon.moodini.business.user.control.UserService;
import ch.fihlon.moodini.business.user.entity.User;
import com.github.toastshaman.dropwizard.auth.jwt.hmac.HmacSHA512Signer;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenClaim;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenHeader;
import io.dropwizard.auth.Auth;
import org.apache.commons.io.Charsets;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("token")
@Produces(APPLICATION_JSON)
public class TokenResource {

    private final byte[] tokenSecret;
    private final UserService userService;

    @Inject
    public TokenResource(@NotNull final MoodiniConfiguration configuration,
                         @NotNull final UserService userService) {
        this.tokenSecret = configuration.getJwtTokenSecret().getBytes(Charsets.UTF_8);
        this.userService = userService;
    }

    @GET
    @Consumes(WILDCARD)
    public Response requestChallenge(@QueryParam("email") final String email) {
        final Response.ResponseBuilder responseBuilder;

        if (email == null || email.trim().isEmpty()) {
            responseBuilder = Response.status(BAD_REQUEST);
        } else {
            responseBuilder = authenticationService.requestChallenge(email) ?
                    Response.ok().build() :
                    Response.status(NOT_FOUND);
        }

        return responseBuilder.build();
    }

    @POST
    public Response requestToken() {
        final User user = userService.readAll().get(0);

        final HmacSHA512Signer signer = new HmacSHA512Signer(tokenSecret);
        final JsonWebToken token = JsonWebToken.builder()
                .header(JsonWebTokenHeader.HS512())
                .claim(JsonWebTokenClaim.builder()
                        .subject(user.getUserId().toString())
                        .issuedAt(DateTime.now())
                        .expiration(DateTime.now().plusHours(12))
                        .build())
                .build();
        final String signedToken = signer.sign(token);
        return Response.status(CREATED)
                .entity(singletonMap("token", signedToken))
                .header("Authorization", "Bearer ".concat(signedToken))
                .build();
    }

    @GET
    @Path("check")
    public Response validateToken(@Auth Principal principal) {
        final User user = (User) principal;
        return Response.ok(user)
                .build();
    }
}
