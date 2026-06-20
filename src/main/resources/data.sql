-- ============================================================
-- Seed data for retail-forecast-platform (H2 in-memory DB)
-- Today's reference date: 2026-06-20
-- ============================================================

-- Products
INSERT INTO products (name, sku, category, unit_price, current_stock, lead_time_days)
VALUES ('Wireless Headphones',    'ELEC-001', 'Electronics',      79.99,  150, 5);
INSERT INTO products (name, sku, category, unit_price, current_stock, lead_time_days)
VALUES ('Bluetooth Speaker',      'ELEC-002', 'Electronics',      59.99,   28, 5);
INSERT INTO products (name, sku, category, unit_price, current_stock, lead_time_days)
VALUES ('Running Shoes',          'CLTH-001', 'Clothing',         89.99,  200, 7);
INSERT INTO products (name, sku, category, unit_price, current_stock, lead_time_days)
VALUES ('Yoga Mat',               'SPRT-001', 'Sports',           34.99,   45, 4);
INSERT INTO products (name, sku, category, unit_price, current_stock, lead_time_days)
VALUES ('Organic Coffee Beans',   'FOOD-001', 'Food & Beverage',  24.99,  500, 3);

-- ============================================================
-- Sales History — last 30 days per product
-- ============================================================

-- ELEC-001 (Wireless Headphones): ~12 units/day, stable
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-21', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-22', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-23', 10, 799.90);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-24', 14, 1119.86);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-25', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-26', 15, 1199.85);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-27', 9,  719.91);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-28', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-29', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-30', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-05-31', 14, 1119.86);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-01', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-02', 10, 799.90);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-03', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-04', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-05', 16, 1279.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-06', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-07', 14, 1119.86);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-08', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-09', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-10', 10, 799.90);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-11', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-12', 15, 1199.85);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-13', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-14', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-15', 12, 959.88);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-16', 14, 1119.86);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-17', 11, 879.89);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-18', 13, 1039.87);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (1, '2026-06-19', 14, 1119.86);

-- ELEC-002 (Bluetooth Speaker): ~5 units/day, low & declining — will trigger reorder alert
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-21', 7, 419.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-22', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-23', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-24', 8, 479.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-25', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-26', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-27', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-28', 7, 419.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-29', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-30', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-05-31', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-01', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-02', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-03', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-04', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-05', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-06', 7, 419.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-07', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-08', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-09', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-10', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-11', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-12', 3, 179.97);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-13', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-14', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-15', 5, 299.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-16', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-17', 6, 359.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-18', 4, 239.96);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (2, '2026-06-19', 5, 299.95);

-- CLTH-001 (Running Shoes): ~17 units/day, healthy
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-21', 15, 1349.85);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-22', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-23', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-24', 20, 1799.80);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-25', 16, 1439.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-26', 19, 1709.81);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-27', 14, 1259.86);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-28', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-29', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-30', 16, 1439.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-05-31', 20, 1799.80);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-01', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-02', 15, 1349.85);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-03', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-04', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-05', 21, 1889.79);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-06', 16, 1439.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-07', 19, 1709.81);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-08', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-09', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-10', 16, 1439.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-11', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-12', 20, 1799.80);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-13', 15, 1349.85);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-14', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-15', 17, 1529.83);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-16', 19, 1709.81);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-17', 16, 1439.84);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-18', 18, 1619.82);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (3, '2026-06-19', 20, 1799.80);

-- SPRT-001 (Yoga Mat): ~7 units/day, low stock — will trigger reorder
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-21', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-22', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-23', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-24', 9,  314.91);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-25', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-26', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-27', 5,  174.95);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-28', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-29', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-30', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-05-31', 9,  314.91);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-01', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-02', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-03', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-04', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-05', 10, 349.90);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-06', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-07', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-08', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-09', 9,  314.91);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-10', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-11', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-12', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-13', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-14', 9,  314.91);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-15', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-16', 8,  279.92);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-17', 6,  209.94);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-18', 7,  244.93);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (4, '2026-06-19', 8,  279.92);

-- FOOD-001 (Organic Coffee Beans): ~38 units/day, high volume, growing trend
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-21', 30,  749.70);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-22', 33,  824.67);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-23', 31,  774.69);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-24', 35,  874.65);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-25', 32,  799.68);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-26', 36,  899.64);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-27', 29,  724.71);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-28', 34,  849.66);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-29', 37,  924.63);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-30', 33,  824.67);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-05-31', 38,  949.62);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-01', 35,  874.65);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-02', 32,  799.68);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-03', 36,  899.64);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-04', 38,  949.62);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-05', 40,  999.60);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-06', 37,  924.63);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-07', 39,  974.61);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-08', 36,  899.64);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-09', 40,  999.60);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-10', 38,  949.62);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-11', 41, 1024.59);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-12', 39,  974.61);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-13', 37,  924.63);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-14', 42, 1049.58);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-15', 40,  999.60);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-16', 43, 1074.57);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-17', 41, 1024.59);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-18', 44, 1099.56);
INSERT INTO sales_history (product_id, sale_date, quantity_sold, revenue) VALUES (5, '2026-06-19', 45, 1124.55);
