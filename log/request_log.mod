sys/request_log.mod

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
title=***ЛОГ-СЭД
request_name=Лог запросов

LOG=OFF
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
$SET_PARAMETERS srt=a.DAT; desc=desc; CASE=Y; ??!srt
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
            <a href="#ServletPath#?c=sys/request_stat_hours">Статистика по времени>></a> &nbsp;
            <a href="#ServletPath#?c=sys/request_stat">Статистика по пользователям>></a> &nbsp;            
            <a class=info href="#ServletPath#?c=sys/showLog_noDB">Log>></a> &nbsp;
            <a class=info href="#ServletPath#?c=sys/showSysconst">sys>></a>      ??USER_ID=2309|USER_ID=413|VU=2309|USER_ID=4241|USER_ID=3663|USER_ID=4790
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
        $SET_PARAMETERS f_table=a_req_log; show_doc=on; f_login=Y; f_err=NF; f_today=8; ??!f_table
        <table border=0 cellpadding=4 style="border:solid 1px gray; margin:10px 0 0 200px; background-color:white;">
            <tr>
                <td class=lab>Пользователь:</td>
                <td colspan=3><input size=20 name="f_user" class="f" value="#f_user#"> &nbsp; 
                Таблица: <select name=f_table>#TNAMES#</select> &nbsp;
                <span class="pt" onClick="if(confirm('Очистить лог?')) {document.theForm.cop.value='clear'; submitForm(true);}"> ??debug_server=true
                    <i class="fa fa-times pt" aria-hidden="true" style="color:red;" onClick="$('input.f').val('');"></i>clear</span> ??debug_server=true
            </td></tr>

            <tr><td class=lab></td><td colspan=3>
                <input type=radio name="f_login" value=""
                    checked ??!f_login
                >все <input type=radio name="f_login" value="Y"
                    checked ??f_login=Y
                >залогиненые <input type=radio name="f_login" value="N"
                    checked ??f_login=N
                >не залогиненые
                <input type=checkbox name=A4>только lt-a4  ??

                <input type=checkbox name=exceptMe
                    checked ??exceptMe
                > кроме меня
            </td></tr>

            <tr><td class=lab>Модуль:</td><td><input size=20 name="f_c" class="f" value="#f_c#">
                <br>
                <input type=radio name="f_type" value=""
                    checked  ??!f_type
                >все
                <input type=radio name="f_type" value="DIRECT"
                    checked  ??f_type=DIRECT
                >direct
                <input type=radio name="f_type" value="AJAX"
                    checked  ??f_type=AJAX
                >AJAX
                <input type=radio name="f_type" value="MONITOR"
                    checked  ??f_type=MONITOR
                >MONITOR
                </td>
                <td class=lab>Запрос:</td><td><input size=25 name="f_query" class="f" value="#f_query#">
                    <br><input type="checkbox" name="CASE" value="Y"
                        checked ??CASE=Y
                        > case-sensitive
                </td>
            </tr>

            <tr><td class=lab>Документ:</td><td><input size=10 name="f_doc" class="f" value="#f_doc#">
                <input type=checkbox name="show_doc"
                    checked ??show_doc
                > показывать
            </td></tr>

            <tr><td class=lab>IP:</td><td><input size=12 name="f_IP" class="f" value="#f_IP#">
                <input type=checkbox name=ip_exept
                    checked ??ip_exept
                > кроме</td>
                <td class=lab>Сессия:</td><td><input size=30 name="f_SESS" class="f" value="#f_SESS#"></td>
            </tr>

            <tr><td class=lab>REF.:</td><td><input size=20 name="f_ref" value="#f_ref#"></td></tr> ??
            <tr><td class=lab>Agent:</td><td><input size=20 name="f_agent" class="f" value="#f_agent#">
                <td class=lab>Dev:</td><td><input size=15 name="f_did" class="f" value="#f_did#"></td>
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
                    час:<input size=3 name="f_hour" class="center f" value="#f_hour#"> &nbsp;
                    <i class="fa fa-times pt" aria-hidden="true" style="padding:5px; color:green;" onClick="$('input.f').val('');"></i>
                </td>
            </tr>

            <tr><td class=lab>Ошибки:</td><td colspan=3>
                <input type=radio name="f_err" value=""
                    checked ??!f_err
                >все <input type=radio name="f_err" value="Y"
                    checked ??f_err=Y
                >все ошибки 
                <input type=radio name="f_err" value="NF"
                    checked ??f_err=NF
                >не исправленные
                <input type=radio name="f_err" value="N" ??
                    checked ??f_err=N_ZZ
                > без ошибки ??
                $INCLUDE [f_comment] 
                    ??USER_ID=2309

                <input type="submit" class="but" style="margin-left:50px; width:80px;" value=" OK ">
            </td></tr>
        </table>
        ================================== FILTERS-END ==================================== ??

    <div id=cont style="margin:20px;">
    <table class="tlist tblue" style="border:solid 1px ##004060;" cellspacing=0>
        <tr><th style_="width:120px;" class="srh" sr="a.DAT">Дата</th>
            <th>ID</th>
            <th class="srh" sr="u.FIO" style="width:130px;">Пользователь</th>
            <th>c (Запрос)</th>
            <th>Документ</th> ??show_doc
            <th>Параметры запроса</th>
            <th class="srh" sr="a.TIM">сек.</th><th >Ошибка</th><th class="srh" sr="a.IP">IP</th> 
            <th class="srh"  style="width:290px;"sr="a.user_agent">agent</th>
            <th>Sess</th>
            <th class="srh"  sr="s.DID">Dev.</th>
            <th class="srh"  style="width:420px;"sr="a.REF">Ref.</th> ??
        </tr>
