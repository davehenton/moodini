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

import ch.fihlon.moodini.PersistenceManager;
import ch.fihlon.moodini.business.user.entity.User;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pl.setblack.airomem.core.SimpleController;
import pl.setblack.airomem.core.VoidCommand;

import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PersistenceManager.class)
public class UserServiceTest {

    private UserService userService;
    private SimpleController<Serializable> simpleControllerMock;
    private UserRepository userRepositoryMock;
    private User testUser;

    @Before
    public void setUp() {
        //noinspection unchecked
        simpleControllerMock = mock(SimpleController.class);
        userRepositoryMock = mock(UserRepository.class);
        when(simpleControllerMock.readOnly())
                .thenReturn(userRepositoryMock);
        mockStatic(PersistenceManager.class);
        when(PersistenceManager.createSimpleController(any(), any()))
                .thenReturn(simpleControllerMock);
        final LifecycleEnvironment lifecycleEnvironmentMock = mock(LifecycleEnvironment.class);
        userService = new UserService(lifecycleEnvironmentMock);
        testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@moodini.ch")
                .build();
    }

    @Test
    public void create() {
        // arrange
        when(simpleControllerMock.executeAndQuery(anyObject()))
                .thenReturn(testUser);

        // act
        final User createdUser = userService.create(testUser);

        // assert
        verify(simpleControllerMock, times(1)).executeAndQuery(anyObject());
        assertThat(createdUser, is(testUser));
    }

    @Test
    public void readAll() {
        // arrange
        final List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(userRepositoryMock.readAll())
                .thenReturn(userList);

        // act
        final List<User> users = userService.readAll();

        // assert
        verify(simpleControllerMock, times(1)).readOnly();
        verify(userRepositoryMock, times(1)).readAll();
        assertThat(users, is(userList));
    }

    @Test
    public void readById() {
        // arrange
        when(userRepositoryMock.readById(1L))
                .thenReturn(Optional.of(testUser));

        // act
        final Optional<User> user = userService.readById(1L);

        // assert
        verify(simpleControllerMock, times(1)).readOnly();
        verify(userRepositoryMock, times(1)).readById(1L);
        assertThat(user.orElseThrow(NotFoundException::new), is(testUser));
    }

    @Test
    public void readByEmail() {
        // arrange
        when(userRepositoryMock.readByEmail("john.doe@moodini.ch"))
                .thenReturn(Optional.of(testUser));

        // act
        final Optional<User> user = userService.readByEmail("john.doe@moodini.ch");

        // assert
        verify(simpleControllerMock, times(1)).readOnly();
        verify(userRepositoryMock, times(1)).readByEmail("john.doe@moodini.ch");
        assertThat(user.orElseThrow(NotFoundException::new), is(testUser));
    }

    @Test
    public void update() {
        // arrange
        final User userToUpdate = User.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email("jane.doe@moodini.ch")
                .build();
        when(simpleControllerMock.executeAndQuery(anyObject()))
                .thenReturn(userToUpdate);

        // act
        final User user = userService.update(userToUpdate);

        // assert
        verify(simpleControllerMock, times(1)).executeAndQuery(anyObject());
        assertThat(user, is(userToUpdate));
    }

    @Test
    public void delete() {
        // arrange

        // act
        userService.delete(1L);

        // assert
        //noinspection unchecked
        verify(simpleControllerMock, times(1)).execute(any(VoidCommand.class));
    }

}
