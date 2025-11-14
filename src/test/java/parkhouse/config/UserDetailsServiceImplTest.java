package parkhouse.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import parkhouse.context.UserRepository;
import parkhouse.domain.Role;
import parkhouse.domain.User;
import parkhouse.service.UserDetailsServiceImpl;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;


public class UserDetailsServiceImplTest {

    @Test
    void loadUserByUsername_returnsUserWithAuthorities() {
        var repo = Mockito.mock(UserRepository.class);
        var svc = new UserDetailsServiceImpl(repo);

        var user = new User("alice", "{noop}pwd", Set.of(new Role("ROLE_ADMIN")));
        Mockito.when(repo.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = svc.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("{noop}pwd");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_throwsIfNotFound() {
        var repo = Mockito.mock(UserRepository.class);
        var svc = new UserDetailsServiceImpl(repo);

        Mockito.when(repo.findByUsername("bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> svc.loadUserByUsername("bob"))
        .isInstanceOf(UsernameNotFoundException.class);
    }
}