[end]

[f_comment]
    <input type=checkbox name=f_comment
        checked ??f_comment
    > с комментарием
[end]

[item]
    <tr
    class=oddRow ??oddRow=1
    ><td class="small nowrap">
        <span class="filter pt" f="f_dat" val="#DAY#">
            #DAY#
        </span>
        <span class="filter pt" f="f_hour" val="#HOUR#">
            #TIME#
        </span>

        </td>
        <td class="small nowrap">#queryLabel#</td>
        <td class="filter" f="f_user" val="#U_FIO#">#U_FIO#
        <small>(#U_ID#: #USERNAME#)</small> ??U_ID>0
        <small>(#REAL_U#)</small>  ??real_user_id>0&!real_user_id=#U_ID#
        </td>
        <td class="filter" f="f_c" val="#C#">#REQUEST_TYPE#: #REQUEST_NAME# <small>(#C#)</small></td>
        $INCLUDE [doc column]  ??show_doc
        <td class="pt small"  onClick="showQuery('#ROWID#');">#QUERY#</td>
        <td class=r>#SEC#</td>
        <td
            bgcolor=##FFD0B0 ??ERR&!FIXED
            >
            <div class="pt" onClick="showQuery('#ROWID#');"> #ERR# 
                -  ??!ERR
            </div>
            $INCLUDE [fix cb] ??ERR
        </td>
        <td class="filter small" f="f_IP" val="#IP#">#IP#</td>
        <td class="filter min" f="f_agent" val="#AGENT#">#AGENT#</td>
        <td class="filter min small" f="f_SESS" val="#SESS#">#SESS_M#</td>
        <td class="filter min small" f="f_did" val="#DID#">#DID#</td>
        <td class="filter small" f="f_ref" val="#REF#">#REF#</td> ??
        <td>#USER_AGENT#</td><td>#SESS#</td> ??
        <td>#comments#</td> ??
             ??USER_ID=2309

    </tr>
    $SET_PARAMETERS DID=; ERR=; FIXED=;
[end]

[fix cb]
    <br>
    <span id='results_#ROWID#'>
        <i>испр. #FIX_DAT#</i> ??FIXED
        <i>НЕ испр.: #FIX_DAT#</i> ??!FIXED&FIX_DAT
    </span>
    <input type=checkbox name=fixed onClick="setFixed(this.checked, '#ROWID#');"
        checked ??FIXED
    >  
[end]

[doc column]
    <td 
        class="pt" f="f_doc" val="#doc_id#" onClick="showDoc('#doc_id#');" ??doc_id>0
    >
        #DOC_TYPE# #DOC_NR#, #DOC_DAT# ??doc_id>0
    </td>
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

        function showQuery(row) {
           openWindow("c=sys/viewRequest&f_table=#f_table#&row=" + row ,"accReq"+row,1200,700);
        }

        var goToRow=function(nr)
        { 
          frm.srn.value=nr;  
          frm.submit();
          return true; 
        }

        var showDoc = function(doc_id) {
            window.open("#ServletPath#?c=docs/view_doc&doc_id=" + doc_id + "&mode=ext", "view_" + doc_id); ??
            window.open("#ServletPath#?sid=" + doc_id + "&et=4&key=#tm#", "dd_view");
            return false;
        }

        var setFixed=function(checked, row) {
        $.ajax({
          url: '#ServletPath#',
          type: 'POST',
          data: {c: "sys/setErrFixed", row: row, fixed: checked },
                dataType: "html",
                success: function(data) { 
                        alert("SUCCESS:" + data); ??
                          $('##results_' + row).html(data); 
                }, 
                error: function(jqXHR, textStatus, errorThrown) { 
                        alert("AJAX ERROR: " + textStatus + ": " + errorThrown); ??
                          $('##results_' + row).html(textStatus + ": " + errorThrown); 
                } 
        });
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
    delete from #f_table#; ??cop=clear
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
    order by 
    create_time desc ??
    table_schema
    table_name desc ??
[end]


[getReCount]
    try: select count(a.ID) as NUM_RECS
    from #f_table# a
    $INCLUDE [criteria] 
      ??f_user|f_query|f_IP|f_SESS|f_login|f_err|exceptMe
    ;
    select count(*) as TOT_NUM_RECS from #f_table#
    ;
[end]

select count(*) as NUM_RECS  from #f_table# a
  left join users u on u.id=a.user_id ??f_user
$INCLUDE [criteria]


[SQL]
    $INCLUDE [SQL_]  ??RWACC
[end]

[SQL_]
    select a.ID as ROWID, a.queryLabel
        , a.USER_ID as U_ID, u.login as USERNAME, u.F as U_FIO
        , a.real_user_id, uu.F as "REAL_U"
        , a.C, a.COOKIES
        , format(a.TIM / 1000, 1) as SEC 
        , a.REQUEST_NAME, a.REQUEST_TYPE
        , a.doc_id
        , t.short_name as "DOC_TYPE", d.number as "DOC_NR", DATE_FORMAT(d.doc_date,'#dateFormat#') as "DOC_DAT" ??show_doc
        , concat(substr(a.QUERY,1,40), case when length(a.QUERY)>40 then '...' else '' end) as QUERY
        , concat(substr(a.ERR,1,80), case when length(a.ERR)>80 then '...' else '' end) as ERR
        , DATE_FORMAT(a.DAT,'#dateTimeSecFormat#') as DAT 
        , DATE_FORMAT(a.DAT,'%H:%i:%S') as TIME
        , DATE_FORMAT(a.DAT,'%d.%m.%y') as DAY
        , DATE_FORMAT(a.DAT,'%H') as HOUR
        , a.IP
        , a.user_agent AGENT
        , concat(substr(a.REF,1,30),'...') as REF
        , a.SESS, concat('...' , substr(a.SESS,28)) as SESS_M
        , a.DID
        , s.DID ??
        , a.USER_AGENT, a.SESS ??
        , a.FIXED
        , a.comments
        , DATE_FORMAT(a.FIX_DAT,'#dateTimeFormat#') as FIX_DAT 
    from #f_table# a
    $INCLUDE [criteria]

    order by 
        #srt# #desc# ??srt
       a.DAT desc ??!srt
    limit #LIM# ??LIM>0
[end]

[criteria]
  left join #table_users_full# u on u.id=a.user_id
  left join #table_users_full# uu on uu.id=a.real_user_id
  left join d_list d on d.id=a.doc_id left join d_types t on t.id=d.type_id ??show_doc
  
where 
    1=1
and a.queryLabel like '% ***' ??A4
    and a.user_id=#f_user_id# ??f_user_id>0
    and (upper(u.login) like upper('#f_user#%') or upper(u.FIO) like upper('#f_user#%')) ??f_user&f_user_id=0
    and upper(a.QUERY) like upper('%#f_query#%')  ??f_query&!CASE=Y
    and a.QUERY like BINARY '%#f_query#%'  ??f_query&CASE=Y
    and a.IP like '#f_IP#%' ??f_IP&!ip_exept
    and a.IP not like '#f_IP#%' ??f_IP&ip_exept
    and a.C like '#f_c#%' ??f_c
    and a.request_type='#f_type#' ??f_type
    and a.SESS='#f_SESS#' ??f_SESS
    and a.REF like '%#f_ref#%' ??f_ref
    and a.USER_ID>0 ??f_login=Y
    and (a.USER_ID is null or a.USER_ID<1) ??f_login=N
    and not a.err is null and not a.err='' ??f_err=Y
    and a.err is null and not a.err=''  ??f_err=N
    and not a.err is null and not a.err='' and FIXED is null ??f_err=NF
    and a.USER_ID<>#USER_ID# and a.IP<>'#ClientIP#' ??exceptMe
    and a.user_agent like '%#f_agent#%' ??f_agent
    and a.doc_id=#f_doc# ??f_doc
    and a.did='#f_did#' ??f_did

    and DATE_SUB(now(),INTERVAL #f_today# DAY) < a.DAT ??f_today&!f_dat
    and DATE_FORMAT(a.DAT,'%d.%m.%y')='#f_dat#' ??f_dat
    and extract(hour from a.dat)=#f_hour#  ??f_hour

    and a.comments <> '' ??f_comment
[end]

??f_today&!f_dat