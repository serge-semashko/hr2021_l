samples/set_param.cfg

[comments]
descr=Описание директивы $SET_PARAMETERS
input=
output=
parents=tab_samples.cfg
childs=
testURL=?c=samples/include
author=Фуряева
[end]

[description]
Описание директивы $SET_PARAMETERS
[end]


[parameters]
title=$SET_PARAMETERS
[end]

[report]
$INCLUDE samples/common.dat[start] 

<h3>Директива $SET_PARAMETERS</h3>
<fieldset class="code bg_white"><legend>Описание:</legend>

Директива $SET_PARAMETERS задает значение нового параметра на время выполнения запроса. При этом значения могут представлять собой константы, строки.
<ul>
<li>подстановка</li>
<li>условное</li>
<li>обработка</li>
</ul>
Таким образом, возможно использовать директиву $SET_PARAMETERS не только 
</p>

Непосредственно в директиве можно <br>
</fieldset>

<fieldset class="code bg_white"><legend>Синтаксис:</legend>
&##36;SET_PARAMETERS {name}={value};<br><br>
<b>{name}</b> - имя параметра<br>
<b>{value}</b> - 
<br><br>
</fieldset>

<fieldset class="code"><legend>Примеры:</legend>

1. Задать значение 
<div class="m20 bg_white">&##36;SET_PARAMETERS srn=1;</div>
2. Установить несколько значений по умолчанию на странице:
<div class="m20 bg_white">&##36;SET_PARAMETERS srn=1; rpp=20;</div>
3. Установить значение переменной с условием:
<div class="m20 bg_white">&##36;SET_PARAMETERS srn=1; ??!srn</div>
<br>
</fieldset>
$INCLUDE samples/common.dat[bottom]
[end]

