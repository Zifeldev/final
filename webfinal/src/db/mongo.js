const mongoose = require('mongoose');

const schema = new mongoose.Schema(
  {
    title: { type: String, required: true },
    description: { type: String, default: '' },
    amount: { type: Number, default: 0 }
  },
  { timestamps: { createdAt: 'createdAt', updatedAt: 'updatedAt' } }
);

schema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    return ret;
  }
});

const Item = mongoose.model('Item', schema);

const ensureId = (id) => {
  if (!mongoose.isValidObjectId(id)) {
    return null;
  }
  return id;
};

module.exports = {
  init: async () => {
    const uri = process.env.MONGO_URI || 'mongodb://localhost:27017';
    const dbName = process.env.MONGO_DB || 'banking';
    await mongoose.connect(uri, { dbName });
  },
  listItems: async () => {
    const items = await Item.find().sort({ createdAt: -1 }).lean();
    return items.map((item) => ({
      id: item._id.toString(),
      title: item.title,
      description: item.description,
      amount: item.amount,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt
    }));
  },
  getItem: async (id) => {
    const validId = ensureId(id);
    if (!validId) return null;
    const item = await Item.findById(validId).lean();
    if (!item) return null;
    return {
      id: item._id.toString(),
      title: item.title,
      description: item.description,
      amount: item.amount,
      createdAt: item.createdAt,
      updatedAt: item.updatedAt
    };
  },
  createItem: async (data) => {
    const created = await Item.create({
      title: data.title,
      description: data.description || '',
      amount: data.amount ?? 0
    });
    return created.toJSON();
  },
  updateItem: async (id, data) => {
    const validId = ensureId(id);
    if (!validId) return null;
    const updated = await Item.findByIdAndUpdate(
      validId,
      { $set: { title: data.title, description: data.description, amount: data.amount } },
      { new: true, runValidators: true }
    ).lean();
    return updated
      ? {
          id: updated._id.toString(),
          title: updated.title,
          description: updated.description,
          amount: updated.amount,
          createdAt: updated.createdAt,
          updatedAt: updated.updatedAt
        }
      : null;
  },
  deleteItem: async (id) => {
    const validId = ensureId(id);
    if (!validId) return false;
    const res = await Item.findByIdAndDelete(validId);
    return Boolean(res);
  }
};
