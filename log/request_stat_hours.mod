sys/request_stat_hours.mod

[comments]
SYS: Статистика http-запросов.
input=
output=HTML таблица http-запросов, 
parents=
childs=sys/viewRequest
[end]


[parameters]
service=dubna.walt.service.TableServiceSimple
service=dubna.walt.service.TableServiceSpecial ??
title=***СТАТ-СЭД
request_name=Статистика запросов

debug=off
KeepLog=false
tableCfg=table_no

[end]

[report header]
$GET_DATA [sql_mode]
$SET_PARAMETERS RWACC=Y; 
 ??USER_ID=1|USER_ID=28
$INCLUDE [OK report header]  ??RWACC
[end]


[OK report header]
$INCLUDE dat/common.dat[head]

<style type="text/css">
$INCLUDE free/main_css_noDB.cfg[report]
</style>

<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>  ??
<script type="text/javascript" src="#jsPath#canvasjs.min.js"></script>


  
<script type="text/javascript">
$INCLUDE free/main_js_noDB.cfg[report]    
</script>
<SCRIPT language=JavaScript src="#jsPath#lib.js"></SCRIPT>

<style>
table.tlist td {text-align:left; padding:3px;}
table.tlist td.r {text-align:right;}
td.lab {text-align:right;}
.filter {color:black;}
body {background-color:##c0c0c0;}
</style>

</head>

<body>

<table border=0 cellpadding=0 cellspacing=0 width=98% style="margin:10px;">
<tr><td class="bg_white" width=1% nowrap style="padding:2px;"><h3>СЭД&nbsp;-&nbsp;статистика&nbsp;запросов&nbsp;по&nbsp;времени</h3></td>
<td style="padding:5px 0 2px 20px;">    
    <a href="#ServletPath#?c=sys/request_log">Статистика>></a> &nbsp;
    <a href="#ServletPath#?c=sys/request_stat">Статистика по пользователям>></a> &nbsp;
    <a class=info href="#ServletPath#?c=sys/showLog_noDB">Log>></a> &nbsp;
<td align=right nowrap=true>
</td><td align=right nowrap=true>
<a class=info href="/adb/adb">
Главная</a>
</td></tr></table>

<form name="theForm" method="POST" enctype="multipart/form-data" >
<input type=hidden name="c" value="#c#">
<input type=hidden name="cop" value="">
<input type=hidden name="srn" value="#srn#">
<input type=hidden name="srt" value="#srt#">
<input type=hidden name="desc" value="#desc#">

================================== FILTERS ==================================== ??
$SET_PARAMETERS f_table=a_req_log; show_doc=on; f_login=Y; f_err=NF; f_today=8; ??!f_table
<center>
<table border=0 cellpadding=0 cellspacing=0 style="border:solid 1px gray; margin:10px 0 20px 0px; background-color:white;"><tr><td>


================================== ГРУППИРОВКА ==================================== ??

<table border=0 cellpadding=4 style="border-bottom:solid 1px gray; border-top:solid 1px gray;">
    <tr>
        <td colspan=2 style="text-align:center; font-weight: bold;">Группировка запросов по времени
            
        </td>
    </tr>
    <tr><td class=lab>Пользователь:</td>
        <td><input size=20 autocomplete="off" name="f_user" value="#f_user#"> &nbsp; 
                Таблица: <select name=f_table>#TNAMES#</select> &nbsp;
                <span class="pt" onClick="if(confirm('Очистить лог?')) {document.theForm.cop.value='clear'; submitForm(true);}"> ??debug_server=true
                    <i class="fa fa-times pt" aria-hidden="true" style="color:red;" onClick="$('input.f').val('');"></i>clear</span> ??debug_server=true
            </td>
    </tr>
    <tr><td class=lab></td><td colspan=2>
            <input type=radio name="f_login" value=""
                checked ??!f_login
                >все <input type=radio name="f_login" value="Y"
                checked ??f_login=Y
                >залогиненые <input type=radio name="f_login" value="N"
                checked ??f_login=N
                >не залогиненые
                <input type=checkbox name=exceptMe
                checked ??exceptMe
                > кроме меня
        </td></tr>
    <tr><td class=lab>C:</td><td colspan=3><input size=15 autocomplete="off" name="f_c" value="#f_c#">
            Запрос: <input size=15 autocomplete="off" name="f_query" value="#f_query#"></td>
    </tr>
    <tr><td class=lab>IP:</td><td><input size=12 autocomplete="off" name="f_IP" value="#f_IP#">
            <input type=checkbox name=ip_exept
                checked ??ip_exept
                > кроме</td>
   <tr><td class=lab>REF.:</td><td><input size=20 autocomplete="off" name="f_ref" value="#f_ref#">
       <!--     <input type=radio name="f_mob" value=""
                checked ??!f_mob
                >все <input type=radio name="f_mob" value="N"
                checked ??f_mob=N
                >полная версия <input type=radio name="f_mob" value="Y"
                checked ??f_mob=Y
                >моб.версия -->
    </td></tr> 
    <tr>
        <td class=lab>за период:</td>
        <td>c <input id="date_for_hour_1" class="center f f_date"  type="text" autocomplete="off" size=8 name="date_for_hour_1" value="#date_for_hour_1#" onchange="$('input.today').val('');"> 
            по <input id="date_for_hour_2" class="center f f_date"  type="text" autocomplete="off" size=8 name="date_for_hour_2" value="#date_for_hour_2#" onchange="$('input.today').val('');"> (дд.мм.гг)
            <i class="fa fa-times pt" aria-hidden="true" style="padding:5px; color:red;" onClick="$('input.f').val('');"></i>
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <input type=checkbox class="today" id="today" name="today" value="2" 
                checked ??today=2
                onchange="$('input.f').val('');"> за сегодня
        </td>
    </tr>
================================== ГРУППИРОВКА ==================================== ??

<tr><td class=lab>Группировка:</td><td>
<select name=f1>
$INCLUDE [options]
</select>
<br>
</td></tr>
</table>
            <br><center>
                <input type="submit" class="butt1" style="width:80;" value=" OK "  >
            </center>
<br>
        </td>
    </tr>
</table>


================================== FILTERS-END ==================================== ??

<script>

var frm=document.theForm;
$(".f_date").datepick();
function refrSelf()
{ document.theForm.submit();
}
selectOptionByVal(frm.f_table,'#f_table#'); ??f_table
setStandardEvents(); ??
window.focus();

</script>
<div id="chartContainer" style="height: 370px; max-width: 650px; margin: 0px auto;"></div> ??!f1=DAY&!f1=HOUR&!f1=WEEK_DAY&!f1=TENMIN&!f1=WEEK1
<div id="chartContainer" style="height: 370px; max-width: 800px; margin: 0px auto;"></div> ??f1=DAY
<div id="chartContainer" style="height: 370px; max-width: 650px; margin: 0px auto;"></div> ??f1=WEEK1
<div id="chartContainer" style="height: 370px; max-width: 650px; margin: 0px auto;"></div> ??f1=WEEK_DAY
<div id="chartContainer" style="height: 370px; max-width: 1100px; margin: 0px auto;"></div> ??f1=HOUR
<div id="chartContainer" style="height: 370px; max-width: 1600px; margin: 0px auto;"></div> ??f1=TENMIN
<br/>
<table id="mytab1" class="tlist tblue" style="border:solid 1px ##004060;" cellspacing=0>
[end]

[options]
<option value="DAY" selected>День (дата)</option>
<option value="WEEK1" >Неделя</option>
<option value="WEEK_DAY" >День недели</option>
<option value="HOUR">Час</option>
<option value="TENMIN">10 минут</option>
[end]

[report footer]
</TABLE>
<br/>


</center>
</form>
$INCLUDE [java day] ??!f1=DAY&!f1=HOUR&!f1=WEEK_DAY&!f1=TENMIN&!f1=WEEK1
$INCLUDE [java day] ??f1=DAY
$INCLUDE [java week] ??f1=WEEK1
$INCLUDE [java week day] ??f1=WEEK_DAY
$INCLUDE [java hour] ??f1=HOUR
$INCLUDE [java tenmin] ??f1=TENMIN
</body></html>
[end]

[java day]
<script>

var frm=document.theForm;
selectOptionByVal(frm.f1,'#f1#');

var marks = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks.push({    	 
    	y: parseInt($(columns[2]).html()),           
        label: $(columns[0]).html(),
      });   
}

