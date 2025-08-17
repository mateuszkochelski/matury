-- Create table for monthly relative indices per field of study
CREATE TABLE IF NOT EXISTS field_of_study_monthly_index (
    id BIGSERIAL PRIMARY KEY,
    uczelnia_id VARCHAR(50),
    kierunek_id VARCHAR(50),
    poziom VARCHAR(50),
    rok_dyplomu INTEGER,
    miesiac INTEGER NOT NULL,
    wwz DECIMAL(8,4),
    wwb DECIMAL(8,4)
);

CREATE INDEX IF NOT EXISTS idx_fosmi_keys ON field_of_study_monthly_index(uczelnia_id, kierunek_id, poziom, rok_dyplomu);
CREATE INDEX IF NOT EXISTS idx_fosmi_miesiac ON field_of_study_monthly_index(miesiac);

-- Extra columns on graduates for stability and dispersion
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS proc_mies_praca DECIMAL(5,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS proc_mies_etat DECIMAL(5,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS proc_mies_samoz DECIMAL(5,2);

ALTER TABLE graduates ADD COLUMN IF NOT EXISTS me_zar DECIMAL(10,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS me_zar_etat DECIMAL(10,2);

ALTER TABLE graduates ADD COLUMN IF NOT EXISTS zar_q1 DECIMAL(10,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS zar_q2 DECIMAL(10,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS zar_q3 DECIMAL(10,2);
ALTER TABLE graduates ADD COLUMN IF NOT EXISTS zar_q4 DECIMAL(10,2);


