sys/viewRequest.cfg

[comments]
SYS: Лог http-запросов - детальный просмотр одного запроса

input=row - ID записи в таблице #f_table#
output=HTML форма с информацией о запросе. 
parents=sys/request_log
childs=sys/setErrFixed
[end]


[parameters]
title=***Запрос
LOG=ON
[end]



[report]
$INCLUDE [report_] 
[end]

[report_]
    $INCLUDE dat/common.dat[head]
    <style type="text/css">
        $INCLUDE free/main_css_noDB.cfg[report]
    </style>

    <script type="text/javascript">
        $INCLUDE free/main_js_noDB.cfg[report]
    </script>
    <SCRIPT language=JavaScript src="#jsPath#lib.js"></SCRIPT>

    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css"> ??
    <style>
        td {font-size:11pt;}
        td.lab {text-align:right;}
         ##cook { width: 500px; height: 150px; padding: 0.5em; overflow:hidden; }
    </style>

    <script>
        function setFixed(checked, row) {
        $.ajax({
          url: '#ServletPath#',
          type: 'POST',
          data: {c: "sys/setErrFixed", row: "#row#", fixed: checked, table: "#f_table#" },
                dataType: "html",
                success: function(data) { 
                        alert("SUCCESS:" + data); ??
                          $('##results').html(data); 
                }, 
                error: function(jqXHR, textStatus, errorThrown) { 
                        alert("AJAX ERROR: " + textStatus + ": " + errorThrown); ??
                          $('##results').html(textStatus + ": " + errorThrown); 
                } 
            });
        }

        function saveComment(row) {
            $("##comment_result").html("...");
            var comment = $('##a_comments').val();
            $.ajax({
              url: '#ServletPath#',
              type: 'POST',
              data: {c: "sys/setErrFixed", row: "#row#", comment: comment, table: "#f_table#" },
                    dataType: "html",
                    success: function(data) { 
                            alert("SUCCESS:" + data); ??
                              $('##comment_result').html(data); 
                    }, 
                    error: function(jqXHR, textStatus, errorThrown) { 
                            alert("AJAX ERROR: " + textStatus + ": " + errorThrown); ??
                              $('##results').html(textStatus + ": " + errorThrown); 
                    } 
                });
        }

        $(function() {
          $( "##cook" ).resizable();
        });
    </script>
</head>

<body bgcolor=F4FFFA style="padding:20px;">
<table border=0 cellpadding=0 cellspacing=0 width=98%>
    <tr><td width=90%><h3>Запрос #queryLabel# </h3> #DAT# :</td>
    <td align=right nowrap=true>
    </td><td align=right nowrap=true>
    </td></tr></table>

    <table border=0 cellpadding=4 style="border:none 1px gray; margin:10px 0 0 10px; background-color:white;">
        <tr><td class=lab>Пользователь:</td><td>#U_FIO#
        (#U_ID#: #USERNAME#) ??U_ID>0
        </td></tr>

        <tr><td class=lab>c:</td><td>#C# (#TIME# сек.)</td></tr>

        <tr><td class=lab>Запрос:</td><td><a href="#ServletPath#?#QUERY#" target=_blank>#QUERY_#</a></td></tr>
        <tr><td class=lab>Куки:</td><td><div id="cook_" class="ui-widget-content" style_="height:70px;">#COOKIES#</div></td></tr>

        $INCLUDE [err]  ??ERR

        <tr><td class=lab>IP:</td><td>#IP#</td></tr>

        <tr><td class=lab>REF.:</td><td><a target=_blank href="#REF#">#REF#</a></td></tr> ??REF

        <tr><td class=lab>Браузер:</td><td>#USER_AGENT#</td></tr>
        <tr><td class=lab>Сессия:</td><td>#SESS_ID# (#SESS#)</td></tr>
        <tr><td class=lab></td><td>#row#</td></tr>
        $INCLUDE [comment]

        <tr><td></td><td align=center><input type="button" class="but" value=" Закрыть " onClick="window.close();"></td></tr>
    </table>
</body></html>
[end]

[comment]
    <tr><td class=lab>Комментарий:
            <br> 
        </td><td>
        <textarea rows=2 name="a_comments" id="a_comments" spellcheck=false style="width:70%;">#comments#</textarea>
        <input type="button" class="but" value=" Сохранить " onClick="saveComment('#row#');">
        <span id="comment_result"></span>
    </td></tr>
[end]

[err]
    <tr><td class=lab>Ошибка:</td><td>
        <textarea rows=10 cols_=80 spellcheck=false style="width:100%;">#ERR#</textarea><br> 
        <input type=checkbox name=fixed onClick="setFixed(this.checked, '#row#');"
        checked ??FIXED
        ><span id=results>
        НЕ ??!FIXED
        исправлено 
        #FIX_DAT#</span>
    </td></tr>
[end]


****************************************************************************
****************************************************************************
****************************************************************************

[preSQLs]
select 
    a.USER_ID as U_ID, u.login as USERNAME, concat(u.F, ' ', u.I, ' ', u.O) as U_FIO
   , a.C, a.COOKIES
   , a.QUERY
   , replace(a.QUERY,'¦','<br>') as QUERY ??
    , replace(replace(a.QUERY,'&','<br>'),'¦','<br>') as QUERY_

   , format(a.TIM / 1000, 1) as TIME
   , replace(a.ERR,'`','''') as ERR
   , DATE_FORMAT(a.DAT,'#dateTimeFormat#:%s') as DAT
   , a.queryLabel
   , a.IP
   , a.REF as REF
   , a.SESS_ID
   , a.USER_AGENT, a.SESS 
   , a.FIXED, DATE_FORMAT(a.FIX_DAT,'#dateTimeFormat#') as FIX_DAT
   , a.comments
from #f_table# a
    left join #table_users_full# u on u.id=a.user_id
where 
    a.ID='#row#'
;

[end]
