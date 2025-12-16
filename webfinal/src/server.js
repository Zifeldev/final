require('dotenv').config();
const app = require('./app');
const repo = require('./db');

const PORT = process.env.PORT || 3000;

const start = async () => {
  try {
    await repo.init();
    app.listen(PORT, () => {
      console.log(`Server listening on http://localhost:${PORT}`);
    });
  } catch (err) {
    console.error('Failed to start server:', err?.message || err);
    if (err?.stack) {
      console.error(err.stack);
    }
    process.exit(1);
  }
};

start();
