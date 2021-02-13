sys/indexDocs.mod

[comments]
descr=S: Индексация документов
input=
output=
parents=
childs=sys/indexDoc.mod
test_URL=?c=sys/indexDocs
author=Куняев
[end]

[description]
<ul>
<li>...</li>
</ul>
[end]


[parameters]
request_name=S:индексация документов
service=dubna.walt.service.TableServiceSpecial
LOG=ON
tableCfg=table_no
[end]


[report header]
    $SET_PARAMETERS srn=1; rpp=100000; 
    $SET_PARAMETERS limit=100; ??!limit
    limit=#limit#
    <br>
[end]


[item]
    #currentRow#: #doc_id#:
    $CALL_SERVICE c=sys/indexDoc; verbose=3;
[end]


[report footer]
    <hr>
    DONE!
[end]



*****************************  ***************************

[SQL]
    select dh.ID as "doc_id"
    from d_list dh
        left join d_types dtp on dtp.Id = dh.type_id
    where dtp.group_id<99
        and extract(year from dh.created) in(2018)
        and dh.is_deleted=0
        and dh.status in(1,2,3)
    order by dh.created desc
    limit #limit#
[end]

