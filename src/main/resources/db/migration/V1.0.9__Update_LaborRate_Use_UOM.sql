-- Migration: Update LaborRate to use UnitOfMeasure instead of String unit
-- Date: 2025-11-02

-- Step 1: Add new column for unit_of_measure_id
ALTER TABLE labor_rates ADD COLUMN unit_of_measure_id BIGINT;

-- Step 2: Add foreign key constraint
ALTER TABLE labor_rates 
    ADD CONSTRAINT fk_labor_rate_uom 
    FOREIGN KEY (unit_of_measure_id) 
    REFERENCES unit_of_measures(id);

-- Step 3: Migrate existing data (map common units to UOM)
-- You may need to adjust these IDs based on your actual UOM data
-- Common mappings:
-- 'hour' -> find UOM with name 'Giờ' or 'Hour'
-- 'shift' -> find UOM with name 'Ca' or 'Shift'
-- 'day' -> find UOM with name 'Ngày' or 'Day'

-- Example: If you have UOM data, uncomment and adjust:
-- UPDATE labor_rates lr
-- SET unit_of_measure_id = (SELECT id FROM unit_of_measures WHERE LOWER(name) LIKE '%giờ%' OR LOWER(name) = 'hour' LIMIT 1)
-- WHERE LOWER(lr.unit) IN ('hour', 'giờ');

-- UPDATE labor_rates lr
-- SET unit_of_measure_id = (SELECT id FROM unit_of_measures WHERE LOWER(name) LIKE '%ca%' OR LOWER(name) = 'shift' LIMIT 1)
-- WHERE LOWER(lr.unit) IN ('shift', 'ca');

-- UPDATE labor_rates lr
-- SET unit_of_measure_id = (SELECT id FROM unit_of_measures WHERE LOWER(name) LIKE '%ngày%' OR LOWER(name) = 'day' LIMIT 1)
-- WHERE LOWER(lr.unit) IN ('day', 'ngày');

-- Step 4: Update existing codes to lowercase
UPDATE labor_rates SET code = LOWER(TRIM(code));

-- Step 5: Drop old unit column (after data migration is verified)
-- ALTER TABLE labor_rates DROP COLUMN unit;

-- Note: Keep the old 'unit' column temporarily for backward compatibility
-- Drop it manually after verifying all data is migrated correctly
