import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const targetDir = path.join(root, 'public');
const targetFile = path.join(targetDir, 'app-config.js');
const apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:8080';

fs.mkdirSync(targetDir, { recursive: true });

const content = `window.__APP_CONFIG__ = {
  apiBaseUrl: "${apiBaseUrl}"
};
`;

fs.writeFileSync(targetFile, content, 'utf8');
