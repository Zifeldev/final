const mongoose = require('mongoose');

const logSchema = new mongoose.Schema(
  {
    action: { type: String, required: true, enum: ['CREATE', 'UPDATE', 'DELETE'] },
    itemId: { type: String },
    itemTitle: { type: String },
    details: { type: String },
    timestamp: { type: Date, default: Date.now }
  },
  { timestamps: false }
);

logSchema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return ret;
  }
});

const Log = mongoose.model('Log', logSchema);

module.exports = {
  createLog: async (action, itemId, itemTitle, details) => {
    const log = await Log.create({ action, itemId, itemTitle, details });
    return log.toJSON();
  },
  listLogs: async (limit = 50) => {
    const logs = await Log.find().sort({ timestamp: -1 }).limit(limit).lean();
    return logs.map((log) => ({
      id: log._id.toString(),
      action: log.action,
      itemId: log.itemId,
      itemTitle: log.itemTitle,
      details: log.details,
      timestamp: log.timestamp
    }));
  },
  clearLogs: async () => {
    await Log.deleteMany({});
  }
};
