-- Migration: Update EnergyTariff to use UnitOfMeasure instead of String unit
-- Date: 2025-11-02

-- Step 1: Add new column for unit_of_measure_id
ALTER TABLE energy_tariffs ADD COLUMN unit_of_measure_id BIGINT;

-- Step 2: Add foreign key constraint
ALTER TABLE energy_tariffs 
    ADD CONSTRAINT fk_energy_tariff_uom 
    FOREIGN KEY (unit_of_measure_id) 
    REFERENCES unit_of_measures(id);

-- Step 3: Migrate existing data (map common units to UOM)
-- Map 'kWh' -> find UOM with name containing 'kWh' or 'kilowatt'
-- You need to adjust these queries based on your actual UOM data

-- Example: If you have UOM data, uncomment and adjust:
-- UPDATE energy_tariffs et
-- SET unit_of_measure_id = (SELECT id FROM unit_of_measures WHERE LOWER(name) LIKE '%kwh%' OR LOWER(name) LIKE '%kilowatt%' LIMIT 1)
-- WHERE LOWER(et.unit) IN ('kwh', 'kilowatt-hour');

-- Step 4: Update existing codes to lowercase
UPDATE energy_tariffs SET code = LOWER(TRIM(code));

-- Step 5: Drop old unit column (after data migration is verified)
-- ALTER TABLE energy_tariffs DROP COLUMN unit;

-- Note: Keep the old 'unit' column temporarily for backward compatibility
-- Drop it manually after verifying all data is migrated correctly
