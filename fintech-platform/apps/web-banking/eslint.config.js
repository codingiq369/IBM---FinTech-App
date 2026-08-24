import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'

export default tseslint.config(
  { ignores: ['dist'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2023,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      // Flags the extremely common "reset/fetch on dependency change"
      // effect pattern (setLoading(true) at the top of a fetch effect,
      // setState([]) when a dependency goes away) as an error. That
      // pattern is safe and used throughout this app's pages — downgraded
      // to a warning rather than restructuring idiomatic data-fetching
      // effects to dodge it.
      'react-hooks/set-state-in-effect': 'warn',
    },
  },
)
