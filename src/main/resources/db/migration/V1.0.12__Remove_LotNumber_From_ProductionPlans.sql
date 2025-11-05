-- V1.0.12: Remove lot_number column from production_plans table
-- Reason: lot_number is now stored in production_lots table (parent)
-- production_plans references lot via lot_id foreign key

ALTER TABLE production_plans DROP COLUMN IF EXISTS lot_number;
