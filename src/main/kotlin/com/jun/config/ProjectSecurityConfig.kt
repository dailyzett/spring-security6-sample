package com.jun.config

import com.jun.filter.CsrfCookieFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class ProjectSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val requestHandler = CsrfTokenRequestAttributeHandler()
        requestHandler.setCsrfRequestAttributeName("_csrf")
        http.securityContext {
            it
                .requireExplicitSave(false)
        }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            }
            .cors {
                it.configurationSource {
                    val config = CorsConfiguration()
                    config.allowedOrigins = listOf("http://localhost:4200")
                    config.setAllowedMethods(listOf("*"))
                    config.allowCredentials = true
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L
                    return@configurationSource config
                }
            }.csrf {
                it
                    .csrfTokenRequestHandler(requestHandler).ignoringRequestMatchers("/contact", "/register")
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .addFilterAfter(CsrfCookieFilter(), BasicAuthenticationFilter::class.java)
            .authorizeHttpRequests {
                it
                    .requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")
                    .requestMatchers("/myBalance").hasAnyAuthority("VIEWBALANCE", "VIEWACCOUNT")
                    .requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
                    .requestMatchers("/myCards").hasAuthority("VIEWCARDS")
                    .requestMatchers("/user").authenticated()
                    .requestMatchers("/notices", "/contact", "/register").permitAll()
            }
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults())
        return http.build()
    }


    /**
     * NoOpPasswordEncoder is deprecated. Use BCryptPasswordEncoder instead.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}