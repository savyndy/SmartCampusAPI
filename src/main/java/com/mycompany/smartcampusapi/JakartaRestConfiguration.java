package com.mycompany.smartcampusapi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// This establishes your API's versioned entry point exactly as required
@ApplicationPath("/api/v1")
public class JakartaRestConfiguration extends Application {
    // Leave this empty. JAX-RS will automatically scan your packages for endpoints.
}
