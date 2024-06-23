package com.cdweb.springboot.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cdweb.springboot.service.Impl.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtValidator extends OncePerRequestFilter {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	 String jwt = request.getHeader("Authorization");
         System.out.println("validate");

         if (jwt != null && jwt.startsWith("Bearer ") && jwtProvider.validateToken(jwt.substring(7))) {
             System.out.println("validate thanh cong");
             String email = jwtProvider.getEmailFromToken(jwt.substring(7));

             // Tải thông tin người dùng sử dụng email
             UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);
             System.out.println("userDetails: " + userDetails);

             // Tạo một token xác thực sử dụng thông tin người dùng
             UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                     userDetails, null, userDetails.getAuthorities());

             // Thiết lập các chi tiết bổ sung
             authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

             // Thiết lập token xác thực trong SecurityContextHolder
             SecurityContextHolder.getContext().setAuthentication(authentication);
         }

         filterChain.doFilter(request, response);
     }
}
