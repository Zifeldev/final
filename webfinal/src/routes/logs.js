const express = require('express');
const logs = require('../db/logs');

module.exports = () => {
  const router = express.Router();

  router.get('/', async (_req, res, next) => {
    try {
      const items = await logs.listLogs(100);
      return res.json(items);
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/', async (_req, res, next) => {
    try {
      await logs.clearLogs();
      return res.status(204).send();
    } catch (err) {
      return next(err);
    }
  });

  return router;
};
