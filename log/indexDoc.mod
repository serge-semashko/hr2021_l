sys/indexDoc.mod

[comments]
descr=U: Индексация документа
input=doc_id - ID документа (из таблицы d_list)
output=
parents=
childs=
test_URL=?c=sys/indexDoc&doc_id=16059&verbose=3
author=Куняев
[end]

[description]
Общая часть:
<ul>
<li>Получение информации о структуре документа</li>
</ul>

[end]


[parameters]
service=jinr.sed.ServiceIndexDoc
request_name=S:Индексация документа
LOG=ON
SYS_FIELDS=DOC_ID, IS_ACTIVE, DATE_FORMAT(modified,'#dateTimeFormat#') as DOC_MODIFIED, VERSION
SYS_FIELDS_TYPES=int,int,varchar,int
divider=<tr><td colspan=2 class="divider"></td></tr> 
[end]


[report]
$SET_PARAMETERS debug=on;  ??
    $INCLUDE dat/common.dat[check login]
    $LOG1 <b>============== indexDoc: doc_id=#doc_id#;  ==================</b><br>
    $CALL_SERVICE c=sys/getARUD; ??

    $SET_PARAMETERS DOC_DATA_RECORD_ID=;
    $GET_DATA [getDocInfo]

    $GET_DATA docs/edit_doc.ajm[getDocTextFields4Index]
    $GET_DATA [get doc fields] ??DOC_DATA_RECORD_ID

    $INCLUDE [doc title]  ??verbose=3
[end]

[doc title]
    <a href="#ServletPath#?c=docs/view_doc&mode=ext&doc_id=#doc_id#" target=_blank> #doc_id# #TITLE#</a>
    <b>#DOC_TYPE# 
        №#NUMBER# ??NUMBER
        от #DOC_DATE# ??DOC_DATE
    <span class="bg_red">УДАЛЕН</span> ??is_deleted=1
    </b>
[end]

[report footer]
    <br>#context#<hr>    ??verbose=3
[end]

