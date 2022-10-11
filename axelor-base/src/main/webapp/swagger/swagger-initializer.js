window.onload = function() {
  const getCookieValue = (name) => (
      document.cookie.match('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)')?.pop() || ''
  )
  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url : '../ws/aos/openapi',
    dom_id : '#swagger-ui',
    requestInterceptor: (req) => {
      req.headers['X-CSRF-Token'] = getCookieValue('CSRF-TOKEN');
      return req;
    },
    deepLinking : true,
    presets : [ SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset ],
    plugins : [ SwaggerUIBundle.plugins.DownloadUrl ]
  });
};