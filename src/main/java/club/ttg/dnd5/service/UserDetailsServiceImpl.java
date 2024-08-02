package club.ttg.dnd5.service;

import club.ttg.dnd5.model.user.User;
import club.ttg.dnd5.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import  club.ttg.dnd5.model.user.Role;

import java.util.Collection;


@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userNameOrEmail) throws UsernameNotFoundException {
	    boolean accountNonExpired = true;
	    boolean credentialsNonExpired = true;
	    boolean accountNonLocked = true;
		User user = userRepository.findByEmailOrUsername(userNameOrEmail, userNameOrEmail)
				.orElseThrow(() ->
        			new UsernameNotFoundException("Не найден пользователь с таким именем или email: " + userNameOrEmail));

        return new org.springframework.security.core.userdetails.User(
        		user.getEmail(),
        		user.getPassword(),
        		user.isEnabled(),
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                getAuthorities(user));
	}

	private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
		String[] userRoles = user.getRoles().stream()
				.map(Role::getName)
				.map(s -> "ROLE_" + s)
				.toArray(String[]::new);
		return AuthorityUtils.createAuthorityList(userRoles);
	}
}
