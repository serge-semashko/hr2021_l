insert users (kod_fl, tab_n,f,i,o,lab_id, rights,personal_filters ) 
   SELECT person_id,tab_n,f,i,o,(div_code div 100000)*100000 , " personal_place_view,salary_view,fte_view", "fin_filter  and  sotr.SubTopParent_code =129000,pers_filter and sotr.SubTopParent_code =129000"
    FROM sotrudniki

    WHERE tab_n=112135