==============================================================================
***************************** Шаблоны SQL запросов ***************************
==============================================================================
[getDocInfo]
    select 
      dtp.NAME as "DOC_TYPE", dtp.ID as "DOC_TYPE_ID"
        , concat('d_data_', cast(dtp.id as char)) as TABLE_NAME
        , dh.type_version as "DOC_TYPE_VERSION"
        , dh.TITLE
        , dh.comment as "INITIATOR_COMMENT"

        , dm.monitor_comment
        , GROUP_CONCAT(dma.monitor_comment, ',<br><i>' ??
            , iof(uma.F, uma.I, uma.O) ??
            , ', ', ifnull(DATE_FORMAT(dma.comment_date,'#dateTimeFormat#'),''), '</i><br>') as MMMMM ??
    from d_list dh 
        left join d_types dtp on dtp.Id = dh.type_id
        left join doc_groups dg on dg.Id = dtp.group_id
        left join #table_users_full# u on u.Id = dh.creator_id
        left join #table_users_full# uu on uu.Id = dh.modifier_id
        left join d_marked dm on (dm.doc_id=dh.id and dm.user_id=#USER_ID#)
    where dh.Id = #doc_id#
    limit 0,1 
    ; 

    try: select /* LAST doc data record ID */ id as DOC_DATA_RECORD_ID 
    from #TABLE_NAME# 
    where doc_id = #doc_id# and is_active=1
     and version=#DOC_VERSION# ??DOC VERSIONS NOT IMPLEMENTED
    order by modified desc
    limit 0,1
;
    select GROUP_CONCAT(uma.F, ': ', dc.comment
        ORDER BY dc.dat
        SEPARATOR ''
    ) as "doc_comments"
    from 
        d_comments dc
        left join #table_users_full# uma on uma.Id = dc.user_id 
    where
        dc.doc_id=#doc_id#
[end]

        , u.FIO as CREATOR, u.email as "CRE_MAIL", u.phone as "CRE_PHONE", u.ID as CREATOR_ID
        , uu.FIO as MODIFIER
        , ifnull(DATE_FORMAT(dh.doc_date,'#dateFormat#'),'') as DOC_DATE
        , ifnull(DATE_FORMAT(dh.created,'#dateTimeFormat#'),'') as CREATED
        , ifnull(DATE_FORMAT(dh.modified,'#dateTimeFormat#'),'') as MODIFIED
        , ifnull(DATE_FORMAT(dh.started,'#dateTimeFormat#'),'') as STARTED
        , dg.has_wf as "HAS_WF", dh.is_deleted


;
try: select concat(dtf.field_db_name, ',') as FIELDS
 , concat(dtf.name, ',') as FIELDS_NAMES 
 , concat(dtf.type, ',') as FIELDS_TYPES
 , concat(dtf.form_field_type, ',') as FORM_FIELDS_TYPES
from d_fields dtf
where dtf.type_id = #DOC_TYPE_ID#
    and dtf.is_visible=1 ??
    and dtf.is_active=1 
    and dtf.min_doc_type_version <= #DOC_TYPE_VERSION#  
    and dtf.max_doc_type_version >= #DOC_TYPE_VERSION#  
    and dtf.field_category>1  ??request=write 
order by nr
;
select as "DOC_TEXT_NAMES" ??



[get doc fields]
select #DOC_TEXT_FIELDS#id from #TABLE_NAME# 
where id=#DOC_DATA_RECORD_ID#
[end]

[get doc monitor comment]
    select GROUP_CONCAT('<hr>'
        , dma.monitor_comment, ',<div class="text-ir">'
        , iof(uma.F, uma.I, uma.O), ', '
        , ifnull(DATE_FORMAT(dma.comment_date,'#dateTimeFormat#'),'')
, '</div>' 
        ORDER BY dma.comment_date
        SEPARATOR ''
    ) as "MONITOR_COMMENT"
    from 
        d_marked dma
        left join #table_users_full# uma on uma.Id = dma.user_id 
    where
        dma.doc_id=#doc_id#
[end]


Общий запрос для view_doc_no_wf.cfg, view_doc_wf.cfg, doc_edit.cfg, obj/edit_object.cfg
[SQL_]
select /* doc data fields description */
  dtf.id as "FIELD_ID", dtf.NR, dtf.NAME, dtf.TYPE, dtf.SIZE, dtf.FORM_FIELD_TYPE, dtf.FIELD_DB_NAME, dtf.field_category as FIELD_CATEGORY
, dtf.NULLS, dtf.mand
, dtf.IS_ACTIVE, dtf.IS_VISIBLE, dtf.rw_4_roles
, dtf.INFO_ID, dtf.info_view_nr as INFO_VIEW
, case when dtf.ly is null then 'Y' else '' end as "AUTORESIZE" ??
, case when dtf.autoResize=1 then 'Y' else '' end as "AUTORESIZE" 
, ifnull(dtf.lx, 400) as LX, ifnull(dtf.ly, 40) as LY
, dtf.maxWidth
, ft.src_file as "FIELD_SRC_FILE"

, ft.section_r  ??request=read
, ft.section_w   ??!request=read 
    as "FIELD_SECTION"
, ft.section_w  as "FIELD_SECTION_RW"

from d_fields dtf
left join d_form_fields_types ft on (ft.id = dtf.form_field_type)
left join d_list dh on dtf.type_id = dh.type_id
where dh.Id = #doc_id# 
    and dtf.is_visible in(1,3) ??!APP_VERSION=MOBILE
    and dtf.is_visible in(2,3) ??APP_VERSION=MOBILE
    and dtf.is_active=1 ??!IS_CONSTRUCTOR=Y
    and dtf.min_doc_type_version <= #DOC_TYPE_VERSION#  
    and dtf.max_doc_type_version >= #DOC_TYPE_VERSION#  
    and dtf.field_category>1  ??request=write 
order by nr
[end]


[get added comments]
select concat('<tr><td>&nbsp;&nbsp;</td><td class="small nowrap">'
 , DATE_FORMAT(wf.finished,'#shortDateTimeFormat#')
 , case when wf.user_id=wf.modifier_id then ', <b>' else ', ' end
 , iof(u.F, u.I, u.O)
 , case when wf.user_id=wf.modifier_id then '' else concat('<b> (', iof(um.F, um.I, um.O), ')') end
 , ':</b></td><td class="bg_white">', wf.comment, '</td></tr>') as ADDED_COMMENTS
from wf 
 left join #table_users_full# u on u.id=wf.user_id
 left join #table_users_full# um on um.id=wf.modifier_id
where wf.wf_id=#WF_ID# and not wf.comment is null and not wf.comment='' 
and wf.step_type=#~wf_step_in_progress#
and not wf.step_type=#~wf_step_signed# ??
order by wf.finished
;
[end]


