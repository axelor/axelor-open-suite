package com.axelor.apps.base.web

import com.axelor.apps.base.db.General

class GeneralControllerSimple {
	
	def BigDecimal getTurnoverDeduction()  {
		
		return General.all().fetchOne().getTurnoverDeduction();
		
	}
	
	def String getDefaultSviStoredProcedure(){
		
		return """-- DELETE
DROP FUNCTION fsvi_get_type_tarif (ref_pdl varchar(255));

-- CREATE SQL FUNCTION
CREATE FUNCTION
	fsvi_get_type_tarif (ref_pdl varchar(255))
	returns varchar(255)
AS \$\$
	SELECT 
	  p.ivs_code 
	FROM 
	  public.contract_contract c, 
	  public.contract_contract_line cl, 
	  public.territory_mpt mpt, 
	  public.administration_status s,
	  public.contract_amendment as a,
	  public.pricing_pricing as p
	WHERE 
	  c.status = s.id 
	  AND cl.contract = c.id
	  AND cl.mpt = mpt.id 
	  AND mpt.code =  \$1 --'15254124375442'
	  AND s.code = 'act'
	  AND a.contract_line = cl.id
	  AND a.pricing = p.id
	LIMIT 1;
\$\$
LANGUAGE sql;


-- LIST SOURCE
-- SELECT prosrc FROM pg_proc WHERE proname = 'fsvi_get_type_tarif';

-- MAKE CALL
-- SELECT fsvi_get_type_tarif('15254124375442');



DROP FUNCTION  fsvi_set_index (
        ref_pdl varchar(20), 
        dat_index timestamp,
        idx_hp varchar(10),
        idx_hc varchar(10),
        idx_3 varchar(10),
        idx_4 varchar(10),
        idx_5 varchar(10),
        idx_6 varchar(10));

CREATE FUNCTION
    fsvi_set_index (
        ref_pdl varchar(20), 
        dat_index timestamp,
        idx_hp varchar(10),
        idx_hc varchar(10),
        idx_3 varchar(10),
        idx_4 varchar(10),
        idx_5 varchar(10),
        idx_6 varchar(10))
RETURNS integer
AS \$\$
DECLARE
    id_amendment integer;
BEGIN
    -- Find amendment corresponding to ref_pdl argument 

    SELECT am.id INTO id_amendment FROM public.territory_mpt mpt 
    LEFT JOIN public.contract_contract_line cl ON (cl.mpt = mpt.id)
    LEFT JOIN public.administration_status st ON (st.id = cl.status)
    LEFT JOIN public.contract_amendment am ON (am.id = cl.amendment)
    WHERE mpt.code = ref_pdl
    AND st.code = 'act';

    IF (id_amendment IS null) THEN
        RETURN 0;
    ELSE
        INSERT INTO public.event_index_input (id, amendment, source, meter_reading_date, 
            index_value1, index_value2, index_value3, index_value4, index_value5, index_value6) 
        VALUES (nextval('hibernate_sequence'), id_amendment, 'ivs', dat_index, 
	    to_number(idx_hp, '9999999999'), to_number(idx_hc, '9999999999'), to_number(idx_3, '9999999999'),
	    to_number(idx_4, '9999999999'), to_number(idx_5, '9999999999'), to_number(idx_6, '9999999999'));
	RETURN 1;
    END IF;
END;
\$\$ language plpgsql;

-- call example
--SELECT fsvi_set_index('12', '2012-01-01', '1','1','1','1','1','1');"""
		
	}

}