var marks_mob = [];
var rows = $('#mytab1 tbody>tr');
var columns;
for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks_mob.push({    	 
    	y: parseInt($(columns[3]).html()),           
        label: $(columns[0]).html(),
      });   
}


var chart = new CanvasJS.Chart("chartContainer",
    {        
    colorSet: "wmwcolorset",
    axisX:{
        title: "день (дата)",
        interval: 4
        labelAngle: -70 ??
     },
    axisY:{
        title: "количество запросов"        
     },
      data: [{
        type: "stackedColumn",
	showInLegend: true,
        name: "Мобильная версия СЭД",
	color: "#EEAE79",
        dataPoints: marks_mob,    
      },
    {
        type: "stackedColumn",
	showInLegend: true,
        color: "#639AA6",
	name: "Полная версия СЭД",
        dataPoints: marks,               
      }]
    });
  
chart.render();

</script>
[end]

[java week day]
<script>

var frm=document.theForm;
selectOptionByVal(frm.f1,'#f1#');

var marks = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks.push({    	 
    	y: parseInt($(columns[2]).html()),      
        label: $(columns[0]).html(),
      });   
}

var marks_mob = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks_mob.push({    	 
    	y: parseInt($(columns[3]).html()),      
        label: $(columns[0]).html(),
      });   
}

