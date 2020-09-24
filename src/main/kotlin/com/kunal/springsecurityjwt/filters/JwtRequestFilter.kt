package com.kunal.springsecurityjwt.filters

import com.kunal.springsecurityjwt.service.MyUserDetailService
import com.kunal.springsecurityjwt.util.JwtUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtRequestFilter:OncePerRequestFilter() {

    @Autowired
    private lateinit var userDetailService: MyUserDetailService

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")
        var username:String?=null
        var jwt:String?=null
        if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7)
            username = jwtUtil.extractUsername(jwt)
        }
            if(username!=null && SecurityContextHolder.getContext().authentication==null) {
                val userDetails = userDetailService.loadUserByUsername(username)
                if(jwtUtil.validateToken(jwt!!,userDetails)) {
                    val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetails,null,userDetails.authorities)
                    usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
                }
            }
        chain.doFilter(request,response)
    }

}