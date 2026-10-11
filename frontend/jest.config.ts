import type { Config } from 'jest';
import presets from 'jest-preset-angular/presets/index.js';

export default {
  ...presets.createCjsPreset({
    testEnvironment: 'jest-preset-angular/environments/jest-jsdom-env',
  }),
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  roots: ['<rootDir>/src'],
  clearMocks: true,
  restoreMocks: true,
  collectCoverageFrom: ['src/app/**/*.ts', '!src/app/**/*.spec.ts'],
  coverageReporters: ['text', 'text-summary', 'html', 'lcov'],
  coverageThreshold: {
    global: { statements: 80, branches: 75, functions: 80, lines: 80 },
  },
} satisfies Config;