var chart = new CanvasJS.Chart("chartContainer",
    {        
    colorSet: "wmwcolorset",
    axisX:{
        title: "день недели",
        interval: 1
     },
    axisY:{
        title: "количество запросов"        
     },
      data: [{
        type: "stackedColumn",
	showInLegend: true,
        name: "Мобильная версия СЭД",
	color: "#EEAE79",
        dataPoints: marks_mob,
      },
    {
        type: "stackedColumn",
	showInLegend: true,
        color: "#639AA6",
	name: "Полная версия СЭД",
        dataPoints: marks,           
      }]
    });
 
chart.render();

</script>
[end]

[java week]
<script>

var frm=document.theForm;
selectOptionByVal(frm.f1,'#f1#');

var marks = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks.push({    	 
    	y: parseInt($(columns[2]).html()),      
        label: $(columns[0]).html(),
      });   
}

var marks_mob = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks_mob.push({    	 
    	y: parseInt($(columns[3]).html()),      
        label: $(columns[0]).html(),
      });   
}

var chart = new CanvasJS.Chart("chartContainer",
    {        
    colorSet: "wmwcolorset",
    axisX:{
        title: "по неделям",
        interval: 1
        labelAngle: -70 ??
     },
    axisY:{
        title: "количество запросов"        
     },
      data: [{
        type: "stackedColumn",
	showInLegend: true,
        name: "Мобильная версия СЭД",
	color: "#EEAE79",
        dataPoints: marks_mob,  
      },
    {
        type: "stackedColumn",
	showInLegend: true,
        color: "#639AA6",
	name: "Полная версия СЭД",
        dataPoints: marks,               
      }]
    });
  
chart.render();

</script>
[end]

[java hour]
<script>
var frm=document.theForm;
selectOptionByVal(frm.f1,'#f1#');

var marks = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks.push({
    	x: parseInt($(columns[0]).html()), 
    	y: parseInt($(columns[2]).html()),          
      });
}

var marks_mob = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');  
    marks_mob.push({
    	x: parseInt($(columns[0]).html()), 
    	y: parseInt($(columns[3]).html()),          
      });
}

var chart = new CanvasJS.Chart("chartContainer",
    {        
      axisX:{
        title: "часы",
        interval: 1
     },
    axisY:{
        title: "количество запросов"        
     },
      data: [{
        type: "stackedColumn",
	showInLegend: true,
        name: "Мобильная версия СЭД",
	color: "#EEAE79",
        dataPoints: marks_mob,
      },
    {
        type: "stackedColumn",
	showInLegend: true,
        color: "#639AA6",
	name: "Полная версия СЭД",
        dataPoints: marks, 
 
      }]
    });
  
chart.render();

</script>
[end]

[java tenmin]
<script>
var frm=document.theForm;

selectOptionByVal(frm.f1,'#f1#');

var marks = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');
    console.log("i=" + i  + ": " + $(columns[1]).html());
    marks.push({    	
    	y: parseInt($(columns[2]).html()),    
    	label: $(columns[0]).html()
      });  
}

var marks_mob = [];
var rows = $('#mytab1 tbody>tr');
var columns;

for (var i = 1; i < rows.length; i++) {    
  columns = $(rows[i]).find('td');
    console.log("i=" + i  + ": " + $(columns[1]).html());
    marks_mob.push({    	
    	y: parseInt($(columns[3]).html()),    
    	label: $(columns[0]).html()
      });  
}

