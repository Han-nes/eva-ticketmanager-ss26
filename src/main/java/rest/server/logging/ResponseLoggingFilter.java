package rest.server.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class ResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingResponseWrapper wrappedResponse =
            new ContentCachingResponseWrapper(response);

        filterChain.doFilter(request, wrappedResponse);

        // Body is only available AFTER the filter chain completes
        byte[] body = wrappedResponse.getContentAsByteArray();
        if (body.length > 0) {
            log.debug("RESPONSE [{}] {}: {}",
                response.getStatus(),
                request.getRequestURI(),
                new String(body)
            );
        } else {
            log.debug("RESPONSE [{}] {}: (no body)",
                response.getStatus(),
                request.getRequestURI()
            );
        }

        // IMPORTANT: copy the cached body back into the real response,
        // otherwise the client receives an empty response
        wrappedResponse.copyBodyToResponse();
    }
}
