sys/showSysconst.mod

[comments]
SYS:
input=
output=HTML таблица http-запросов, 
parents=
childs=sys/viewRequest
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
title=***SYS
request_name=sys
LOG=ON
debug=off
tableCfg=table_no
[end]

[report header]
    $SET_PARAMETERS RWACC=
    $SET_PARAMETERS RWACC=Y;   ??USER_ID=2309|USER_ID=413|VU=2309|USER_ID=4241|USER_ID=3663|USER_ID=4790
    $INCLUDE [OK report header]  ??RWACC
[end]


[OK report header]
    $INCLUDE dat/common.dat[head]
    $SET_PARAMETERS srn=1; $SET_PARAMETERS rpp=9999;

    <style type="text/css">
        $INCLUDE free/main_css_noDB.cfg[report]
    </style>

    <style>
        body {background-color:##d0d0d0;}
    </style>
    </head>

    $SET_PARAMETERS val_param=val_#alias#   ??cop=save
    $SET_PARAMETERS #alias#=^#val_param#;  ??cop=save
    $GET_DATA [update SQL] ??cop=save
    $SET_PARAMETERS_GLOBAL #alias#=#NEW_VAL#; const_inited=;  ??cop=save


<body>
    <table border=0 cellpadding=0 cellspacing=0 width=98% style="margin:10px;">
        <tr><td class="bg_white" width=1% nowrap style="padding:2px;"><h3>СЭД&nbsp;-&nbsp;sys</h3></td>
        <td style="padding:5px 0 2px 20px;">
            <a class=info href="#ServletPath#?c=sys/request_log">Log запросов>></a> &nbsp;
            <a class=info href="#ServletPath#?c=sys/showLog_noDB">Log>></a> &nbsp;
            <a href="#ServletPath#?c=sys/request_stat">Статистика>></a> &nbsp;
            <a href="#ServletPath#?c=sys/doc_access_log">Документы>></a> &nbsp;
        </td>
        <td align=right nowrap=true><a class=info href="/sed/dubna">Главная</a></td>
    </tr></table>

    <form name="theForm" method="POST" enctype="multipart/form-data">
        <input type=hidden name="c" value="#c#">
        <input type=hidden name="cop" value="">
        <input type=hidden name="alias" value="">

    <table class="tlist tblue" style="border:solid 1px ##004060;" cellspacing=0>
        <tr>
            <th>alias<br><input size=20 name="f_alias" value="#f_alias#" onChange="refrSelf();"></th>
            <th>значение</th>
            <th>тип</th>
            <th>комментарий</th>
        </tr>
[end]


[item]
    <tr
    class=oddRow ??oddRow=1
    ><td class="nowrap">#alias#</td>
        <td>
            <div id="d_#alias#" class="ro pt" onClick="setRW('#alias#');"><xmp>#value#</xmp></div>
            <div id="dw_#alias#" class="rw" style="display:none;">
                <textarea class="val" id="val_#alias#" name="val_#alias#" style="width:95%; height:50px;">#value#</textarea>
                <br><center><span class="pt" onClick="doSave('#alias#')">сохранить</span> &nbsp;
                <span class="pt" onClick="$('.rw').hide();$('.ro').show();">отмена</span></center>
            </div>
        </td>
        <td>#type#</td>
        <td>#comment#</td>
    </tr>
[end]
 
[report footer]
    </TABLE>  
    </form>

    <script>
        var frm=document.theForm;
        setStandardEvents();  

        var setRW=function(a) {
            log(3,"setRW: " + a);
            if(a.indexOf('~') == 0) {
                $("##dw_\\" + a).show();        
                $("##d_\\" + a).hide(); 
            }
            else {
                $("##dw_" + a).show();        
                $("##d_" + a).hide(); 
            }
        }

        var doSave=function(alias){
            frm.cop.value="save";
            frm.alias.value=alias;
            if(alias.indexOf('~') == 0)
                $("textarea.val").not("##val_\\" + alias).val("");
            else
                $("textarea.val").not("##val_" + alias).val("");
            frm.submit();
        }
    </script>
    </body></html>
[end]


****************************************************************************
****************************************************************************
****************************************************************************

[update SQL]
    update sys_const set value=replace(replace('^#alias#','\r',''),'\n','') where alias='#alias#'
;
    select value "NEW_VAL" from sys_const where alias='#alias#' 
[end]

[SQL]
    $INCLUDE [SQL_]  ??RWACC
[end]

[SQL_]
    select alias
        , replace(value,',','\r,') as value
        , type, comment
    from sys_const
    where alias like '%#f_alias#%' ??f_alias
    order by alias  ??!f_alias       
    order by value  ??f_alias       
[end]

