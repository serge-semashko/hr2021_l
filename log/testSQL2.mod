sys/testSQL2.mod

[report]
    $SET_PARAMETERS CURR_DB=1C
    $SET_PARAMETERS SQL_TEXT=select * from DB_1C_2.dbo.jinr_1c2sed order by id desc; 
    ?? select * from DB_1C_2.dbo.jinr_sed21c  order by id desc;
    $CALL_SERVICE c=sys/testSQL;
[end]
