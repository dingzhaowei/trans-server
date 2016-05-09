package com.ding.trans.server;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthenticationFilter implements Filter {

	private ServletContext context;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		context = filterConfig.getServletContext();
		context.log("AuthenticationFilter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession(false);
		if (session != null || inWhitelist(req)) {
			chain.doFilter(request, response);
		} else {
			String remoteAddr = req.getRemoteAddr();
			String requestUri = req.getRequestURI();
			String error = URLEncoder.encode("尚未登录", "UTF-8");
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
			context.log("Unauthorized access from " + remoteAddr + " to " + requestUri);
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	private boolean inWhitelist(HttpServletRequest req) {
		return req.getRequestURI().endsWith("login");
	}

}
