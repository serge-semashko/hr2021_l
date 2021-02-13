sys/request_stat.mod

[comments]
SYS: Лог http-запросов.
Вывод таблицы запросов, поступивших в Томкат

input=
output=HTML таблица http-запросов, 
parents=
childs=sys/viewRequest
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
title=***СЭД-стат
request_name=Статистика запросов

LOG=ON
debug=off

tableCfg=table_no

rowLink=|<a class=wt href="javascript:goToRow(#srn_i#);">#srn_i#-#ern_i#</a> ??!currentPage
rowLink=|<font color=FFFF00><b>#srn_i#-#ern_i#</b></font> ??currentPage
prevSetLink=<a class=wt href="javascript:goToRow(#srn_i#);"> <<< предыд. </a> 
nextSetLink=| <a class=wt href="javascript:goToRow(#srn_i#);"> следующие >>> </a>
[end]

[report header]
    $SET_PARAMETERS RWACC=Y; ??AR_SYS_ADMIN=1|VU
    $INCLUDE [OK report header]  ??RWACC
[end]


[OK report header]
$INCLUDE dat/common.dat[head]
$SET_PARAMETERS srt=a.DAT; desc=desc;  ??!srt
$SET_PARAMETERS srn=1;  ??!srn
$SET_PARAMETERS rpp=50; ??!rpp
$SET_PARAMETERS LIM=501; ??!LIM

<style type="text/css">
    $INCLUDE free/main_css_noDB.cfg[report]
</style>

<script type="text/javascript">
    $INCLUDE free/main_js_noDB.cfg[report]
</script>

<SCRIPT language=JavaScript src="#jsPath#lib.js"></SCRIPT>

