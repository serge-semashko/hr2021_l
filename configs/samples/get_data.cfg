samples/set_param.cfg

[comments]
descr=Описание директивы $GET_DATA
input=
output=
parents=tab_samples.cfg
childs=
testURL=?c=samples/get_data
author=Фуряева
[end]

[description]
Описание директивы $GET_DATA
[end]


[parameters]
title=$SET_PARAMETERS
[end]

[report]
$INCLUDE samples/common.dat[start] 

<h3>Директива &##36;GET_DATA</h3>
<fieldset class="code bg_white"><legend>Описание:</legend>

Директива &##36;GET_DATA получает данные заданной секции из текущего модуля.<br>
В процессе вставки производится построчная интерпретация содержимого вставляемой секции по общим правилам:
<ul>
<li>подстановка</li>
<li>условное</li>
<li>обработка</li>
</ul>
Таким образом, возможно использовать директиву &##36;GET_DATA не только 
</p>

Непосредственно в директиве можно <br>
</fieldset>

<fieldset class="code bg_white"><legend>Синтаксис:</legend>
&##36;GET_DATA [sectionName]<br><br>
<b>sectionName</b> - имя секции<br>
<br><br>
</fieldset>

<fieldset class="code"><legend>Примеры:</legend>

1. Задать значение 
<div class="m20 bg_white">&##36;GET_DATA [get some value SQL]</div>
<br>
2. Установить несколько значений по умолчанию на странице:
<div class="m20 bg_white">&##36;GET_DATA [get dropdowns]<br>
...<br>...<br>
<br>

&##91;get dropdowns]<br>
&##36;GET_DATA [get some value SQL]; <br>
&##91;end]
</div>
3. Установить значение переменной с условием:
<div class="m20 bg_white">&##36;GET_DATA [get dropdowns]<br>
...<br>...<br>
<br>

&##91;get dropdowns]<br>
&##36;SET_PARAMETERS p1=Значение 1; p2=Значение 2; <br>
&##36;SET_PARAMETERS_SESSION name=Значение сессионного параметра; <br>

&##91;end]
</div>
</fieldset>
$INCLUDE samples/common.dat[bottom]
[end]


