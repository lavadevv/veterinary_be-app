-- =====================================================
-- Migration V1.0.11: Add unique constraint for (header_id, version)
-- Purpose: Prevent duplicate versions in same formula header
-- Date: 2025-11-03
-- =====================================================

-- Add unique constraint to product_formulas table
-- Ensures that each version is unique within a formula header
ALTER TABLE product_formulas 
ADD CONSTRAINT uk_formula_header_version UNIQUE (header_id, version);

-- Create index for better query performance
CREATE INDEX idx_formula_header_version ON product_formulas(header_id, version);
