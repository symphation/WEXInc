window.onload = function() {
  // Inject custom dark theme before the UI boots
  var link = document.createElement('link');
  link.rel = 'stylesheet';
  link.type = 'text/css';
  link.href = '/swagger-ui/custom.css';
  document.head.appendChild(link);

  window.ui = SwaggerUIBundle({
    configUrl: "/api-docs/swagger-config",
    dom_id: '#swagger-ui',
    deepLinking: true,
    docExpansion: "list",
    displayRequestDuration: true,
    tagsSorter: "alpha",
    operationsSorter: "alpha",
    tryItOutEnabled: true,
    persistAuthorization: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });
};
