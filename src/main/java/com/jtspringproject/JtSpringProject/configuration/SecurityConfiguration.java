package com.jtspringproject.JtSpringProject.configuration;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.userService;

@Configuration
public class SecurityConfiguration {

	private final userService userService;

	public SecurityConfiguration(userService userService) {
		this.userService = userService;
	}

	@Configuration
	@Order(1)
	public static class AdminConfigurationAdapter {

		@Bean
		SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
			http.securityMatcher("/admin/**")
					.authorizeHttpRequests(requests -> requests
							.requestMatchers(new AntPathRequestMatcher("/admin/login")).permitAll()
							.requestMatchers(new AntPathRequestMatcher("/resources/**")).permitAll()
							.requestMatchers(new AntPathRequestMatcher("/403")).permitAll()
							.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN"))
					.formLogin(login -> login
							.loginPage("/admin/login")
							.loginProcessingUrl("/admin/loginvalidate")
							.successHandler((request, response, authentication) -> {
								response.sendRedirect("/admin/");
							})
							.failureHandler((request, response, exception) -> {
								response.sendRedirect("/admin/login?error=true");
							})
							.permitAll())

					// Keep GET logout to remain compatible with existing logout anchor links.
					.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout", "GET"))
							.logoutSuccessUrl("/admin/login")
							.deleteCookies("JSESSIONID"))
					.exceptionHandling(exception -> exception
							.accessDeniedPage("/403"));
			return http.build();
		}
	}

	@Configuration
	@Order(2)
	public static class UserConfigurationAdapter {

		@Bean
		SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
			http.authorizeHttpRequests(requests -> requests
					.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
					.requestMatchers("/login", "/register", "/newuserregister", "/resources/**", "/403",
							"/actuator/health", "/actuator/info").permitAll()
					.requestMatchers("/**").hasRole("USER"))
					.formLogin(login -> login
							.loginPage("/login")
							.loginProcessingUrl("/userloginvalidate")
							.successHandler((request, response, authentication) -> {
								response.sendRedirect("/");
							})
							.failureHandler((request, response, exception) -> {
								response.sendRedirect("/login?error=true");
							})
							.permitAll())

					// Keep GET logout to remain compatible with existing logout anchor links.
					.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
							.logoutSuccessUrl("/login")
							.deleteCookies("JSESSIONID"))
					.exceptionHandling(exception -> exception
							.accessDeniedPage("/403"));
			return http.build();
		}
	}

	@Bean
	UserDetailsService userDetailsService() {
		return username -> {
			User user = userService.getUserByUsername(username);
			if (user == null) {
				throw new UsernameNotFoundException("User with username " + username + " not found.");
			}
			String role = "ROLE_ADMIN".equals(user.getRole()) ? "ADMIN" : "USER";

			return org.springframework.security.core.userdetails.User
					.withUsername(username)
					.password(user.getPassword())
					.roles(role)
					.build();
		};
	}


}