var chart = new CanvasJS.Chart("chartContainer",
    {        
      axisX:{
        title: "время"
        , interval: 2
        , labelAngle: -60  
     },
    axisY:{
        title: "количество запросов"        
     },
      data: [{
        type: "stackedColumn",
	showInLegend: true,
        name: "Мобильная версия СЭД",
	color: "#EEAE79",
        dataPoints: marks_mob,
      },
    {
        type: "stackedColumn",
	showInLegend: true,
        color: "#639AA6",
	name: "Полная версия СЭД",
        dataPoints: marks,
      }]
    });
  
chart.render();

</script>
[end]
****************************************************************************
****************************************************************************
****************************************************************************

[preSQLs]
try: select cast('#f_user#' as unsigned) as f_user_id ??f_user
;
try: select count(a.ID) as TOT_NUM_ROWS
from a_req_log a
$INCLUDE [criteria] ??f_user|f_query|f_IP|f_sess_id|f_login||exceptMe
;
try: select concat('<option value="', table_name,'"'
    , case when table_name='#f_table#' then ' selected ' else '' end
    ,'>',table_name,'</option>') as TNAMES
    from information_schema.tables
    where table_schema='#database#'
    and table_name like 'a_req_log%'
    order by    
    table_schema
[end]

[SQL]
select
  DATE_FORMAT(a.DAT,'%d.%m.%Y') as DAY ??f1=DAY
, DATE_FORMAT(a.DAT,'%d.%m.%Y') as DAY ??!f1=DAY&!f1=HOUR&!f1=WEEK_DAY&!f1=TENMIN&!f1=WEEK1
, DATE_FORMAT(a.DAT,'%H') as HOUR   ??f1=HOUR
, DATE_FORMAT(a.DAT,'%d.%m.%Y') as WEEK1 ??f1=WEEK1
, concat(lpad(WEEKDAY(a.DAT),1, ' '), "-", lpad(DAYNAME(a.DAT), 15, ' ')) as WEEK_DAY ??f1=WEEK_DAY
, concat(lpad(hour(a.DAT),2,'0'), ":", lpad((minute(a.DAT) DIV 10) * 10, 2, '0')) as TENMIN ??f1=TENMIN
, count(a.ID) as NUM_QUERIES
, count(CASE WHEN a.C not LIKE 'mob%' THEN 1 END) as NUM_QUERIES_FULL
, count(CASE WHEN a.C LIKE 'mob%' THEN 1 END) as NUM_QUERIES_MOB

from #f_table# a
$INCLUDE [criteria]

group by      
     DAY    ??!f1=DAY&!f1=HOUR&!f1=WEEK_DAY&!f1=TENMIN&!f1=WEEK1
     DAY    ??f1=DAY
     HOUR   ??f1=HOUR
     WEEK(a.DAT)    ??f1=WEEK1 
     WEEK_DAY    ??f1=WEEK_DAY      
     TENMIN  ??f1=TENMIN 

order by DATE_FORMAT(a.DAT,'%H') desc   ??f1=HOUR
order by WEEK_DAY  ??f1=WEEK_DAY     
order by TENMIN ??f1=TENMIN 
order by DAY    ??f1=DAY 
   
[end]

[criteria]
left join #table_users_full# u on u.id=a.user_id

where 
    1=1
    and a.user_id=#f_user_id# ??f_user_id>0
    and (upper(u.login) like upper('#f_user#%') or upper(u.FIO) like upper('#f_user#%')) ??f_user&f_user_id=0
    and upper(a.QUERY) like upper('%#f_query#%')  ??f_query
    and a.IP like '#f_IP#%' ??f_IP&!ip_exept
    and a.IP not like '#f_IP#%' ??f_IP&ip_exept
    and a.C like '#f_c#%' ??f_c    
    and a.REF like '%#f_ref#%' ??f_ref
    and a.USER_ID>0 ??f_login=Y
    and a.USER_ID=0 ??f_login=N   
    and a.USER_ID<>#USER_ID# and a.IP<>'#ClientIP#' and a.IP<>'159.93.40.211' ??exceptMe

and a.DAT>=STR_TO_DATE('#date_for_hour_1#', '%d.%m.%Y') ??date_for_hour_1
and a.DAT<=STR_TO_DATE('#date_for_hour_2#', '%d.%m.%Y')+ INTERVAL 1 DAY ??date_for_hour_2

    and a.DAT>= CURDATE() ??today


[end]

[ColNames]
DAY=день
HOUR=час
WEEK1=недели
WEEK_DAY=день недели
TENMIN=10 минут
NUM_QUERIES=кол-во
NUM_QUERIES_MOB=кол-во моб
NUM_QUERIES_FULL=кол-во полной
[end]

[sql_mode]
SET sql_mode = ''
[end]