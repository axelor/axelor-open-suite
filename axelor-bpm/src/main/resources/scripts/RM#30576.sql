ALTER TABLE bpm_wkf_model ADD COLUMN IF NOT EXISTS code VARCHAR(255);
UPDATE bpm_wkf_model wkfModel SET CODE = (SELECT CONCAT(name,'',id) FROM bpm_wkf_model innerWkfModel WHERE innerWkfModel.id = wkfModel.id);
ALTER TABLE bpm_wkf_model ALTER COLUMN code SET NOT NULL;
ALTER TABLE bpm_wkf_model ADD CONSTRAINT uk_bpm_wkf_model_code UNIQUE (code);