<style>
    table.tlist td {text-align:left; padding:3px;}
    table.tlist td.r {text-align:right;}
    td.lab {text-align:right;}
    .filter {color:black;}
    body {background-color:##d0d0d0;}
</style>
</head>

<body>
    <table border=0 cellpadding=0 cellspacing=0 width=98% style="margin:10px;">
        <tr><td class="bg_white" width=1% nowrap style="padding:2px;"><h3>СЭД&nbsp;-&nbsp;лог&nbsp;запросов</h3></td>
        <td style="padding:5px 0 2px 20px;">
            <a href="#ServletPath#?c=sys/request_log">Статистика>></a> &nbsp;
            <a href="#ServletPath#?c=sys/request_stat_hours">Статистика по времени>></a> &nbsp;
            <a class=info href="#ServletPath#?c=sys/showLog_noDB">Log>></a> &nbsp;
            <a class=info href="#ServletPath#?c=sys/showSysconst">sys>></a>      ??USER_ID=2309|VU=2309|USER_ID=4241|USER_ID=3663
USER_ID=#USER_ID#; VU=#VU#
        </td>
        <td align=right nowrap=true>
        </td><td align=right nowrap=true>  ??
            <a class=info href="/sed/dubna">Главная</a>
        </td>
    </tr></table>

    <form name="theForm" method="POST" enctype="multipart/form-data" onSubmit="return submitForm(true);">
        <input type=hidden name="c" value="#c#">
        <input type=hidden name="cop" value="">
        <input type=hidden name="srn" value="#srn#">
        <input type=hidden name="srt" value="#srt#">
        <input type=hidden name="desc" value="#desc#">

    ================================== FILTERS ==================================== ??
        $SET_PARAMETERS f_table=a_req_log;  f_today=8; ??!f_table
        <table border=0 cellpadding=4 style="border:solid 1px gray; margin:10px 0 0 200px; background-color:white;">
            <tr>
                <td class=lab>Пользователь:</td>
                <td colspan=3><input size=20 name="f_user" class="f" value="#f_user#"> &nbsp; 
                Таблица: <select name=f_table>#TNAMES#</select> &nbsp;
                <span class="pt" onClick="if(confirm('Очистить лог?')) {document.theForm.cop.value='clear'; submitForm(true);}"> ??debug_server=true
                    <i class="fa fa-times pt" aria-hidden="true" style="color:red;" onClick="$('input.f').val('');"></i>clear</span> ??debug_server=true
            </td></tr>

            <tr><td class=lab>IP:</td><td><input size=12 name="f_IP" class="f" value="#f_IP#">
                <input type=checkbox name=ip_exept
                    checked ??ip_exept
                > кроме</td>
            </tr>

            <tr><td class=lab>Agent:</td><td colspan=3><input size=70 name="f_agent" class="f" value="#f_agent#"></tr>

            <tr><td class=lab>Dev:</td><td><input size=10 name="f_did" class="f" value="#f_did#"></td>  </tr>

            <tr><td class=lab>Модуль:</td><td><input size=20 name="f_c" class="f" value="#f_c#">
                </td>
                <td class=lab>
                </td>
            </tr>

            <tr><td class=lab>Срок:</td>
                <td colspan=3><input type=radio name=f_today value="2"
                    checked ??f_today=2
                >за сутки
                <input type=radio name=f_today value="8"
                    checked ??f_today=8
                >за неделю
                <input type=radio name=f_today value=""
                    checked ??!f_today
                >всё &nbsp; 
                    дата:<input size=8 name="f_dat" class="center f" value="#f_dat#">(дд.мм.гг)
                    <i class="fa fa-times pt" aria-hidden="true" style="padding:5px; color:green;" onClick="$('input.f').val('');"></i>
                </td>
            </tr>

            <tr><td class=lab></td><td colspan=3>

                <input type="submit" class="but" style="margin-left:50px; width:80px;" value=" OK ">
            </td></tr>
        </table>
        ================================== FILTERS-END ==================================== ??

    <div id=cont style="margin:20px;">
    <table class="tlist tblue" style="border:solid 1px ##004060;" cellspacing=0>
        <tr>
            <th>№</th>
            <th class="srh" sr="u.FIO" style="width:130px;">Пользователь</th>
            <th>Даты</th>
            <th>Документ</th> ??show_doc
<th class="srh" sr="a.IP">IP</th> 
            <th class="srh"  style="width:290px;"sr="a.user_agent">agent</th>
            <th class="srh"  sr="s.DID">Dev.</th>
            <th class="srh"  style="width:420px;"sr="a.REF">Ref.</th> ??
        </tr>
[end]


[item]
    <tr
    class=oddRow ??oddRow=1
    >
        <td class="small nowrap">#NUM#</td>
        <td class="filter" f="f_user" val="#U_FIO#">#U_FIO#
        <small>(#U_ID#)</small> ??U_ID>0
        <small>(#REAL_U#)</small>  ??real_user_id>0&!real_user_id=#U_ID#
        </td>
        <td class="filter" f="f_c" val="#C#">
#DAY_START#-#DAY_END#
#REQUEST_NAME# <small>(#C#)</small>  ??
</td>
        <td class="filter small" f="f_IP" val="#IP#">#IP#</td>
        <td class="filter min" f="f_agent" val="#AGENT#">#AGENT#</td>
        <td class="filter min" f="f_did" val="#DID#">#DID#</td>
        <td class="filter small" f="f_ref" val="#REF#">#REF#</td> ??
        <td>#USER_AGENT#</td><td>#SESS#</td> ??
    </tr>
    $SET_PARAMETERS DID=; ERR=; FIXED=;
[end]



[report footer]
    <tr class=bg_blue><td colspan=15 class="align_left">
    $INCLUDE [rpp]  param: noTR=Y; ??!NumTableRows=0
    <input type=hidden name="rpp" value="#rpp#"> ??NumTableRows=0
    <b><i>ВСЕГО: #TOT_NUM_ROWS#</i></b> ??TOT_NUM_ROWS
    </TD></TR></TABLE>  
    $GET_DATA [getReCount]
    Всего записей: #NUM_RECS# / #TOT_NUM_RECS#
    <a href="#ServletPath#?c=sys/copyAccStory" target=_blank>Сбросить в архив</a> ??f_table=a_req_log
    </div>
    </form>

    #ERROR#
    $SET_PARAMETERS srt=; ??ERROR
    <div id=loadingMsg style="display:none;"><br><br><center><b>Загрузка...</b>
    <img src="#imgPath#wait.gif">
    </center></div>

    <script>
        var frm=document.theForm;

        selectOptionByVal(frm.f_table,'#f_table#'); ??f_table

        window.focus();

        setStandardEvents(); 
        showSrt("#srt#","sup"); ??!desc
        showSrt("#srt#","sdown"); ??desc

        var submitForm=function(reset) {
            if(reset) {
                document.theForm.srn.value=1; 
            }
            document.theForm.submit();
            return true;
        }

        var goToRow=function(nr)
        { 
          frm.srn.value=nr;  
          frm.submit();
          return true; 
        }


    </script>
    </body></html>
[end]

[rpp] param: noTR;
    <tr class=blue><td align=left> ??!noTR
    <font color=white><i>Выдавать по:
    <SELECT NAME="rpp" onChange="goToRow(1);">
    <OPTION>20<OPTION>50 <OPTION>100<OPTION VALUE="9999"> не огр.  
    </SELECT> записей &nbsp; &nbsp; &nbsp; Записи:</i> #rowLinks# |
    <script>try {selectOptionByVal(document.theForm.rpp, '#rpp#');} catch(e){;}</script> ??rpp
[end]


****************************************************************************
****************************************************************************
****************************************************************************

[preSQLs]
    try: select cast('#f_user#' as unsigned) as f_user_id ??f_user
    ;
    try: select #srn# + #rpp# * 10 as LIM ??srn&rpp
    ;
    try: select concat('<option value="', table_name,'"'
        , case when table_name='#f_table#' then ' selected ' else '' end
        ,'>',table_name,'</option>') as TNAMES
    from information_schema.tables
    where table_schema='#database#'
        and table_name like 'a_req_log%'
    order by table_schema
[end]


[getReCount]
    try: select count(a.ID) as NUM_RECS
    from #f_table# a
    $INCLUDE [criteria] 
    ;
    select count(*) as TOT_NUM_RECS from #f_table#
    ;
[end]


[SQL]
    $INCLUDE [SQL_]  ??RWACC
[end]

[SQL_]
    select 
        count(a.ID) as "NUM"
        , u.F as U_FIO
        , concat(u.F, ' (',a.USER_ID,')') as U_FIO ??
        , a.real_user_id, uu.F as "REAL_U" ??
        , a.C   ??
        , a.REQUEST_NAME ??
, DATE_FORMAT(min(a.DAT),'%d.%m') as DAY_START
, DATE_FORMAT(max(a.DAT),'%d.%m.%y') as DAY_END
        , group_concat(distinct a.IP order by a.IP separator '<br>') as "IP"
        , group_concat(distinct a.user_agent order by a.user_agent separator '<br>') as "AGENT"
        , a.DID 
    from #f_table# a
    $INCLUDE [criteria]
    group by 
        u.F
        concat(u.F, ' (',a.USER_ID,')') ??
        , a.IP  ??
        , a.user_agent ??
        , a.DID 
        order by U_FIO, min(a.DAT), AGENT, a.DID
limit #LIM# ??LIM>0&ZZZ
[end]
    order by 
        #srt# #desc# ??srt
       a.DAT desc ??!srt


[criteria]
  left join #table_users_full# u on u.id=a.user_id
  left join #table_users_full# uu on uu.id=a.real_user_id
  left join d_list d on d.id=a.doc_id left join d_types t on t.id=d.type_id ??show_doc
where 
    1=1
and real_user_id not in(97,413,2309,8329)
    and a.user_id=#f_user_id# ??f_user_id>0
    and (upper(u.login) like upper('#f_user#%') or upper(u.FIO) like upper('#f_user#%')) ??f_user&f_user_id=0
    and a.IP like '#f_IP#%' ??f_IP&!ip_exept
    and a.IP not like '#f_IP#%' ??f_IP&ip_exept
    and a.C like '#f_c#%' ??f_c
    and a.USER_ID>0
    and a.user_agent like '%#f_agent#%' ??f_agent
    and a.did='#f_did#' ??f_did

    and DATE_SUB(now(),INTERVAL #f_today# DAY) < a.DAT ??f_today&!f_dat
    and DATE_FORMAT(a.DAT,'%d.%m.%y')='#f_dat#' ??f_dat
    and extract(hour from a.dat)=#f_hour#  ??f_hour
[end]
mysql group_concat
 

37.220898%2C56.724471
37.167150%2C56.739968 
https://yandex.ru/maps/?z=11&ll=37.167150%2C56.739968
https://yandex.ru/maps/?ll=37.438726%2C56.639506&z=11