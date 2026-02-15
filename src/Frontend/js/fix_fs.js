db.flash_sales.updateOne({ _id: ObjectId('67adbdf6bd0f0a2ba898991b') }, { $set: { status: 'ACTIVE' } });
db.flash_sale_items.updateOne({ flashSaleId: '67adbdf6bd0f0a2ba898991b' }, { $set: { status: 'APPROVED' } });
