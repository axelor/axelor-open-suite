# Axelor Map View

This application integrate a Map view for Axelor Open Suite.

## Connect to Axelor

### Local Server

To test this view with a local server, please follow those steps:

- First login into Local server i.e. http://localhost:8080/axelor-erp (admin/admin)
- Change 'proxy' server's url and suburl in '.env' file
  - Set `VITE_PROXY_TARGET` to http://localhost:8080
  - Set `VITE_PROXY_CONTEXT` to /axelor-erp/
  - Note: If the server doesn't have a suburl, set `VITE_PROXY_CONTEXT` to `/`
- Try to run the app with a relative suburl i.e. on http://localhost:5173/axelor-erp

### Test instance

To test this view with the distant test instance, please follow those steps:

- First login into Online server i.e. http://preview.axelor.com/demo/
  - Set `VITE_PROXY_TARGET` to http://preview.axelor.com
  - Set `VITE_PROXY_CONTEXT` to /demo/
  - Note: If the server doesn't have a suburl, set `VITE_PROXY_CONTEXT` to `/`
- Try to run the app with a relative suburl i.e. on http://localhost:5173/demo/
- Manually copy-paste CSRF-TOKEN and JSESSIONID from http://preview.axelor.com server to localhost server (Developer Tools -> Storage -> Cookies Section), reload the page

## Available Scripts

In the project directory, you can run:

- `yarn dev` : Runs the app in the development mode.
- `yarn build`: Builds the app for production.
