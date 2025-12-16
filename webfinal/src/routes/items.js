const express = require('express');
const logs = require('../db/logs');

const parseAmount = (value) => {
  if (value === undefined || value === null || value === '') return undefined;
  const num = Number(value);
  if (Number.isNaN(num)) {
    const err = new Error('Invalid amount');
    err.status = 400;
    throw err;
  }
  return num;
};

module.exports = (repo) => {
  const router = express.Router();

  router.get('/', async (_req, res) => {
    const items = await repo.listItems();
    res.json(items);
  });

  router.get('/:id', async (req, res, next) => {
    try {
      const item = await repo.getItem(req.params.id);
      if (!item) {
        return res.status(404).json({ error: 'Not found' });
      }
      return res.json(item);
    } catch (err) {
      if (err.name === 'CastError') {
        return res.status(400).json({ error: 'Invalid id' });
      }
      return next(err);
    }
  });

  router.post('/', async (req, res, next) => {
    try {
      const { title, description } = req.body || {};
      if (!title) {
        return res.status(400).json({ error: 'title is required' });
      }
      const amount = parseAmount(req.body.amount ?? 0);
      const created = await repo.createItem({ title, description, amount });
      await logs.createLog('CREATE', created.id, created.title, `Created with amount ${created.amount}`);
      return res.status(201).json(created);
    } catch (err) {
      return next(err);
    }
  });

  router.put('/:id', async (req, res, next) => {
    try {
      const payload = {
        title: req.body.title,
        description: req.body.description,
        amount: parseAmount(req.body.amount)
      };
      const updated = await repo.updateItem(req.params.id, payload);
      if (!updated) {
        return res.status(404).json({ error: 'Not found' });
      }
      await logs.createLog('UPDATE', updated.id, updated.title, `Updated to amount ${updated.amount}`);
      return res.json(updated);
    } catch (err) {
      if (err.name === 'CastError') {
        return res.status(400).json({ error: 'Invalid id' });
      }
      return next(err);
    }
  });

  router.delete('/:id', async (req, res, next) => {
    try {
      const item = await repo.getItem(req.params.id);
      if (!item) {
        return res.status(404).json({ error: 'Not found' });
      }
      const deleted = await repo.deleteItem(req.params.id);
      if (!deleted) {
        return res.status(404).json({ error: 'Not found' });
      }
      await logs.createLog('DELETE', item.id, item.title, `Deleted item with amount ${item.amount}`);
      return res.status(204).send();
    } catch (err) {
      if (err.name === 'CastError') {
        return res.status(400).json({ error: 'Invalid id' });
      }
      return next(err);
    }
  });

  return router;
};
