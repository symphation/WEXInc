package com.wex.purchasetransactions.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves our custom swagger-ui overrides at the exact same URLs that
 * springdoc registers, which ensures our versions take precedence.
 *
 * A @RestController with explicit mappings always wins over a resource handler
 * because Spring MVC processes @RequestMapping before ResourceHttpRequestHandler.
 *
 * @Hidden keeps these internal endpoints out of the generated OpenAPI docs.
 */
@Hidden
@RestController
public class SwaggerUiConfig {

    /** Overrides the webjar's swagger-initializer.js with our custom version */
    @GetMapping(value = "/swagger-ui/swagger-initializer.js",
                produces = "application/javascript")
    public ResponseEntity<Resource> swaggerInitializer() {
        Resource res = new ClassPathResource("static/swagger-ui/swagger-initializer.js");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(res);
    }

    /** Serves the dark theme CSS */
    @GetMapping(value = "/swagger-ui/custom.css",
                produces = "text/css")
    public ResponseEntity<Resource> swaggerCustomCss() {
        Resource res = new ClassPathResource("static/swagger-ui/custom.css");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/css"))
                .body(res);
    }
}
