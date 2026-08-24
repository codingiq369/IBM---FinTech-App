/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the API gateway. Defaults to http://localhost:8080 when unset —
   * see src/api/httpClient.ts. Override with a .env file (see .env.example)
   * for local dev against a differently-hosted gateway. */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
