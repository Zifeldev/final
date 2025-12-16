const path = require('path');
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const repo = require('./db');
const createItemsRouter = require('./routes/items');
const createLogsRouter = require('./routes/logs');

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));
app.use(express.static(path.join(__dirname, '..', 'public')));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', db: (process.env.DB_TYPE || 'mongo').toLowerCase() });
});

app.use('/items', createItemsRouter(repo));
app.use('/logs', createLogsRouter());

app.use((err, _req, res, _next) => {
  const status = err.status || 500;
  res.status(status).json({ error: err.message || 'Internal Server Error' });
});

module.exports = app;
