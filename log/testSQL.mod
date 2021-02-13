sys/testSQL.mod

[parameters]
service=dubna.walt.service.SQLExecService   ??USER_ID=2309|USER_ID=4241|VU
ReportName=SQL Executing
rowLink=|<a class=r href="javascript:goToRow(#srn_i#);">#srn_i#-#ern_i#</a> ??!currentPage
rowLink=|<font color=FFFF00><b>#srn_i#-#ern_i#</b></font> ??currentPage
tableCfg=this
thsnDelimiter=none  
[end]

[report]
<script type="text/javascript"> window.location.replace("https://adb2.jinr.ru#ServletPath#");</script> ??
[end]

[report header]
    $SET_PARAMETERS srn=1; ??!srn
    $SET_PARAMETERS rpp=10; ??!rpp
    $TIMER
    <HTML><HEAD><TITLE>SQL tester</TITLE>
    <META http-equiv=Content-Type content="text/html; charset=utf-8">
    <link rel="stylesheet" href="/bp.css" type="text/css"> ??
        <script type="text/javascript" src="#jsPath#jquery-1.11.0.min.js"></script> 
        <script language=JavaScript src="#jsPath#lib.min.js"></script>
        <script language=JavaScript src="#jsPath#sed.js"></script>

    </HEAD>

    <body style="padding:5px; background-color:##e0e0e0;">
    USER_ID=#USER_ID#; TIMER_START=#TIMER_START#; TIMER=#TIMER#; ??
    <form name="theForm" method="POST" enctype="multipart/form-data">
    <input type=hidden name="c" value="#c#">
    <input type=hidden name=cop value="">
    CURR_DB: <input type_=hidden size=4 name="CURR_DB" value="#CURR_DB#"> 

    <input type=hidden name=rpp value="#rpp#">  ??NumTableRows=0
    <input type=hidden name=srn value="">
SQL:
    <TEXTAREA NAME="SQL_TEXT" wrap="VIRTUAL" class="autoresize" style="font-size:12px; height:500px; width:99%; margin:10px 0 10px 0; border:ridge 3px ##a0a0a0;">#SQL_TEXT#</TEXTAREA>
    <br>
    <input class=ro readonly name=timer size=25 > ??
    <span id="timer">...</span>
    <input type=button name=b_doIt value=" Execute " onClick="doIt();">

    <br>
    <span id="cancmsg"> Press <font color=FF0000><b>&lt;Esc></b></font> to cancel</span>   ??cop=exe


    $USE_DB #CURR_DB# ??CURR_DB
    $ USE_DB #CURR_DB# ??CURR_DB

    <script>
        $INCLUDE free/js_service.js[report] ??
        var frm=document.theForm;
        var timer=0;
        var timerRunning = true;

        var log=function(){};  ??
    $INCLUDE free/main_js_noDB.cfg[log function_]

        function showtime()
        { if (timerRunning)
          { 
            $("##timer").html(timer + " s.");
            setTimeout("showtime()",1000);
            timer=timer+1;
          }
        }
        showtime(); frm.b_doIt.disabled=true; ??cop=exe

        function doIt()
        { frm.cop.value="exe";
          try { frm.srn.value="1"; } catch (e) {}
          frm.b_doIt.disabled=true;
          frm.submit();
        }

        function stopIt_()
        { timerRunning = false;
          try {  document.all['cancmsg'].style.visibility = 'hidden'; } catch (e) {}
          $("##timer").html("Execution canceled"); 
          frm.b_doIt.disabled=false;
          frm.cop.value="";
          frm.b_doIt.focus();
        //  return false;
        }

        /*
         * Непосредственно выполнение авторесайз textarea. Переопределение sed.js
         */
        var doResize = function (elem) {
            elem.style.height = 'auto';
            var h = elem.scrollHeight + 30;
            if(h > 700) h = 700;
//            log(4, "doResize... name=" + $(elem).prop("name") + "; id=" + $(elem).prop("id") + ": " + elem.scrollHeight + " / " + h);  
            elem.style.height = h + 'px';
        };


        function checkKey(ev) { if (event.keyCode == 27) stopIt_();}
        document.onkeydown = checkKey;
        textAreaResize();
        document.onstop=stopIt_;     ??cop=exe
    </script>

[end]


[begin_results]
    NUM_QUERIES=#NUM_QUERIES# ??

    <br><b>Запрос #SQL_NR#:</b><br>
     <xmp style="border:solid 1px grey; font-size:10px; padding:5px; background-color:white; display:inline-block;">#SQL_TEXT#</xmp>  ??NUM_QUERIES>1
[end]

[finished]
    <i> Executed in #timer#</i>
<style>
    table.result_table tr { font-size:9pt; vertical-align:top; } 
    table.result_table td, table.result_table th { font-size:9pt; vertical-align:top; } ??
    table.result_table td div { max-height:100px; overflow:auto;}
    a.r {color:white;}  
</style>
   <table class="result_table" border=0 bgcolor=gray cellspacing=0 cellpadding=0  style="border: solid 1 ##000080;">
   <tr><td>
[end]

[end_results]
    </td></tr></table>
[end]

[report footer]
    
    <table border=0 cellpadding=0 cellspacing=0 width=100%  style="border: solid 1 ##000000; background-color:##203060; color:white; font-size:12px;"><tr><td>&nbsp;&nbsp;
        Rows Per Page:  ??NumTableRows
        Get Top Rows:   ??!NumTableRows
        <SELECT NAME="rpp"><OPTION> 10<OPTION> 25<OPTION> 100<OPTION VALUE="99999">no limit</SELECT> &nbsp; &nbsp;
        Rows:</i> #rowLinks# | ??NumTableRows
    </td></tr></table>
    </form>
    TIMER 0=#TIMER#; TIMER_START=#TIMER_START#; ??
    $TIMER #TIMER_START#
    TIMER 1=#TIMER#; ??
    $TIMER #TIMER_START#s
    TIMER 2=#TIMER#; ??

    <script>
        timerRunning = false;
        $("##timer").html("");
        $("##timer").html("DONE!");  ??cop=exe

        frm.b_doIt.disabled=false;
        frm.b_doIt.focus();

        try 
        { frm.b_doIt.disabled=false;
          frm.b_doIt.focus();
          document.all['cancmsg'].style.visibility = 'hidden';
        }
        catch (e) {}

        selectOptionByVal(frm.rpp, "#rpp#");  ??rpp

        function goToRow(nr)
        { frm.srn.value=nr;
          frm.cop.value="exe";
          frm.submit(); 
        }
    </script>
[end]


[err msg]
    <script>timerRunning = false; $("##timer").html("SQL executed in #timer#"); </script> 
    <table bgcolor=white style="border: solid 1 ##FF0000;"><tr><td>
    <img src="#imgPath#alert.gif" width=16 height=16>&nbsp;Error:  #ERROR#
[end]


