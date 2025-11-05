-- Add product_brand column to production_plan_products table
ALTER TABLE production_plan_products
ADD COLUMN product_brand VARCHAR(200) NULL COMMENT 'Thương hiệu sản phẩm';

-- Add index for better query performance
CREATE INDEX idx_plan_product_brand ON production_plan_products(product_brand);
